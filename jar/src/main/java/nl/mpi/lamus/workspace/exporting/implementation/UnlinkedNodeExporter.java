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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.TransformerException;
import net.handle.hdllib.HandleException;
import nl.mpi.lamus.archive.ArchiveHandleHelper;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.VersioningHandler;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceExportPhase;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author guisil
 */
@Component
public class UnlinkedNodeExporter implements NodeExporter{
    
    private static final Logger logger = LoggerFactory.getLogger(UnlinkedNodeExporter.class);

    @Autowired
    private VersioningHandler versioningHandler;
    @Autowired
    private ArchiveHandleHelper archiveHandleHelper;
    @Autowired
    private WorkspaceTreeExporter workspaceTreeExporter;
    @Autowired
    private MetadataApiBridge metadataApiBridge;
    @Autowired
    private MetadataAPI metadataAPI;
    

    /**
     * @see NodeExporter#exportNode(
     *          nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode,
     *          nl.mpi.lamus.workspace.model.WorkspaceNode, boolean,
     *          nl.mpi.lamus.workspace.model.WorkspaceSubmissionType, nl.mpi.lamus.workspace.model.WorkspaceExportPhase)
     */
    @Override
    public void exportNode(
        Workspace workspace, WorkspaceNode parentNode, WorkspaceNode currentNode,
        boolean keepUnlinkedFiles,
        WorkspaceSubmissionType submissionType, WorkspaceExportPhase exportPhase)
            throws WorkspaceExportException {

        if (workspace == null) {
	    String errorMessage = "Workspace not set";
	    logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
	}
        
        if(WorkspaceExportPhase.TREE_EXPORT.equals(exportPhase)) {
            String errorMessage = "This exporter should only be used when exporting unlinked nodes, not for the tree";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        logger.debug("Exporting unlinked node to archive; workspaceID: " + workspace.getWorkspaceID() + "; currentNodeID: " + currentNode.getWorkspaceNodeID());
        logger.debug("Keep unlinked files: " + keepUnlinkedFiles + "; Submission type: " + submissionType.toString() + "; Export phase: " + exportPhase.toString());
        
        if(currentNode.isProtected()) { // a protected node should remain intact after the workspace submission
            logger.info("Node " + currentNode.getWorkspaceNodeID() + " is protected; skipping export of this node to keep it intact in the archive");
            return;
        }
        
        if(!keepUnlinkedFiles && WorkspaceSubmissionType.DELETE_WORKSPACE.equals(submissionType)) {
            logger.debug("Nodes not to be kept and workspace to be deleted. Nothing to do here.");
            return;
        }
        
        URI currentArchiveUri = currentNode.getArchiveURI();
        
        if(!keepUnlinkedFiles && WorkspaceSubmissionType.SUBMIT_WORKSPACE.equals(submissionType)) {
            
            if(currentArchiveUri != null) {
                
                if(currentNode.isMetadata()) {
                    workspaceTreeExporter.explore(workspace, currentNode, keepUnlinkedFiles, submissionType, exportPhase);
                }
                
                URL trashedNodeArchiveURL = this.versioningHandler.moveFileToTrashCanFolder(currentNode);
                currentNode.setArchiveURL(trashedNodeArchiveURL);
                URI trashedNodeArchiveUri = URI.create(trashedNodeArchiveURL.toString());
                
                if(parentNode != null) {
                    ReferencingMetadataDocument parentDocument = retrieveReferencingMetadataDocument(workspace.getWorkspaceID(), parentNode);
                    updateReferenceInParent(workspace.getWorkspaceID(), currentArchiveUri, trashedNodeArchiveUri, parentNode.getWorkspaceURL(), parentDocument, trashedNodeArchiveURL.getFile());
                }
                logger.debug("Node " + currentArchiveUri + " not to be kept, but moved to the trashcan.");
                return;
            } else {
                logger.debug("Node " + currentNode.getWorkspaceURL() + " not to be kept and not from the archive. Nothing to do here.");
                return;
            }
        }
        
        if(keepUnlinkedFiles) {
            
            if(WorkspaceSubmissionType.DELETE_WORKSPACE.equals(submissionType) && currentArchiveUri != null) {
                // do nothing... the node is in the archive and the workspace is being deleted, so this shouldn't change
                logger.debug("Node " + currentArchiveUri + " is in the archive and the workspace is being deleted, therefore this unlinked file won't be kept outside of the archive.");

                if(parentNode != null) {
                    ReferencingMetadataDocument parentDocument = retrieveReferencingMetadataDocument(workspace.getWorkspaceID(), parentNode);
                    updateReferenceInParent(workspace.getWorkspaceID(), currentArchiveUri, currentArchiveUri, parentNode.getWorkspaceURL(), parentDocument, null);
                }
                
                return;
            }
    
            if(currentNode.isMetadata()) {
                workspaceTreeExporter.explore(workspace, currentNode, keepUnlinkedFiles, submissionType, exportPhase);
            }

            URL orphanedNodeURL = versioningHandler.moveFileToOrphansFolder(workspace, currentNode);
            URI nodeUri = URI.create(orphanedNodeURL.toString());
                
            if(currentArchiveUri != null) {

                nodeUri = currentArchiveUri;

                try {
                    archiveHandleHelper.deleteArchiveHandle(currentNode, false);
                } catch (HandleException | IOException | TransformerException | MetadataException ex) {
                    logger.warn("There was a problem while deleting the handle for node " + currentNode.getArchiveURL());
                }
            }

            if(parentNode != null) {
                URI nodeWsUri = URI.create(currentNode.getWorkspaceURL().toString());
                ReferencingMetadataDocument parentDocument = retrieveReferencingMetadataDocument(workspace.getWorkspaceID(), parentNode);
                updateReferenceInParent(workspace.getWorkspaceID(), nodeWsUri, nodeUri, parentNode.getWorkspaceURL(), parentDocument, FilenameUtils.getName(orphanedNodeURL.toString()));
            }
        }

        //TODO is this necessary?
//        searchClientBridge.removeNode(currentNode.getArchiveURI());
    }
    
    
    private MetadataDocument retrieveMetadataDocument(int workspaceID, WorkspaceNode node) throws WorkspaceExportException {
        
        MetadataDocument document = null;
        
        if(node.isMetadata()) {
            try {
                document = metadataAPI.getMetadataDocument(node.getWorkspaceURL());
                
            } catch (IOException | MetadataException ex) {
                String errorMessage = "Error getting Metadata Document for node " + node.getWorkspaceURL();
                throwWorkspaceExportException(workspaceID, errorMessage, ex);
            }
        }
        
        return document;
    }
    
    private ReferencingMetadataDocument retrieveReferencingMetadataDocument(int workspaceID, WorkspaceNode node) throws WorkspaceExportException {
        
        MetadataDocument document = retrieveMetadataDocument(workspaceID, node);
        ReferencingMetadataDocument referencingParentDocument = null;
        if(document instanceof ReferencingMetadataDocument) {
            referencingParentDocument = (ReferencingMetadataDocument) document;
        } else {
            String errorMessage = "Error retrieving child reference in file " + node.getWorkspaceURL();
            throwWorkspaceExportException(workspaceID, errorMessage, null);
        }
        
        return referencingParentDocument;
    }
    
    private void updateReferenceInParent(int workspaceID, URI currentNodeOldUri, URI currentNodeUri, URL parentLocation,
            ReferencingMetadataDocument referencingParentDocument, String filename) throws WorkspaceExportException {
        
        try {
            Reference currentReference = referencingParentDocument.getDocumentReferenceByLocation(currentNodeOldUri);
            
            if(filename == null) {
                referencingParentDocument.removeDocumentReference(currentReference);
                return;
            }
            
            URI filenameUri = URI.create(filename);
            currentReference.setURI(filenameUri);
            currentReference.setLocation(currentNodeUri);
            metadataApiBridge.saveMetadataDocument(referencingParentDocument, parentLocation);
        } catch (IOException | MetadataException | TransformerException ex) {
            String errorMessage = "Error writing file (updating child reference) for node " + parentLocation;
            throwWorkspaceExportException(workspaceID, errorMessage, ex);
        }
    }
    
    private void throwWorkspaceExportException(int workspaceID, String errorMessage, Exception cause) throws WorkspaceExportException {
        logger.error(errorMessage, cause);
        throw new WorkspaceExportException(errorMessage, workspaceID, cause);
    }
}
