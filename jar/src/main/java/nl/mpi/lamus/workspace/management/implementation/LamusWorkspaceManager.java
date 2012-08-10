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

import java.util.Calendar;
import java.util.Date;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceDirectoryException;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceImportRunner;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceManager implements WorkspaceManager {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceManager.class);
    
    private final TaskExecutor executor;
    private final WorkspaceFactory workspaceFactory;
    private final WorkspaceDao workspaceDao;
    private final WorkspaceDirectoryHandler workspaceDirectoryHandler;
//    private final FileImporterFactory importerFactory;
    private final WorkspaceImportRunner workspaceImportRunner;
    
    @Autowired
    @Qualifier("numberOfDaysOfInactivityAllowedSinceLastSession")
    private int numberOfDaysOfInactivityAllowedSinceLastSession;

    //TODO use Spring injection
    //TODO Executor can be created using Executors.newSingleThreadExecutor()
    @Autowired
    public LamusWorkspaceManager(TaskExecutor executor, WorkspaceFactory factory, WorkspaceDao dao,
        WorkspaceDirectoryHandler directoryHandler, WorkspaceImportRunner wsImportRunner) {
        this.executor = executor;
        this.workspaceFactory = factory;
        this.workspaceDao = dao;
        this.workspaceDirectoryHandler = directoryHandler;
        this.workspaceImportRunner = wsImportRunner;
    }
    
    public Workspace createWorkspace(String userID, int topNodeArchiveID) {
        
        Workspace newWorkspace = workspaceFactory.getNewWorkspace(userID, topNodeArchiveID);
        workspaceDao.addWorkspace(newWorkspace);
        try {
            workspaceDirectoryHandler.createWorkspaceDirectory(newWorkspace);
        } catch(FailedToCreateWorkspaceDirectoryException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
        
        //TODO get more values to add to the node (e.g. from the file, using the metadataAPI)
//        OurURL tempUrl = archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(archiveNodeID), ArchiveAccessContext.getFileUrlContext());
//        URL archiveNodeURL = tempUrl.toURL();
//        WorkspaceNode topNode = workspaceNodeFactory.getNewWorkspaceNode(newWorkspace.getWorkspaceID(), archiveNodeID, archiveNodeURL);

        
        //TODO change this call - use Spring injection

//        FileImporterFactory importerFactory = new WorkspaceFileImporterFactory(newWorkspace);
//        Runnable workspaceImportRunner = new WorkspaceImportRunner(workspaceDao, newWorkspace, topNodeArchiveID, importerFactory);
        workspaceImportRunner.setWorkspace(newWorkspace);
        workspaceImportRunner.setTopNodeArchiveID(topNodeArchiveID);
//        Thread importThread = new Thread(workspaceImportRunner);
//        importThread.start();
        
        
        //TODO use Callable and Future instead of Runnable, in order to get the result of the thread
        
        
        executor.execute(workspaceImportRunner);
        
        return newWorkspace;
    }

    public void submitWorkspace(int workspaceID) {
        throw new UnsupportedOperationException("Not supported yet.");
        
        //TODO workspaceDao - get workspace from DB
        //TODO workspaceFactory (or something else) - set workspace as submitted
        
        
        
        //TODO workspaceDirectoryHandler - move workspace to "submitted workspaces" directory
            // this should be part of the export thread?
        
        //TODO workspaceExportRunner - start export thread
    }

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
