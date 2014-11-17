/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.actions;

import java.util.Collection;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;

/**
 * Interface for node actions in the workspace tree.
 * (Based on the NodeAction interface of the Metadata Browser)
 * 
 * @author guisil
 */
public abstract class WsTreeNodesAction implements WsNodesAction {
    
    protected Collection<WorkspaceTreeNode> selectedTreeNodes;
    protected Collection<WorkspaceTreeNode> selectedUnlinkedNodes;

    
    /**
     * Executes the action.
     * The setters might have to be used first, in order to set the selected tree nodes
     * and/or selected child nodes.
     * 
     * @param userID ID of the user to execute the action
     * @param wsService WorkspaceService to be used to execute the actions
     */
    public abstract void execute(String userID, WorkspaceService wsService)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException;
    
    /**
     * Sets the collection of nodes to be used in the action as selected nodes
     * (e.g. in the action for deleting nodes, these are the nodes to be deleted from the tree)
     * 
     * @param selectedTreeNodes Nodes currently selected in the tree
     */
    public void setSelectedTreeNodes(Collection<WorkspaceTreeNode> selectedTreeNodes) {
        this.selectedTreeNodes = selectedTreeNodes;
    }
    
    /**
     * Sets the collection of unlinked nodes to be used in the action
     * (e.g. in the action for linking nodes, these are the nodes to be linked to the tree)
     * 
     * @param selectedUnlinkedNodes Nodes currently selected as child nodes
     */
    public void setSelectedUnlinkedNodes(Collection<WorkspaceTreeNode> selectedChildNodes) {
        this.selectedUnlinkedNodes = selectedChildNodes;
    }
}
