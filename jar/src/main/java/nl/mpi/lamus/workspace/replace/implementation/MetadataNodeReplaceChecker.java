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
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.NodeReplaceExplorer;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of NodeReplaceChecker for metadata nodes.
 * 
 * @author guisil
 */
public class MetadataNodeReplaceChecker implements NodeReplaceChecker {

    private static final Logger logger = LoggerFactory.getLogger(ResourceNodeReplaceChecker.class);

    private final ReplaceActionManager replaceActionManager;
    private final ReplaceActionFactory replaceActionFactory;
    private final NodeReplaceExplorer nodeReplaceExplorer;
    
    public MetadataNodeReplaceChecker(ReplaceActionManager actionManager,
            ReplaceActionFactory actionFactory, NodeReplaceExplorer replaceExplorer) {
        
        replaceActionManager = actionManager;
        replaceActionFactory = actionFactory;
        nodeReplaceExplorer = replaceExplorer;
    }
    
    /**
     * @see NodeReplaceChecker#decideReplaceActions(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, boolean, java.util.List)
     */
    @Override
    public void decideReplaceActions(WorkspaceNode oldNode, WorkspaceNode newNode, WorkspaceNode parentNode, boolean newNodeAlreadyLinked, List<NodeReplaceAction> actions) {
        
        logger.debug("Deciding which actions should take place to perform the replacement of metadata node " + oldNode.getWorkspaceNodeID() + " by node " + newNode.getWorkspaceNodeID());
        
        //TODO CHECK IF OLD NODE AND NEW NODE ARE OF SIMILAR TYPES
        //TODO CHECK IF OLD NODE AND NEW NODE ARE THE SAME (can happen in a tree replace with links to the archive)
        //TODO CHECK EXTERNAL NODES?
        //TODO ...
        
        //TODO CHECK IF NEW FILE EXISTS, IS READABLE?...
        
        
        
        //TODO CHECK IF THE OLD NODE WAS NEWLY ADDED IN THIS WORKSPACE
        
        //TODO MAKE SURE THAT HANDLES ARE THE SAME (BUT THE NEW NODE WAS NEWLY ADDED IN THIS WORKSPACE),
            // OTHERWISE WARN THAT THE WHOLE BRANCH WILL BE REPLACED WITHOUT VERSIONING
        
        //TODO CHECK FOR CIRCULAR LINKS (ADD MULTIPLE PARENTS TO THE CHECK?)
        
        
        //TODO IF THERE'S NO HANDLE IN THE NEW NODE (IT'S NOT A NEW VERSION), A REPLACE SHOULD BE PERFORMED,
            // BUT THE NODE ITSELF SHOULD BE HANDLED LATER AS AN ADDED NODE, AND THE OLD NODE SHOULD BE HANDLED AS AN UNLINKED? DELETED NODE?
        //TODO IF THERE'S A HANDLE IN THE NEW NODE (IT'S A NEW VERSION), A REPLACE SHOULD BE PERFORMED,
            // AND LATER IT SHOULD BE HANDLED AS A NEW NODE, AND THE OLD NODE SHOULD BE HANDLED AS A REPLACED NODE
        
        
        //TODO IF THERE ARE CHILDREN, EXPLORE THOSE
        //TODO CHECK IF ALL LINKS IN METADATA ARE IN THE DB? (SHOULD BE, OF COURSE), BUT I WOULD PREFER TO ONLY RELY ON ONE OF THESE THINGS...
        
        //TODO FOR EACH CHILD (OLD NODE) CALL THE APPROPRIATE REPLACE CHECKER...
        
        
        
        replaceActionManager.addActionToList(replaceActionFactory.getReplaceAction(oldNode, parentNode, newNode, newNodeAlreadyLinked), actions);
        
        
        //TODO CHECK CIRCULAR LINKS
        
        
        // if the node to replace is protected, the replace action is added to the list anyway
        // but the exploring of the child nodes is skipped (protected nodes don't have their children imported)
        
        if(!oldNode.isProtected()) {
            nodeReplaceExplorer.exploreReplace(oldNode, newNode, actions);
        }
    }
    
}
