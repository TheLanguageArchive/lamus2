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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
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
    private final ArchiveFileHelper archiveFileHelper;
    
    private final String corpusstructureDirectoryName;
    private final String metadataDirectoryName;
    
    
    @Autowired
    public LamusCorpusStructureBridge(
            CorpusStructureProvider csProvider, NodeResolver nResolver,
            ArchiveFileHelper afHelper,
            @Qualifier("corpusstructureDirectoryName") String csDirName,
            @Qualifier("metadataDirectoryName") String mdDirName) {
        corpusStructureProvider = csProvider;
        nodeResolver = nResolver;
        archiveFileHelper = afHelper;
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
        
        String nextNodeNameToInsert = "";
        
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
                    
                    //for the top node, the path name (instead of the node name) should be used, since top node folders were probably created by corpus managers
                    if(!nextNodeNameToInsert.isEmpty()) {
                        insertStringInTheBeginning(pathSoFar, getFolderNameBeforeCorpusstructure(currentDirectory));
                    }
                    
                } else {
                    if(!nextNodeNameToInsert.isEmpty()) {
                        insertStringInTheBeginning(pathSoFar, nextNodeNameToInsert);
                    }
                    nextNodeNameToInsert = archiveFileHelper.correctPathElement(parentCorpusNode.getName(), "getCorpusNamePathToClosestTopNode");
                }
            } else if(currentPathContainsMetadataDir && parentPathContainsCorpusstructureDir) {
                if(!nextNodeNameToInsert.isEmpty()) {
                    insertStringInTheBeginning(pathSoFar, nextNodeNameToInsert);
                }
                nextNodeNameToInsert = archiveFileHelper.correctPathElement(parentCorpusNode.getName(), "getCorpusNamePathToClosestTopNode");
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

    /**
     * @see CorpusStructureBridge#getURIsOfAncestorsAndDescendants(java.net.URI)
     */
    @Override
    public List<String> getURIsOfAncestorsAndDescendants(URI nodeURI) {
        
        List<String> collectionToReturn = new ArrayList<>();
        
        URI ancestorURI = corpusStructureProvider.getCanonicalParent(nodeURI);
        while(ancestorURI != null) {
            URI ancestorPID = nodeResolver.getPID(ancestorURI);
            if(ancestorPID == null) {
                collectionToReturn.add(ancestorURI.toString());
            } else {
                collectionToReturn.add(ancestorPID.toString());
            }
            ancestorURI = corpusStructureProvider.getCanonicalParent(ancestorURI);
        }
        
        Collection<URI> descendants = corpusStructureProvider.getDescendants(nodeURI);
        for(URI descendantUri : descendants) {
            collectionToReturn.add(descendantUri.toString());
        }
        
        return collectionToReturn;
    }

    /**
     * @see CorpusStructureBridge#getURIsOfAncestors(java.net.URI)
     */
    @Override
    public List<URI> getURIsOfAncestors(URI nodeURI) {
        
        List<URI> collectionToReturn = new ArrayList<>();
        
        URI ancestorURI = corpusStructureProvider.getCanonicalParent(nodeURI);
        while(ancestorURI != null) {
            URI ancestorPID = nodeResolver.getPID(ancestorURI);
            if(ancestorPID == null) {
                collectionToReturn.add(ancestorURI);
            } else {
                collectionToReturn.add(ancestorPID);
            }
            ancestorURI = corpusStructureProvider.getCanonicalParent(ancestorURI);
        }
        
        return collectionToReturn;
    }
    
    
    private void insertStringInTheBeginning(StringBuilder path, String toInsert) {
        if(path.length() > 0) {
            path.insert(0, File.separator);
        }
        path.insert(0, toInsert);
    }
    
    private String getFolderNameBeforeCorpusstructure(String directory) {
        String pathBeforeCorpusstructure = directory.substring(0, directory.indexOf(File.separator + corpusstructureDirectoryName + File.separator));
        return pathBeforeCorpusstructure.substring(pathBeforeCorpusstructure.lastIndexOf(File.separator) + 1, pathBeforeCorpusstructure.length());
    }
}
