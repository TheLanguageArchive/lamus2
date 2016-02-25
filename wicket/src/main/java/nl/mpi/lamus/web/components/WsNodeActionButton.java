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
package nl.mpi.lamus.web.components;

import java.util.Collection;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.model.Model;

/**
 * Button with an associated workspace node action that can be triggered from here.
 * @author guisil
 */
public class WsNodeActionButton extends AutoDisablingAjaxButton {
    
    private final WsTreeNodesAction action;
    private final WorkspaceService workspaceService;
    private final WorkspaceDao workspaceDao;
    private final NodeUtil nodeUtil;
    
    public WsNodeActionButton(String id,
            WsTreeNodesAction action, WorkspaceService wsService,
            WorkspaceDao wsDao, NodeUtil nodeUtil) {
        super(id, new Model<>(action.getName()));
        this.action = action;
        this.workspaceService = wsService;
        this.workspaceDao = wsDao;
        this.nodeUtil = nodeUtil;
    }

    /**
     * To override from the page where the panel is included.
     */
    public void refreshStuff() {
        
    }
    
    /**
     * To override from the page where the panel is included.
     */
    public void refreshSelectedUnlinkedNodes() {
        
    }

    /**
     * Retrieves the action associated with this button.
     * @return Action
     */
    public WsTreeNodesAction getAction() {
        return action;
    }
    
    /**
     * Sets the parameters used to perform the action associated with this button.
     * @param selectedTreeNodes Currently selected tree nodes
     * @param selectedUnlinkedNodes Currently selected unlinked nodes
     */
    public void setActionParameters(Collection<WorkspaceTreeNode> selectedTreeNodes, Collection<WorkspaceTreeNode> selectedUnlinkedNodes) {
        action.setSelectedTreeNodes(selectedTreeNodes);
        action.setSelectedUnlinkedNodes(selectedUnlinkedNodes);
    }
    
    /**
     * Executes the action associated with this button
     * @param currentUserId ID of the user
     */
    public void executeAction(String currentUserId) throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        action.execute(currentUserId, workspaceService, workspaceDao, nodeUtil);
    }
    
}
