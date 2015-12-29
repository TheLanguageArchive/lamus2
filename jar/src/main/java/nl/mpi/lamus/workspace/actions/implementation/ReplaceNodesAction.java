/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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

import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;

/**
 * Implementation of the action to replace nodes.
 * 
 * @author guisil
 */
public class ReplaceNodesAction extends WsTreeNodesAction {

    private final String name = "replace_node_action";
    
    
    /**
     * @see WsTreeNodesAction#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }
    
    /**
     * @see WsTreeNodesAction#execute(java.lang.String, nl.mpi.lamus.service.WorkspaceService,
     *  nl.mpi.lamus.dao.WorkspaceDao, nl.mpi.lamus.workspace.model.NodeUtil) 
     */
    @Override
    public void execute(String userID, WorkspaceService wsService,
            WorkspaceDao wsDao, NodeUtil nodeUtil)
                throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        if(wsService == null) {
            throw new IllegalArgumentException("WorkspaceService should have been set");
        }
        
        if(selectedTreeNodes == null) {
            throw new IllegalArgumentException("Action for replacing nodes requires exactly one tree node; currently null");
        }
        else if(selectedTreeNodes.size() != 1) {
            throw new IllegalArgumentException("Action for replacing nodes requires exactly one tree node; currently selected " + selectedTreeNodes.size());
        }
        if(selectedUnlinkedNodes == null) {
            throw new IllegalArgumentException("Action for replacing nodes requires exactly one selected unlinked node; currently null");
        }
        else if(selectedUnlinkedNodes.size() != 1) {
            throw new IllegalArgumentException("Action for replacing nodes requires exactly one selected unlinked node; currently selected " + selectedUnlinkedNodes.size());
        }
        
        WorkspaceTreeNode oldNode = selectedTreeNodes.iterator().next();
        WorkspaceTreeNode newNode = selectedUnlinkedNodes.iterator().next();
        WorkspaceTreeNode parentNode = oldNode.getParent();
        
        wsService.replaceTree(userID, oldNode, newNode, parentNode);
    }
    
}
