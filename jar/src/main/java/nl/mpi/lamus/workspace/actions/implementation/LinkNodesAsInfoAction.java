/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;

/**
 * Implementation of the action to link nodes as info files.
 * 
 * @author guisil
 */
public class LinkNodesAsInfoAction extends WsTreeNodesAction {

    private final String name = "link_node_info_action";
    
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
        if(wsDao == null) {
            throw new IllegalArgumentException("WorkspaceDao should have been set");
        }
        if(nodeUtil == null) {
            throw new IllegalArgumentException("NodeUtil should have been set");
        }
        
        if(selectedTreeNodes == null) {
            throw new IllegalArgumentException("Action for linking nodes as info files requires exactly one tree node; currently null");
        }
        else if(selectedTreeNodes.size() != 1) {
            throw new IllegalArgumentException("Action for linking nodes as info files requires exactly one tree node; currently selected " + selectedTreeNodes.size());
        }
        if(selectedUnlinkedNodes == null) {
            throw new IllegalArgumentException("Action for linking nodes as info files requires at least one selected child node; currently null");
        }
        else if(selectedUnlinkedNodes.isEmpty()) {
            throw new IllegalArgumentException("Action for linking nodes as info files requires at least one selected child node");
        }
        
        int failedLinks = 0;
        for(WorkspaceTreeNode currentNode : selectedUnlinkedNodes) {
            if(nodeUtil.isNodeMetadata(currentNode)) {
                failedLinks++;
                continue;
            }
            currentNode.setType(WorkspaceNodeType.RESOURCE_INFO);
            wsService.linkNodes(userID, selectedTreeNodes.iterator().next(), currentNode);
            wsDao.updateNodeType(currentNode);
        }
        
        if(failedLinks > 0) {
            throw new IllegalArgumentException("Some nodes (" + failedLinks + ") were not linked. Metadata nodes cannot be linked as info files.");
        }
    }
}
