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
package nl.mpi.lamus.workspace.importing;

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
     * @param parentNode WorkspaceNode object corresponding to the parent node
     * @param childNode WorkspaceNode object corresponding to the child node
     * @param childLink Reference to the child node from the parent metadata file
     */
    public void linkNodesWithReference(WorkspaceNode parentNode, WorkspaceNode childNode, Reference childLink);
    
    /**
     * Links, in the workspace, two nodes.
     * 
     * @param parentNode WorkspaceNode object corresponding to the parent node
     * @param childNode WorkspaceNode object corresponding to the child node
     */
    public void linkNodes(WorkspaceNode parentNode, WorkspaceNode childNode);
    
    /**
     * Unlinks, in the workspace, two nodes.
     * 
     * @param parentNode WorkspaceNode object corresponding to the parent node
     * @param childNode WorkspaceNode object corresponding to the child node
     */
    public void unlinkNodes(WorkspaceNode parentNode, WorkspaceNode childNode);
    
    /**
     * Unlinks, in the workspace, a node from all its parent nodes.
     * @param childNode WorkspaceNode object corresponding to the node to be unlinked
     */
    public void unlinkNodeFromAllParents(WorkspaceNode childNode);
}
