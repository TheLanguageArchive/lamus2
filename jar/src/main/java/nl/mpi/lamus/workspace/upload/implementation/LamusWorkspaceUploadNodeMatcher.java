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
package nl.mpi.lamus.workspace.upload.implementation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.lamus.archive.CorpusStructureBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadNodeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceUploadNodeMatcher
 * 
 * @author guisil
 */
@Component
public class LamusWorkspaceUploadNodeMatcher implements WorkspaceUploadNodeMatcher {

    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceUploadNodeMatcher.class);
    
    private final CorpusStructureProvider corpusStructureProvider;
    private final NodeResolver nodeResolver;
    private final HandleParser handleParser;
    private final WorkspaceNodeFactory workspaceNodeFactory;
    private final WorkspaceDao workspaceDao;
    private final NodeUtil nodeUtil;
    private final CorpusStructureBridge corpusStructureBridge;
    
    
    @Autowired
    public LamusWorkspaceUploadNodeMatcher(
            CorpusStructureProvider csProvider, NodeResolver nodeResolver,
            HandleParser handleParser,
            WorkspaceNodeFactory wsNodeFactory, WorkspaceDao wsDao,
            NodeUtil nodeUtil, CorpusStructureBridge csBridge) {
        this.corpusStructureProvider = csProvider;
        this.nodeResolver = nodeResolver;
        this.handleParser = handleParser;
        this.workspaceNodeFactory = wsNodeFactory;
        this.workspaceDao = wsDao;
        this.nodeUtil = nodeUtil;
        this.corpusStructureBridge = csBridge;
    }
    
    /**
     * @see WorkspaceUploadNodeMatcher#findNodeForHandle(
     *  nl.mpi.lamus.workspace.model.Workspace, java.util.Collection, java.net.URI) 
     */
    @Override
    public WorkspaceNode findNodeForHandle(Workspace workspace, Collection<WorkspaceNode> nodesToCheck, URI handle) {
        
        int topNodeID = workspace.getTopNodeID();
        URI topNodeURI = workspace.getTopNodeArchiveURI();
        
        for(WorkspaceNode innerNode : nodesToCheck) {
            
            if(nodeUtil.isNodeMetadata(innerNode)) {
                
                try {
                    if(handleParser.areHandlesEquivalent(handle, innerNode.getArchiveURI())) { // handle matches
                        return innerNode;
                    }
                } catch(IllegalArgumentException ex) {
                    logger.info("Invalid handle(s): '" + handle + "'; '" + innerNode.getArchiveURI() + "'");
                }
            }
        }
        
        // Search in the rest of the workspace for matches
        Collection<WorkspaceNode> matchingNodes = workspaceDao.getWorkspaceNodeByArchiveURI(handle);
        if(matchingNodes.size() == 1) { // one match - shouldn't be more than that
            
            //considered an external reference (and not legal)
            if(matchingNodes.iterator().next().getWorkspaceNodeID() == topNodeID) {
                // circular link found
                    String message = "Circular link found in reference " + handle;
                    logger.error(message);
                    throw new IllegalStateException(message);
            }
            
            logger.debug("One match in workspace for URI " + handle);
            return matchingNodes.iterator().next();
        } else if(matchingNodes.size() > 1) { // several matches - problem
            String message = "Several matches found in workspace for URI " + handle;
            logger.error(message);
            throw new IllegalStateException(message);
        }
        
        CorpusNode referenceCorpusNode = corpusStructureProvider.getNode(handle);
        URL referenceUrl = null;
        if(referenceCorpusNode == null) {
            logger.warn("Node not found in CS DB for handle " + handle.toString());
        } else {
            referenceUrl = nodeResolver.getUrl(referenceCorpusNode);
        }
            
            
        if(referenceCorpusNode != null) { // match was not found but node exists in archive
            
            // check if this could cause a circular link in the tree
            List<URI> ancestorsOfTopNode = corpusStructureBridge.getURIsOfAncestors(topNodeURI);
            for(URI ancestor : ancestorsOfTopNode) {
                if(handleParser.areHandlesEquivalent(handle, ancestor)) {
                    // circular link found
                    String message = "Circular link found in reference " + handle;
                    logger.error(message);
                    throw new IllegalStateException(message);
                }
            }
            
            WorkspaceNode newNode = workspaceNodeFactory.getNewExternalNodeFromArchive(workspace.getWorkspaceID(), referenceCorpusNode, handle, referenceUrl);
            workspaceDao.addWorkspaceNode(newNode);
            return newNode;
        }
        
        return null;
    }
    
    /**
     * @see WorkspaceUploadNodeMatcher#findExternalNodeForUri(
     *  nl.mpi.lamus.workspace.model.Workspace, java.net.URI) 
     */
    @Override
    public WorkspaceNode findExternalNodeForUri(Workspace workspace, URI uri) {
        
        if(handleParser.isHandleUriWithKnownPrefix(uri)) {
            // shouldn't be a handle at this point
            return null;
        }
        
        boolean uriIsExternalUrl = uri.getScheme() != null && !"file".equals(uri.getScheme());
        
        //if it is not a handle nor a local URL, it should at least be a valid external URL
        if(uriIsExternalUrl) {
            try {
                uri.toURL();
            } catch(MalformedURLException | IllegalArgumentException ex) {
                logger.info(ex.getMessage());
                return null;
            }
            WorkspaceNode externalNode = workspaceNodeFactory.getNewExternalNode(workspace.getWorkspaceID(), uri);
            workspaceDao.addWorkspaceNode(externalNode);
            return externalNode;
        } else {
            return null;
        }
    }
    
    @Override
    public WorkspaceNode findNodeForPath(Collection<WorkspaceNode> nodesToCheck, String referencePath) {
        
        if(!referencePath.isEmpty()) {            
            String referencePathEnding = referencePath;
            while(referencePathEnding.startsWith(".")) {
                referencePathEnding = referencePathEnding.substring(referencePathEnding.indexOf(File.separator) + 1);
            }
            
            for(WorkspaceNode innerNode : nodesToCheck) {
                    
                Path refPath = Paths.get(referencePathEnding);
                Path nodePath = Paths.get(innerNode.getWorkspaceURL().getPath().toString());

                if(nodePath.endsWith(refPath)) { //check if the node URL contains the relative path that comes in the link reference
                    return innerNode;
                }
            }
        }
        
        return null;
    }
}
