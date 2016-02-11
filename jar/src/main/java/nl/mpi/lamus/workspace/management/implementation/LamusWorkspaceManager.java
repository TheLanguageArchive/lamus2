/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.management.implementation;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import nl.mpi.lamus.archive.permissions.PermissionAdjuster;
import nl.mpi.lamus.archive.permissions.implementation.PermissionAdjusterScope;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.MetadataValidationException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.metadata.validation.WorkspaceFileValidator;
import nl.mpi.lamus.util.CalendarHelper;
import nl.mpi.lamus.workspace.exporting.implementation.WorkspaceExportRunner;
import nl.mpi.lamus.workspace.exporting.WorkspaceExportRunnerFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.importing.implementation.WorkspaceImportRunner;
import nl.mpi.lamus.workspace.importing.WorkspaceImportRunnerFactory;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceManager
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceManager implements WorkspaceManager {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceManager.class);
    
    private final ExecutorService executorService;
    private final WorkspaceFactory workspaceFactory;
    private final WorkspaceDao workspaceDao;
    private final WorkspaceDirectoryHandler workspaceDirectoryHandler;
    private final CalendarHelper calendarHelper;
    private final WorkspaceFileValidator workspaceFileValidator;
    private final PermissionAdjuster permissionAdjuster;
    
    private final WorkspaceImportRunnerFactory workspaceImportRunnerFactory;
    private final WorkspaceExportRunnerFactory workspaceExportRunnerFactory;
    
    @Autowired
    @Qualifier("numberOfDaysOfInactivityAllowedSinceLastSession")
    private int numberOfDaysOfInactivityAllowedSinceLastSession;

    @Autowired
    public LamusWorkspaceManager(
            @Qualifier("WorkspaceExecutorService") ExecutorService executorService,
            WorkspaceFactory factory, WorkspaceDao dao,
            WorkspaceDirectoryHandler directoryHandler, CalendarHelper calendarHelper,
            WorkspaceFileValidator wsFileValidator, PermissionAdjuster permAdjuster,
            WorkspaceImportRunnerFactory wsImportRunnerFactory, WorkspaceExportRunnerFactory wsExportRunnerFactory) {
        this.executorService = executorService;
        this.workspaceFactory = factory;
        this.workspaceDao = dao;
        this.workspaceDirectoryHandler = directoryHandler;
        this.calendarHelper = calendarHelper;
        this.workspaceFileValidator = wsFileValidator;
        this.permissionAdjuster = permAdjuster;
        
        this.workspaceImportRunnerFactory = wsImportRunnerFactory;
        this.workspaceExportRunnerFactory = wsExportRunnerFactory;
    }
    
    /**
     * @see WorkspaceManager#createWorkspace(java.lang.String, java.net.URI)
     */
    @Override
    public Workspace createWorkspace(String userID, URI topArchiveNodeURI)
            throws WorkspaceImportException {
        
        Workspace newWorkspace = workspaceFactory.getNewWorkspace(userID, topArchiveNodeURI);
        newWorkspace.setStatusMessageInitialising();
        workspaceDao.addWorkspace(newWorkspace);
        try {
            workspaceDirectoryHandler.createWorkspaceDirectory(newWorkspace.getWorkspaceID());
            workspaceDirectoryHandler.createUploadDirectoryForWorkspace(newWorkspace.getWorkspaceID());
        } catch(IOException ex) {
            String errorMessage = "Error creating workspace in node " + topArchiveNodeURI;
            failWorkspaceImport(newWorkspace, errorMessage, ex);
        }
        
        WorkspaceImportRunner workspaceImportRunner = workspaceImportRunnerFactory.getNewImportRunner();
        workspaceImportRunner.setWorkspace(newWorkspace);
        workspaceImportRunner.setTopNodeArchiveURI(topArchiveNodeURI);
        
        //TODO use Callable and Future instead of Runnable, in order to get the result of the thread
        
        Future<Boolean> importResult = executorService.submit(workspaceImportRunner);
        
//        executorService.execute(workspaceImportRunner);

            // TODO implement some notification mechanism to let the caller know when this is ready
                // OR just have some different way of filling up the tree
        
        Boolean isSuccessful = false;
        
        try {            
            isSuccessful = importResult.get();
        } catch (InterruptedException iex) {
            String errorMessage = "Interruption in thread while creating workspace in node " + topArchiveNodeURI;
            failWorkspaceImport(newWorkspace, errorMessage, iex);
        } catch (ExecutionException eex) {
            String errorMessage = "Problem with thread execution while creating workspace in node " + topArchiveNodeURI;
            failWorkspaceImport(newWorkspace, errorMessage, eex);
        }
        
        if(!isSuccessful) {
            String errorMessage = "Workspace creation failed in node " + topArchiveNodeURI;
            failWorkspaceImport(newWorkspace, errorMessage, null);
        }
        
        // updated workspace
        Workspace toReturn;
        try {
            toReturn = workspaceDao.getWorkspace(newWorkspace.getWorkspaceID());
        } catch (WorkspaceNotFoundException ex) {
            throw new WorkspaceImportException(ex.getMessage(), ex.getWorkspaceID(), ex);
        }
        
        return toReturn;
    }
    
    /**
     * @see WorkspaceManager#deleteWorkspace(int, boolean)
     */
    @Override
    public void deleteWorkspace(int workspaceID, boolean keepUnlinkedFiles)
            throws WorkspaceNotFoundException, WorkspaceExportException, IOException {
        
        Workspace workspace = workspaceDao.getWorkspace(workspaceID);
        WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.DELETE_WORKSPACE;
        
        WorkspaceExportRunner workspaceExportRunner = workspaceExportRunnerFactory.getNewExportRunner();
        workspaceExportRunner.setWorkspace(workspace);
        workspaceExportRunner.setKeepUnlinkedFiles(keepUnlinkedFiles);
        workspaceExportRunner.setSubmissionType(submissionType);
        
        Future<Boolean> exportResult = executorService.submit(workspaceExportRunner);
        
        Boolean isSuccessful;
        
        try {
            isSuccessful = exportResult.get();
        } catch(InterruptedException iex) {
            String errorMessage = "Interruption in thread while deleting workspace " + workspaceID;
            logger.error(errorMessage, iex);
            adjustPermissionsInArchive(workspaceID, submissionType);
            throw new WorkspaceExportException(errorMessage, workspaceID, iex);
        } catch(ExecutionException eex) {
            String errorMessage = "Problem with thread execution while deleting workspace " + workspaceID;
            logger.error(errorMessage, eex);
            adjustPermissionsInArchive(workspaceID, submissionType);
            throw new WorkspaceExportException(errorMessage, workspaceID, eex);
        }
        
        if(!isSuccessful) {
            String errorMessage = "Workspace deletion failed for workspace " + workspaceID;
            logger.error(errorMessage);
            adjustPermissionsInArchive(workspaceID, submissionType);
            throw new WorkspaceExportException(errorMessage, workspaceID, null);
        }
        
        workspaceDao.deleteWorkspace(workspace);
        workspaceDirectoryHandler.deleteWorkspaceDirectory(workspaceID);
        adjustPermissionsInArchive(workspaceID, submissionType);
    }

    /**
     * @see WorkspaceManager#submitWorkspace(int, boolean)
     */
    @Override
    public void submitWorkspace(int workspaceID, boolean keepUnlinkedFiles)
            throws WorkspaceNotFoundException, WorkspaceExportException, MetadataValidationException {
        
        Workspace workspace = workspaceDao.getWorkspace(workspaceID);
        WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        
        try{
            workspaceFileValidator.triggerSchematronValidationForMetadataFilesInWorkspace(workspaceID);
        } catch(MetadataValidationException ex) {
            String issuesMessage = workspaceFileValidator.validationIssuesToString(ex.getValidationIssues());
            if(workspaceFileValidator.validationIssuesContainErrors(ex.getValidationIssues())) {
                throw ex;
            } else {
                String message;
                if(issuesMessage.isEmpty()) {
                    message = ex.getMessage();
                } else {
                    message = issuesMessage;
                }
                logger.warn(message);
            }
        }
        
        workspace.setStatus(WorkspaceStatus.SUBMITTED);
        workspace.setMessage("workspace was submitted");
        workspaceDao.updateWorkspaceStatusMessage(workspace);
        
        WorkspaceExportRunner workspaceExportRunner = workspaceExportRunnerFactory.getNewExportRunner();
        workspaceExportRunner.setWorkspace(workspace);
        workspaceExportRunner.setKeepUnlinkedFiles(keepUnlinkedFiles);
        workspaceExportRunner.setSubmissionType(submissionType);
        
        //TODO workspaceDirectoryHandler - move workspace to "submitted workspaces" directory
            // this should be part of the export thread?
        
        Future<Boolean> exportResult = executorService.submit(workspaceExportRunner);
        
        Boolean isSuccessful;
        
        workspace = workspaceDao.getWorkspace(workspaceID);
        
        try {
            isSuccessful = exportResult.get();
        } catch(InterruptedException iex) {
            String errorMessage = "Interruption in thread while submitting workspace " + workspaceID;
            logger.error(errorMessage, iex);
            finaliseWorkspace(workspace, Boolean.FALSE);
            adjustPermissionsInArchive(workspaceID, submissionType);
            throw new WorkspaceExportException(errorMessage, workspaceID, iex);
        } catch(ExecutionException eex) {
            String errorMessage = "Problem with thread execution while submitting workspace " + workspaceID;
            logger.error(errorMessage, eex);
            finaliseWorkspace(workspace, Boolean.FALSE);
            adjustPermissionsInArchive(workspaceID, submissionType);
            throw new WorkspaceExportException(errorMessage, workspaceID, eex);
        }
        
        if(!isSuccessful) {
            String errorMessage = "Workspace submission failed for workspace " + workspaceID;
            logger.error(errorMessage);
            finaliseWorkspace(workspace, Boolean.FALSE);
            adjustPermissionsInArchive(workspaceID, submissionType);
            throw new WorkspaceExportException(errorMessage, workspaceID, null);
        }
        
        finaliseWorkspace(workspace, Boolean.TRUE);
        adjustPermissionsInArchive(workspaceID, submissionType);
    }

    /**
     * @see WorkspaceManager#openWorkspace(int)
     */
    @Override
    public Workspace openWorkspace(int workspaceID)
            throws WorkspaceNotFoundException, IOException {
        
        Workspace workspace = workspaceDao.getWorkspace(workspaceID);
        
        if(workspaceDirectoryHandler.workspaceDirectoryExists(workspace)) {
            Calendar calendarNow = calendarHelper.getCalendarInstance();
            Date now = calendarNow.getTime();
            workspace.setSessionStartDate(now);
            calendarNow.add(Calendar.DATE, numberOfDaysOfInactivityAllowedSinceLastSession);
            Date nowPlusExpiry = calendarNow.getTime();
            workspace.setSessionEndDate(nowPlusExpiry);
            workspaceDao.updateWorkspaceSessionDates(workspace);
        } else {
            String errorMessage = "Directory for workpace " + workspaceID + " does not exist";
            logger.error(errorMessage);
            throw new IOException(errorMessage);
        }
        
        return workspace;
    }
    
    
    private void failWorkspaceImport(Workspace ws, String errorMessage, Exception ex) throws WorkspaceImportException {
        
        workspaceDao.unlockAllNodesOfWorkspace(ws.getWorkspaceID());
        
        logger.error(errorMessage, ex);
        throw new WorkspaceImportException(errorMessage, ws.getWorkspaceID(), ex);
    }
    
    private void finaliseWorkspace(Workspace workspace, boolean submitSuccessful) {
        
        Date endDate = calendarHelper.getCalendarInstance().getTime();
        workspace.setSessionEndDate(endDate);
        workspace.setEndDate(endDate);
        
        if(submitSuccessful) {
            workspace.setStatus(WorkspaceStatus.UPDATING_ARCHIVE);
            workspace.setMessage("Data was successfully moved to the archive. It is now being updated in the database.\nAn email will be sent after this process is finished (it can take a while, depending on the size of the workspace).");
        } else {
            workspace.setStatus(WorkspaceStatus.ERROR_MOVING_DATA);
            workspace.setMessage("There were errors when submitting the workspace. Please contact the corpus management team.");
        }
        
        workspaceDao.updateWorkspaceEndDates(workspace);
        workspaceDao.updateWorkspaceStatusMessage(workspace);
    }
    
    private void adjustPermissionsInArchive(int workspaceID, WorkspaceSubmissionType submissionType) {
    	
    	PermissionAdjusterScope scope;
    	if(WorkspaceSubmissionType.SUBMIT_WORKSPACE.equals(submissionType)) {
    		scope = PermissionAdjusterScope.ALL_NODES;
    	} else {
    		scope = PermissionAdjusterScope.UNLINKED_NODES_ONLY;
    	}
    	
    	permissionAdjuster.adjustPermissions(workspaceID, scope);
    }
}
