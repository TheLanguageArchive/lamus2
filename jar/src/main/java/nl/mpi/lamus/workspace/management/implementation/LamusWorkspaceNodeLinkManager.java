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
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.metadata.implementation.MetadataReferenceType;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.cmdi.api.model.CMDIContainerMetadataElement;
import nl.mpi.metadata.cmdi.api.model.CMDIDocument;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceNodeLinkManager
 * @author guisil
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
     * @see WorkspaceNodeLinkManager#linkNodesWithReference(nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.metadata.api.model.Reference)
     */
    @Override
    public void linkNodesWithReference(Workspace workspace, WorkspaceNode parentNode, WorkspaceNode childNode, Reference childLink) {
        
	if(parentNode == null && childLink == null) {
            
        logger.debug("Setting top node of workspace; workspaceID: " + workspace.getWorkspaceID() + "; topNodeID: " + childNode.getWorkspaceNodeID());

        workspace.setTopNodeID(childNode.getWorkspaceNodeID());
	    workspace.setTopNodeArchiveURI(childNode.getArchiveURI());
        workspace.setTopNodeArchiveURL(childNode.getArchiveURL());
        this.workspaceDao.updateWorkspaceTopNode(workspace);
	} else if(parentNode != null && childLink != null) {
            
            if(!nodeUtil.isNodeMetadata(parentNode)) {
                throw new IllegalArgumentException("Unable to create link. Parent node (" + parentNode.getWorkspaceNodeID() + ") is not metadata.");
            }
            
            logger.debug("Linking nodes in workspace; workspaceID: " + workspace.getWorkspaceID() + "; parentNodeID: " + parentNode.getWorkspaceNodeID() + "; childNodeID: " + childNode.getWorkspaceNodeID());
            
	    WorkspaceNodeLink nodeLink = this.workspaceNodeLinkFactory.getNewWorkspaceNodeLink(
	    parentNode.getWorkspaceNodeID(), childNode.getWorkspaceNodeID());
	    this.workspaceDao.addWorkspaceNodeLink(nodeLink);
	} else {
            throw new IllegalArgumentException("Unable to create link (parent node: " + parentNode + "; child link: " + childLink);
        }
    }

    /**
     * @see WorkspaceNodeLinkManager#linkNodes(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, boolean)
     */
    @Override
    public void linkNodes(WorkspaceNode parentNode, WorkspaceNode childNode, boolean isInfoLink)
            throws WorkspaceException, ProtectedNodeException {
        
        if(!nodeUtil.isNodeMetadata(parentNode)) {
            throw new IllegalArgumentException("Unable to create link. Parent node (" + parentNode.getWorkspaceNodeID() + ") is not metadata.");
        }
        
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
        
        CMDIDocument parentDocument = null;
        if(tempParentDocument instanceof CMDIDocument) {
            parentDocument = (CMDIDocument) tempParentDocument;
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
            
            ResourceProxy createdProxy;
            
            if(nodeUtil.isNodeMetadata(childNode)) {
                
                if(!metadataApiBridge.isMetadataReferenceAllowedInProfile(parentNode.getProfileSchemaURI())) {
                    String message = "A metadata reference is not allowed in the profile of the selected parent node";
                    throwWorkspaceException(message, workspaceID, null);
                }
                
                createdProxy = parentDocument.createDocumentMetadataReference(
                        childUri, childLocation, childNode.getFormat());
                
                addReferenceToComponent(parentNode, childNode, createdProxy, parentDocument, false);
                
                
            } else {
                
                if(!metadataApiBridge.isResourceReferenceAllowedInProfile(parentNode.getProfileSchemaURI())) {
                    String message = "A resource reference is not allowed in the profile of the selected parent node";
                    throwWorkspaceException(message, workspaceID, null);
                }
                if(isInfoLink && !metadataApiBridge.isInfoLinkAllowedInProfile(parentNode.getProfileSchemaURI())) {
                    String message = "An info link is not allowed in the profile of the selected parent node";
                    throwWorkspaceException(message, workspaceID, null);
                }
                
                createdProxy = parentDocument.createDocumentResourceReference(childUri, childLocation, MetadataReferenceType.REFERENCE_TYPE_RESOURCE, childNode.getFormat());
                
                addReferenceToComponent(parentNode, childNode, createdProxy, parentDocument, isInfoLink);
                
            }
            
            metadataApiBridge.saveMetadataDocument(parentDocument, parentNode.getWorkspaceURL());
            
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
        
        CMDIDocument parentDocument;
        if(tempParentDocument instanceof CMDIDocument) {
            parentDocument = (CMDIDocument) tempParentDocument;
        } else {
            String errorMessage = "Error retrieving referencing document for node " + parentNode.getWorkspaceNodeID();
            logger.error(errorMessage);
            throw new WorkspaceException(errorMessage, workspaceID, null);
        }

        try {
            URL childWsUrl = childNode.getWorkspaceURL();
            ResourceProxy childReference = null;
            if(childWsUrl != null) {
                childReference = parentDocument.getDocumentReferenceByLocation(childWsUrl.toURI());
            }
            if(childReference == null) {
                childReference = metadataApiBridge.getDocumentReferenceByDoubleCheckingURI(parentDocument, getNodeURI(childNode));
            }
            
            removeComponent(parentNode, childNode, childReference, parentDocument, nodeUtil.isNodeInfoFile(childNode));
            parentDocument.removeDocumentReference(childReference);
            
            metadataApiBridge.saveMetadataDocument(parentDocument, parentNode.getWorkspaceURL());
                
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
     * @see WorkspaceNodeLinkManager#unlinkNodeFromReplacedParent(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void unlinkNodeFromReplacedParent(WorkspaceNode childNode, WorkspaceNode newParentNode)
            throws WorkspaceException, ProtectedNodeException{
        
        WorkspaceNode replacedParentNode = null;
        try {
            replacedParentNode = this.workspaceDao.getOlderVersionOfNode(newParentNode.getWorkspaceID(), newParentNode.getWorkspaceNodeID());
        } catch(WorkspaceNodeNotFoundException ex) {
            //keep node as null
        }
        if(replacedParentNode == null) {
            // nothing happens
            return;
        }
        this.unlinkNodes(replacedParentNode, childNode, false);
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
            linkNodes(parentNode, newNode, WorkspaceNodeType.RESOURCE_INFO.equals(oldNode.getType()));
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
            CMDIDocument parentDocument;
            if(tempParentDocument instanceof CMDIDocument) {
                parentDocument = (CMDIDocument) tempParentDocument;
            } else {
                throw new UnsupportedOperationException("not referencing document not handled yet");
            }
            ResourceProxy childReference = metadataApiBridge.getDocumentReferenceByDoubleCheckingURI(parentDocument, childNode.getArchiveURI());
            childReference.setURI(childNode.getWorkspaceURL().toURI());
            
            metadataApiBridge.saveMetadataDocument(parentDocument, parentNode.getWorkspaceURL());
            
            removeArchiveUriFromNode(childNode);
            
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
    
    private String getComponentName(String mimetype, URI profileSchemaURI,
            WorkspaceNodeType childNodeType, boolean isInfoLink) {
        if(mimetype != null) {
            return metadataApiBridge.getComponentPathForProfileAndReferenceType(
                    profileSchemaURI, mimetype, null, isInfoLink);
        } else {
            return metadataApiBridge.getComponentPathForProfileAndReferenceType(
                    profileSchemaURI, null, childNodeType, isInfoLink);
        }
    }
    
    private void addReferenceToComponent(WorkspaceNode parentNode, WorkspaceNode childNode,
                ResourceProxy createdProxy, CMDIDocument parentDocument, boolean isInfoLink)
            throws WorkspaceException {
        
        String componentName = getComponentName(
                createdProxy.getMimetype(), parentNode.getProfileSchemaURI(),
                childNode.getType(), isInfoLink);
        
        if(componentName == null) {
            return;
        }
        CMDIContainerMetadataElement component = null;
        try {
            component = metadataApiBridge.createComponentPathWithin(parentDocument, componentName);
        } catch (MetadataException ex) {
            String errorMessage = "Error assuring existance of path [" + componentName + "] in document " + parentDocument.getName();
            throwWorkspaceException(errorMessage, parentNode.getWorkspaceID(), ex);
        }
        metadataApiBridge.addReferenceInComponent(component, createdProxy);
    }
    
    private void removeComponent(WorkspaceNode parentNode, WorkspaceNode childNode,
            ResourceProxy proxyToRemove, CMDIDocument parentDocument, boolean isInfoLink) throws MetadataException {
        
        String componentName = getComponentName(
                proxyToRemove.getMimetype(), parentNode.getProfileSchemaURI(), childNode.getType(), isInfoLink);
        if(componentName == null) {
            return;
        }

        nl.mpi.metadata.cmdi.api.model.Component component = 
                metadataApiBridge.getComponent(parentDocument, componentName, proxyToRemove.getId());
        if(component == null) {
            return;
        }
        
        metadataApiBridge.removeComponent(component);
    }
    
    
    /**
     * @see WorkspaceNodeLinkManager#removeArchiveUriFromUploadedNodeRecursively(nl.mpi.lamus.workspace.model.WorkspaceNode, boolean)
     */
    @Override
    public void removeArchiveUriFromUploadedNodeRecursively(WorkspaceNode node, boolean firstIteration)
            throws WorkspaceException {
        
        try {
            if(firstIteration) {
                removeArchiveUriIfNodeWasUploaded(node);
            }

            if(!nodeUtil.isNodeMetadata(node)) {
                return;
            }
            
            Collection<WorkspaceNode> children = workspaceDao.getChildWorkspaceNodes(node.getWorkspaceNodeID());

            for(WorkspaceNode child : children) {

                removeArchiveUriFromParentReferenceIfNodeWasUploaded(node, child);

                removeArchiveUriIfNodeWasUploaded(child);

                removeArchiveUriFromUploadedNodeRecursively(child, false);
            }
        } catch(IOException | MetadataException | TransformerException | URISyntaxException ex) {
            String errorMessage = "Error removing archive URI from node [" + node.getWorkspaceURL() + "] or descendant";
            logger.error(errorMessage);
            throw new WorkspaceException(errorMessage, node.getWorkspaceID(), ex);
        }
    }
    
    private void removeArchiveUriFromParentReferenceIfNodeWasUploaded(WorkspaceNode parent, WorkspaceNode child) throws IOException, MetadataException, URISyntaxException, TransformerException {
        
        // only applicable if the node was uploaded and the archive URI is not null
        if(WorkspaceNodeStatus.UPLOADED.equals(child.getStatus()) && child.getArchiveURI() != null) {
            removeArchiveUriFromParentReference(parent, child);
        }
    }
    
    private void removeArchiveUriFromParentReference(WorkspaceNode parent, WorkspaceNode child) throws IOException, MetadataException, URISyntaxException, TransformerException {
        
        if(child.getArchiveURI() == null) {
            return;
        }
        
        MetadataDocument tempParentDocument = metadataAPI.getMetadataDocument(parent.getWorkspaceURL());
        CMDIDocument parentDocument;
        if(tempParentDocument instanceof CMDIDocument) {
            parentDocument = (CMDIDocument) tempParentDocument;
        } else {
            throw new UnsupportedOperationException("not referencing document not handled yet");
        }
        
        ResourceProxy childReference = metadataApiBridge.getDocumentReferenceByDoubleCheckingURI(parentDocument, child.getArchiveURI());
        childReference.setURI(child.getWorkspaceURL().toURI());

        metadataApiBridge.saveMetadataDocument(parentDocument, parent.getWorkspaceURL());
    }
    
    private void removeArchiveUriIfNodeWasUploaded(WorkspaceNode node) throws IOException, MetadataException, TransformerException {
        
        // only applicable if the node was uploaded and the archive URI is not null
        if(WorkspaceNodeStatus.UPLOADED.equals(node.getStatus()) && node.getArchiveURI() != null) {
            removeArchiveUriFromNode(node);
        }
    }
    
    private void removeArchiveUriFromNode(WorkspaceNode node) throws IOException, MetadataException, TransformerException {
        
        // remove archive URI from the metadata
        if(nodeUtil.isNodeMetadata(node)) {
            MetadataDocument tempDocument = metadataAPI.getMetadataDocument(node.getWorkspaceURL());
            HandleCarrier handleCarrierDocument = null;
            if(tempDocument instanceof HandleCarrier) {
                handleCarrierDocument = (HandleCarrier) tempDocument;
            } else {
                throw new UnsupportedOperationException("not handle carrier document not handled yet");
            }
            handleCarrierDocument.setHandle(null);
            metadataApiBridge.saveMetadataDocument((MetadataDocument) handleCarrierDocument, node.getWorkspaceURL());
        }
        
        // remove archive URI and URL from the database
        node.setArchiveURI(null);
        node.setArchiveURL(null);
        workspaceDao.updateNodeArchiveUri(node);
        workspaceDao.updateNodeArchiveUrl(node);
    }
}
