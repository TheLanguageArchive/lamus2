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

import java.util.Collection;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.actions.WsParentMultipleChildNodesAction;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;

/**
 * Implementation of the action to link nodes.
 * 
 * @author guisil
 */
public class LinkNodesAction implements WsParentMultipleChildNodesAction {

    private final String name = "Link";
    
    
    /**
     * @see WsParentMultipleChildNodesAction#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see WsParentMultipleChildNodesAction#execute(java.lang.String, nl.mpi.lamus.workspace.tree.WorkspaceTreeNode, java.util.Collection, nl.mpi.lamus.service.WorkspaceService)
     */
    @Override
    public void execute(String userID, WorkspaceTreeNode parentNode, Collection<WorkspaceTreeNode> childNodes, WorkspaceService wsService)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        for(WorkspaceTreeNode currentNode : childNodes) {
            wsService.linkNodes(userID, parentNode, currentNode);
        }
    }
}
