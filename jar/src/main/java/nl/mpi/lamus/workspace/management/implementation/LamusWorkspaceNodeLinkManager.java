/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.management.implementation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceNodeLinkManager
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceNodeLinkManager implements WorkspaceNodeLinkManager {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceNodeLinkManager.class);
    
    private final WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    private final WorkspaceDao workspaceDao;
    private final MetadataAPI metadataAPI;
    private final WorkspaceFileHandler workspaceFileHandler;
    private final MetadataApiBridge metadataApiBridge;
    private final NodeUtil nodeUtil;
    
    @Autowired
    public LamusWorkspaceNodeLinkManager(WorkspaceNodeLinkFactory nodeLinkFactory,
            WorkspaceDao wsDao, MetadataAPI mdAPI, WorkspaceFileHandler wsFileHandler,
            MetadataApiBridge mdApiBridge, NodeUtil nodeUtil) {
        
        this.workspaceNodeLinkFactory = nodeLinkFactory;
        this.workspaceDao = wsDao;
        this.metadataAPI = mdAPI;
        this.workspaceFileHandler = wsFileHandler;
        this.metadataApiBridge = mdApiBridge;
        this.nodeUtil = nodeUtil;
    }

    /**
     * @see WorkspaceNodeLinkManager#linkNodesWithReference(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.metadata.api.model.Reference)
     */
    @Override
    public void linkNodesWithReference(Workspace workspace, WorkspaceNode parentNode, WorkspaceNode childNode, Reference childLink) {
        
	if(parentNode == null && childLink == null) { //TODO find a better way of checking for the top node
            
        logger.debug("Setting top node of workspace; workspaceID: " + workspace.getWorkspaceID() + "; topNodeID: " + childNode.getWorkspaceNodeID());

        workspace.setTopNodeID(childNode.getWorkspaceNodeID());
	    workspace.setTopNodeArchiveURI(childNode.getArchiveURI());
        workspace.setTopNodeArchiveURL(childNode.getArchiveURL());
        this.workspaceDao.updateWorkspaceTopNode(workspace);
	} else if(parentNode != null && childLink != null) { //TODO Is there a situation when this would be different?
            
        logger.debug("Linking nodes in workspace; workspaceID: " + workspace.getWorkspaceID() + "; parentNodeID: " + parentNode.getWorkspaceNodeID() + "; childNodeID: " + childNode.getWorkspaceNodeID());
            
	    WorkspaceNodeLink nodeLink = this.workspaceNodeLinkFactory.getNewWorkspaceNodeLink(
	    parentNode.getWorkspaceNodeID(), childNode.getWorkspaceNodeID());
	    this.workspaceDao.addWorkspaceNodeLink(nodeLink);
	    //TODO possible problems with adding the link? if the link already exists?
	} else {
            throw new IllegalArgumentException("Unable to create link (parent node: " + parentNode + "; child link: " + childLink);
        }
    }

    /**
     * @see WorkspaceNodeLinkManager#linkNodes(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void linkNodes(WorkspaceNode parentNode, WorkspaceNode childNode) throws WorkspaceException, ProtectedNodeException {
        
        int workspaceID = parentNode.getWorkspaceID();
        
        if(parentNode.isProtected()) {
            String message = "Cannot proceed with linking because parent node (ID = " + parentNode.getWorkspaceNodeID() + ") is protected (WS ID = " + workspaceID + ").";
            throw new ProtectedNodeException(message, parentNode.getArchiveURI(), workspaceID);
        }
        
        Collection<WorkspaceNode> existingParents = workspaceDao.getParentWorkspaceNodes(childNode.getWorkspaceNodeID());
        if(!existingParents.isEmpty()) {
            String message = "Child node (ID = " + childNode.getWorkspaceNodeID() + ") already has a parent. Cannot be linked again.";
            throw new WorkspaceException(message, workspaceID, null);
        }
        
        logger.debug("Linking nodes; workspaceID: " + workspaceID + "; parentNodeID: " + parentNode.getWorkspaceNodeID() + "; childNodeID: " + childNode.getWorkspaceNodeID());
        
        MetadataDocument tempParentDocument = null;
        try {
            tempParentDocument = this.metadataAPI.getMetadataDocument(parentNode.getWorkspaceURL());
        } catch (IOException | MetadataException ex) {
            String errorMessage = "Error retrieving metadata document for node " + parentNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        }
        
        ReferencingMetadataDocument parentDocument = null;
        if(tempParentDocument instanceof ReferencingMetadataDocument) {
            parentDocument = (ReferencingMetadataDocument) tempParentDocument;
        } else {
            String errorMessage = "Error retrieving referencing document for node " + parentNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, null);
        }
        
        try {
            URI childLocation = null;
            URI childUri;
            if(childNode.getWorkspaceURL() != null) {
                childUri = childNode.getArchiveURI();
                childLocation = childNode.getWorkspaceURL().toURI();
            } else {
                childUri = getNodeURI(childNode);
            }
            
            if(nodeUtil.isNodeMetadata(childNode)) {
                parentDocument.createDocumentMetadataReference(
                        childUri, childLocation, childNode.getFormat());
            } else {
                parentDocument.createDocumentResourceReference(
                        childUri, childLocation, childNode.getType().toString(), childNode.getFormat());
            }
            
            StreamResult parentStreamResult =
                    this.workspaceFileHandler.getStreamResultForNodeFile(FileUtils.toFile(parentNode.getWorkspaceURL()));
                
            this.metadataAPI.writeMetadataDocument(parentDocument, parentStreamResult);
                
        } catch (URISyntaxException | MetadataException | IOException | TransformerException ex) {
            String errorMessage = "Error creating reference in document with node ID " + parentNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        }
        
        linkNodesOnlyInDb(parentNode, childNode);
    }
    
    /**
     * @see WorkspaceNodeLinkManager#linkNodesOnlyInDb(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void linkNodesOnlyInDb(WorkspaceNode parentNode, WorkspaceNode childNode) throws WorkspaceException {
        
        WorkspaceNodeLink nodeLink =
                this.workspaceNodeLinkFactory.getNewWorkspaceNodeLink(
                    parentNode.getWorkspaceNodeID(), childNode.getWorkspaceNodeID());
        
        this.workspaceDao.addWorkspaceNodeLink(nodeLink);
    }

    /**
     * @see WorkspaceNodeLinkManager#unlinkNodes(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void unlinkNodes(WorkspaceNode parentNode, WorkspaceNode childNode)
            throws WorkspaceException, ProtectedNodeException {
        
        unlinkNodes(parentNode, childNode, true);
    }
    
    /**
     * Similar to the other unlinkNodes method, but including a boolean
     * to indicate if protected nodes should be checked or not
     */
    private void unlinkNodes(WorkspaceNode parentNode, WorkspaceNode childNode, boolean checkProtectedNode)
            throws WorkspaceException, ProtectedNodeException {
        
        int workspaceID = parentNode.getWorkspaceID();
        
        if(checkProtectedNode && parentNode.isProtected()) {
            String message = "Cannot proceed with unlinking because parent node (ID = " + parentNode.getWorkspaceNodeID() + ") is protected (WS ID = " + workspaceID + ").";
            logger.error(message);
            throw new ProtectedNodeException(message, parentNode.getArchiveURI(), workspaceID);
        }
        
        logger.debug("Unlinking nodes: " + workspaceID + "; parentNodeID: " + parentNode.getWorkspaceNodeID() + "; childNodeID: " + childNode.getWorkspaceNodeID());
        
        MetadataDocument tempParentDocument = null;
        try {
            tempParentDocument = this.metadataAPI.getMetadataDocument(parentNode.getWorkspaceURL());
        } catch (IOException | MetadataException ex) {
            String errorMessage = "Error retrieving metadata document for node " + parentNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        }
        
        ReferencingMetadataDocument parentDocument;
        if(tempParentDocument instanceof ReferencingMetadataDocument) {
            parentDocument = (ReferencingMetadataDocument) tempParentDocument;
        } else {
            String errorMessage = "Error retrieving referencing document for node " + parentNode.getWorkspaceNodeID();
            logger.error(errorMessage);
            throw new WorkspaceException(errorMessage, workspaceID, null);
        }

        try {
            URL childWsUrl = childNode.getWorkspaceURL();
            Reference childReference = null;
            if(childWsUrl != null) {
                childReference = parentDocument.getDocumentReferenceByLocation(childWsUrl.toURI());
            }
            if(childReference == null) {
                childReference = parentDocument.getDocumentReferenceByURI(getNodeURI(childNode));
            }
            
            parentDocument.removeDocumentReference(childReference);
            
            StreamResult parentStreamResult =
                    this.workspaceFileHandler.getStreamResultForNodeFile(FileUtils.toFile(parentNode.getWorkspaceURL()));
                
            this.metadataAPI.writeMetadataDocument(parentDocument, parentStreamResult);
                
        } catch (URISyntaxException | MetadataException | IOException | TransformerException ex) {
            String errorMessage = "Error removing reference in document with node ID " + childNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        }
        
        this.workspaceDao.deleteWorkspaceNodeLink(parentNode.getWorkspaceID(), parentNode.getWorkspaceNodeID(), childNode.getWorkspaceNodeID());
    }

    /**
     * @see WorkspaceNodeLinkManager#unlinkNodeFromAllParents(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void unlinkNodeFromAllParents(WorkspaceNode childNode)
            throws WorkspaceException, ProtectedNodeException {
        
        logger.debug("Unlinking node from all parents; workspaceID: " + childNode.getWorkspaceID() + "; nodeID: " + childNode.getWorkspaceNodeID());
        
        Collection<WorkspaceNode> parentNodes =
                this.workspaceDao.getParentWorkspaceNodes(childNode.getWorkspaceNodeID());
        
        for(WorkspaceNode parent : parentNodes) {
            this.unlinkNodes(parent, childNode, false);
        }
    }
    
    /**
     * @see WorkspaceNodeLinkManager#replaceNode(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, boolean)
     */
    @Override
    public void replaceNode(WorkspaceNode parentNode, WorkspaceNode oldNode,
            WorkspaceNode newNode, boolean isNewNodeAlreadyLinked) throws WorkspaceException, ProtectedNodeException {
        
        String workspaceID = "" + oldNode.getWorkspaceID();
        String parentID = parentNode != null ? "" + parentNode.getWorkspaceNodeID() : "null";
        String oldNodeID = "" + oldNode.getWorkspaceNodeID();
        String newNodeID = "" + newNode.getWorkspaceNodeID();
        
        logger.debug("Replacing nodes; workspaceID: " + workspaceID + "; parentNodeID: " + parentID +
                "; oldNodeID: " + oldNodeID + "; newNodeID: " + newNodeID +
                "; isNewNodeAlreadyLinked: " + isNewNodeAlreadyLinked);
        
        boolean isTopNode = workspaceDao.isTopNodeOfWorkspace(oldNode.getWorkspaceID(), oldNode.getWorkspaceNodeID());
        
        if(!isNewNodeAlreadyLinked && !isTopNode) {
            
            unlinkNodes(parentNode, oldNode, false);
            linkNodes(parentNode, newNode);
        } else if(isTopNode) {
            
            Workspace ws = workspaceDao.getWorkspace(oldNode.getWorkspaceID());
            ws.setTopNodeID(newNode.getWorkspaceNodeID());
            ws.setTopNodeArchiveURI(newNode.getArchiveURI());
            ws.setTopNodeArchiveURL(newNode.getArchiveURL());
            workspaceDao.updateWorkspaceTopNode(ws);
        }
        
        workspaceDao.replaceNode(oldNode, newNode);
    }
    
    /**
     * @see WorkspaceNodeLinkManager#removeArchiveUriFromChildNode(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void removeArchiveUriFromChildNode(WorkspaceNode parentNode, WorkspaceNode childNode) throws WorkspaceException {
        
        logger.debug("Removing archive URI from child node; workspaceID: " + parentNode.getWorkspaceID() + "; parentNodeID: " + parentNode.getWorkspaceNodeID() + "; childNodeID: " + childNode.getWorkspaceNodeID());
        
        if(childNode.getArchiveURI() == null) {
            logger.debug("Archive URI already null. Skipping.");
            return;
        }
        
        try {
            MetadataDocument tempParentDocument = metadataAPI.getMetadataDocument(parentNode.getWorkspaceURL());
            ReferencingMetadataDocument parentDocument;
            if(tempParentDocument instanceof ReferencingMetadataDocument) {
                parentDocument = (ReferencingMetadataDocument) tempParentDocument;
            } else {
                throw new UnsupportedOperationException("not referencing document not handled yet");
            }
            Reference childReference = parentDocument.getDocumentReferenceByURI(childNode.getArchiveURI());
            childReference.setURI(childNode.getWorkspaceURL().toURI());
            
            metadataApiBridge.saveMetadataDocument(parentDocument, parentNode.getWorkspaceURL());
            
            if(nodeUtil.isNodeMetadata(childNode)) {
                MetadataDocument tempChildDocument = metadataAPI.getMetadataDocument(childNode.getWorkspaceURL());
                HandleCarrier childHandleCarrier = null;
                if(tempChildDocument instanceof HandleCarrier) {
                    childHandleCarrier = (HandleCarrier) tempChildDocument;
                } else {
                    throw new UnsupportedOperationException("not handle carrier child document not handled yet");
                }
                childHandleCarrier.setHandle(null);
                metadataApiBridge.saveMetadataDocument((MetadataDocument) childHandleCarrier, childNode.getWorkspaceURL());
            }
            
            childNode.setArchiveURI(null);
            childNode.setArchiveURL(null);
            workspaceDao.updateNodeArchiveUri(childNode);
            workspaceDao.updateNodeArchiveUrl(childNode);
            
        } catch (IOException | MetadataException | URISyntaxException | TransformerException ex) {
            String errorMessage = "Error when trying to remove URI of node " + childNode.getWorkspaceNodeID() + ", referenced in node " + parentNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, parentNode.getWorkspaceID(), ex);
        }
        
    }
    
    
    private void throwWorkspaceException(String errorMessage, int workspaceID, Exception cause) throws WorkspaceException {
        logger.error(errorMessage, cause);
        throw new WorkspaceException(errorMessage, workspaceID, cause);
    }
    
    private URI getNodeURI(WorkspaceNode node) throws WorkspaceException {
        
        URI nodeURI = null;
        try {
            if(node.getArchiveURI() != null) {
                nodeURI = node.getArchiveURI();
            } else if(node.getWorkspaceURL() != null) {
                nodeURI = node.getWorkspaceURL().toURI();
            } else { //in case of a newly added external node, this is the only available location
                nodeURI = node.getOriginURI();
            }
        } catch(URISyntaxException ex) {
            String errorMessage = "Error getting URI of the node " + node.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, node.getWorkspaceID(), ex);
        }
        
        return nodeURI;
    }
}
