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
package nl.mpi.lamus.service;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceService extends Serializable {
    
    /**
     * Creates a workspace starting in a given archive node, for a given user.
     * 
     * @param archiveNodeID archive ID of the node
     * @param userID ID of the user
     * @return Workspace object
     */
    public Workspace createWorkspace(String userID, int archiveNodeID);
    
    /**
     * Deletes the workspace with the given ID.
     * 
     * @param userID ID of the user who is trying to delete the workspace
     * @param workspaceID ID of the workspace to be deleted
     */
    public void deleteWorkspace(String userID, int workspaceID);
    
    /**
     * Retrieves a workspace with the given ID.
     * 
     * @param workspaceID ID of the workspace to retrieve
     * @return Retrieved workspace object
     */
    public Workspace getWorkspace(int workspaceID);
    
    /**
     * Retrieves a collection containing the active workspaces belonging to the
     * given user.
     * @param userID ID of the user
     * @return Collection with the user's active workspaces
     */
    public Collection<Workspace> listUserWorkspaces(String userID);
    
    /**
     * Opens a workspace, retrieving the corresponding object from the database.
     * While doing so, it also updates the session start date of the workspace.
     * 
     * @param workspaceID ID of the workspace to open
     * @return Retrieved workspace object
     */
    public Workspace openWorkspace(String userID, int workspaceID);
    
    /**
     * Submits a workspace back into the archive.
     * 
     * @param userID ID of the user
     * @param workspaceID ID of the workspace
     * @return true if successfully submitted
     */
    public boolean submitWorkspace(String userID, int workspaceID);
    
    /**
     * Retrieves a workspace node with the given ID.
     * 
     * @param nodeID ID of the node to retrieve
     * @return corresponding workspace node
     */
    public WorkspaceNode getNode(int nodeID);
    
    /**
     * Retrieves a collection containing the child nodes of the node with 
     * the given ID.
     * 
     * @param nodeID ID of the parent node
     * @return child nodes of the given node
     */
    public Collection<WorkspaceNode> getChildNodes(int nodeID);
    
    /**
     * Links two nodes in a workspace.
     * 
     * @param userID ID of the user
     * @param parentNode WorkspaceNode object corresponding to the parent node
     * @param childNode WorkspaceNode object corresponding to the child node
     */
    public void linkNodes(String userID, WorkspaceNode parentNode, WorkspaceNode childNode);
    
    /**
     * Uploads the given files into the workspace.
     * 
     * @param userID ID of the user
     * @param workspaceID ID of the workspace
     * @param fileItems Files to be uploaded
     */
    public void uploadFilesIntoWorkspace(String userID, int workspaceID, Collection<FileItem> fileItems);
}
