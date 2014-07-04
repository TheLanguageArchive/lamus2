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
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.handle.util.implementation.HandleManagerImpl;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadNodeMatcher;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadReferenceHandler;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
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
    private HandleManagerImpl handleMatcher;
    private MetadataAPI metadataAPI;
    private WorkspaceFileHandler workspaceFileHandler;
    
    @Autowired
    public LamusWorkspaceUploadReferenceHandler(
            HandleUtil mdApiHandleUtil, WorkspaceUploadNodeMatcher wsUploadNodeMatcher,
            WorkspaceDao wsDao, WorkspaceNodeLinkManager wsNodeLinkManager,
            HandleManagerImpl handleMatcher, MetadataAPI mdAPI, WorkspaceFileHandler wsFileHandler) {
        this.metadataApiHandleUtil = mdApiHandleUtil;
        this.workspaceUploadNodeMatcher = wsUploadNodeMatcher;
        this.workspaceDao = wsDao;
        this.workspaceNodeLinkManager = wsNodeLinkManager;
        this.handleMatcher = handleMatcher;
        this.metadataAPI = mdAPI;
        this.workspaceFileHandler = wsFileHandler;
    }
    
    /**
     * @see WorkspaceUploadReferenceHandler#matchReferencesWithNodes(
     *          int, java.util.Collection, nl.mpi.lamus.workspace.model.WorkspaceNode,
     *          nl.mpi.metadata.api.model.ReferencingMetadataDocument, java.util.List, java.util.Map)
     */
    @Override
    public void matchReferencesWithNodes(
            int workspaceID, Collection<WorkspaceNode> nodesToCheck,
            WorkspaceNode currentNode, ReferencingMetadataDocument currentDocument,
            Collection<Reference> failedLinks) {
        
        List<Reference> references = currentDocument.getDocumentReferences();
        
        for(Reference ref : references) {
            
            URL refLocalURL = ref.getLocation();
            URI refURI = ref.getURI();
            WorkspaceNode matchedNode = null;

            if(refLocalURL != null) {
                
                matchedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, refLocalURL.toString());
                
                if(matchedNode != null) {
                    if(refURI != null && !refURI.toString().isEmpty() && metadataApiHandleUtil.isHandleUri(refURI)) {
                            matchedNode.setArchiveURI(refURI);
                            workspaceDao.updateNodeArchiveUri(matchedNode);
                    } else {
//                        try {
//                            updateReferenceUri(currentDocument, ref, refLocalURL.toURI(), null, matchedNode);
//                        } catch (URISyntaxException ex) {
//                            throw new UnsupportedOperationException("not handled yet");
//                        }
                        
                        clearReferenceUri(currentDocument, ref, matchedNode);
                        
                    }
                    
                }
                
            } else if(metadataApiHandleUtil.isHandleUri(refURI)) {
                matchedNode = workspaceUploadNodeMatcher.findNodeForHandle(workspaceID, nodesToCheck, refURI);
                
                if(matchedNode != null) {
                
                    updateLocalUrl(currentDocument, ref, matchedNode.getWorkspaceURL(), matchedNode);
                    
                    //set handle in DB
                    if(!handleMatcher.areHandlesEquivalent(refURI, matchedNode.getArchiveURI())) {
                        matchedNode.setArchiveURI(refURI);
                        workspaceDao.updateNodeArchiveUri(matchedNode);
                    }
                }
                
            } else {

                matchedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, refURI.toString());
                
                if(matchedNode != null) {
                    updateLocalUrl(currentDocument, ref, matchedNode.getWorkspaceURL(), matchedNode);
                } else {
                    matchedNode = workspaceUploadNodeMatcher.findExternalNodeForUri(workspaceID, refURI);
                }
            }

            if(matchedNode != null) {
                
                try {
                    workspaceNodeLinkManager.linkNodesOnlyInDb(currentNode, matchedNode);
                } catch (WorkspaceException ex) {
                    logger.error("Error linking nodes " + currentNode.getWorkspaceNodeID() + " and " + matchedNode.getWorkspaceNodeID() + " in workspace " + workspaceID, ex);
                    failedLinks.add(ref);
                }
                
            } else {
                try {
                    currentDocument.removeDocumentReference(ref);
                    File documentFile = new File(currentDocument.getFileLocation().getPath());
                    StreamResult documentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(documentFile);
                    metadataAPI.writeMetadataDocument(currentDocument, documentStreamResult);
                    
                    //TODO LEAVE SOME MESSAGE HERE
                    
                } catch (MetadataException ex) {
                    logger.error("Error removing reference '" + refURI + "' from node " + currentNode.getWorkspaceNodeID(), ex);
                } catch (IOException ex) {
                    logger.error("Error removing reference '" + refURI + "' from node " + currentNode.getWorkspaceNodeID(), ex);
                } catch (TransformerException ex) {
                    logger.error("Error removing reference '" + refURI + "' from node " + currentNode.getWorkspaceNodeID(), ex);
                }
            }
            
        }
    }
    
    
    private void clearReferenceUri(ReferencingMetadataDocument document, Reference ref, WorkspaceNode referencedNode) {
        
        try {
            ref.setURI(new URI(""));
        } catch (URISyntaxException ex) {
           throw new UnsupportedOperationException("not handled yet");
        }
        
        try {
            File documentFile = new File(document.getFileLocation().getPath());
            StreamResult documentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(documentFile);
            metadataAPI.writeMetadataDocument(document, documentStreamResult);
        } catch (IOException ex) {
            logger.error("Error clearing the reference for node " + referencedNode.getWorkspaceNodeID(), ex);
        } catch (TransformerException ex) {
            logger.error("Error updating the reference for node " + referencedNode.getWorkspaceNodeID(), ex);
        } catch (MetadataException ex) {
            logger.error("Error updating the reference for node " + referencedNode.getWorkspaceNodeID(), ex);
        }
    }
    
    
    private void updateLocalUrl(ReferencingMetadataDocument document, Reference ref, URL newLocation, WorkspaceNode referencedNode) {
        
        StringBuilder message = new StringBuilder();
        
        String oldLocation = (ref.getLocation() != null) ? ref.getLocation().toString() : "";
        message.append("[old URL: '").append(oldLocation).append("']");
        ref.setLocation(newLocation);
        
        try {
            File documentFile = new File(document.getFileLocation().getPath());
            StreamResult documentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(documentFile);
            metadataAPI.writeMetadataDocument(document, documentStreamResult);
        } catch (IOException ex) {
            logger.error("Error updating the reference for node " + referencedNode.getWorkspaceNodeID() + message.toString(), ex);
        } catch (TransformerException ex) {
            logger.error("Error updating the reference for node " + referencedNode.getWorkspaceNodeID() + message.toString(), ex);
        } catch (MetadataException ex) {
            logger.error("Error updating the reference for node " + referencedNode.getWorkspaceNodeID() + message.toString(), ex);
        }
    }
    
}
