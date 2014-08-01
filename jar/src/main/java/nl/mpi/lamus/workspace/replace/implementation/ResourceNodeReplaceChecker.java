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
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of NodeReplaceChecker for resource nodes.
 * 
 * @author guisil
 */
public class ResourceNodeReplaceChecker implements NodeReplaceChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(ResourceNodeReplaceChecker.class);

    private final CorpusStructureProvider corpusStructureProvider;
    private final ArchiveFileHelper archiveFileHelper;
    private final ReplaceActionManager replaceActionManager;
    private final ReplaceActionFactory replaceActionFactory;
    
    public ResourceNodeReplaceChecker(CorpusStructureProvider csProvider,
            ArchiveFileHelper aFileHelper, ReplaceActionManager actionManager,
            ReplaceActionFactory actionFactory) {
        
        corpusStructureProvider = csProvider;
        archiveFileHelper = aFileHelper;
        replaceActionManager = actionManager;
        replaceActionFactory = actionFactory;
    }
    
    /**
     * @see NodeReplaceChecker#decideReplaceActions(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, boolean, java.util.List)
     */
    @Override
    public void decideReplaceActions(WorkspaceNode oldNode, WorkspaceNode newNode, WorkspaceNode parentNode, boolean newNodeAlreadyLinked, List<NodeReplaceAction> actions) {
        
        logger.debug("Deciding which actions should take place to perform the replacement of resource node " + oldNode.getWorkspaceNodeID() + " by node " + newNode.getWorkspaceNodeID());
        
        if(!oldNode.getFormat().equals(newNode.getFormat())) {
            
            replaceActionManager.addActionToList(replaceActionFactory.getUnlinkAction(oldNode, parentNode), actions);
            replaceActionManager.addActionToList(replaceActionFactory.getDeleteAction(oldNode), actions);
            replaceActionManager.addActionToList(replaceActionFactory.getLinkAction(newNode, parentNode), actions);
            return;
        } 
        
        CorpusNode archiveNode = corpusStructureProvider.getNode(oldNode.getArchiveURI());
        
        
        //TODO CHECK IF OLD NODE AND NEW NODE ARE OF SIMILAR TYPES
        //TODO CHECK IF OLD NODE AND NEW NODE ARE THE SAME (can happen in a tree replace with links to the archive)
        //TODO CHECK EXTERNAL NODES?
        //TODO ...
        
        //TODO CHECK IF NEW FILE EXISTS, IS READABLE?...
        
        
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
