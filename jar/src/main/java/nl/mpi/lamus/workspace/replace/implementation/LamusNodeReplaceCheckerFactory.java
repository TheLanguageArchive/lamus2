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

import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.NodeReplaceCheckerFactory;
import nl.mpi.lamus.workspace.replace.NodeReplaceExplorer;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see NodeReplaceCheckerFactory
 * 
 * @author guisil
 */
@Component
public class LamusNodeReplaceCheckerFactory implements NodeReplaceCheckerFactory {

    private static final Logger logger = LoggerFactory.getLogger(LamusNodeReplaceCheckerFactory.class);
    
    @Autowired
    private CorpusStructureProvider corpusStructureProvider;
    @Autowired
    private ArchiveFileHelper archiveFileHelper;
    @Autowired
    private ReplaceActionManager replaceActionManager;
    @Autowired
    private ReplaceActionFactory replaceActionFactory;
    @Autowired
    private NodeReplaceExplorer nodeReplaceExplorer;
    
    private NodeReplaceChecker metadataNodeReplaceChecker;
    private NodeReplaceChecker resourceNodeReplaceChecker;
    
    public LamusNodeReplaceCheckerFactory() {
        
    }
    
//    @Autowired
//    public LamusNodeReplaceCheckerFactory(CorpusStructureProvider csProvider,
//            ArchiveFileHelper aFileHelper, ReplaceActionManager actionManager,
//            ReplaceActionFactory actionFactory, NodeReplaceExplorer replaceExplorer) {
//        corpusStructureProvider = csProvider;
//        archiveFileHelper = aFileHelper;
//        replaceActionManager = actionManager;
//        replaceActionFactory = actionFactory;
//        nodeReplaceExplorer = replaceExplorer;
//    }
    
    /**
     * @see NodeReplaceCheckerFactory#getReplaceCheckerForNode(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public NodeReplaceChecker getReplaceCheckerForNode(WorkspaceNode node) {
        
        if(WorkspaceNodeType.RESOURCE.equals(node.getType())) {
            return getResourceNodeReplaceChecker();
        } else { //assuming any other node is metadata (?)
            return getMetadataNodeReplaceChecker();
        }
    }
    
    private NodeReplaceChecker getMetadataNodeReplaceChecker() {
        logger.debug("Retrieving NodeReplaceChecker instance for a metadata node");
        
        if(metadataNodeReplaceChecker == null) {
            metadataNodeReplaceChecker = new MetadataNodeReplaceChecker(replaceActionManager,
                    replaceActionFactory, nodeReplaceExplorer);
        }
        return metadataNodeReplaceChecker;
    }
    
    private NodeReplaceChecker getResourceNodeReplaceChecker() {
        logger.debug("Retrieving NodeReplaceChecker instance for a resource node");
        
        if(resourceNodeReplaceChecker == null) {
            resourceNodeReplaceChecker =
                    new ResourceNodeReplaceChecker(corpusStructureProvider,
                        archiveFileHelper, replaceActionManager, replaceActionFactory);
        }
        return resourceNodeReplaceChecker;
    }
}
