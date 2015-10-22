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
import nl.mpi.handle.util.HandleParser;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.IncompatibleNodesException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.NodeReplaceExplorer;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import org.apache.commons.io.FilenameUtils;
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
    @Autowired
    private WorkspaceDao workspaceDao;
    @Autowired
    private HandleParser handleParser;
    
    
    /**
     * @see NodeReplaceChecker#decideReplaceActions(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, boolean, java.util.List)
     */
    @Override
    public void decideReplaceActions(WorkspaceNode oldNode, WorkspaceNode newNode, WorkspaceNode parentNode, boolean newNodeAlreadyLinked, List<NodeReplaceAction> actions)
            throws ProtectedNodeException, IncompatibleNodesException {
        
        logger.debug("Deciding which actions should take place to perform the replacement of metadata node " + oldNode.getWorkspaceNodeID() + " by node " + newNode.getWorkspaceNodeID());

        // if the node to replace is the top node of the workspace, the replace action should not go ahead
        if(parentNode == null) {
            
            //TODO If the oldNode is the top node of the workspace, its compatibility with the new node should be checked (same archive handle and filename)
            if(workspaceDao.isTopNodeOfWorkspace(oldNode.getWorkspaceID(), oldNode.getWorkspaceNodeID())) {
                ensureWsTopNodesAreCompatible(oldNode, newNode);
            } else {
                String message = "Parent node was passed as null but node to replace is not top node of the workspace";
                throw new IllegalArgumentException(message);
            }
        }
        
        // not actually replacing this one, so this check should be done before anything else can block the action
        if(oldNode.getWorkspaceNodeID() == newNode.getWorkspaceNodeID()) {
            logger.debug("Old Node and New Node are the same. Unlinking from old parent.");
            replaceActionManager.addActionToList(replaceActionFactory.getUnlinkFromOldParentAction(oldNode, parentNode), actions);
            return;
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
    
    
    private boolean ensureWsTopNodesAreCompatible(WorkspaceNode oldNode, WorkspaceNode newNode) throws IncompatibleNodesException {
        
        boolean handlesAreEquivalent = false;
        try {
            handlesAreEquivalent = handleParser.areHandlesEquivalent(oldNode.getArchiveURI(), newNode.getArchiveURI());
        } catch(IllegalArgumentException ex) {
            //since the old node would always have a valid handle, this means that the new one is not valid, so they're not equivalent
            logger.warn("New node has invalid archive URI (" + newNode.getArchiveURI() + ")");
        }
        
    	if(!handlesAreEquivalent) {
            String message = "Incompatible top nodes (different handles). Old: " + oldNode.getArchiveURI() + "; New: " + newNode.getArchiveURI();
            logger.error(message);
            throw new IncompatibleNodesException(message, oldNode.getWorkspaceID(), oldNode.getWorkspaceNodeID(), newNode.getWorkspaceNodeID(), null);
        }
        
        if(oldNode.getArchiveURL() != null && newNode.getWorkspaceURL() != null) {
            String oldNodeArchiveURLStr = oldNode.getArchiveURL().toString();
            String oldNodeFilename = FilenameUtils.getName(oldNodeArchiveURLStr);
            String newNodeWorkspaceURLtr = newNode.getWorkspaceURL().toString();
            String newNodeFilename = FilenameUtils.getName(newNodeWorkspaceURLtr);
            if(!oldNodeFilename.equals(newNodeFilename)) {
                String message = "Incompatible top nodes (different filename). Old: " + oldNodeFilename + "; New: " + newNodeFilename;
                logger.error(message);
                throw new IncompatibleNodesException(message, oldNode.getWorkspaceID(), oldNode.getWorkspaceNodeID(), newNode.getWorkspaceNodeID(), null);
            }
        } else {
            String message = "Couldn't verify filename compatibility. Old node Archive URL: " + oldNode.getArchiveURL() + "; New node Workspace URL: " + newNode.getWorkspaceURL();
            logger.error(message);
            throw new IncompatibleNodesException(message, oldNode.getWorkspaceID(), oldNode.getWorkspaceNodeID(), newNode.getWorkspaceNodeID(), null);
        }
        
        return true;
    }
}
