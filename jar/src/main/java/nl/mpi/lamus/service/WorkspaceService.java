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
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceService extends Serializable {
    
    /**
     * Creates a workspace starting in a given archive node, for a given user.
     * 
     * @param userID ID of the user
     * @param archiveNodeURI URI of the node in the archive
     * @return Workspace object
     */
    public Workspace createWorkspace(String userID, URI archiveNodeURI)
            throws NodeAccessException, WorkspaceImportException, NodeNotFoundException;
    
    /**
     * Deletes the workspace with the given ID.
     * 
     * @param userID ID of the user who is trying to delete the workspace
     * @param workspaceID ID of the workspace to be deleted
     */
    public void deleteWorkspace(String userID, int workspaceID)
            throws WorkspaceNotFoundException, WorkspaceAccessException, IOException;
    
    /**
     * Retrieves a workspace with the given ID.
     * 
     * @param workspaceID ID of the workspace to retrieve
     * @return Retrieved workspace object
     */
    public Workspace getWorkspace(int workspaceID)
            throws WorkspaceNotFoundException;
    
    /**
     * Retrieves a collection containing the active workspaces belonging to the
     * given user.
     * @param userID ID of the user
     * @return Collection with the user's active workspaces
     */
    public Collection<Workspace> listUserWorkspaces(String userID);
    
    /**
     * Retrieves a list containing all workspaces.
     * @return  List with all workspaces
     */
    public List<Workspace> listAllWorkspaces();
    
    /**
     * @param userID ID of the user
     * @return true if the user has active workspaces
     */
    public boolean userHasWorkspaces(String userID);
    
    /**
     * Opens a workspace, retrieving the corresponding object from the database.
     * While doing so, it also updates the session start date of the workspace.
     * 
     * @param workspaceID ID of the workspace to open
     * @return Retrieved workspace object
     */
    public Workspace openWorkspace(String userID, int workspaceID)
            throws WorkspaceNotFoundException, WorkspaceAccessException, IOException;
    
    /**
     * Submits a workspace back into the archive.
     * 
     * @param userID ID of the user
     * @param workspaceID ID of the workspace
     */
    public void submitWorkspace(String userID, int workspaceID)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceExportException;
    
    /**
     * Retrieves a workspace node with the given ID.
     * 
     * @param nodeID ID of the node to retrieve
     * @return corresponding workspace node
     */
    public WorkspaceNode getNode(int nodeID)
            throws WorkspaceNodeNotFoundException;
    
    /**
     * Retrieves a collection containing the child nodes of the node with 
     * the given ID.
     * 
     * @param nodeID ID of the parent node
     * @return child nodes of the given node
     */
    public Collection<WorkspaceNode> getChildNodes(int nodeID);
    
    /**
     * Adds a node to the workspace.
     * 
     * @param userID ID of the user
     * @param node node to be added to the workspace
     */
    public void addNode(String userID, WorkspaceNode node)
            throws WorkspaceNotFoundException, WorkspaceAccessException;
    
    /**
     * Links two nodes in a workspace.
     * 
     * @param userID ID of the user
     * @param parentNode WorkspaceNode object corresponding to the parent node
     * @param childNode WorkspaceNode object corresponding to the child node
     */
    public void linkNodes(String userID, WorkspaceNode parentNode, WorkspaceNode childNode)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException;
    
    /**
     * Unlinks two nodes in a workspace.
     * @param userID ID of the user
     * @param parentNode WorkspaceNode object corresponding to the parent node
     * @param childNode WorkspaceNode object corresponding to the child node
     */
    public void unlinkNodes(String userID, WorkspaceNode parentNode, WorkspaceNode childNode)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException;
    
    /**
     * Deletes a node in the workspace.
     * @param userID ID of the user
     * @param node WorkspaceNode object corresponding to the node that should be deleted
     */
    public void deleteNode(String userID, WorkspaceNode node)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException;
    
    /**
     * Replaces a tree by a new version of that same tree.
     * The parameters can be either the top node of the trees involved (old and new)
     * or the two leaf nodes involved (in case only one node is to be replaced).
     * 
     * @param userID ID of the user
     * @param oldTreeTopNode top node of the tree to be replaced
     * @param newTreeTopNode top node of the new tree
     * @param parentNode parent node of the node to replace
     */
    public void replaceTree(String userID, WorkspaceNode oldTreeTopNode, WorkspaceNode newTreeTopNode, WorkspaceNode parentNode)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException;
    
    /**
     * Uploads the given files into the workspace.
     * 
     * @param userID ID of the user
     * @param workspaceID ID of the workspace
     * @param fileItems Files to be uploaded
     */
//    public void uploadFilesIntoWorkspace(String userID, int workspaceID, Collection<FileItem> fileItems);
    
    /**
     * Returns the upload directory for the given workspace.
     * @param workspaceID ID of the workspace
     * @return Upload directory
     */
    public File getWorkspaceUploadDirectory(int workspaceID);
    
    /**
     * Given an InputStream and filename, upload the file into the workspace,
     * in case it's archivable.
     * @param userID ID of the user
     * @param workspaceID ID of the workspace
     * @param inputStream InputStream to be uploaded
     * @param filename name of the file to upload
     */
    public void uploadFileIntoWorkspace(String userID, int workspaceID, InputStream inputStream, String filename)
            throws IOException, TypeCheckerException, WorkspaceException;
    
    /**
     * After the files are uploaded, process the files by performing
     * typechecks and checking for links between them and the existing tree.
     * @param userID ID of the user
     * @param workspaceID ID of the workspace
     * @param uploadedFiles Files previously uploaded
     * @return Map containing the files which could not be successfully uploaded and the reason for each
     */
    public Map<File, String> processUploadedFiles(String userID, int workspaceID, Collection<File> uploadedFiles)
            throws IOException, WorkspaceException;
    
    /**
     * Lists the unlinked nodes of the given workspace.
     * 
     * @param userID ID of the user
     * @param workspaceID ID of the workspace
     * @return List of unlinked nodes
     */
    public List<WorkspaceNode> listUnlinkedNodes(String userID, int workspaceID);
    
}
