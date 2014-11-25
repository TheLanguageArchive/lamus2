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

import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.NodeReplaceCheckerAssigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see NodeReplaceCheckerAssigner
 * 
 * @author guisil
 */
@Component
public class LamusNodeReplaceCheckerAssigner implements NodeReplaceCheckerAssigner {

    private static final Logger logger = LoggerFactory.getLogger(LamusNodeReplaceCheckerAssigner.class);
    
    @Autowired
    private NodeReplaceChecker metadataNodeReplaceChecker;
    @Autowired
    private NodeReplaceChecker resourceNodeReplaceChecker;
    
    
    /**
     * @see NodeReplaceCheckerAssigner#getReplaceCheckerForNode(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public NodeReplaceChecker getReplaceCheckerForNode(WorkspaceNode node) {
        
        if(WorkspaceNodeType.RESOURCE.equals(node.getType())) {
            return resourceNodeReplaceChecker;
        } else { //assuming any other node is metadata (?)
            return metadataNodeReplaceChecker;
        }
    }
}
