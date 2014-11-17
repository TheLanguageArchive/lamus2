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
import java.net.URL;
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
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for exporting nodes that are not applicable
 * to the other, more specific exporters.
 * @see NodeExporter
 * 
 * @author guisil
 */
public class GeneralNodeExporter implements NodeExporter {
    
    private final static Logger logger = LoggerFactory.getLogger(GeneralNodeExporter.class);

    private Workspace workspace;
    
    private final MetadataAPI metadataAPI;
    private final WorkspaceFileHandler workspaceFileHandler;
    private final WorkspaceTreeExporter workspaceTreeExporter;
    private final CorpusStructureProvider corpusStructureProvider;
    private final NodeResolver nodeResolver;
    private final ArchiveFileLocationProvider archiveFileLocationProvider;
    
    public GeneralNodeExporter(MetadataAPI mAPI, WorkspaceFileHandler wsFileHandler,
            WorkspaceTreeExporter wsTreeExporter,
            CorpusStructureProvider csProvider, NodeResolver nodeResolver,
            ArchiveFileLocationProvider archiveFileLocationProvider) {
        
        this.metadataAPI = mAPI;
        this.workspaceFileHandler = wsFileHandler;
        this.workspaceTreeExporter = wsTreeExporter;
        this.corpusStructureProvider = csProvider;
        this.nodeResolver = nodeResolver;
        this.archiveFileLocationProvider = archiveFileLocationProvider;
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
        
        logger.debug("Exporting previously existing node to archive; workspaceID: " + workspace.getWorkspaceID() + "; parentNodeID: "
                + (parentNode != null ? parentNode.getWorkspaceNodeID() : -1) + "; currentNodeID: " + currentNode.getWorkspaceNodeID());
        
        if(currentNode.isProtected()) { // a protected node should remain intact after the workspace submission
            logger.info("Node " + currentNode.getWorkspaceNodeID() + " is protected; skipping export of this node to keep it intact in the archive");
            return;
        }
        
        URI parentArchiveLocalUri = null;
//        String nodeArchivePath = null;
//        try {
//            nodeArchivePath = archiveFileLocationProvider.getUriWithLocalRoot(currentNode.getArchiveURL().toURI()).getSchemeSpecificPart();
            
            CorpusNode corpusNode = this.corpusStructureProvider.getNode(currentNode.getArchiveURI());
            if(corpusNode == null) {
                String errorMessage = "Node not found in archive database for URI " + currentNode.getArchiveURI();
                throwWorkspaceExportException(errorMessage, null);
        }
            
            File nodeArchiveFile = nodeResolver.getLocalFile(corpusNode);
            
            
//        } catch (URISyntaxException ex) {
//            String errorMessage = "Error retrieving archive location of node " + currentNode.getArchiveURI();
//            throwWorkspaceExportException(errorMessage, ex);
//        }
        
        if(currentNode.isMetadata()) {
            
            workspaceTreeExporter.explore(workspace, currentNode);
            
            //TODO ensureChecksum - will this be done by the crawler??
            
//            CorpusNode corpusNode = this.corpusStructureProvider.getNode(currentNode.getArchiveURI());
//            if(corpusNode == null) {
//                String errorMessage = "Node not found in archive database for URI " + currentNode.getArchiveURI();
//                throwWorkspaceExportException(errorMessage, null);
//            }
            
//            String archiveChecksum = corpusNode.getFileInfo().getChecksum();
//            String workspaceChecksum = Checksum.create(currentNode.getWorkspaceURL().getPath());
            
            //TODO should the checksum be created at some other point (when the node is actually changed, for instance)
                // so it takes less time at this point?
            // node hasn't changed
            
            // It's possible that a metadata document was changed just to adjust the reference to the workspace URL, for instance
                // and not necessarily because of a real change
            //TODO Maybe there should be a better way of comparing the files
            
//            if(workspaceChecksum.equals(archiveChecksum)) {
            
            //ASSUME THAT METADATA ALWAYS CHANGES (due to the localURI attribute being edited during the import)
                // SO THIS CANNOT BE CHECKED WITH FILESIZE OR CHECKSUM
//            if(!archiveFileHelper.hasArchiveFileChanged(corpusNode.getFileInfo(), new File(currentNode.getWorkspaceURL().getPath()))) {
//                return;
//            }
            
            MetadataDocument nodeDocument = null;
            try {
                nodeDocument = metadataAPI.getMetadataDocument(currentNode.getWorkspaceURL());
            } catch (IOException | MetadataException ex) {
                String errorMessage = "Error getting Metadata Document for node " + currentNode.getArchiveURI();
                throwWorkspaceExportException(errorMessage, ex);
            }

//            File nodeWsFile = new File(currentNode.getWorkspaceURL().getPath());
//            File nodeArchiveFile = new File(currentNode.getArchiveURL().getPath());
//            File nodeArchiveFile = new File(nodeArchivePath);
            StreamResult nodeArchiveStreamResult = workspaceFileHandler.getStreamResultForNodeFile(nodeArchiveFile);
            
            try {
                metadataAPI.writeMetadataDocument(nodeDocument, nodeArchiveStreamResult);
            } catch (IOException | TransformerException | MetadataException ex) {
                String errorMessage = "Error writing file for node " + currentNode.getArchiveURI();
                throwWorkspaceExportException(errorMessage, ex);
            }           
            
            if(parentNode == null) { // top node
                return;
            }
            
        } else {
            
            //TODO resources
                // they were not copied from the archive to the workspace, so should not be copied back...
                // they might need some database update, due to some possible changes in their information...
                    // the file itself shouldn't have changed, otherwise it's a replaced node
        }
        
//        try {
//            parentArchiveLocalUri = archiveFileLocationProvider.getUriWithLocalRoot(parentNode.getArchiveURL().toURI());
//        } catch (URISyntaxException ex) {
//            String errorMessage = "Error retrieving archive location of node " + parentNode.getArchiveURI();
//            throwWorkspaceExportException(errorMessage, ex);
//        }
        
        CorpusNode parentCorpusNode = this.corpusStructureProvider.getNode(parentNode.getArchiveURI());
        if(parentCorpusNode == null) {
            String errorMessage = "Parent node not found in archive database for URI " + parentNode.getArchiveURI();
            throwWorkspaceExportException(errorMessage, null);
        }
        File parentNodeArchiveFile = nodeResolver.getLocalFile(parentCorpusNode);
        
        
        ReferencingMetadataDocument referencingParentDocument = retrieveReferencingMetadataDocument(parentNode);
        String currentPathRelativeToParent = 
                archiveFileLocationProvider.getChildPathRelativeToParent(parentNodeArchiveFile, nodeArchiveFile);
        
        updateReferenceInParent(currentNode, parentNode, parentNodeArchiveFile, referencingParentDocument, currentPathRelativeToParent);
        
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
    
    private void updateReferenceInParent(
            WorkspaceNode currentNode, WorkspaceNode parentNode, File parentArchiveLocalFile,
            ReferencingMetadataDocument referencingParentDocument, String currentPathRelativeToParent) throws WorkspaceExportException {
        
        try {
            Reference currentReference = referencingParentDocument.getDocumentReferenceByURI(currentNode.getArchiveURI());
            URL currentUrlRelativeToParent = new URL(parentArchiveLocalFile.toURI().toURL(), currentPathRelativeToParent);
            currentReference.setLocation(currentUrlRelativeToParent);
            StreamResult targetParentStreamResult = workspaceFileHandler.getStreamResultForNodeFile(new File(parentNode.getWorkspaceURL().getPath()));
            metadataAPI.writeMetadataDocument(referencingParentDocument, targetParentStreamResult);
            
        } catch (IOException | MetadataException | TransformerException ex) {
            String errorMessage = "Error writing file (updating child reference) for node " + parentNode.getWorkspaceURL();
            throwWorkspaceExportException(errorMessage, ex);
        }
    }
    
    private void throwWorkspaceExportException(String errorMessage, Exception cause) throws WorkspaceExportException {
        logger.error(errorMessage, cause);
        throw new WorkspaceExportException(errorMessage, workspace.getWorkspaceID(), cause);
    }
    
}
