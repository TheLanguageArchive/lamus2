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

import java.util.List;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.NodeReplaceExplorer;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of NodeReplaceChecker for metadata nodes.
 * 
 * @author guisil
 */
@Component
public class MetadataNodeReplaceChecker implements NodeReplaceChecker {

    private static final Logger logger = LoggerFactory.getLogger(ResourceNodeReplaceChecker.class);

    @Autowired
    private ReplaceActionManager replaceActionManager;
    @Autowired
    private ReplaceActionFactory replaceActionFactory;
    @Autowired
    private NodeReplaceExplorer nodeReplaceExplorer;
    
    
    /**
     * @see NodeReplaceChecker#decideReplaceActions(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, boolean, java.util.List)
     */
    @Override
    public void decideReplaceActions(WorkspaceNode oldNode, WorkspaceNode newNode, WorkspaceNode parentNode, boolean newNodeAlreadyLinked, List<NodeReplaceAction> actions)
            throws ProtectedNodeException {
        
        logger.debug("Deciding which actions should take place to perform the replacement of metadata node " + oldNode.getWorkspaceNodeID() + " by node " + newNode.getWorkspaceNodeID());

        // if the node to replace is the top node of the workspace, the replace action should not go ahead
        if(parentNode == null) {
            String message = "Cannot proceed with replacement because replacing the top node of the workspace is not allowed";
            throw new ProtectedNodeException(message, oldNode.getArchiveURI(), oldNode.getWorkspaceID());
        }
        
        
        // if the node to replace is protected, the replace action should not go ahead
        if(oldNode.isProtected()) {
            String message = "Cannot proceed with replacement because old node (ID = " + oldNode.getWorkspaceNodeID() + ") is protected (WS ID = " + oldNode.getWorkspaceID() + ").";
            throw new ProtectedNodeException(message, oldNode.getArchiveURI(), oldNode.getWorkspaceID());
        }
        
        
        replaceActionManager.addActionToList(replaceActionFactory.getReplaceAction(oldNode, parentNode, newNode, newNodeAlreadyLinked), actions);
        
        
        //TODO CHECK CIRCULAR LINKS
        
        nodeReplaceExplorer.exploreReplace(oldNode, newNode, actions);
    }
    
}
