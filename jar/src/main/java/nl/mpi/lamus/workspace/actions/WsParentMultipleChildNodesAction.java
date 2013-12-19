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
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;

/**
 * Interface for node actions in a workspace which involve a parent and child nodes.
 * 
 * @author guisil
 */
public interface WsParentMultipleChildNodesAction extends WsNodesAction {
    
    /**
     * Executes the action.
     * 
     * @param userID ID of the user
     * @param parentNode parent node in which the action will be applied
     * @param childNodes collection of child nodes in which the action will be applied
     * @param wsService service which will be called to perform the action
     */
    public void execute(String userID, WorkspaceTreeNode parentNode, Collection<WorkspaceTreeNode> childNodes, WorkspaceService wsService)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException;
}
