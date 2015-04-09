/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceExportPhase;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class responsible for exporting nodes that are not applicable
 * to the other, more specific exporters.
 * @see NodeExporter
 * 
 * @author guisil
 */
@Component
public class GeneralNodeExporter implements NodeExporter {
    
    private final static Logger logger = LoggerFactory.getLogger(GeneralNodeExporter.class);

    @Autowired
    private MetadataAPI metadataAPI;
    @Autowired
    private WorkspaceFileHandler workspaceFileHandler;
    @Autowired
    private WorkspaceTreeExporter workspaceTreeExporter;
    @Autowired
    private CorpusStructureProvider corpusStructureProvider;
    @Autowired
    private NodeResolver nodeResolver;
    @Autowired
    private ArchiveFileLocationProvider archiveFileLocationProvider;
    @Autowired
    private NodeUtil nodeUtil;
    

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
        
        if(WorkspaceSubmissionType.DELETE_WORKSPACE.equals(submissionType)) {
            String errorMessage = "This exporter should only be used when submitting the workspace, not when deleting";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        if(WorkspaceExportPhase.UNLINKED_NODES_EXPORT.equals(exportPhase)) {
            String errorMessage = "This exporter should only be used when exporting the tree, not for unlinked nodes";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        
        int workspaceID = workspace.getWorkspaceID();
        
        logger.debug("Exporting previously existing node to archive; workspaceID: " + workspaceID + "; parentNodeID: "
                + (parentNode != null ? parentNode.getWorkspaceNodeID() : -1) + "; currentNodeID: " + currentNode.getWorkspaceNodeID());
        
        if(currentNode.isProtected()) { // a protected node should remain intact after the workspace submission
            logger.info("Node " + currentNode.getWorkspaceNodeID() + " is protected; skipping export of this node to keep it intact in the archive");
            return;
        }
            
        CorpusNode corpusNode = this.corpusStructureProvider.getNode(currentNode.getArchiveURI());
        if(corpusNode == null) {
            String errorMessage = "Node not found in archive database for URI " + currentNode.getArchiveURI();
            throwWorkspaceExportException(workspaceID, errorMessage, null);
        }
        
        File nodeArchiveFile = nodeResolver.getLocalFile(corpusNode);
        
        if(nodeUtil.isNodeMetadata(currentNode)) {
            
            workspaceTreeExporter.explore(workspace, currentNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            // assuming that the metadata always changes (due to the localURI attribute being edited during the import)
                // so a file size or checksum check wouldn't work in this case
            
            MetadataDocument nodeDocument = null;
            try {
                nodeDocument = metadataAPI.getMetadataDocument(currentNode.getWorkspaceURL());
            } catch (IOException | MetadataException ex) {
                String errorMessage = "Error getting Metadata Document for node " + currentNode.getArchiveURI();
                throwWorkspaceExportException(workspaceID, errorMessage, ex);
            }
            
            StreamResult nodeArchiveStreamResult = workspaceFileHandler.getStreamResultForNodeFile(nodeArchiveFile);
            
            try {
                metadataAPI.writeMetadataDocument(nodeDocument, nodeArchiveStreamResult);
            } catch (IOException | TransformerException | MetadataException ex) {
                String errorMessage = "Error writing file for node " + currentNode.getArchiveURI();
                throwWorkspaceExportException(workspaceID, errorMessage, ex);
            }           
            
            if(parentNode == null) { // top node
                return;
            }
            
        } else {
            
            // resources were not copied from the archive to the workspace, so should not be copied back
        }
        
        CorpusNode parentCorpusNode = this.corpusStructureProvider.getNode(parentNode.getArchiveURI());
        if(parentCorpusNode == null) {
            String errorMessage = "Parent node not found in archive database for URI " + parentNode.getArchiveURI();
            throwWorkspaceExportException(workspaceID, errorMessage, null);
        }
        File parentNodeArchiveFile = nodeResolver.getLocalFile(parentCorpusNode);
        
        
        ReferencingMetadataDocument referencingParentDocument = retrieveReferencingMetadataDocument(workspaceID, parentNode);
        String currentPathRelativeToParent = 
                archiveFileLocationProvider.getChildPathRelativeToParent(parentNodeArchiveFile, nodeArchiveFile);
        
        updateReferenceInParent(workspaceID, currentNode, parentNode, referencingParentDocument, currentPathRelativeToParent);
    }
    
    
    private MetadataDocument retrieveMetadataDocument(int workspaceID, WorkspaceNode node) throws WorkspaceExportException {
        
        MetadataDocument document = null;
        
        if(nodeUtil.isNodeMetadata(node)) {
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
    
    private void updateReferenceInParent(int workspaceID, WorkspaceNode currentNode, WorkspaceNode parentNode,
            ReferencingMetadataDocument referencingParentDocument, String currentPathRelativeToParent) throws WorkspaceExportException {
        
        try {
            Reference currentReference = referencingParentDocument.getDocumentReferenceByURI(currentNode.getArchiveURI());
            URI currentUriRelativeToParent = URI.create(currentPathRelativeToParent);
            currentReference.setLocation(currentUriRelativeToParent);
            StreamResult targetParentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(new File(parentNode.getWorkspaceURL().getPath()));
            metadataAPI.writeMetadataDocument(referencingParentDocument, targetParentStreamResult);
            
        } catch (IOException | MetadataException | TransformerException ex) {
            String errorMessage = "Error writing file (updating child reference) for node " + parentNode.getWorkspaceURL();
            throwWorkspaceExportException(workspaceID, errorMessage, ex);
        }
    }
    
    private void throwWorkspaceExportException(int workspaceID, String errorMessage, Exception cause) throws WorkspaceExportException {
        logger.error(errorMessage, cause);
        throw new WorkspaceExportException(errorMessage, workspaceID, cause);
    }
    
}
