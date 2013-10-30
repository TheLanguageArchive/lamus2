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
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;

/**
 *
 * @author guisil
 */
public class DeleteNodesAction implements WsTreeNodesAction {

    private final String name = "Delete";
    
    private WorkspaceService workspaceService;
    
    public DeleteNodesAction(WorkspaceService wsService) {
        this.workspaceService = wsService;
    }

    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public void execute(String userID, Collection<WorkspaceTreeNode> nodes) {
        for(WorkspaceTreeNode currentNode : nodes) {
            this.workspaceService.deleteNode(userID, currentNode);
        }
    }
    
}
