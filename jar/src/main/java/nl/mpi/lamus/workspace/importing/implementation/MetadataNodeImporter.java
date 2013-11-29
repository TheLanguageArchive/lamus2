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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.WorkspaceNodeFilesystemException;
import nl.mpi.lamus.workspace.exception.NodeExplorerException;
import nl.mpi.lamus.workspace.exception.NodeImporterException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceFileImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeExplorer;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Node importer specific for metadata files.
 * @see NodeImporter
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class MetadataNodeImporter implements NodeImporter<MetadataReference> {

    private static final Logger logger = LoggerFactory.getLogger(MetadataNodeImporter.class);
    
    private final CorpusStructureProvider corpusStructureProvider;
    private final NodeResolver nodeResolver;
    private final WorkspaceDao workspaceDao;
    private final MetadataAPI metadataAPI;
    
    private final NodeDataRetriever nodeDataRetriever;
    private final WorkspaceNodeLinkManager workspaceNodeLinkManager;
    private final WorkspaceFileImporter workspaceFileImporter;
    
    private final WorkspaceNodeFactory workspaceNodeFactory;
    private final WorkspaceParentNodeReferenceFactory workspaceParentNodeReferenceFactory;
    private final WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    private final WorkspaceFileHandler workspaceFileHandler;
    private final WorkspaceNodeExplorer workspaceNodeExplorer;

    private int workspaceID = -1;
    
    @Autowired
    public MetadataNodeImporter(CorpusStructureProvider csProvider, NodeResolver nodeResolver, WorkspaceDao wsDao, MetadataAPI mAPI,
	    NodeDataRetriever nodeDataRetriever, WorkspaceNodeLinkManager nodeLinkManager, WorkspaceFileImporter fileImporter,
            WorkspaceNodeFactory nodeFactory, WorkspaceParentNodeReferenceFactory parentNodeReferenceFactory,
	    WorkspaceNodeLinkFactory wsNodelinkFactory, WorkspaceFileHandler fileHandler,
	    WorkspaceNodeExplorer workspaceNodeExplorer) {

	this.corpusStructureProvider = csProvider;
        this.nodeResolver = nodeResolver;
	this.workspaceDao = wsDao;
	this.metadataAPI = mAPI;
        
        this.nodeDataRetriever = nodeDataRetriever;
        this.workspaceNodeLinkManager = nodeLinkManager;
        this.workspaceFileImporter = fileImporter;
        
	this.workspaceNodeFactory = nodeFactory;
	this.workspaceParentNodeReferenceFactory = parentNodeReferenceFactory;
	this.workspaceNodeLinkFactory = wsNodelinkFactory;
	this.workspaceFileHandler = fileHandler;
	this.workspaceNodeExplorer = workspaceNodeExplorer;
    }

    /**
     * @see NodeImporter#importNode(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.metadata.api.model.ReferencingMetadataDocument, nl.mpi.metadata.api.model.Reference, int)
     */
    @Override
    public void importNode(int wsID, WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument,
	    Reference childLink, URI childArchiveURI) throws NodeImporterException, NodeExplorerException {

        workspaceID = wsID;
        
	if (workspaceID < 0) {
	    String errorMessage = "MetadataNodeImporter.importNode: workspace not set";
	    logger.error(errorMessage);
	    throw new NodeImporterException(errorMessage, workspaceID, this.getClass(), null);
	}


	//TODO if not onsite: create external node
	//TODO setURID
	//TODO if no access: create forbidden node
	//TODO if unknown node: create forbidden node
	//TODO if needsProtection: create external node



	//TODO check if node already exists in db
	//TODO if so, it should be for the same workspace
	//TODO also, the node file should already exist in the workspace directory

        
        
        URL childArchiveURL;
        String childName;
        
        MetadataDocument childDocument;
        try {
            
            CorpusNode childCorpusNode = corpusStructureProvider.getNode(childArchiveURI);
            childArchiveURL = nodeResolver.getUrl(childCorpusNode);
            childName = childCorpusNode.getName();
            
            childDocument = metadataAPI.getMetadataDocument(childArchiveURL);

        } catch (IOException ioex) {
	    String errorMessage = "Error importing Metadata Document for node " + childArchiveURI;
	    logger.error(errorMessage, ioex);
	    throw new NodeImporterException(errorMessage, workspaceID, this.getClass(), ioex);
        } catch (MetadataException mdex) {
	    String errorMessage = "Error importing Metadata Document for node " + childArchiveURI;
	    logger.error(errorMessage, mdex);
	    throw new NodeImporterException(errorMessage, workspaceID, this.getClass(), mdex);
        } catch (UnknownNodeException unex) {
	    String errorMessage = "Error getting information for node " + childArchiveURI;
	    logger.error(errorMessage, unex);
	    throw new NodeImporterException(errorMessage, workspaceID, this.getClass(), unex);
        }
        
        WorkspaceNode childNode = workspaceNodeFactory.getNewWorkspaceMetadataNode(workspaceID, childArchiveURI, childArchiveURL, childDocument, childName);
        
        workspaceDao.addWorkspaceNode(childNode);
        
        
        workspaceNodeLinkManager.linkNodesWithReference(parentNode, childNode, childLink);
        
        
        try {
            this.workspaceFileImporter.importMetadataFileToWorkspace(childArchiveURL, childNode, childDocument);
        } catch (WorkspaceNodeFilesystemException fwsnex) {
	    String errorMessage = "Failed to create file for workspace node " + childNode.getWorkspaceNodeID()
		    + " in workspace " + workspaceID;
	    logger.error(errorMessage, fwsnex);
	    throw new NodeImporterException(errorMessage, workspaceID, this.getClass(), fwsnex);
	}

	//TODO change the referenced URL in the parent document
	//TODO not of the original one, but the one IN THE WORKSPACE FOLDER
	if (parentDocument != null) {
	    //TODO how to change the child link/element (of the new document) using the MetadataAPI?
	    //TODO change link of the copied document to have a different handle when it is null, for instance
	}


	//TODO get metadata file links (references)
	if (childDocument instanceof ReferencingMetadataDocument) {
	    ReferencingMetadataDocument childReferencingDocument = (ReferencingMetadataDocument) childDocument;
            List<Reference> links = childReferencingDocument.getDocumentReferences();
	    workspaceNodeExplorer.explore(childNode, childReferencingDocument, links);
	}

	//TODO What else?
    }
}
