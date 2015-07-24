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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import net.handle.hdllib.HandleException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.handle.util.HandleManager;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
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
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class responsible for exporting nodes that were newly added
 * and are supposed to get a new location in the archive.
 * @see NodeExporter
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class AddedNodeExporter implements NodeExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(AddedNodeExporter.class);

    @Autowired
    private ArchiveFileLocationProvider archiveFileLocationProvider;
    @Autowired
    private WorkspaceFileHandler workspaceFileHandler;
    @Autowired
    private MetadataAPI metadataAPI;
    @Autowired
    private WorkspaceDao workspaceDao;
    @Autowired
    private WorkspaceTreeExporter workspaceTreeExporter;
    @Autowired
    private HandleManager handleManager;
    @Autowired
    private HandleParser handleParser;
    @Autowired
    private MetadataApiBridge metadataApiBridge;
    @Autowired
    private CorpusStructureProvider corpusStructureProvider;
    @Autowired
    private NodeResolver nodeResolver;
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
        
        logger.debug("Exporting added node to archive; workspaceID: " + workspaceID + "; parentNodeID: " + parentNode.getWorkspaceNodeID() + "; currentNodeID: " + currentNode.getWorkspaceNodeID());
        
        File parentArchiveFile = retrieveParentArchiveLocation(workspaceID, parentNode);
        
        String currentNodeFilename = FilenameUtils.getName(currentNode.getWorkspaceURL().getPath());
        
        File nextAvailableFile = retrieveAndUpdateNewArchivePath(workspaceID, currentNode, currentNodeFilename, parentArchiveFile.getAbsolutePath());
        
        if(nodeUtil.isNodeMetadata(currentNode)) {
            workspaceTreeExporter.explore(workspace, currentNode, keepUnlinkedFiles, submissionType, exportPhase);
        }
        
        MetadataDocument currentDocument = retrieveMetadataDocument(workspaceID, currentNode);
        
        File currentNodeWorkspaceFile = new File(currentNode.getWorkspaceURL().getPath());
        
        assignAndUpdateNewHandle(workspaceID, currentNode);
        
        if(nodeUtil.isNodeMetadata(currentNode)) {
            updateSelfHandle(workspaceID, currentNode, currentDocument);
        }
        
        moveFileIntoArchive(workspaceID, currentNode, nextAvailableFile, currentDocument, currentNodeWorkspaceFile);
        
        ReferencingMetadataDocument referencingParentDocument = retrieveReferencingMetadataDocument(workspaceID, parentNode);
        
        String currentPathRelativeToParent = archiveFileLocationProvider.getChildPathRelativeToParent(parentArchiveFile, nextAvailableFile);
            
        updateReferenceInParent(workspaceID, currentNode, parentNode, referencingParentDocument, currentPathRelativeToParent);
        
        //TODO is this necessary?
