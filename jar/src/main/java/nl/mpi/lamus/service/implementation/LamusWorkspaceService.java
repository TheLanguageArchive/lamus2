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

import java.util.Collection;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.management.NodeAccessChecker;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @see WorkspaceService
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Service
public class LamusWorkspaceService implements WorkspaceService {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceService.class);

    private final NodeAccessChecker nodeAccessChecker;
    private final WorkspaceManager workspaceManager;
    private final WorkspaceDao workspaceDao;

    @Autowired
    public LamusWorkspaceService(NodeAccessChecker accessChecker, WorkspaceManager workspaceManager,
            WorkspaceDao workspaceDao) {
        this.nodeAccessChecker = accessChecker;
        this.workspaceManager = workspaceManager;
        this.workspaceDao = workspaceDao;
    }
    
    
    /**
     * @see WorkspaceService#createWorkspace(java.lang.String, int)
     */
    public Workspace createWorkspace(String userID, int archiveNodeID) {

        if(!this.nodeAccessChecker.canCreateWorkspace(userID, archiveNodeID)) {
            
            //TODO Inform the user of the reason why the workspace can't be created (either there is already a workspace from the same user or from a different one)
            //TODO Throw an exception instead?
            
            logger.error("Cannot create workspace in node with archive ID " + archiveNodeID);
            return null;
        }
        
        //TODO what about the browser session? does it make sense to check for a workspace in the session? disconnect it?
        
        //TODO thread for timeout checking? - WorkspaceTimeoutChecker/WorkspaceDates...
        

        Workspace newWorkspace = this.workspaceManager.createWorkspace(userID, archiveNodeID);
        
        

        return newWorkspace;
    }

    /**
     * @see WorkspaceService#submitWorkspace(int)
     */
    public void submitWorkspace(int workspaceID) {
        throw new UnsupportedOperationException("Not supported yet.");
        
        //TODO requests in this session?
        //TODO workspace should be initialised / connected
        
        //TODO nodeAccessChecker - check access?
        
        //TODO workspaceManager - submit workspace
    }

    /**
     * @see WorkspaceService#getWorkspace(int)
     */
    public Workspace getWorkspace(int workspaceID) {
        
        return this.workspaceDao.getWorkspace(workspaceID);
    }
    
    /**
     * @see WorkspaceService#listUserWorkspaces(java.lang.String)
     */
    public Collection<Workspace> listUserWorkspaces(String userID) {
        
        return this.workspaceDao.listWorkspacesForUser(userID);
    }

    /**
     * @see WorkspaceService#openWorkspace(java.lang.String, int)
     */
    public Workspace openWorkspace(String userID, int workspaceID) {
        
        return this.workspaceManager.openWorkspace(userID, workspaceID);
    }

    /**
     * @see WorkspaceService#getNode(int)
     */
    public WorkspaceNode getNode(int nodeID) {
        
        return this.workspaceDao.getWorkspaceNode(nodeID);
    }

    /**
     * @see WorkspaceService#getChildNodes(int)
     */
    public Collection<WorkspaceNode> getChildNodes(int nodeID) {
        
        return this.workspaceDao.getChildWorkspaceNodes(nodeID);
    }
}
