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

import java.io.File;
import java.util.Collection;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see WorkspaceService
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceService implements WorkspaceService {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceService.class);

    private final WorkspaceAccessChecker nodeAccessChecker;
    private final WorkspaceManager workspaceManager;
    protected final WorkspaceDao workspaceDao;
    private final WorkspaceUploader workspaceUploader;
    private final WorkspaceNodeLinkManager workspaceNodeLinkManager;

    public LamusWorkspaceService(WorkspaceAccessChecker aChecker, WorkspaceManager wsManager,
            WorkspaceDao wsDao, WorkspaceUploader wsUploader, WorkspaceNodeLinkManager wsnLinkManager) {
        this.nodeAccessChecker = aChecker;
        this.workspaceManager = wsManager;
        this.workspaceDao = wsDao;
        this.workspaceUploader = wsUploader;
        this.workspaceNodeLinkManager = wsnLinkManager;
    }
    
    
    /**
     * @see WorkspaceService#createWorkspace(java.lang.String, int)
     */
    @Override
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
     * @see WorkspaceService#deleteWorkspace(java.lang.String, int)
     */
    @Override
    public void deleteWorkspace(String userID, int workspaceID) {
        
        if(!this.nodeAccessChecker.hasAccessToWorkspace(userID, workspaceID)) {
            
            //TODO Inform the user of the reason why the workspace can't be deleted
            //TODO Throw an exception instead?
            
            logger.error("Cannot delete workspace with ID " + workspaceID);
        } else {
        
            this.workspaceManager.deleteWorkspace(workspaceID);
        }
    }

    /**
     * @see WorkspaceService#submitWorkspace(String, int)
     */
    @Override
    public boolean submitWorkspace(String userID, int workspaceID/*, boolean keepUnlinkedFiles*/) {

        //TODO requests in this session?
        //TODO workspace should be initialised / connected
        
        //TODO nodeAccessChecker - check access?
        
        if(!this.nodeAccessChecker.hasAccessToWorkspace(userID, workspaceID)) {
            
            //TODO Inform the user of the reason why the workspace can't be submitted
            //TODO Throw an exception instead?
            logger.error("Cannot submit workspace with ID " + workspaceID);
            return false;
        } else {
        
            return this.workspaceManager.submitWorkspace(workspaceID/*, keepUnlinkedFiles*/);
        }
    }

    /**
     * @see WorkspaceService#getWorkspace(int)
     */
    @Override
    public Workspace getWorkspace(int workspaceID) {
        
        return this.workspaceDao.getWorkspace(workspaceID);
    }
    
    /**
     * @see WorkspaceService#listUserWorkspaces(java.lang.String)
     */
    @Override
    public Collection<Workspace> listUserWorkspaces(String userID) {
        
        return this.workspaceDao.listWorkspacesForUser(userID);
    }

    /**
     * @see WorkspaceService#openWorkspace(java.lang.String, int)
     */
    @Override
    public Workspace openWorkspace(String userID, int workspaceID) {
        
        return this.workspaceManager.openWorkspace(userID, workspaceID);
    }

    /**
     * @see WorkspaceService#getNode(int)
     */
    @Override
    public WorkspaceNode getNode(int nodeID) {
        
        return this.workspaceDao.getWorkspaceNode(nodeID);
    }

    /**
     * @see WorkspaceService#getChildNodes(int)
     */
    @Override
    public Collection<WorkspaceNode> getChildNodes(int nodeID) {
        
        return this.workspaceDao.getChildWorkspaceNodes(nodeID);
    }
    
    /**
     * @see WorkspaceService#linkNodes(java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void linkNodes(String userID, WorkspaceNode parentNode, WorkspaceNode childNode) {
        
        if(!this.nodeAccessChecker.hasAccessToWorkspace(userID, parentNode.getWorkspaceID())) {
            
            //TODO Inform the user of the reason why the nodes can't be linked
            //TODO Throw an exception instead?
            logger.error("Cannot link nodes in workspace with ID " + parentNode.getWorkspaceID());
        } else {
            
            this.workspaceNodeLinkManager.linkNodes(parentNode, childNode);
        }
    }
    
    /**
     * @see WorkspaceService#unlinkNodes(java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void unlinkNodes(String userID, WorkspaceNode parentNode, WorkspaceNode childNode) {
        
        if(!this.nodeAccessChecker.hasAccessToWorkspace(userID, parentNode.getWorkspaceID())) {
            
            //TODO Inform the user of the reason why the nodes can't be unlinked
            //TODO Throw an exception instead?
            logger.error("Cannot unlink nodes in workspace with ID " + parentNode.getWorkspaceID());
        } else {
            
            this.workspaceNodeLinkManager.unlinkNodes(parentNode, childNode);
        }
    }
    
    /**
     * @see WorkspaceService#deleteNode(java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void deleteNode(String userID, WorkspaceNode node) {
        
        if(!this.nodeAccessChecker.hasAccessToWorkspace(userID, node.getWorkspaceID())) {
            
            //TODO Inform the user of the reason why the node can't be deleted
            //TODO Throw an exception instead?
            logger.error("Cannot delete node in workspace with ID " + node.getWorkspaceID());
        } else {
            
            this.workspaceNodeLinkManager.unlinkNodeFromAllParents(node);
            this.workspaceDao.setWorkspaceNodeAsDeleted(node.getWorkspaceID(), node.getWorkspaceNodeID());
        }
    }

    /**
     * @see WorkspaceService#uploadFilesIntoWorkspace(java.lang.String, int, java.util.Collection)
     */
    @Override
    public void uploadFilesIntoWorkspace(String userID, int workspaceID, Collection<FileItem> fileItems) {
        
        if(!this.nodeAccessChecker.hasAccessToWorkspace(userID, workspaceID)) {
            
            //TODO Inform the user of the reason why the files can't be uploaded
            //TODO Throw an exception instead?
            logger.error("Cannot upload files to workspace with ID " + workspaceID);
        } else {
        
            this.workspaceUploader.uploadFiles(workspaceID, fileItems);
        }
    }
}
