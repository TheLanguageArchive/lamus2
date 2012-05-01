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

import java.util.concurrent.Executor;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceDirectoryException;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.importing.FileImporterFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceImportRunner;
import nl.mpi.lamus.workspace.importing.implementation.WorkspaceFileImporterFactory;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceManager implements WorkspaceManager {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceManager.class);
    
    private final Executor executor;
    private final WorkspaceFactory workspaceFactory;
    private final WorkspaceDao workspaceDao;
    private final WorkspaceDirectoryHandler workspaceDirectoryHandler;

    //TODO use Spring injection
    //TODO Executor can be created using Executors.newSingleThreadExecutor()
    public LamusWorkspaceManager(Executor executor, WorkspaceFactory factory, WorkspaceDao dao, WorkspaceDirectoryHandler directoryHandler) {
        this.executor = executor;
        this.workspaceFactory = factory;
        this.workspaceDao = dao;
        this.workspaceDirectoryHandler = directoryHandler;
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

        FileImporterFactory importerFactory = new WorkspaceFileImporterFactory(newWorkspace);
        Runnable workspaceImportRunner = new WorkspaceImportRunner(workspaceDao, newWorkspace, topNodeArchiveID, importerFactory);
//        Thread importThread = new Thread(workspaceImportRunner);
//        importThread.start();
        
        
        //TODO use Callable and Future instead of Runnable, in order to get the result of the thread
        
        
        executor.execute(workspaceImportRunner);
        
        return newWorkspace;
    }
    
}
