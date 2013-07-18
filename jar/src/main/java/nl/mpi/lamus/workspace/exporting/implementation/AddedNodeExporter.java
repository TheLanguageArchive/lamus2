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
import javax.xml.transform.stream.StreamResult;
import nl.mpi.corpusstructure.AccessInfo;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.WorkspaceNodeFilesystemException;
import nl.mpi.lamus.workspace.exporting.CorpusStructureBridge;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.model.ResourceReference;
import nl.mpi.metadata.cmdi.api.model.CMDIDocument;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for exporting nodes that were newly added
 * and are supposed to get a new location in the archive.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class AddedNodeExporter implements NodeExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(AddedNodeExporter.class);

    private final ArchiveFileLocationProvider archiveFileLocationProvider;
    private final WorkspaceFileHandler workspaceFileHandler;
    private final MetadataAPI metadataAPI;
    private final CorpusStructureBridge corpusStructureBridge;
    private final WorkspaceDao workspaceDao;
    private final SearchClientBridge searchClientBridge;
    private final WorkspaceTreeExporter workspaceTreeExporter;
    
    private Workspace workspace;
    
    public AddedNodeExporter(ArchiveFileLocationProvider aflProvider, WorkspaceFileHandler wsFileHandler,
            MetadataAPI mdAPI, CorpusStructureBridge csBridge, WorkspaceDao wsDao,
            SearchClientBridge scBridge, WorkspaceTreeExporter wsTreeExporter) {
        this.archiveFileLocationProvider = aflProvider;
        this.workspaceFileHandler = wsFileHandler;
        this.metadataAPI = mdAPI;
        this.corpusStructureBridge = csBridge;
        this.workspaceDao = wsDao;
        this.searchClientBridge = scBridge;
        this.workspaceTreeExporter = wsTreeExporter;
    }
    
    @Override
    public Workspace getWorkspace() {
        return this.workspace;
    }
    
    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public void exportNode(WorkspaceNode parentNode, WorkspaceNode currentNode) {
        
        String parentArchivePath = parentNode.getArchiveURL().getPath();
        String currentNodeFilename = FilenameUtils.getName(currentNode.getWorkspaceURL().getPath());
        
        File nextAvailableFile = null;
        URL newNodeArchiveURL = null;
        
        try {
            nextAvailableFile = archiveFileLocationProvider.getAvailableFile(parentArchivePath, currentNodeFilename, currentNode.getType());
            newNodeArchiveURL = nextAvailableFile.toURI().toURL();
        } catch (MalformedURLException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        } catch (IOException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        }
        currentNode.setArchiveURL(newNodeArchiveURL);
        workspaceDao.updateNodeArchiveURL(currentNode);
        
        //TODO WHEN METADATA, CALL (RECURSIVELY) exploreTree FOR CHILDREN IN THE BEGINNING
            // this way child files would have the pids calculated in advance,
                // so the references in the parent can be set before the files are copied to their archive location
        if(currentNode.isMetadata()) {
            workspaceTreeExporter.explore(workspace, currentNode);
        }
        
        int currentNodeNewArchiveID = corpusStructureBridge.addNewNodeToCorpusStructure(
                    newNodeArchiveURL, currentNode.getPid(), workspace.getUserID());
            
            //TODO PID should have already been assigned when the node was uploaded/linked...
        
        MetadataDocument currentDocument = null;
        if(currentNode.isMetadata()) {
            try {
                currentDocument = metadataAPI.getMetadataDocument(currentNode.getWorkspaceURL());
                
            } catch (IOException ex) {
                throw new UnsupportedOperationException("exception not handled yet", ex);
            } catch (MetadataException ex) {
                throw new UnsupportedOperationException("exception not handled yet", ex);
            }
        }
        
        File currentNodeWorkspaceFile = new File(currentNode.getWorkspaceURL().getPath());
        
        try {
            if(currentNode.isMetadata()) {
                StreamResult targetStreamResult = workspaceFileHandler.getStreamResultForNodeFile(nextAvailableFile);
                workspaceFileHandler.copyMetadataFile(currentNode, metadataAPI, currentDocument, currentNodeWorkspaceFile, targetStreamResult);
            } else {
                workspaceFileHandler.copyResourceFile(currentNode, currentNodeWorkspaceFile, nextAvailableFile);
            }
        } catch (WorkspaceNodeFilesystemException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        }
        
        try {
            //TODO will this be done by the crawler??
            corpusStructureBridge.ensureChecksum(currentNodeNewArchiveID, nextAvailableFile.toURI().toURL());
        } catch (MalformedURLException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        }
        
        if(searchClientBridge.isFormatSearchable(currentNode.getFormat())) {
            searchClientBridge.addNode(currentNodeNewArchiveID);
        }// else {
           // throw new UnsupportedOperationException("AddedNodeExporter.exportNode (when currentNode is not searchable by SearchClient) not implemented yet");
        //}
        
    }
}
