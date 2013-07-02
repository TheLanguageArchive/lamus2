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
import java.net.URISyntaxException;
import java.util.logging.Level;
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
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        
        // node
        // virtpaths
        // subnodes
        // keep directory structure?
        
        
        // for each subnode
        
            // metadata?
                // get virt path...?
            // else if deleted or unknown type
                // ...

            // external?
                // ...

            // keepdirstruct && fromarchive && ! wasunlinked ? (i.e. already existed in the archive and - do not rename already archived files / directories...' (allow resource basename rename, though))

            // metadata?
                // recursion
            // else
                // change name (and url) if old and new name are different and, if so
                    // get resource file for url
                    // move file to new location
                    // setWsDbArchiveUrl...
                    // wsdb setnodenametitle...
                    // continue loop
        
            // corpus or catalogue?
                // not in archive before?
                    // calculate url based on parent, if possible
                    // setWsDbArchiveUrl...
                // else
                    // ...
            // else session?
                // not in archive before and protocol is file (?)
                    // oldsessionfile (?)
                    // get next session name available
                    // if ?
                        // if ?
                        // eventually move file and updata wsdb...
        
        
        
        
        String parentArchivePath = parentNode.getArchiveURL().getPath();
        String currentNodeFilename = FilenameUtils.getName(currentNode.getWorkspaceURL().getPath());
        
        File nextAvailableFile = null;
        
        try {
            nextAvailableFile = archiveFileLocationProvider.getAvailableFile(parentArchivePath, currentNodeFilename);
        } catch (IOException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        }
        
        //TODO WHEN METADATA, CALL (RECURSIVELY) exploreTree FOR CHILDREN IN THE BEGINNING
            // this way child files would have the pids calculated in advance,
                // so the references in the parent can be set before the files are copied to their archive location
        if(currentNode.isMetadata()) {
            workspaceTreeExporter.explore(workspace, currentNode);
        }
        
        int currentNodeNewArchiveID = -1;
        AccessInfo currentNodeAccessRights = corpusStructureBridge.getDefaultAccessInfoForUser(workspace.getUserID());
        try {
            currentNodeNewArchiveID = corpusStructureBridge.addNewNodeToCorpusStructure(nextAvailableFile.toURI().toURL(), currentNodeAccessRights); //TODO IMPLEMENT METHOD AND ADD PARAMETERS
            //TODO use return value
        } catch (MalformedURLException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        }
        
        String currentNodeNewArchivePID = corpusStructureBridge.calculatePID(currentNodeNewArchiveID);
        corpusStructureBridge.updateArchiveObjectsNodePID(currentNodeNewArchiveID, currentNodeNewArchivePID);
        
        CMDIDocument parentDocument = null;
        
        try {
            
//            MetadataDocument tempDocument = metadataAPI.getMetadataDocument(parentNode.getArchiveURL());
            MetadataDocument tempDocument = metadataAPI.getMetadataDocument(parentNode.getWorkspaceURL());
            if(tempDocument instanceof CMDIDocument) {
                parentDocument = (CMDIDocument) tempDocument;
            } else {
                throw new UnsupportedOperationException("not handled yet");
            }
        } catch (IOException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        } catch (MetadataException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        }
        
        ResourceProxy currentNodeReference = null;
        
        if(currentNode.isMetadata()) {
            throw new UnsupportedOperationException("not implemented yet");
        } else {
            try {
                currentNodeReference = parentDocument.getDocumentReferenceByURI(currentNode.getWorkspaceURL().toURI());
            } catch (URISyntaxException ex) {
                throw new UnsupportedOperationException("exception not handled yet", ex);
            }
        }
        
        currentNodeReference.setHandle(currentNodeNewArchivePID);
        
        
//        File currentNodeOriginFile = new File(currentNode.getOriginURL().getPath());
        File currentNodeWorkspaceFile = new File(currentNode.getWorkspaceURL().getPath());
        
        if(currentNode.isMetadata()) {
            
        } else {
            try {
                workspaceFileHandler.copyResourceFile(currentNode, currentNodeWorkspaceFile, nextAvailableFile);
            } catch (WorkspaceNodeFilesystemException ex) {
                throw new UnsupportedOperationException("exception not handler yet", ex);
            }
        }
        
        //TODO will this be done by the crawler??
//        corpusStructureBridge.linkNodesInCorpusStructure(parentNode.getArchiveNodeID(), currentNodeNewArchiveID);
        
        corpusStructureBridge.ensureChecksum(currentNodeNewArchiveID, currentNode.getArchiveURL());
        
        if(searchClientBridge.isFormatSearchable(currentNode.getFormat())) {
            searchClientBridge.addNode(currentNodeNewArchiveID);
        } else {
            throw new UnsupportedOperationException("not implemented yet");
        }
        
    }
}
