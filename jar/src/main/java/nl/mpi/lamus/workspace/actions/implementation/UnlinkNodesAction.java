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
package nl.mpi.lamus.workspace.actions.implementation;

import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;

/**
 * Implementation of the action to unlink nodes.
 * 
 * @author guisil
 */
public class UnlinkNodesAction extends WsTreeNodesAction {

    private final String name = "unlink_node_action";
    
    /**
     * @see WsTreeNodesAction#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see WsTreeNodesAction#execute(java.lang.String, nl.mpi.lamus.service.WorkspaceService)
     */
    @Override
    public void execute(String userID, WorkspaceService wsService) throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        if(wsService == null) {
            throw new IllegalArgumentException("WorkspaceService should have been set");
        }
        
        if(selectedTreeNodes == null) {
            throw new IllegalArgumentException("Action for unlinking nodes requires at least one tree node; currently null");
        }
        else if(selectedTreeNodes.isEmpty()) {
            throw new IllegalArgumentException("Action for unlinking nodes requires at least one tree node; currently selected " + selectedTreeNodes.size());
        }
        
        // childNodes not used for this action
        
        for(WorkspaceTreeNode currentNode : selectedTreeNodes) {
            wsService.unlinkNodes(userID, currentNode.getParent(), currentNode);
        }
    }
    
}
