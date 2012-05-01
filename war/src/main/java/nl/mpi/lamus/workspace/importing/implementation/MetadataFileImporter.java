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
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceNodeFileException;
import nl.mpi.lamus.workspace.exception.FileImporterException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.FileImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceFileExplorer;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceParentNodeReference;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.*;
import nl.mpi.metadata.api.type.MetadataDocumentType;
import nl.mpi.util.OurURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class MetadataFileImporter implements FileImporter<MetadataReference> {
    
    private static final Logger logger = LoggerFactory.getLogger(MetadataFileImporter.class);
    
    private ArchiveObjectsDB archiveObjectsDB;
    private WorkspaceDao workspaceDao;
    private MetadataAPI metadataAPI;
    private WorkspaceNodeFactory workspaceNodeFactory;
    private WorkspaceParentNodeReferenceFactory workspaceParentNodeReferenceFactory;
    private WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    private WorkspaceFileHandler workspaceFileHandler;
    private WorkspaceFileExplorer workspaceFileExplorer;
    private Workspace workspace;
    
    public MetadataFileImporter() {
        
    }
    
    //TODO use Spring injection
    public MetadataFileImporter(ArchiveObjectsDB aoDB, WorkspaceDao wsDao, MetadataAPI mAPI,
            WorkspaceNodeFactory nodeFactory, WorkspaceParentNodeReferenceFactory parentNodeReferenceFactory,
            WorkspaceNodeLinkFactory wsNodelinkFactory, WorkspaceFileHandler fileHandler,
            WorkspaceFileExplorer fileExplorer, Workspace ws) {
        
        this.archiveObjectsDB = aoDB;
        this.workspaceDao = wsDao;
        this.metadataAPI = mAPI;
        this.workspaceNodeFactory = nodeFactory;
        this.workspaceParentNodeReferenceFactory = parentNodeReferenceFactory;
        this.workspaceNodeLinkFactory = wsNodelinkFactory;
        this.workspaceFileHandler = fileHandler;
        this.workspaceFileExplorer = fileExplorer;
        this.workspace = ws;
    }

    public void importFile(WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument,
            Reference childLink, int childNodeArchiveID) throws FileImporterException {
        
        
        //TODO if not onsite: create external node
        //TODO setURID
        //TODO if no access: create forbidden node
        //TODO if unknown node: create forbidden node
        //TODO if needsProtection: create external node
        
        
        
        //TODO check if node already exists in db
        //TODO if so, it should be for the same workspace
        //TODO also, the node file should already exist in the workspace directory
        
        
        
        //TODO get more values to add to the node (e.g. from the file, using the metadataAPI)
        OurURL tempUrl = archiveObjectsDB.getObjectURL(NodeIdUtils.TONODEID(childNodeArchiveID), ArchiveAccessContext.getFileUrlContext());
        if(tempUrl == null) {
            String errorMessage = "Error getting object URL for node ID " + childNodeArchiveID;
            logger.error(errorMessage);
            throw new FileImporterException(errorMessage, workspace, this.getClass(), null);
        }
        URL childNodeArchiveURL = tempUrl.toURL();
        WorkspaceNode childNode = workspaceNodeFactory.getNewWorkspaceNode(workspace.getWorkspaceID(), childNodeArchiveID, childNodeArchiveURL);

        
        MetadataDocument childDocument = null;
        try {
            childDocument = metadataAPI.getMetadataDocument(childNode.getArchiveURL());
        } catch(IOException ioex) {
            String errorMessage = "Error importing Metadata Document " + childNode.getArchiveURL();
            logger.error(errorMessage, ioex);
            throw new FileImporterException(errorMessage, workspace, this.getClass(), ioex);
        } catch(MetadataException mdex) {
            String errorMessage = "Error importing Metadata Document " + childNode.getArchiveURL();
            logger.error(errorMessage, mdex);
            throw new FileImporterException(errorMessage, workspace, this.getClass(), mdex);
        }
        
        setWorkspaceNodeInformationFromMetadataDocument(childDocument, childNode);
        workspaceDao.addWorkspaceNode(childNode);
        
        WorkspaceParentNodeReference parentNodeReference =
                workspaceParentNodeReferenceFactory.getNewWorkspaceParentNodeReference(parentNode, childLink);
        
        //TODO set top node ID in workspace (if reference is null), set workspace status / Save workspace
        if(parentNodeReference == null) { //TODO find a better way of indicating this
            workspace.setTopNodeID(childNode.getWorkspaceNodeID());
            workspaceDao.updateWorkspaceTopNode(workspace);
        } else {
            //TODO add information about parent link
            // add the link in the database
            WorkspaceNodeLink nodeLink = workspaceNodeLinkFactory.getNewWorkspaceNodeLink(
                    parentNodeReference.getParentWorkspaceNodeID(), childNode.getWorkspaceNodeID(), childLink.getURI());
            workspaceDao.addWorkspaceNodeLink(nodeLink);
            //TODO possible problems with adding the link? if the link already exists?
        }
        
        workspace.setStatusMessageInitialising();
        workspaceDao.updateWorkspaceStatusMessage(workspace);
        
        File childNodeFile = workspaceFileHandler.getFileForWorkspaceNode(childNode);
        
        try {
            OutputStream outputStream = workspaceFileHandler.getOutputStreamForWorkspaceNodeFile(workspace, childNode, childNodeFile);
            workspaceFileHandler.copyMetadataFileToWorkspace(workspace, childNode, metadataAPI, childDocument, outputStream);
        } catch(FailedToCreateWorkspaceNodeFileException fwsnex) {
            String errorMessage = "Failed to create file for workspace node " + childNode.getWorkspaceNodeID()
                    + " in workspace " + workspace.getWorkspaceID();
            logger.error(errorMessage, fwsnex);
            throw new FileImporterException(errorMessage, workspace, this.getClass(), fwsnex);
        }
        
        //TODO change the referenced URL in the parent document
            //TODO not of the original one, but the one IN THE WORKSPACE FOLDER
        if(parentDocument != null) {
            //TODO how to change the child link/element (of the new document) using the MetadataAPI?
            //TODO change link of the copied document to have a different handle when it is null, for instance
        }
        
        
        //TODO get metadata file links (references)
        if(childDocument instanceof ReferencingMetadataDocument) {
            ReferencingMetadataDocument childReferencingDocument = (ReferencingMetadataDocument) childDocument;
            Collection<Reference> links = childReferencingDocument.getDocumentReferences();
            workspaceFileExplorer.explore(childNode, childReferencingDocument, links);
        }

        //TODO What else?
    }
    
    
    private void setWorkspaceNodeInformationFromMetadataDocument(MetadataDocument mdDocument, WorkspaceNode wsNode) {
        
        //TODO add some more information to the node (name, etc)
        String nodeName = mdDocument.getDisplayValue();
        wsNode.setName(nodeName);
        //TODO is the title to be kept?
        String nodeTitle = mdDocument.getDisplayValue();
        wsNode.setTitle(nodeTitle);
        WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO it's metadata, so it should be CMDI? otherwise, should I get it based on what? What are the possible node types?
        wsNode.setType(nodeType);
        String nodeFormat = ""; //TODO get this based on what? typechecker?
        wsNode.setFormat(nodeFormat);
        MetadataDocumentType metadataDocumentType = mdDocument.getType();
        URI profileSchemaURI = metadataDocumentType.getSchemaLocation();
        wsNode.setProfileSchemaURI(profileSchemaURI);
        String nodePid = WorkspacePidValue.NONE.toString();
        
        if(mdDocument instanceof HandleCarrier) {
            nodePid = ((HandleCarrier) mdDocument).getHandle();
        } else {
            logger.warn("Metadata document '" + mdDocument.getFileLocation().toString() + "' does not contain a handle.");
        }
        
        wsNode.setPid(nodePid);
        wsNode.setStatus(WorkspaceNodeStatus.NODE_ISCOPY);
    }
}
