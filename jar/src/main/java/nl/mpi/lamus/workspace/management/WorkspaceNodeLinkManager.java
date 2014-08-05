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

import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.model.Reference;

/**
 * Provides a way to link or unlink nodes in a workspace.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
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
     */
    public void linkNodes(WorkspaceNode parentNode, WorkspaceNode childNode)
            throws WorkspaceException;
    
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
            throws WorkspaceException;
    
    /**
     * Unlinks, in the workspace, a node from all its parent nodes.
     * @param childNode WorkspaceNode object corresponding to the node to be unlinked
     */
    public void unlinkNodeFromAllParents(WorkspaceNode childNode)
            throws WorkspaceException;
    
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
            throws WorkspaceException;
    
    /**
     * Removes the Archive URI from the given child node, including
     * from the reference in the parent node and in the file itself, if it is metadata.
     * @param parentNode WorkspaceNode object corresponding to the parent node
     * @param childNode WorkspaceNode object corresponding to the child node
     */
    public void removeArchiveUriFromChildNode(WorkspaceNode parentNode, WorkspaceNode childNode)
            throws WorkspaceException;
}
