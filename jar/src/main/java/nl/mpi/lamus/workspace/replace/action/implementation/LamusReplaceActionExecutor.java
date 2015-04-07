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
package nl.mpi.lamus.workspace.replace.action.implementation;

import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.management.WorkspaceNodeManager;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see ReplaceActionExecutor
 * 
 * @author guisil
 */
@Component
public class LamusReplaceActionExecutor implements ReplaceActionExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusReplaceActionExecutor.class);

    private WorkspaceNodeLinkManager workspaceNodeLinkManager;
    private WorkspaceNodeManager workspaceNodeManager;
    
    
    @Autowired
    public LamusReplaceActionExecutor(
            WorkspaceNodeLinkManager wsNodeLinkManager, WorkspaceNodeManager wsNodeManager) {
        workspaceNodeLinkManager = wsNodeLinkManager;
        workspaceNodeManager = wsNodeManager;
    }
    
    /**
     * @see ReplaceActionExecutor#execute(nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction)
     */
    @Override
    public void execute(NodeReplaceAction action) throws WorkspaceException, ProtectedNodeException {
        
        logger.debug("Executing action: " + action.toString());

        if(action instanceof ReplaceNodeReplaceAction) {
            executeReplaceAction((ReplaceNodeReplaceAction) action); //(ir, ws, tag, (ReplaceNodeAction) currentAction, error);
        } else if(action instanceof DeleteNodeReplaceAction) {
            executeDeleteAction((DeleteNodeReplaceAction) action); //(ws, tag, (DeleteNodeAction) currentAction, error);
        } else if(action instanceof UnlinkNodeReplaceAction) {
            executeUnlinkAction((UnlinkNodeReplaceAction) action); //(ws, tag, (UnlinkNodeAction) currentAction, error);
        } else if(action instanceof LinkNodeReplaceAction) {
            executeLinkAction((LinkNodeReplaceAction) action); //(ws, tag, (LinkNodeAction) currentAction, error);
        } else if(action instanceof MoveLinkLocationNodeReplaceAction) {
            executeMoveLinkLocationAction((MoveLinkLocationNodeReplaceAction) action); //(ws, tag, (MoveLinkLocationAction) currentAction, error);
        } else if(action instanceof RemoveArchiveUriReplaceAction) {
            executeRemoveArchiveUriAction((RemoveArchiveUriReplaceAction) action);
        }
    }
    
    private void executeReplaceAction(ReplaceNodeReplaceAction action) throws WorkspaceException, ProtectedNodeException {
        
        logger.debug("Executing Replace Action: " + action.toString());

        workspaceNodeLinkManager.replaceNode(action.getParentNode(), action.getAffectedNode(), action.getNewNode(), action.isAlreadyLinked());
    }
    
    private void executeDeleteAction(DeleteNodeReplaceAction action) throws WorkspaceException, ProtectedNodeException {
        
        logger.debug("Executing Delete Action: " + action.toString());

        workspaceNodeManager.deleteNodesRecursively(action.getAffectedNode());
        
    }
    
    private void executeUnlinkAction(UnlinkNodeReplaceAction action) throws WorkspaceException, ProtectedNodeException {
        
        logger.debug("Executing Unlink Action: " + action.toString());

        workspaceNodeLinkManager.unlinkNodes(action.getParentNode(), action.getAffectedNode());
    }
    
    private void executeLinkAction(LinkNodeReplaceAction action) throws WorkspaceException, ProtectedNodeException {
        
        logger.debug("Executing Link Action: " + action.toString());

        workspaceNodeLinkManager.linkNodes(action.getParentNode(), action.getAffectedNode());
    }
    
    private void executeMoveLinkLocationAction(MoveLinkLocationNodeReplaceAction action) {
        throw new UnsupportedOperationException("not implemented yet");
    }
    
    private void executeRemoveArchiveUriAction(RemoveArchiveUriReplaceAction action) throws WorkspaceException {
        
        logger.debug("Executing Remove Archive URI Action: " + action.toString());
        
        workspaceNodeLinkManager.removeArchiveUriFromChildNode(action.getParentNode(), action.getAffectedNode());
    }
}
