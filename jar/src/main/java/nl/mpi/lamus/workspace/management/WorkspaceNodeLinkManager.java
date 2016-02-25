/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.management;

import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.model.Reference;

/**
 * Provides a way to link or unlink nodes in a workspace.
 * 
 * @author guisil
 */
public interface WorkspaceNodeLinkManager {
    
    /**
     * Links, in the workspace, two nodes that were already linked in the archive
     * (for instance, when importing set of nodes from the archive in order to create a workspace).
     * 
     * @param workspace Workspace where the linking is happening
     * @param parentNode WorkspaceNode object corresponding to the parent node
     * @param childNode WorkspaceNode object corresponding to the child node
     * @param childLink Reference to the child node from the parent metadata file
     */
    public void linkNodesWithReference(Workspace workspace, WorkspaceNode parentNode, WorkspaceNode childNode, Reference childLink);
    
    /**
     * Links, in the workspace, two nodes.
     * 
     * @param parentNode WorkspaceNode object corresponding to the parent node
     * @param childNode WorkspaceNode object corresponding to the child node
     * @param isInfoLink true if the child node is to be linked as an info file
     */
    public void linkNodes(WorkspaceNode parentNode, WorkspaceNode childNode, boolean isInfoLink)
            throws WorkspaceException, ProtectedNodeException;
    
    /**
     * Links, only in the workspace DB, two nodes.
     * 
     * @param parentNode WorkspaceNode object corresponding to the parent node
     * @param childNode WorkspaceNode object corresponding to the child node
     */
    public void linkNodesOnlyInDb(WorkspaceNode parentNode, WorkspaceNode childNode)
            throws WorkspaceException;
    
    /**
     * Unlinks, in the workspace, two nodes.
     * 
     * @param parentNode WorkspaceNode object corresponding to the parent node
     * @param childNode WorkspaceNode object corresponding to the child node
     */
    public void unlinkNodes(WorkspaceNode parentNode, WorkspaceNode childNode)
            throws WorkspaceException, ProtectedNodeException;
    
    /**
     * Unlinks, in the workspace, a node from all its parent nodes.
     * @param childNode WorkspaceNode object corresponding to the node to be unlinked
     */
    public void unlinkNodeFromAllParents(WorkspaceNode childNode)
            throws WorkspaceException, ProtectedNodeException;
    
    /**
     * Unlinks the given node from its replaced
     * (or older, if none is replaced) parent.
     * @param childNode WorkspaceNode object corresponding to the node to be unlinked
     * @param newParentNode WorkspaceNode object corresponding to the new parent node
     */
    public void unlinkNodeFromReplacedParent(WorkspaceNode childNode, WorkspaceNode newParentNode)
            throws WorkspaceException, ProtectedNodeException;
    
    /**
     * Replaces, in the workspace, a node by a newer version.
     * The link in the parent has to be changed as well.
     * 
     * @param parentNode WorkspaceNode object corresponding to the parent node
     * @param oldNode WorkspaceNode object corresponding to the old node
     * @param newNode WorkspaceNode object corresponding to the new node
     * @param isNewNodeAlreadyLinked true if parent node and new node are already linked
     */
    public void replaceNode(WorkspaceNode parentNode, WorkspaceNode oldNode,
            WorkspaceNode newNode, boolean isNewNodeAlreadyLinked)
            throws WorkspaceException, ProtectedNodeException;
    
    /**
     * Removes the Archive URI from the given child node, including
     * from the reference in the parent node and in the file itself, if it is metadata.
     * @param parentNode WorkspaceNode object corresponding to the parent node
     * @param childNode WorkspaceNode object corresponding to the child node
     */
    public void removeArchiveUriFromChildNode(WorkspaceNode parentNode, WorkspaceNode childNode)
            throws WorkspaceException;
    
    /**
     * Removes an existing archiveURI from an uploaded node and does the same
     * for the children. This is meant to be used to prevent nodes containing
     * archive handles to be linked again in some other part of the tree.
     * @param node Node where to start the URI removal
     * @param firstIteration true if it's the initial call
     */
    public void removeArchiveUriFromUploadedNodeRecursively(WorkspaceNode node, boolean firstIteration)
            throws WorkspaceException;
}
