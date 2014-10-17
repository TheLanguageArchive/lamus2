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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.handle.util.implementation.HandleManagerImpl;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
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
    
    private CorpusStructureProvider corpusStructureProvider;
    private NodeResolver nodeResolver;
    private HandleManagerImpl handleMatcher;
    private WorkspaceNodeFactory workspaceNodeFactory;
    private WorkspaceDao workspaceDao;
    
    
    @Autowired
    public LamusWorkspaceUploadNodeMatcher(
            CorpusStructureProvider csProvider, NodeResolver nodeResolver,
            HandleManagerImpl handleMatcher,
            WorkspaceNodeFactory wsNodeFactory, WorkspaceDao wsDao) {
        this.corpusStructureProvider = csProvider;
        this.nodeResolver = nodeResolver;
        this.handleMatcher = handleMatcher;
        this.workspaceNodeFactory = wsNodeFactory;
        this.workspaceDao = wsDao;
    }
    
    /**
     * @see WorkspaceUploadNodeMatcher#findNodeForHandle(int, java.util.Collection, java.net.URI)
     */
    @Override
    public WorkspaceNode findNodeForHandle(int workspaceID, Collection<WorkspaceNode> nodesToCheck, URI handle) {
        
        
        
//        File wsUploadDirectory = workspaceDirectoryHandler.getUploadDirectoryForWorkspace(workspaceID);
        
        for(WorkspaceNode innerNode : nodesToCheck) {
            
            if(innerNode.isMetadata()) {
                
                try {
                    if(handleMatcher.areHandlesEquivalent(handle, innerNode.getArchiveURI())) { // handle matches
                        return innerNode;
                    }
                } catch(IllegalArgumentException ex) {
                    logger.warn("Invalid handle(s): '" + handle + "'; '" + innerNode.getArchiveURI() + "'", ex);
                    
                    //TODO Or do something else?
                    continue;
                }
//            } else {
//                
//                // There is no self link in a resource, of course, and there is no information about the path either
//                // Probably the best way is to find the resource in the archive and then try to match it with the path of one of the uploaded resources
//
//                // if the retrieved archive URL is null, we can't continue with this check
//                // (the node wasn't found in the corpusstructure DB)
//                if(referenceUrl == null) {
//                    continue;
//                }
//                
//                // use only the last part of the path to compare with the archive URL
//                String shortenedWorkspaceFilePath = innerNode.getWorkspaceURL().toString().replace(wsUploadDirectory.toString(), "");
//
//                if(referenceUrl.toString().contains(shortenedWorkspaceFilePath)) { // URL matches
//                    return innerNode;
//                }
            }
        }
        
        // Search in the rest of the workspace for matches
        Collection<WorkspaceNode> matchingNodes = workspaceDao.getWorkspaceNodeByArchiveURI(handle);
        if(matchingNodes.size() == 1) { // one match - shouldn't be more than that
            logger.debug("One match in workspace for URI " + handle);
            return matchingNodes.iterator().next();
        } else if(matchingNodes.size() > 1) { // several matches - problem
            logger.error("Several matches found in workspace for URI " + handle);
            throw new IllegalStateException("Several matches found in workspace for URI " + handle);
        }
        
        CorpusNode referenceCorpusNode = corpusStructureProvider.getNode(handle);
        URL referenceUrl = null;
        if(referenceCorpusNode == null) {
            logger.warn("Node not found in CS DB for handle " + handle.toString());
        } else {
            referenceUrl = nodeResolver.getUrl(referenceCorpusNode);
        }
            
            
        if(referenceCorpusNode != null) { // match was not found but node exists in archive
            
            WorkspaceNode newNode = workspaceNodeFactory.getNewExternalNodeFromArchive(workspaceID, referenceCorpusNode, handle, referenceUrl);
            workspaceDao.addWorkspaceNode(newNode);
            return newNode;
        }
        
        
        //TODO Should this return null? It's not good that there was a handle and it was not even found in the archive...
        return null;
    }
    
    /**
     * @see WorkspaceUploadNodeMatcher#findExternalNodeForUri(int, java.net.URI)
     */
    @Override
    public WorkspaceNode findExternalNodeForUri(int workspaceID, URI uri) {
        
        URL validUrl;
        
        try {
            validUrl = uri.toURL();
        } catch( MalformedURLException | IllegalArgumentException ex) {
            logger.warn(ex.getMessage(), ex);
            return null;
        }
        
        if(!"file".equals(validUrl.getProtocol())) {
            WorkspaceNode externalNode = workspaceNodeFactory.getNewExternalNode(workspaceID, validUrl);
            workspaceDao.addWorkspaceNode(externalNode);
            return externalNode;
        } else {
            return null;
        }
    }
    
    @Override
    public WorkspaceNode findNodeForPath(Collection<WorkspaceNode> nodesToCheck, String referencePath) {
        
        if(!referencePath.isEmpty()) {
            for(WorkspaceNode innerNode : nodesToCheck) {

                if(innerNode.getWorkspaceURL().toString().contains(referencePath)) { //check if the node URL contains the relative path that comes in the link reference
                    return innerNode;
                }
            }
        }
        
        return null;
    }
}
