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
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.util.CalendarHelper;
import nl.mpi.lamus.workspace.exporting.implementation.WorkspaceExportRunner;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.importing.implementation.WorkspaceImportRunner;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
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
    private final WorkspaceImportRunner workspaceImportRunner;
    private final WorkspaceExportRunner workspaceExportRunner;
    private final CalendarHelper calendarHelper;
    
    @Autowired
    @Qualifier("numberOfDaysOfInactivityAllowedSinceLastSession")
    private int numberOfDaysOfInactivityAllowedSinceLastSession;

    @Autowired
    public LamusWorkspaceManager(@Qualifier("WorkspaceExecutorService") ExecutorService executorService, WorkspaceFactory factory, WorkspaceDao dao,
        WorkspaceDirectoryHandler directoryHandler, WorkspaceImportRunner wsImportRunner, WorkspaceExportRunner wsExportRunner,
        CalendarHelper calendarHelper) {
        this.executorService = executorService;
        this.workspaceFactory = factory;
        this.workspaceDao = dao;
        this.workspaceDirectoryHandler = directoryHandler;
        this.workspaceImportRunner = wsImportRunner;
        this.workspaceExportRunner = wsExportRunner;
        this.calendarHelper = calendarHelper;
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
            logger.error(errorMessage, ex);
            throw new WorkspaceImportException(errorMessage, newWorkspace.getWorkspaceID(), ex);
        }
        
        workspaceImportRunner.setWorkspace(newWorkspace);
        workspaceImportRunner.setTopNodeArchiveURI(topArchiveNodeURI);
        
        //TODO use Callable and Future instead of Runnable, in order to get the result of the thread
        
        Future<Boolean> importResult = executorService.submit(workspaceImportRunner);
        
//        executorService.execute(workspaceImportRunner);

            // TODO implement some notification mechanism to let the caller know when this is ready
                // OR just have some different way of filling up the tree
        
        Boolean isSuccessful;
        
        try {            
            isSuccessful = importResult.get();
        } catch (InterruptedException iex) {
            String errorMessage = "Interruption in thread while creating workspace in node " + topArchiveNodeURI;
            logger.error(errorMessage, iex);
            throw new WorkspaceImportException(errorMessage, newWorkspace.getWorkspaceID(), iex);
        } catch (ExecutionException eex) {
            
            //TODO Find a better way of handling these exceptions
                // In most cases this WorkspaceImportException is wrapping an ExecutionException
                    // that already contains a nested WorkspaceImportException
                // One possibility would be to just throw here the cause of the ExecutionException
                    // but perhaps some stacktrace information would be lost that way
            
            String errorMessage = "Problem with thread execution while creating workspace in node " + topArchiveNodeURI;
            logger.error(errorMessage, eex);
            throw new WorkspaceImportException(errorMessage, newWorkspace.getWorkspaceID(), eex);
        }
        
        if(!isSuccessful) {
            //TODO remove whatever was already created for the workspace
                // (filesystem, database) and return some error instead??
            
            String errorMessage = "Workspace creation failed in node " + topArchiveNodeURI;
            logger.error(errorMessage);
            throw new WorkspaceImportException(errorMessage, newWorkspace.getWorkspaceID(), null);
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
     * @see WorkspaceManager#deleteWorkspace(java.lang.String, int)
     */
    @Override
    public void deleteWorkspace(int workspaceID) throws IOException {
        
        workspaceDao.deleteWorkspace(workspaceID);
        
        workspaceDirectoryHandler.deleteWorkspaceDirectory(workspaceID);
    }

    /**
     * @see WorkspaceManager#submitWorkspace(int)
     */
    @Override
    public void submitWorkspace(int workspaceID/*, boolean keepUnlinkedFiles*/)
            throws WorkspaceNotFoundException, WorkspaceExportException {
                
        //TODO workspaceDao - get workspace from DB
        //TODO workspaceFactory (or something else) - set workspace as submitted
        Workspace workspace = workspaceDao.getWorkspace(workspaceID);
        
        workspace.setStatus(WorkspaceStatus.SUBMITTED);
        workspace.setMessage("workspace was submitted");
        workspaceDao.updateWorkspaceStatusMessage(workspace);
        
        workspaceExportRunner.setWorkspace(workspace);
//        workspaceExportRunner.setKeepUnlinkedFiles(keepUnlinkedFiles);
        
        //TODO workspaceDirectoryHandler - move workspace to "submitted workspaces" directory
            // this should be part of the export thread?
        
        Future<Boolean> exportResult = executorService.submit(workspaceExportRunner);
        
        Boolean isSuccessful = false;
        
        workspace = workspaceDao.getWorkspace(workspaceID);
        
        try {
            isSuccessful = exportResult.get();
        } catch(InterruptedException iex) {
            String errorMessage = "Interruption in thread while submitting workspace " + workspaceID;
            logger.error(errorMessage, iex);
            finaliseWorkspace(workspace, Boolean.FALSE);
            throw new WorkspaceExportException(errorMessage, workspaceID, iex);
        } catch(ExecutionException eex) {
            
            //TODO Find a better way of handling these exceptions
                // In most cases this WorkspaceImportException is wrapping an ExecutionException
                    // that already contains a nested WorkspaceImportException
                // One possibility would be to just throw here the cause of the ExecutionException
                    // but perhaps some stacktrace information would be lost that way
            
            String errorMessage = "Problem with thread execution while submitting workspace " + workspaceID;
            logger.error(errorMessage, eex);
            finaliseWorkspace(workspace, Boolean.FALSE);
            throw new WorkspaceExportException(errorMessage, workspaceID, eex);
        }
        
        //TODO CATCH CrawlerException?
        
        
        
        if(!isSuccessful) {
            String errorMessage = "Workspace submission failed for workspace " + workspaceID;
            logger.error(errorMessage);
            finaliseWorkspace(workspace, Boolean.FALSE);
            throw new WorkspaceExportException(errorMessage, workspaceID, null);
        }
        
        finaliseWorkspace(workspace, Boolean.TRUE);
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

            //TODO necessary?
//                workspace = workspaceDao.getWorkspace(workspaceID);

        } else {
            String errorMessage = "Directory for workpace " + workspaceID + " does not exist";
            logger.error(errorMessage);
            throw new IOException(errorMessage);
        }
        
        return workspace;
    }
    
    
    private void finaliseWorkspace(Workspace workspace, boolean submitSuccessful) {
        
        Date endDate = calendarHelper.getCalendarInstance().getTime();
        workspace.setSessionEndDate(endDate);
        workspace.setEndDate(endDate);
        
        if(submitSuccessful) {
            workspace.setStatus(WorkspaceStatus.PENDING_ARCHIVE_DB_UPDATE);
            workspace.setMessage("Data was successfully move to the archive. It is now being updated in the database.\nAn email will be sent after this process is finished (it can take a while, depending on the size of the workspace).");
            
            //TODO remove data from DB (nodes and links?)
            workspaceDao.cleanWorkspaceNodesAndLinks(workspace);
        } else {
            workspace.setStatus(WorkspaceStatus.DATA_MOVED_ERROR);
            workspace.setMessage("There were errors when submitting the workspace. Please contact the corpus management team.");
        }
        
        workspaceDao.updateWorkspaceEndDates(workspace);
        workspaceDao.updateWorkspaceStatusMessage(workspace);
    }
}
