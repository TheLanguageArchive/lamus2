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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.handle.util.implementation.HandleManagerImpl;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadNodeMatcher;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadReferenceHandler;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.util.HandleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceUploadReferenceHandler
 * 
 * @author guisil
 */
@Component
public class LamusWorkspaceUploadReferenceHandler implements WorkspaceUploadReferenceHandler {

    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceUploadReferenceHandler.class);
    
    private HandleUtil metadataApiHandleUtil;
    private WorkspaceUploadNodeMatcher workspaceUploadNodeMatcher;
    private WorkspaceDao workspaceDao;
    private WorkspaceNodeLinkManager workspaceNodeLinkManager;
    private HandleManagerImpl handleManager;
    private MetadataAPI metadataAPI;
    private MetadataApiBridge metadataApiBridge;
    private WorkspaceFileHandler workspaceFileHandler;
    
    @Autowired
    public LamusWorkspaceUploadReferenceHandler(
            HandleUtil mdApiHandleUtil, WorkspaceUploadNodeMatcher wsUploadNodeMatcher,
            WorkspaceDao wsDao, WorkspaceNodeLinkManager wsNodeLinkManager,
            HandleManagerImpl handleManager, MetadataAPI mdAPI,
            MetadataApiBridge mdApiBridge, WorkspaceFileHandler wsFileHandler) {
        this.metadataApiHandleUtil = mdApiHandleUtil;
        this.workspaceUploadNodeMatcher = wsUploadNodeMatcher;
        this.workspaceDao = wsDao;
        this.workspaceNodeLinkManager = wsNodeLinkManager;
        this.handleManager = handleManager;
        this.metadataAPI = mdAPI;
        this.metadataApiBridge = mdApiBridge;
        this.workspaceFileHandler = wsFileHandler;
    }
    
    /**
     * @see WorkspaceUploadReferenceHandler#matchReferencesWithNodes(int,
     * java.util.Collection, nl.mpi.lamus.workspace.model.WorkspaceNode,
     * nl.mpi.metadata.api.model.ReferencingMetadataDocument, java.util.Collection) 
     */
    @Override
    public Collection<UploadProblem> matchReferencesWithNodes(
            int workspaceID, Collection<WorkspaceNode> nodesToCheck,
            WorkspaceNode currentNode, ReferencingMetadataDocument currentDocument,
            Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles) {
        
        Collection<UploadProblem> failedLinks = new ArrayList<>();
        
        //check if document has external self-handle
        if(!handleManager.isHandlePrefixKnown(metadataApiBridge.getSelfHandleFromDocument(currentDocument))) {
            documentsWithExternalSelfHandles.put(currentDocument, currentNode);
        }
        
        List<Reference> references = currentDocument.getDocumentReferences();
        
        for(Reference ref : references) {
            
            URI refLocalURI = ref.getLocation();
            URI refURI = ref.getURI();
            WorkspaceNode matchedNode;

            if(refLocalURI != null) {
                
                matchedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, refLocalURI.toString());
                
                if(matchedNode != null) {
                    if(refURI != null && !refURI.toString().isEmpty() && metadataApiHandleUtil.isHandleUri(refURI)) {
                            matchedNode.setArchiveURI(refURI);
                            workspaceDao.updateNodeArchiveUri(matchedNode);
                    } else {
                        clearReferenceUri(currentDocument, ref, matchedNode);
                    }
                    
                }
                
            } else if(metadataApiHandleUtil.isHandleUri(refURI)) {
                matchedNode = workspaceUploadNodeMatcher.findNodeForHandle(workspaceID, nodesToCheck, refURI);
                
                if(matchedNode != null) {
                    try {
                        updateLocalUrl(currentDocument, ref, matchedNode.getWorkspaceURL().toURI(), matchedNode);
                    } catch (URISyntaxException ex) {
                        logger.warn("Problems updating localUrl in reference (URI: " + refURI + ")");
                    }
                    
                    //set handle in DB
                    if(!handleManager.areHandlesEquivalent(refURI, matchedNode.getArchiveURI())) {
                        matchedNode.setArchiveURI(refURI);
                        workspaceDao.updateNodeArchiveUri(matchedNode);
                    }
                }
                
            } else {

                matchedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, refURI.toString());
                
                if(matchedNode != null) {
                    try {
                        updateLocalUrl(currentDocument, ref, matchedNode.getWorkspaceURL().toURI(), matchedNode);
                    } catch (URISyntaxException ex) {
                        logger.warn("Problems updating localUrl in reference (URI: " + refURI + ")");
                    }
                } else {
                    matchedNode = workspaceUploadNodeMatcher.findExternalNodeForUri(workspaceID, refURI);
                }
            }

            if(matchedNode != null) {
                
                Collection<WorkspaceNode> alreadyLinkedParents = workspaceDao.getParentWorkspaceNodes(matchedNode.getWorkspaceNodeID());
                
                if(alreadyLinkedParents.isEmpty()) {
                    try {
                        workspaceNodeLinkManager.linkNodesOnlyInDb(currentNode, matchedNode);
                    } catch (WorkspaceException ex) {
                        String message = "Error linking nodes " + currentNode.getWorkspaceNodeID() + " and " + matchedNode.getWorkspaceNodeID() + " in workspace " + workspaceID;
                        logger.error(message, ex);
                        failedLinks.add(new LinkUploadProblem(currentNode, matchedNode, message, ex));
                    }
                } else {
                    //Multiple parents NOT allowed - won't be linked
                    String message = "Matched node (ID " + matchedNode.getWorkspaceNodeID() + ") cannot be linked to parent node (ID " + currentNode.getWorkspaceNodeID() + ") because it already has a parent. Multiple parents are not allowed.";
                    logger.error(message, null);
                    failedLinks.add(new LinkUploadProblem(currentNode, matchedNode, message, null));
                }
                
                // check if ref is handle, and if is external... if so, remove it
                if(metadataApiHandleUtil.isHandleUri(refURI)) {
                    if(!handleManager.isHandlePrefixKnown(refURI)) { // external handle
                        clearReferenceUri(currentDocument, ref, currentNode);
                    }
                }
            } else {
                removeReference(currentDocument, ref, currentNode);
                String message = "Reference (" + ref.getURI() + ") in node " + currentNode.getWorkspaceNodeID() + " not matched";
                failedLinks.add(new MatchUploadProblem(currentNode, ref, message, null));
            }
            
        }
        return failedLinks;
    }
    
    
    private void clearReferenceUri(ReferencingMetadataDocument document, Reference ref, WorkspaceNode referencedNode) {
        
        ref.setURI(URI.create(""));
        
        try {
            File documentFile = new File(document.getFileLocation().getPath());
            StreamResult documentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(documentFile);
            metadataAPI.writeMetadataDocument(document, documentStreamResult);
        } catch (IOException ex) {
            logger.error("Error clearing the reference for node " + referencedNode.getWorkspaceNodeID(), ex);
        } catch (TransformerException | MetadataException ex) {
            logger.error("Error updating the reference for node " + referencedNode.getWorkspaceNodeID(), ex);
        }
    }
    
    private void removeReference(ReferencingMetadataDocument document, Reference ref, WorkspaceNode currentNode) {
        try {
            document.removeDocumentReference(ref);
            File documentFile = new File(document.getFileLocation().getPath());
            StreamResult documentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(documentFile);
            metadataAPI.writeMetadataDocument(document, documentStreamResult);
        } catch (MetadataException | IOException | TransformerException ex) {
            logger.error("Error removing reference '" + ref.getURI() + "' from node " + currentNode.getWorkspaceNodeID(), ex);
        }
    }
    
    private void updateLocalUrl(ReferencingMetadataDocument document, Reference ref, URI newLocation, WorkspaceNode referencedNode) {
        
        StringBuilder message = new StringBuilder();
        
        String oldLocation = (ref.getLocation() != null) ? ref.getLocation().toString() : "";
        message.append("[old URL: '").append(oldLocation).append("']");
        ref.setLocation(newLocation);
        
        try {
            File documentFile = new File(document.getFileLocation().getPath());
            StreamResult documentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(documentFile);
            metadataAPI.writeMetadataDocument(document, documentStreamResult);
        } catch (IOException | TransformerException | MetadataException ex) {
            logger.error("Error updating the reference for node " + referencedNode.getWorkspaceNodeID() + message.toString(), ex);
        }
    }
    
}
