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
package nl.mpi.lamus.workspace.importing.implementation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.WorkspaceParentNodeReference;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
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
    
    private final WorkspaceParentNodeReferenceFactory workspaceParentNodeReferenceFactory;
    private final WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    private final WorkspaceDao workspaceDao;
    private final MetadataAPI metadataAPI;
    private final WorkspaceFileHandler workspaceFileHandler;
    
    @Autowired
    public LamusWorkspaceNodeLinkManager(WorkspaceParentNodeReferenceFactory parentNodeReferenceFactory,
            WorkspaceNodeLinkFactory nodeLinkFactory, WorkspaceDao wsDao, MetadataAPI mdAPI,
            WorkspaceFileHandler wsFileHandler) {
        
        this.workspaceParentNodeReferenceFactory = parentNodeReferenceFactory;
        this.workspaceNodeLinkFactory = nodeLinkFactory;
        this.workspaceDao = wsDao;
        this.metadataAPI = mdAPI;
        this.workspaceFileHandler = wsFileHandler;
    }

    /**
     * @see WorkspaceNodeLinkManager#linkNodesWithReference(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.metadata.api.model.Reference)
     */
    @Override
    public void linkNodesWithReference(Workspace workspace, WorkspaceNode parentNode, WorkspaceNode childNode, Reference childLink) {
        
        WorkspaceParentNodeReference parentNodeReference =
		this.workspaceParentNodeReferenceFactory.getNewWorkspaceParentNodeReference(parentNode, childLink);

	//TODO set top node ID in workspace (if reference is null), set workspace status / Save workspace
	if (parentNodeReference == null) { //TODO find a better way of indicating this
            
//            Workspace workspace;
//
//            workspace = this.workspaceDao.getWorkspace(childNode.getWorkspaceID());

            workspace.setTopNodeID(childNode.getWorkspaceNodeID());
	    workspace.setTopNodeArchiveURI(childNode.getArchiveURI());
            workspace.setTopNodeArchiveURL(childNode.getArchiveURL());
            
	    this.workspaceDao.updateWorkspaceTopNode(workspace);
	} else {
	    //TODO add information about parent link
	    // add the link in the database
	    WorkspaceNodeLink nodeLink = this.workspaceNodeLinkFactory.getNewWorkspaceNodeLink(
		    parentNodeReference.getParentWorkspaceNodeID(), childNode.getWorkspaceNodeID(), childLink.getURI());
	    this.workspaceDao.addWorkspaceNodeLink(nodeLink);
	    //TODO possible problems with adding the link? if the link already exists?
	}
    }

    /**
     * @see WorkspaceNodeLinkManager#linkNodes(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void linkNodes(WorkspaceNode parentNode, WorkspaceNode childNode) throws WorkspaceException {
        
        int workspaceID = parentNode.getWorkspaceID();
        
        MetadataDocument tempParentDocument = null;
        try {
            tempParentDocument = this.metadataAPI.getMetadataDocument(parentNode.getWorkspaceURL());
        } catch (IOException ex) {
            String errorMessage = "Error retrieving metadata document for node " + parentNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        } catch (MetadataException ex) {
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
            if(childNode.isMetadata()) {
                parentDocument.createDocumentMetadataReference(
                        childNode.getWorkspaceURL().toURI(), childNode.getFormat());
            } else {
                parentDocument.createDocumentResourceReference(
                        childNode.getWorkspaceURL().toURI(), childNode.getType().toString(), childNode.getFormat());
            }
            
            StreamResult parentStreamResult =
                    this.workspaceFileHandler.getStreamResultForNodeFile(FileUtils.toFile(parentNode.getWorkspaceURL()));
                
            this.metadataAPI.writeMetadataDocument(parentDocument, parentStreamResult);
                
        } catch (URISyntaxException ex) {
            String errorMessage = "Error creating reference in document with node ID " + parentNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        } catch (MetadataException ex) {
            String errorMessage = "Error creating reference in document with node ID " + parentNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        } catch (IOException ex) {
            String errorMessage = "Error creating reference in document with node ID " + parentNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        } catch (TransformerException ex) {
            String errorMessage = "Error creating reference in document with node ID " + parentNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        }
        
        WorkspaceNodeLink nodeLink = null;
        try {
            nodeLink =
                    this.workspaceNodeLinkFactory.getNewWorkspaceNodeLink(
                        parentNode.getWorkspaceNodeID(), childNode.getWorkspaceNodeID(), childNode.getWorkspaceURL().toURI());
        } catch (URISyntaxException ex) {
            String errorMessage = "Error creating link between nodes " + parentNode.getWorkspaceNodeID() + " and " + childNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        }
        
        this.workspaceDao.addWorkspaceNodeLink(nodeLink);
    }

    @Override
    public void unlinkNodes(WorkspaceNode parentNode, WorkspaceNode childNode)
            throws WorkspaceException {
        
        int workspaceID = parentNode.getWorkspaceID();
        
        MetadataDocument tempParentDocument = null;
        try {
            tempParentDocument = this.metadataAPI.getMetadataDocument(parentNode.getWorkspaceURL());
        } catch (IOException ex) {
            String errorMessage = "Error retrieving metadata document for node " + parentNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        } catch (MetadataException ex) {
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
//            URI childURI;
            
//            if(!childNode.getPid().isEmpty()) {
//                childURI = new URI(childNode.getPid());
//            } else {
//                childURI = childNode.getWorkspaceURL().toURI();
//            }

            URI uriToQuery = childNode.getArchiveURI();
            
            if(uriToQuery == null) {
                uriToQuery = childNode.getWorkspaceURL().toURI();
            }
            
            //TODO CHECK IF URI IS NULL OR NOT... HOW TO USE URL TO RETRIEVE REFERENCE?
            
            Reference childReference = parentDocument.getDocumentReferenceByURI(uriToQuery);
            parentDocument.removeDocumentReference(childReference);
            
            StreamResult parentStreamResult =
                    this.workspaceFileHandler.getStreamResultForNodeFile(FileUtils.toFile(parentNode.getWorkspaceURL()));
                
            this.metadataAPI.writeMetadataDocument(parentDocument, parentStreamResult);
                
        } catch (URISyntaxException ex) {
            String errorMessage = "Error removing reference in document with node ID " + childNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        } catch (MetadataException ex) {
            String errorMessage = "Error removing reference in document with node ID " + childNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        } catch (IOException ex) {
            String errorMessage = "Error removing reference in document with node ID " + childNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        } catch (TransformerException ex) {
            String errorMessage = "Error removing reference in document with node ID " + childNode.getWorkspaceNodeID();
            throwWorkspaceException(errorMessage, workspaceID, ex);
        }
        
        this.workspaceDao.deleteWorkspaceNodeLink(parentNode.getWorkspaceID(), parentNode.getWorkspaceNodeID(), childNode.getWorkspaceNodeID());
    }

    @Override
    public void unlinkNodeFromAllParents(WorkspaceNode childNode)
            throws WorkspaceException {
        
        Collection<WorkspaceNode> parentNodes =
                this.workspaceDao.getParentWorkspaceNodes(childNode.getWorkspaceNodeID());
        
        for(WorkspaceNode parent : parentNodes) {
            this.unlinkNodes(parent, childNode);
        }
    }
    
    private void throwWorkspaceException(String errorMessage, int workspaceID, Exception cause) throws WorkspaceException {
        logger.error(errorMessage, cause);
        throw new WorkspaceException(errorMessage, workspaceID, cause);
    }
}
