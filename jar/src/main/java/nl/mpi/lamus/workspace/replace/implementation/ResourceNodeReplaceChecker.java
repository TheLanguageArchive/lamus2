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

import java.io.File;
import java.util.List;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of NodeReplaceChecker for resource nodes.
 * 
 * @author guisil
 */
@Component
public class ResourceNodeReplaceChecker implements NodeReplaceChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(ResourceNodeReplaceChecker.class);

    @Autowired
    private CorpusStructureProvider corpusStructureProvider;
    @Autowired
    private ArchiveFileHelper archiveFileHelper;
    @Autowired
    private ReplaceActionManager replaceActionManager;
    @Autowired
    private ReplaceActionFactory replaceActionFactory;
    
    
    /**
     * @see NodeReplaceChecker#decideReplaceActions(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, boolean, java.util.List)
     */
    @Override
    public void decideReplaceActions(WorkspaceNode oldNode, WorkspaceNode newNode, WorkspaceNode parentNode, boolean newNodeAlreadyLinked, List<NodeReplaceAction> actions)
            throws ProtectedNodeException {
        
        logger.debug("Deciding which actions should take place to perform the replacement of resource node " + oldNode.getWorkspaceNodeID() + " by node " + newNode.getWorkspaceNodeID());
        
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
        
        if(oldNode.isExternal() || !oldNode.getFormat().equals(newNode.getFormat())) {
            
            replaceActionManager.addActionToList(replaceActionFactory.getUnlinkAction(oldNode, parentNode), actions);
            replaceActionManager.addActionToList(replaceActionFactory.getDeleteAction(oldNode), actions);
            replaceActionManager.addActionToList(replaceActionFactory.getLinkAction(newNode, parentNode), actions);
            return;
        } 
        
        CorpusNode archiveNode = corpusStructureProvider.getNode(oldNode.getArchiveURI());
        
        if(archiveNode == null ||
                archiveFileHelper.hasArchiveFileChanged(archiveNode.getFileInfo(), new File(newNode.getWorkspaceURL().getPath()))) {
            
            replaceActionManager.addActionToList(replaceActionFactory.getReplaceAction(oldNode, parentNode, newNode, newNodeAlreadyLinked), actions);
            
        } else {
            
            if(newNodeAlreadyLinked) {
                replaceActionManager.addActionToList(replaceActionFactory.getUnlinkAction(newNode, parentNode), actions);
                replaceActionManager.addActionToList(replaceActionFactory.getDeleteAction(newNode), actions);
                replaceActionManager.addActionToList(replaceActionFactory.getLinkAction(oldNode, parentNode), actions);
            } else {
                
                
                //TODO do nothing? add some action? DELETE action?
            }
        }
    }
    
}
