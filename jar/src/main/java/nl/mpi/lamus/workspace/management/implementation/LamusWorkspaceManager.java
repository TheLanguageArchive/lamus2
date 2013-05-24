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
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.exception.WorkspaceFilesystemException;
import nl.mpi.lamus.workspace.exporting.WorkspaceExportRunner;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceImportRunner;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.Workspace;
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
    
    @Autowired
    @Qualifier("numberOfDaysOfInactivityAllowedSinceLastSession")
    private int numberOfDaysOfInactivityAllowedSinceLastSession;

    @Autowired
    public LamusWorkspaceManager(ExecutorService executorService, WorkspaceFactory factory, WorkspaceDao dao,
        WorkspaceDirectoryHandler directoryHandler, WorkspaceImportRunner wsImportRunner, WorkspaceExportRunner wsExportRunner) {
        this.executorService = executorService;
        this.workspaceFactory = factory;
        this.workspaceDao = dao;
        this.workspaceDirectoryHandler = directoryHandler;
        this.workspaceImportRunner = wsImportRunner;
        this.workspaceExportRunner = wsExportRunner;
    }
    
    /**
     * @see WorkspaceManager#createWorkspace(java.lang.String, int)
     */
    public Workspace createWorkspace(String userID, int topNodeArchiveID) {
        
        Workspace newWorkspace = workspaceFactory.getNewWorkspace(userID, topNodeArchiveID);
        newWorkspace.setStatusMessageInitialising();
        workspaceDao.addWorkspace(newWorkspace);
        try {
            workspaceDirectoryHandler.createWorkspaceDirectory(newWorkspace);
        } catch(WorkspaceFilesystemException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
        
        workspaceImportRunner.setWorkspace(newWorkspace);
        workspaceImportRunner.setTopNodeArchiveID(topNodeArchiveID);
        
        //TODO use Callable and Future instead of Runnable, in order to get the result of the thread
        
        Future<Boolean> importResult = executorService.submit(workspaceImportRunner);
        
//        executorService.execute(workspaceImportRunner);

            // TODO implement some notification mechanism to let the caller know when this is ready
                // OR just have some different way of filling up the tree
        
        Boolean isSuccessful;
        
        try {            
            isSuccessful = importResult.get();
        } catch (InterruptedException iex) {
            logger.error("The thread was interrupted", iex);
            
            //TODO return some error instead??
            return null;
        } catch (ExecutionException eex) {
            logger.error("There was a problem with the thread execution", eex);
            
            //TODO return some error instead??
            return null;
        }
        
        if(!isSuccessful) {
            //TODO remove whatever was already created for the workspace
                // (filesystem, database) and return some error instead??
            
            return null;
        }
        
        return newWorkspace;
    }
    
    /**
     * @see WorkspaceManager#deleteWorkspace(java.lang.String, int)
     */
    public boolean deleteWorkspace(int workspaceID) {
        
        this.workspaceDao.deleteWorkspace(workspaceID);
        try {
            this.workspaceDirectoryHandler.deleteWorkspaceDirectory(workspaceID);
        } catch (IOException ex) {
            String errorMessage = "Problems deleting workspace directory for workspace with ID " + workspaceID;
            logger.error(errorMessage, ex);
            return false;
        }
        
        return true;
    }

    /**
     * @see WorkspaceManager#submitWorkspace(int)
     */
    public boolean submitWorkspace(int workspaceID, boolean keepUnlinkedFiles) {
                
        //TODO workspaceDao - get workspace from DB
        //TODO workspaceFactory (or something else) - set workspace as submitted
        Workspace workspace = this.workspaceDao.getWorkspace(workspaceID);
        
        workspaceExportRunner.setWorkspace(workspace);
        workspaceExportRunner.setKeepUnlinkedFiles(keepUnlinkedFiles);
        
        //TODO workspaceDirectoryHandler - move workspace to "submitted workspaces" directory
            // this should be part of the export thread?
        
        //TODO workspaceExportRunner - start export thread
        
        Future<Boolean> exportResult = executorService.submit(workspaceExportRunner);
        
        Boolean isSuccessful;
        
        try {
            isSuccessful = exportResult.get();
        } catch (InterruptedException iex) {
            logger.error("The thread was interrupted", iex);
            
            //TODO return some error instead??
            isSuccessful = false;
        } catch (ExecutionException eex) {
            logger.error("There was a problem with the thread execution", eex);
            
            //TODO return some error instead??
            isSuccessful = false;
        }
        
        return isSuccessful;
    }

    /**
     * @see WorkspaceManager#openWorkspace(java.lang.String, int)
     */
    public Workspace openWorkspace(String userID, int workspaceID) {
        
        Workspace workspace = this.workspaceDao.getWorkspace(workspaceID);
        
        if(workspace != null) {
            if(userID.equals(workspace.getUserID())) {
                if(this.workspaceDirectoryHandler.workspaceDirectoryExists(workspace)) {
                    Calendar calendarNow = Calendar.getInstance();
                    Date now = calendarNow.getTime();
                    workspace.setSessionStartDate(now);
                    calendarNow.add(Calendar.DATE, this.numberOfDaysOfInactivityAllowedSinceLastSession);
                    Date nowPlusExpiry = calendarNow.getTime();
                    workspace.setSessionEndDate(nowPlusExpiry);
                    this.workspaceDao.updateWorkspaceSessionDates(workspace);
                    workspace = this.workspaceDao.getWorkspace(workspaceID);
                } else {
                    //TODO Or throw exception?
                    logger.error("LamusWorkspaceManager.openWorkspace: Directory for workpace " + workspaceID +
                        " does not exist");
                    return null;
                }
            } else {
                //TODO Or throw exception?
                logger.error("LamusWorkspaceManager.openWorkspace: Given userID (" + userID +
                        ") different from expected (" + workspace.getUserID() + ")");
                return null;
            }
        }
        
        return workspace;
    }
    
}
