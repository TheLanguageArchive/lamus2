/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.archive.implementation;

import java.io.File;
import java.net.URI;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.CorpusStructureBridge;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see CorpusStructureBridge
 * @author guisil
 */
@Component
public class LamusCorpusStructureBridge implements CorpusStructureBridge{
    
    private static final Logger logger = LoggerFactory.getLogger(LamusCorpusStructureBridge.class);
    
    private final CorpusStructureProvider corpusStructureProvider;
    private final NodeResolver nodeResolver;
    
    private final String corpusstructureDirectoryName;
    private final String metadataDirectoryName;
    
    
    @Autowired
    public LamusCorpusStructureBridge(
            CorpusStructureProvider csProvider, NodeResolver nResolver,
            @Qualifier("corpusstructureDirectoryName") String csDirName,
            @Qualifier("metadataDirectoryName") String mdDirName) {
        corpusStructureProvider = csProvider;
        nodeResolver = nResolver;
        corpusstructureDirectoryName = csDirName;
        metadataDirectoryName = mdDirName;
    }

    /**
     * @see CorpusStructureBridge#getCorpusNamePathToClosestTopNode(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public String getCorpusNamePathToClosestTopNode(WorkspaceNode node) {
        
        StringBuilder pathSoFar = new StringBuilder();
        boolean foundTopNode = false;
        
        URI currentNodeURI = node.getArchiveURI();
        CorpusNode currentCorpusNode = corpusStructureProvider.getNode(currentNodeURI);
        if(currentCorpusNode == null) {
            String errorMessage = "Node not found in archive database for URI " + currentNodeURI;
            logger.error(errorMessage);
            return null;
        }
        File currentLocalFile = nodeResolver.getLocalFile(currentCorpusNode);
        String currentLocalPath = currentLocalFile.getAbsolutePath();
        boolean currentPathContainsCorpusstructureDir = currentLocalPath.contains(File.separator + corpusstructureDirectoryName + File.separator);
        boolean currentPathContainsMetadataDir = currentLocalPath.contains(File.separator + metadataDirectoryName + File.separator);
        
        while(!foundTopNode) {
        
            URI parentNodeURI = corpusStructureProvider.getCanonicalParent(currentNodeURI);
            if(parentNodeURI == null) {
                String errorMessage = "Could not retrieve canonical parent for node " + currentNodeURI;
                logger.error(errorMessage);
                return null;
            }
            CorpusNode parentCorpusNode = corpusStructureProvider.getNode(parentNodeURI);
            if(parentCorpusNode == null) {
                String errorMessage = "Node not found in archive database for URI " + parentNodeURI;
                logger.error(errorMessage);
                return null;
            }
            File parentLocalFile = nodeResolver.getLocalFile(parentCorpusNode);
            String parentLocalPath = parentLocalFile.getAbsolutePath();
            boolean parentPathContainsCorpusstructureDir = parentLocalPath.contains(File.separator + corpusstructureDirectoryName + File.separator);
            boolean parentPathContainsMetadataDir = parentLocalPath.contains(File.separator + metadataDirectoryName + File.separator);
            
            if(currentPathContainsCorpusstructureDir) {
                String currentDirectory = FilenameUtils.getFullPath(currentLocalPath);
                String parentDirectory = FilenameUtils.getFullPath(parentLocalPath);
                if(!currentDirectory.equals(parentDirectory)) {
                    foundTopNode = true;
                } else {
                    insertStringInTheBeginning(pathSoFar, parentCorpusNode.getName());
                }
            } else if(currentPathContainsMetadataDir && parentPathContainsCorpusstructureDir) {
                insertStringInTheBeginning(pathSoFar, parentCorpusNode.getName());
            }
            
            currentNodeURI = parentNodeURI;
            currentCorpusNode = parentCorpusNode;
            currentLocalFile = parentLocalFile;
            currentLocalPath = parentLocalPath;
            currentPathContainsCorpusstructureDir = parentPathContainsCorpusstructureDir;
            currentPathContainsMetadataDir = parentPathContainsMetadataDir;
        }
        
        return pathSoFar.toString();
    }
    
    
    private void insertStringInTheBeginning(StringBuilder builder, String toInsert) {
        if(builder.length() > 0) {
            builder.insert(0, File.separator);
        }
        builder.insert(0, toInsert);
    }
}
