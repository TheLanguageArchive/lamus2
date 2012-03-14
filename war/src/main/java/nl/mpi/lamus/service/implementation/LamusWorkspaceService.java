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
package nl.mpi.lamus.service.implementation;

import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFilesystemHandler;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.NodeAccessChecker;
import nl.mpi.lamus.workspace.Workspace;
import nl.mpi.lamus.workspace.WorkspaceFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceService implements WorkspaceService {

    private final NodeAccessChecker nodeAccessChecker;
    private final WorkspaceFactory workspaceFactory;
    private final WorkspaceDao workspaceDao;
    private final WorkspaceFilesystemHandler workspaceFilesystemHandler;

    public LamusWorkspaceService(NodeAccessChecker accessChecker, WorkspaceFactory factory,
            WorkspaceDao workspaceDao, WorkspaceFilesystemHandler workspaceFilesystemHandler) {
        this.workspaceFactory = factory;
        this.nodeAccessChecker = accessChecker;
        this.workspaceDao = workspaceDao;
        this.workspaceFilesystemHandler = workspaceFilesystemHandler;
    }
    
    
    /**
     * 
     * @param archiveNodeID
     * @param userID
     * @return 
     */
    public Workspace createWorkspace(String userID, int archiveNodeID) {

        if(!this.nodeAccessChecker.canCreateWorkspace(userID, archiveNodeID)) {
            return null;
        }
        
        //TODO what about the browser session? does it make sense to check for a workspace in the session? disconnect it?
        
        //TODO thread for timeout checking? - WorkspaceTimeoutChecker/WorkspaceDates...
        
        //TODO create workspace object
        Workspace newWorkspace = workspaceFactory.getNewWorkspace(userID, archiveNodeID);
        
        //TODO create workspace in database
        workspaceDao.addWorkspace(newWorkspace);
        
        //TODO create workspace directories
        workspaceFilesystemHandler.createWorkspaceDirectory(newWorkspace);
        
        
        //TODO start exploring the nodes in the filesystem - WorkspaceImporter - what about having a super class for both threads (importer/exported)?
            // use the MetadataAPI to check CMDI files
                // copy the files to the workspace directory (what to do with the names???? if changing, change links)
                // create nodes in the database for the files
                // fill the database with the proper metadata (is external, which type, is protected, etc)
                // use typechecker to fill format data and check files
                // add links in the database for children
        
        
        
        
        

        return newWorkspace;
    }
    
}
