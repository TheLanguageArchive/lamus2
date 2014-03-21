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
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.actions.WsParentSingleChildNodeAction;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;

/**
 * Implementation of the action to link an external node to the tree.
 * 
 * @author guisil
 */
public class LinkExternalNodesAction implements WsParentSingleChildNodeAction {

    private final String name = "link_external_node_action";
    
    
    /**
     * @see WsParentSingleChildNodeAction#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public void execute(String userID, WorkspaceTreeNode parentNode, WorkspaceNode childNode, WorkspaceService wsService)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        wsService.addNode(userID, childNode);
        wsService.linkNodes(userID, parentNode, childNode);
    }
}
