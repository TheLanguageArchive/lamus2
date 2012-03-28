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
package nl.mpi.lamus.workspace;

import java.io.File;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceDirectoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceManager implements WorkspaceManager {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceManager.class);
    
    private final WorkspaceFactory workspaceFactory;
    private final WorkspaceDao workspaceDao;
    private final WorkspaceDirectoryHandler workspaceDirectoryHandler;
    

    public LamusWorkspaceManager(WorkspaceFactory factory, WorkspaceDao dao, WorkspaceDirectoryHandler directoryHandler) {
        this.workspaceFactory = factory;
        this.workspaceDao = dao;
        this.workspaceDirectoryHandler = directoryHandler;
        
    }
    
    public Workspace createWorkspace(String userID, int archiveNodeID, WorkspaceImporter workspaceImporter) {
        
                //TODO create workspace object
        Workspace newWorkspace = workspaceFactory.getNewWorkspace(userID, archiveNodeID);
        
        //TODO create workspace in database
        workspaceDao.addWorkspace(newWorkspace);
        
        //TODO create workspace directories
        File workspaceDirectory;
        try {
            workspaceDirectory = workspaceDirectoryHandler.createWorkspaceDirectory(newWorkspace);
        } catch(FailedToCreateWorkspaceDirectoryException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
        
        //TODO get the information from the top node (metadata API) and add the node
            // call some helper class from which the metadata should be retrieved and the node insertion triggered
        
        
        
        
        //TODO start exploring the nodes in the filesystem - WorkspaceImporter - what about having a super class for both threads (importer/exporter)?
            // use the MetadataAPI to check CMDI files
                // copy the files to the workspace directory (what to do with the names???? if changing, change links)
                // create nodes in the database for the files
                // fill the database with the proper metadata (is external, which type, is protected, etc)
                // use typechecker to fill format data and check files
                // add links in the database for children
        
        
        workspaceImporter.importWorkspace(newWorkspace);
        
        
        
        return newWorkspace;
    }
    
}
