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
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HeaderInfo;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for exporting nodes that were newly added
 * and are supposed to get a new location in the archive.
 * @see NodeExporter
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class AddedNodeExporter implements NodeExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(AddedNodeExporter.class);

    private final ArchiveFileLocationProvider archiveFileLocationProvider;
    private final WorkspaceFileHandler workspaceFileHandler;
    private final MetadataAPI metadataAPI;
    private final WorkspaceDao workspaceDao;
    private final WorkspaceTreeExporter workspaceTreeExporter;
    private final HandleManager handleManager;
    private final MetadataApiBridge metadataApiBridge;
    private final CorpusStructureProvider corpusStructureProvider;
    private final NodeResolver nodeResolver;
    
    private Workspace workspace;
    
    public AddedNodeExporter(ArchiveFileLocationProvider aflProvider, WorkspaceFileHandler wsFileHandler,
            MetadataAPI mdAPI, WorkspaceDao wsDao, WorkspaceTreeExporter wsTreeExporter,
            HandleManager handleManager, MetadataApiBridge mdApiBridge,
            CorpusStructureProvider csProvider, NodeResolver resolver) {
        this.archiveFileLocationProvider = aflProvider;
        this.workspaceFileHandler = wsFileHandler;
        this.metadataAPI = mdAPI;
        this.workspaceDao = wsDao;
        this.workspaceTreeExporter = wsTreeExporter;
        this.handleManager = handleManager;
        this.metadataApiBridge = mdApiBridge;
        this.corpusStructureProvider = csProvider;
        this.nodeResolver = resolver;
    }
    
    /**
     * @see NodeExporter#getWorkspace()
     */
    @Override
    public Workspace getWorkspace() {
        return this.workspace;
    }
    
    /**
     * @see NodeExporter#setWorkspace(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * @see NodeExporter#exportNode(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void exportNode(WorkspaceNode parentNode, WorkspaceNode currentNode)
            throws WorkspaceExportException {
        
        if (workspace == null) {
	    String errorMessage = "Workspace not set";
	    logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
	}
        
        logger.debug("Exporting added node to archive; workspaceID: " + workspace.getWorkspaceID() + "; parentNodeID: " + parentNode.getWorkspaceNodeID() + "; currentNodeID: " + currentNode.getWorkspaceNodeID());
        
        File parentArchiveFile = retrieveParentArchiveLocation(parentNode);
        
        String currentNodeFilename = FilenameUtils.getName(currentNode.getWorkspaceURL().getPath());
        
        File nextAvailableFile = retrieveAndUpdateNewArchivePath(currentNode, currentNodeFilename, parentArchiveFile.getAbsolutePath());
        
        if(currentNode.isMetadata()) {
            workspaceTreeExporter.explore(workspace, currentNode);
        }
        
        MetadataDocument currentDocument = retrieveMetadataDocument(currentNode);
        
        File currentNodeWorkspaceFile = new File(currentNode.getWorkspaceURL().getPath());
        
        assignAndUpdateNewHandle(currentNode);
        
        if(currentNode.isMetadata()) {
            updateSelfHandle(currentNode, currentDocument);
        }
        
        moveFileIntoArchive(currentNode, nextAvailableFile, currentDocument, currentNodeWorkspaceFile);
        
        ReferencingMetadataDocument referencingParentDocument = retrieveReferencingMetadataDocument(parentNode);
        
        String currentPathRelativeToParent = archiveFileLocationProvider.getChildPathRelativeToParent(parentArchiveFile, nextAvailableFile);
            
        updateReferenceInParent(currentNode, parentNode, referencingParentDocument, currentPathRelativeToParent);
        
        //TODO is this necessary?
//        if(searchClientBridge.isFormatSearchable(currentNode.getFormat())) {
//            searchClientBridge.addNode(currentNode.getArchiveURI());
//        }
    }
    
    private File retrieveParentArchiveLocation(WorkspaceNode parentNode) throws WorkspaceExportException {
        
        File parentArchiveFile = null;
        CorpusNode parentArchiveNode = corpusStructureProvider.getNode(parentNode.getArchiveURI());
        if(parentArchiveNode != null) {
            parentArchiveFile = nodeResolver.getLocalFile(parentArchiveNode);
        } else {
            try {
                //TODO MUDAR COMO NO OUTRO EXPORTER
                parentArchiveFile = new File(archiveFileLocationProvider.getUriWithLocalRoot(parentNode.getArchiveURL().toURI()));
            } catch (URISyntaxException ex) {
                String errorMessage = "Error retrieving archive location of node " + parentNode.getArchiveURI();
                throwWorkspaceExportException(errorMessage, ex);
            }
        }
        return parentArchiveFile;
    }
    
    private File retrieveAndUpdateNewArchivePath(WorkspaceNode currentNode, String currentNodeFilename, String parentArchivePath) throws WorkspaceExportException {
        
        File nextAvailableFile = null;
        URL newNodeArchiveURL = null;
        
        try {
            nextAvailableFile = archiveFileLocationProvider.getAvailableFile(parentArchivePath, currentNodeFilename, currentNode.getType());
            logger.info("Retrieved new archive file path for added node: " + nextAvailableFile.getAbsolutePath());
            newNodeArchiveURL = nextAvailableFile.toURI().toURL();
        } catch (MalformedURLException ex) {
            String errorMessage = "Error getting new file for node " + currentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        } catch (IOException ex) {
            String errorMessage = "Error getting new file for node " + currentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        }
        currentNode.setArchiveURL(newNodeArchiveURL);
        workspaceDao.updateNodeArchiveUrl(currentNode); //TODO Is it worth it to update this in the database?
        
        return nextAvailableFile;
    }
    
    private MetadataDocument retrieveMetadataDocument(WorkspaceNode node) throws WorkspaceExportException {
        
        MetadataDocument document = null;
        
        if(node.isMetadata()) {
            try {
                document = metadataAPI.getMetadataDocument(node.getWorkspaceURL());
                
            } catch (IOException | MetadataException ex) {
                String errorMessage = "Error getting Metadata Document for node " + node.getWorkspaceURL();
                throwWorkspaceExportException(errorMessage, ex);
            }
        }
        
        return document;
    }
    
    private ReferencingMetadataDocument retrieveReferencingMetadataDocument(WorkspaceNode node) throws WorkspaceExportException {
        
        MetadataDocument document = retrieveMetadataDocument(node);
        ReferencingMetadataDocument referencingParentDocument = null;
        if(document instanceof ReferencingMetadataDocument) {
            referencingParentDocument = (ReferencingMetadataDocument) document;
        } else {
            String errorMessage = "Error retrieving child reference in file " + node.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, null);
        }
        
        return referencingParentDocument;
    }
    
    private void moveFileIntoArchive(WorkspaceNode currentNode, File nextAvailableFile, MetadataDocument currentDocument, File currentNodeWorkspaceFile) throws WorkspaceExportException {
        
        //TODO THIS IS NOT EXACTLY MOVING AT THE MOMENT...
        
        try {
            if(currentNode.isMetadata()) {
                StreamResult targetStreamResult = workspaceFileHandler.getStreamResultForNodeFile(nextAvailableFile);
                metadataAPI.writeMetadataDocument(currentDocument, targetStreamResult);
            } else {
                workspaceFileHandler.copyFile(currentNodeWorkspaceFile, nextAvailableFile);
            }
        } catch (IOException | TransformerException | MetadataException ex) {
            String errorMessage = "Error writing file for node " + currentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        }
    }
    
    private void updateSelfHandle(WorkspaceNode node, MetadataDocument document) throws WorkspaceExportException {
        
        //create self link in header, either if it is already there (will be replaced) or not (will be added)
        try {
            HeaderInfo newInfo = metadataApiBridge.getNewSelfHandleHeaderInfo(handleManager.prepareHandleWithHdlPrefix(node.getArchiveURI()));
            document.putHeaderInformation(newInfo);
        } catch (MetadataException | URISyntaxException ex) {
            String errorMessage = "Error updating header information for node " + node.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        }
    }
    
    private void updateReferenceInParent(
            WorkspaceNode currentNode, WorkspaceNode parentNode,
            ReferencingMetadataDocument referencingParentDocument, String currentPathRelativeToParent) throws WorkspaceExportException {
        
        try {    
            Reference currentReference = referencingParentDocument.getDocumentReferenceByLocation(currentNode.getWorkspaceURL());
            currentReference.setURI(handleManager.prepareHandleWithHdlPrefix(currentNode.getArchiveURI()));
            URL currentUrlRelativeToParent = new URL(parentNode.getArchiveURL(), currentPathRelativeToParent);
            currentReference.setLocation(currentUrlRelativeToParent);
            StreamResult targetParentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(new File(parentNode.getWorkspaceURL().getPath()));
            metadataAPI.writeMetadataDocument(referencingParentDocument, targetParentStreamResult);
            
        } catch (IOException | MetadataException | URISyntaxException | TransformerException ex) {
            String errorMessage = "Error writing file (updating child reference) for node " + parentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        }
    }
    
    private void assignAndUpdateNewHandle(WorkspaceNode currentNode) throws WorkspaceExportException {
        
        try {
            
            URI targetUri = archiveFileLocationProvider.getUriWithHttpsRoot(currentNode.getArchiveURL().toURI());
            
            URI newNodeArchiveHandle = handleManager.assignNewHandle(new File(currentNode.getWorkspaceURL().getPath()), targetUri);
            currentNode.setArchiveURI(handleManager.prepareHandleWithHdlPrefix(newNodeArchiveHandle));
            workspaceDao.updateNodeArchiveUri(currentNode);
        } catch (URISyntaxException | HandleException | IOException ex) {
            String errorMessage = "Error assigning new handle for node " + currentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        }
    }
    
    private void throwWorkspaceExportException(String errorMessage, Exception cause) throws WorkspaceExportException {
        logger.error(errorMessage, cause);
        throw new WorkspaceExportException(errorMessage, workspace.getWorkspaceID(), cause);
    }
}
