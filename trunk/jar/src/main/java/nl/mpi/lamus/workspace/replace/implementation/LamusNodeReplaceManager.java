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
package nl.mpi.lamus.workspace.replace.implementation;

import java.util.ArrayList;
import java.util.List;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.NodeReplaceManager;
import nl.mpi.lamus.workspace.replace.NodeReplaceCheckerAssigner;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see NodeReplaceManager
 * 
 * @author guisil
 */
@Component
public class LamusNodeReplaceManager implements NodeReplaceManager {

    private NodeReplaceCheckerAssigner nodeReplaceCheckerFactory;
    private ReplaceActionManager replaceActionManager;
    
    @Autowired
    public LamusNodeReplaceManager(NodeReplaceCheckerAssigner nodeReplaceManagerFactory, ReplaceActionManager replaceActionManager) {
        this.nodeReplaceCheckerFactory = nodeReplaceManagerFactory;
        this.replaceActionManager = replaceActionManager;
    }
    
    /**
     * @see NodeReplaceManager#replaceTree(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void replaceTree(WorkspaceNode oldNode, WorkspaceNode newNode, WorkspaceNode parentNode) throws WorkspaceException, ProtectedNodeException {
        
        if(oldNode.getWorkspaceID() != newNode.getWorkspaceID()) {
            throw new IllegalArgumentException("Old node and new node belong to different workspaces.");
        }
        
        List<NodeReplaceAction> actions = new ArrayList<>();
        NodeReplaceChecker replaceChecker = nodeReplaceCheckerFactory.getReplaceCheckerForNode(oldNode);
        replaceChecker.decideReplaceActions(oldNode, newNode, parentNode, false, actions);
        
        replaceActionManager.applyActions(actions);
    }
    
}
