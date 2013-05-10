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
import java.net.MalformedURLException;
import java.util.List;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.UnknownNodeException;
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
import nl.mpi.lamus.workspace.importing.WorkspaceNodeLinker;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Node importer specific for metadata files.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class MetadataNodeImporter implements NodeImporter<MetadataReference> {

    private static final Logger logger = LoggerFactory.getLogger(MetadataNodeImporter.class);
    
    private final ArchiveObjectsDB archiveObjectsDB;
    private final WorkspaceDao workspaceDao;
    private final MetadataAPI metadataAPI;
    
    private final NodeDataRetriever nodeDataRetriever;
    private final WorkspaceNodeLinker workspaceNodeLinker;
    private final WorkspaceFileImporter workspaceFileImporter;
    
    private final WorkspaceNodeFactory workspaceNodeFactory;
    private final WorkspaceParentNodeReferenceFactory workspaceParentNodeReferenceFactory;
    private final WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    private final WorkspaceFileHandler workspaceFileHandler;
    private final WorkspaceNodeExplorer workspaceNodeExplorer;
    private Workspace workspace = null;

    @Autowired
    public MetadataNodeImporter(@Qualifier("ArchiveObjectsDB") ArchiveObjectsDB aoDB, WorkspaceDao wsDao, MetadataAPI mAPI,
	    NodeDataRetriever nodeDataRetriever, WorkspaceNodeLinker nodeLinker, WorkspaceFileImporter fileImporter,
            WorkspaceNodeFactory nodeFactory, WorkspaceParentNodeReferenceFactory parentNodeReferenceFactory,
	    WorkspaceNodeLinkFactory wsNodelinkFactory, WorkspaceFileHandler fileHandler,
	    WorkspaceNodeExplorer workspaceNodeExplorer) {

	this.archiveObjectsDB = aoDB;
	this.workspaceDao = wsDao;
	this.metadataAPI = mAPI;
        
        this.nodeDataRetriever = nodeDataRetriever;
        this.workspaceNodeLinker = nodeLinker;
        this.workspaceFileImporter = fileImporter;
        
	this.workspaceNodeFactory = nodeFactory;
	this.workspaceParentNodeReferenceFactory = parentNodeReferenceFactory;
	this.workspaceNodeLinkFactory = wsNodelinkFactory;
	this.workspaceFileHandler = fileHandler;
	this.workspaceNodeExplorer = workspaceNodeExplorer;
    }

    /**
     * @see NodeImporter#setWorkspace(nl.mpi.lamus.workspace.model.Workspace)
     */
    public void setWorkspace(Workspace ws) {
	this.workspace = ws;
    }

    /**
     * @see NodeImporter#importNode(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.metadata.api.model.ReferencingMetadataDocument, nl.mpi.metadata.api.model.Reference, int)
     */
    public void importNode(WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument,
	    Reference childLink, int childNodeArchiveID) throws NodeImporterException, NodeExplorerException {

	if (workspace == null) {
	    String errorMessage = "MetadataNodeImporter.importNode: workspace not set";
	    logger.error(errorMessage);
	    throw new NodeImporterException(errorMessage, workspace, this.getClass(), null);
	}


	//TODO if not onsite: create external node
	//TODO setURID
	//TODO if no access: create forbidden node
	//TODO if unknown node: create forbidden node
	//TODO if needsProtection: create external node



	//TODO check if node already exists in db
	//TODO if so, it should be for the same workspace
	//TODO also, the node file should already exist in the workspace directory

        
        MetadataDocument childDocument;
        try {
            childDocument = nodeDataRetriever.getArchiveNodeMetadataDocument(childNodeArchiveID);
        } catch (IOException ioex) {
	    String errorMessage = "Error importing Metadata Document for node with ID " + childNodeArchiveID;
	    logger.error(errorMessage, ioex);
	    throw new NodeImporterException(errorMessage, workspace, this.getClass(), ioex);
        } catch (MetadataException mdex) {
	    String errorMessage = "Error importing Metadata Document for node with ID " + childNodeArchiveID;
	    logger.error(errorMessage, mdex);
	    throw new NodeImporterException(errorMessage, workspace, this.getClass(), mdex);
        } catch (UnknownNodeException unex) {
	    String errorMessage = "Error getting object URL for node ID " + childNodeArchiveID;
	    logger.error(errorMessage, unex);
	    throw new NodeImporterException(errorMessage, workspace, this.getClass(), unex);
        }
        
        WorkspaceNode childNode;
        try {
            childNode = workspaceNodeFactory.getNewWorkspaceMetadataNode(workspace, childNodeArchiveID, childDocument);
        } catch (MalformedURLException muex) {
            String errorMessage = "Error creating workspace node for file with location: " + childDocument.getFileLocation();
            logger.error(errorMessage, muex);
            throw new NodeImporterException(errorMessage, workspace, this.getClass(), muex);
        }
        
        workspaceDao.addWorkspaceNode(childNode);
        
        
        workspaceNodeLinker.linkNodes(workspace, parentNode, childNode, childLink);
        
        
        try {
            this.workspaceFileImporter.importMetadataFileToWorkspace(workspace, childNode, childDocument);
        } catch (WorkspaceNodeFilesystemException fwsnex) {
	    String errorMessage = "Failed to create file for workspace node " + childNode.getWorkspaceNodeID()
		    + " in workspace " + workspace.getWorkspaceID();
	    logger.error(errorMessage, fwsnex);
	    throw new NodeImporterException(errorMessage, workspace, this.getClass(), fwsnex);
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
	    workspaceNodeExplorer.explore(workspace, childNode, childReferencingDocument, links);
	}

	//TODO What else?
    }
}
