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
import java.util.Collection;
import java.util.List;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.NodeReplaceCheckerFactory;
import nl.mpi.lamus.workspace.replace.NodeReplaceExplorer;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see NodeReplaceExplorer
 * 
 * @author guisil
 */
@Component
public class LamusNodeReplaceExplorer implements NodeReplaceExplorer {

    private static final Logger logger = LoggerFactory.getLogger(LamusNodeReplaceExplorer.class);
    
    private final WorkspaceDao workspaceDao;
    private final NodeReplaceCheckerFactory nodeReplaceCheckerFactory;
    private final ReplaceActionFactory replaceActionFactory;
    private final ReplaceActionManager replaceActionManager;
    
    @Autowired
    public LamusNodeReplaceExplorer(WorkspaceDao wsDao,
            NodeReplaceCheckerFactory replaceCheckerFactory,
            ReplaceActionFactory actionFactory, ReplaceActionManager actionManager) {
        workspaceDao = wsDao;
        nodeReplaceCheckerFactory = replaceCheckerFactory;
        replaceActionFactory = actionFactory;
        replaceActionManager = actionManager;
    }
    
    /**
     * @see NodeReplaceExplorer#exploreReplace(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void exploreReplace(WorkspaceNode oldNode, WorkspaceNode newNode, List<NodeReplaceAction> actions) {
        
        Collection<WorkspaceNode> oldNodeChildren = workspaceDao.getChildWorkspaceNodes(oldNode.getWorkspaceNodeID());
        Collection<WorkspaceNode> newNodeChildren = workspaceDao.getChildWorkspaceNodes(newNode.getWorkspaceNodeID());
        
        Collection<WorkspaceNode> matchedNewChildren = new ArrayList<WorkspaceNode>();
        
        for(WorkspaceNode oldChild : oldNodeChildren) {
            // traversing old node children
            
            WorkspaceNode currentMatch = null;
            
            
//            if(oldChild.getArchiveURI() == null) {
//                throw new UnsupportedOperationException("null archiveURI not handled");
//            }
            
            for(WorkspaceNode newChild : newNodeChildren) {
                
                if(oldChild.getArchiveURI().equals(newChild.getArchiveURI())) {
                    matchedNewChildren.add(newChild);
                    currentMatch = newChild;
                    break;
                }
            }
            
            if(currentMatch != null) {
                NodeReplaceChecker currentNodeReplaceChecker = nodeReplaceCheckerFactory.getReplaceCheckerForNode(oldChild);
                currentNodeReplaceChecker.decideReplaceActions(oldChild, currentMatch, newNode, true, actions);
            } else {
                
                //TODO something else?
                
                replaceActionManager.addActionToList(replaceActionFactory.getDeleteAction(oldChild), actions);
                
                //TODO something else?
            }
        }
        
        if(matchedNewChildren.size() != newNodeChildren.size()) { // not all new nodes have a match
         
            for(WorkspaceNode newChild : newNodeChildren) {
                if(matchedNewChildren.contains(newChild)) {
                    continue;
                }
                replaceActionManager.addActionToList(replaceActionFactory.getRemoveArchiveUriAction(newChild, newNode), actions);
            }
            
        } else {
            //do nothing in this case? - nodes already matched...
        }
        
        
    }
    
}