//        if(searchClientBridge.isFormatSearchable(currentNode.getFormat())) {
//            searchClientBridge.addNode(currentNode.getArchiveURI());
//        }
    }
    
    private File retrieveParentArchiveLocation(int workspaceID, WorkspaceNode parentNode) throws WorkspaceExportException {
        
        File parentArchiveFile = null;
        
        if(parentNode.getArchiveURI() != null) {
                            
            CorpusNode parentArchiveNode = corpusStructureProvider.getNode(parentNode.getArchiveURI());
            if(parentArchiveNode != null) {
                parentArchiveFile = nodeResolver.getLocalFile(parentArchiveNode);
            }
        }
        
        if(parentArchiveFile == null) {
            try {
                parentArchiveFile = new File(archiveFileLocationProvider.getUriWithLocalRoot(parentNode.getArchiveURL().toURI()));
            } catch (URISyntaxException ex) {
                String errorMessage = "Error retrieving archive location of node " + parentNode.getArchiveURI();
                throwWorkspaceExportException(workspaceID, errorMessage, ex);
            }
        }
        return parentArchiveFile;
    }
    
    private File retrieveAndUpdateNewArchivePath(int workspaceID, WorkspaceNode currentNode, String currentNodeFilename, String parentArchivePath) throws WorkspaceExportException {
        
        File nextAvailableFile = null;
        URL newNodeArchiveURL = null;
        
        try {
            nextAvailableFile = archiveFileLocationProvider.getAvailableFile(parentArchivePath, currentNode, currentNodeFilename);
            logger.info("Retrieved new archive file path for added node: " + nextAvailableFile.getAbsolutePath());
            newNodeArchiveURL = nextAvailableFile.toURI().toURL();
        } catch (MalformedURLException ex) {
            String errorMessage = "Error getting new file for node " + currentNode.getWorkspaceURL();
            throwWorkspaceExportException(workspaceID, errorMessage, ex);
        } catch (IOException ex) {
            String errorMessage = "Error getting new file for node " + currentNode.getWorkspaceURL();
            throwWorkspaceExportException(workspaceID, errorMessage, ex);
        }
        currentNode.setArchiveURL(newNodeArchiveURL);
        workspaceDao.updateNodeArchiveUrl(currentNode);
        
        return nextAvailableFile;
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
    
    private void moveFileIntoArchive(int workspaceID, WorkspaceNode currentNode, File nextAvailableFile, MetadataDocument currentDocument, File currentNodeWorkspaceFile) throws WorkspaceExportException {
        
        try {
            if(archiveFileLocationProvider.isFileInOrphansDirectory(currentNodeWorkspaceFile)) {
                workspaceFileHandler.moveFile(currentNodeWorkspaceFile, nextAvailableFile);
            } else {
                if(nodeUtil.isNodeMetadata(currentNode)) {
                    StreamResult targetStreamResult = workspaceFileHandler.getStreamResultForNodeFile(nextAvailableFile);
                    metadataAPI.writeMetadataDocument(currentDocument, targetStreamResult);
                } else {
                    workspaceFileHandler.copyFile(currentNodeWorkspaceFile, nextAvailableFile);
                }
            }
        } catch (IOException | TransformerException | MetadataException ex) {
            String errorMessage = "Error writing file for node " + currentNode.getWorkspaceURL();
            throwWorkspaceExportException(workspaceID, errorMessage, ex);
        }
    }
    
    private void updateSelfHandle(int workspaceID, WorkspaceNode node, MetadataDocument document) throws WorkspaceExportException {
        
        //create self link in header, either if it is already there (will be replaced) or not (will be added)
        try {
            metadataApiBridge.addSelfHandleAndSaveDocument(document, node.getArchiveURI(), node.getWorkspaceURL());
        } catch (MetadataException | TransformerException | IOException ex) {
            String errorMessage = "Error updating header information for node " + node.getWorkspaceURL();
            throwWorkspaceExportException(workspaceID, errorMessage, ex);
        }
    }
    
    private void updateReferenceInParent(int workspaceID, WorkspaceNode currentNode, WorkspaceNode parentNode,
            ReferencingMetadataDocument referencingParentDocument, String currentPathRelativeToParent) throws WorkspaceExportException {
        
        try {
            Reference currentReference = referencingParentDocument.getDocumentReferenceByLocation(currentNode.getWorkspaceURL().toURI());
            currentReference.setURI(handleParser.prepareHandleWithHdlPrefix(currentNode.getArchiveURI()));
            URI currentUriRelativeToParent = URI.create(currentPathRelativeToParent);
            currentReference.setLocation(currentUriRelativeToParent);
            StreamResult targetParentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(new File(parentNode.getWorkspaceURL().getPath()));
            metadataAPI.writeMetadataDocument(referencingParentDocument, targetParentStreamResult);
            
        } catch (IOException | MetadataException | URISyntaxException | TransformerException ex) {
            String errorMessage = "Error writing file (updating child reference) for node " + parentNode.getWorkspaceURL();
            throwWorkspaceExportException(workspaceID, errorMessage, ex);
        }
    }
    
    private void assignAndUpdateNewHandle(int workspaceID, WorkspaceNode currentNode) throws WorkspaceExportException {
        
        try {
            
            URI targetUri = archiveFileLocationProvider.getUriWithHttpsRoot(currentNode.getArchiveURL().toURI());
            
            URI newNodeArchiveHandle = handleManager.assignNewHandle(new File(currentNode.getWorkspaceURL().getPath()), targetUri);
            currentNode.setArchiveURI(handleParser.prepareHandleWithHdlPrefix(newNodeArchiveHandle));
            workspaceDao.updateNodeArchiveUri(currentNode);
        } catch (URISyntaxException | HandleException | IOException ex) {
            String errorMessage = "Error assigning new handle for node " + currentNode.getWorkspaceURL();
            throwWorkspaceExportException(workspaceID, errorMessage, ex);
        }
    }
    
    private void throwWorkspaceExportException(int workspaceID, String errorMessage, Exception cause) throws WorkspaceExportException {
        logger.error(errorMessage, cause);
        throw new WorkspaceExportException(errorMessage, workspaceID, cause);
    }
}
