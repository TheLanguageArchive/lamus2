/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import javax.xml.transform.TransformerException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.OutputFormat;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceFileImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeExplorer;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Node importer specific for metadata files.
 * @see NodeImporter
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class MetadataNodeImporter implements NodeImporter<MetadataReference> {

    private static final Logger logger = LoggerFactory.getLogger(MetadataNodeImporter.class);
    
    @Autowired
    private CorpusStructureProvider corpusStructureProvider;
    @Autowired
    private NodeResolver nodeResolver;
    @Autowired
    private WorkspaceDao workspaceDao;
    @Autowired
    private MetadataAPI metadataAPI;
    @Autowired
    private MetadataApiBridge metadataApiBridge;
    @Autowired
    private WorkspaceNodeLinkManager workspaceNodeLinkManager;
    @Autowired
    private WorkspaceFileImporter workspaceFileImporter;
    @Autowired
    private WorkspaceNodeFactory workspaceNodeFactory;
    @Autowired
    private WorkspaceNodeExplorer workspaceNodeExplorer;
    @Autowired
    private NodeDataRetriever nodeDataRetriever;

    
    /**
     * @see NodeImporter#importNode(
     *      nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode,
     *      nl.mpi.metadata.api.model.ReferencingMetadataDocument, nl.mpi.metadata.api.model.Reference)
     */
    @Override
    public void importNode(Workspace workspace, WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument,
	    Reference referenceFromParent) throws WorkspaceImportException {

	if (workspace == null) {
	    String errorMessage = "Workspace not set";
	    logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
	}
        
        int workspaceID = workspace.getWorkspaceID();
        
	//TODO if not onsite: create external node
	//TODO setURID
	//TODO if no access: create forbidden node
	//TODO if unknown node: create forbidden node
	//TODO if needsProtection: create external node

	//TODO check if node already exists in db
	//TODO if so, it should be for the same workspace
	//TODO also, the node file should already exist in the workspace directory

        URI childArchiveURI;
        
        //TODO another way of doing this?
        if(referenceFromParent == null) { // top node
            childArchiveURI = workspace.getTopNodeArchiveURI();
        } else {
            if(referenceFromParent instanceof HandleCarrier) {
                childArchiveURI = ((HandleCarrier) referenceFromParent).getHandle();
            } else {
                childArchiveURI = referenceFromParent.getURI();
            }
        }
        
        logger.debug("Importing node into new workspace; workspaceID: " + workspaceID + "; nodeURI: " + childArchiveURI);
        
        URL childArchiveURL = null;
        String childName = null;
        boolean childOnSite = true;
        
        MetadataDocument childDocument = null;
        CorpusNode childCorpusNode;
        File childLocalFile = null;
        try {
            
            childCorpusNode = corpusStructureProvider.getNode(childArchiveURI);
            
            if(childCorpusNode == null) {
                String errorMessage = "Error getting information for node " + childArchiveURI;
                throwWorkspaceImportException(workspaceID, errorMessage, null);
            }
            
            childName = childCorpusNode.getName();
            childOnSite = childCorpusNode.isOnSite();
            
            if(childOnSite) {
                childLocalFile = nodeResolver.getLocalFile(childCorpusNode);
                childArchiveURL = childLocalFile.toURI().toURL();
            } else {
                URI childArchiveUrlUri = nodeResolver.getUrl(childCorpusNode, OutputFormat.CMDI);
                childArchiveURL = childArchiveUrlUri.toURL();
            }
            
            childDocument = metadataAPI.getMetadataDocument(childArchiveURL);

        } catch (IOException | MetadataException ioex) {
	    String errorMessage = "Error getting Metadata Document for node " + childArchiveURI;
	    throwWorkspaceImportException(workspaceID, errorMessage, ioex);
        }
        
        boolean childToBeProtected = false;
        
        if(!childArchiveURI.equals(workspace.getTopNodeArchiveURI())) { //if child is not the top node of the workspace it can be protected
            childToBeProtected = nodeDataRetriever.isNodeToBeProtected(childArchiveURI);
        }
        
        WorkspaceNode childNode =
                workspaceNodeFactory.getNewWorkspaceMetadataNode(workspaceID, childArchiveURI, childArchiveURL, childDocument, childName, childOnSite, childToBeProtected);
        workspaceDao.addWorkspaceNode(childNode);
        
        workspaceNodeLinkManager.linkNodesWithReference(workspace, parentNode, childNode, referenceFromParent);
        
        if(childNode.isExternal() || childNode.isProtected()) {
            return;
        }
        
        try {
            this.workspaceFileImporter.importMetadataFileToWorkspace(childLocalFile, childNode, childDocument);
            
            if(referenceFromParent != null) {
                referenceFromParent.setLocation(childNode.getWorkspaceURL().toURI());
                metadataApiBridge.saveMetadataDocument(parentDocument, parentNode.getWorkspaceURL());
            }
            
	} catch (MalformedURLException muex) {
            String errorMessage = "Failed to set URL for node " + childNode.getArchiveURI()
		    + " in workspace " + workspaceID;
	    throwWorkspaceImportException(workspaceID, errorMessage, muex);
        } catch (IOException ioex) {
            String errorMessage = "Failed to create file for node " + childNode.getArchiveURI()
		    + " in workspace " + workspaceID;
	    throwWorkspaceImportException(workspaceID, errorMessage, ioex);
        } catch (TransformerException trex) {
            String errorMessage = "Failed to create file for node " + childNode.getArchiveURI()
		    + " in workspace " + workspaceID;
	    throwWorkspaceImportException(workspaceID, errorMessage, trex);
        } catch (MetadataException mdex) {
            String errorMessage = "Failed to create file for node " + childNode.getArchiveURI()
		    + " in workspace " + workspaceID;
	    throwWorkspaceImportException(workspaceID, errorMessage, mdex);
        } catch (URISyntaxException usex) {
            String errorMessage = "Failed to set location for node " + childNode.getArchiveURI()
                    + " in workspace " + workspaceID;
            throwWorkspaceImportException(workspaceID, errorMessage, usex);
        }

	//TODO change the referenced URL in the parent document
	//TODO not of the original one, but the one IN THE WORKSPACE FOLDER
	if (parentDocument != null) {
	    //TODO how to change the child link/element (of the new document) using the MetadataAPI?
	    //TODO change link of the copied document to have a different handle when it is null, for instance
	}

	if (childDocument instanceof ReferencingMetadataDocument) {
	    ReferencingMetadataDocument childReferencingDocument = (ReferencingMetadataDocument) childDocument;
            List<Reference> links = childReferencingDocument.getDocumentReferences();
	    workspaceNodeExplorer.explore(workspace, childNode, childReferencingDocument, links);
	}
    }
    
    private void throwWorkspaceImportException(int workspaceID, String errorMessage, Exception cause) throws WorkspaceImportException {
        logger.error(errorMessage, cause);
        throw new WorkspaceImportException(errorMessage, workspaceID, cause);
    }
}
