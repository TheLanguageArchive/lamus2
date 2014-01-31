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
import java.util.logging.Level;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
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
    private final SearchClientBridge searchClientBridge;
    private final WorkspaceTreeExporter workspaceTreeExporter;
    private final NodeDataRetriever nodeDataRetriever;
    private final CorpusStructureProvider corpusStructureProvider;
    private final NodeResolver nodeResolver;
    
    private Workspace workspace;
    
    public AddedNodeExporter(ArchiveFileLocationProvider aflProvider, WorkspaceFileHandler wsFileHandler,
            MetadataAPI mdAPI, WorkspaceDao wsDao,
            SearchClientBridge scBridge, WorkspaceTreeExporter wsTreeExporter,
            NodeDataRetriever nodeDataRetriever,
            CorpusStructureProvider csProvider, NodeResolver nodeResolver) {
        this.archiveFileLocationProvider = aflProvider;
        this.workspaceFileHandler = wsFileHandler;
        this.metadataAPI = mdAPI;
        this.workspaceDao = wsDao;
        this.searchClientBridge = scBridge;
        this.workspaceTreeExporter = wsTreeExporter;
        this.nodeDataRetriever = nodeDataRetriever;
        this.corpusStructureProvider = csProvider;
        this.nodeResolver = nodeResolver;
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
        
        String parentArchivePath = parentNode.getArchiveURL().getPath();
        String currentNodeFilename = FilenameUtils.getName(currentNode.getWorkspaceURL().getPath());
        
        File nextAvailableFile = null;
        URL newNodeArchiveURL = null;
        try {
            nextAvailableFile = archiveFileLocationProvider.getAvailableFile(parentArchivePath, currentNodeFilename, currentNode.getType());
            newNodeArchiveURL = nextAvailableFile.toURI().toURL();
        } catch (MalformedURLException ex) {
            String errorMessage = "Error getting new file for node " + currentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        } catch (IOException ex) {
            String errorMessage = "Error getting new file for node " + currentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        }
        currentNode.setArchiveURL(newNodeArchiveURL);
        
        //TODO how to generate handles exactly?
        URI newNodeArchiveURI = nodeDataRetriever.getNewArchiveURI();
        currentNode.setArchiveURI(newNodeArchiveURI);
        workspaceDao.updateNodeArchiveUriUrl(currentNode);
        
        if(currentNode.isMetadata()) {
            workspaceTreeExporter.explore(workspace, currentNode);
        }
        
        MetadataDocument currentDocument = null;
        if(currentNode.isMetadata()) {
            try {
                currentDocument = metadataAPI.getMetadataDocument(currentNode.getWorkspaceURL());
                
            } catch (IOException ex) {
                String errorMessage = "Error getting Metadata Document for node " + currentNode.getWorkspaceURL();
                throwWorkspaceExportException(errorMessage, ex);
            } catch (MetadataException ex) {
                String errorMessage = "Error getting Metadata Document for node " + currentNode.getWorkspaceURL();
                throwWorkspaceExportException(errorMessage, ex);
            }
        }
        
        File currentNodeWorkspaceFile = new File(currentNode.getWorkspaceURL().getPath());
        
        try {
            if(currentNode.isMetadata()) {
                StreamResult targetStreamResult = workspaceFileHandler.getStreamResultForNodeFile(nextAvailableFile);
                metadataAPI.writeMetadataDocument(currentDocument, targetStreamResult);
            } else {
                workspaceFileHandler.copyFile(currentNodeWorkspaceFile, nextAvailableFile);
            }
        } catch (IOException ex) {
            String errorMessage = "Error writing file for node " + currentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        } catch (TransformerException ex) {
            String errorMessage = "Error writing file for node " + currentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        } catch (MetadataException ex) {
            String errorMessage = "Error writing file for node " + currentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        }
        
        try {
            //TODO CHANGE REFERENCE IN PARENT
            MetadataDocument parentDocument = metadataAPI.getMetadataDocument(parentNode.getWorkspaceURL());
            ReferencingMetadataDocument referencingParentDocument = null;
            if(parentDocument instanceof ReferencingMetadataDocument) {
                referencingParentDocument = (ReferencingMetadataDocument) parentDocument;
            } else {
                String errorMessage = "Error retrieving child reference in file " + parentNode.getWorkspaceURL();
                throwWorkspaceExportException(errorMessage, null);
            }
            
            Reference currentReference = referencingParentDocument.getDocumentReferenceByURI(currentNode.getWorkspaceURL().toURI());
            currentReference.setURI(newNodeArchiveURL.toURI());
            StreamResult targetParentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(new File(parentNode.getWorkspaceURL().getPath()));
            metadataAPI.writeMetadataDocument(referencingParentDocument, targetParentStreamResult);
            
        } catch (IOException ex) {
            String errorMessage = "Error writing file (updating child reference) for node " + parentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        } catch (MetadataException ex) {
            String errorMessage = "Error writing file (updating child reference) for node " + parentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        } catch (URISyntaxException ex) {
            String errorMessage = "Error writing file (updating child reference) for node " + parentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        } catch (TransformerException ex) {
            String errorMessage = "Error writing file (updating child reference) for node " + parentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        }
        
        
        
        
        //TODO ensureChecksum - will this be done by the crawler??
        
        if(searchClientBridge.isFormatSearchable(currentNode.getFormat())) {
            searchClientBridge.addNode(currentNode.getArchiveURI());
        }
    }
    
    private void throwWorkspaceExportException(String errorMessage, Exception cause) throws WorkspaceExportException {
        logger.error(errorMessage, cause);
        throw new WorkspaceExportException(errorMessage, workspace.getWorkspaceID(), cause);
    }
}
