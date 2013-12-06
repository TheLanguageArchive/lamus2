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
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.util.Checksum;
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
    
    private MetadataAPI metadataAPI;
    private WorkspaceFileHandler workspaceFileHandler;
    private WorkspaceTreeExporter workspaceTreeExporter;
    private CorpusStructureProvider corpusStructureProvider;
    
    public GeneralNodeExporter(MetadataAPI mAPI, WorkspaceFileHandler wsFileHandler,
            WorkspaceTreeExporter wsTreeExporter,
            CorpusStructureProvider csProvider) {
        
        this.metadataAPI = mAPI;
        this.workspaceFileHandler = wsFileHandler;
        this.workspaceTreeExporter = wsTreeExporter;
        this.corpusStructureProvider = csProvider;
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
        
        if(currentNode.isMetadata()) {
            
            workspaceTreeExporter.explore(workspace, currentNode);
            
            //TODO ensureChecksum - will this be done by the crawler??
            
            CorpusNode corpusNode = null;
            try {
                corpusNode = this.corpusStructureProvider.getNode(currentNode.getArchiveURI());
            } catch (UnknownNodeException ex) {
                String errorMessage = "Node not found in archive database for URI " + currentNode.getArchiveURI();
                throwWorkspaceExportException(errorMessage, ex);
            }
            String archiveChecksum = corpusNode.getFileInfo().getChecksum();
            String workspaceChecksum = Checksum.create(currentNode.getWorkspaceURL().getPath());
            
            //TODO should the checksum be created at some other point (when the node is actually changed, for instance)
                // so it takes less time at this point?
            // node hasn't changed
            if(workspaceChecksum.equals(archiveChecksum)) {
                return;
            }
            
            MetadataDocument nodeDocument = null;
            try {
                nodeDocument = metadataAPI.getMetadataDocument(currentNode.getWorkspaceURL());
            } catch (IOException ex) {
                String errorMessage = "Error getting Metadata Document for node " + currentNode.getArchiveURI();
                throwWorkspaceExportException(errorMessage, ex);
            } catch (MetadataException ex) {
                String errorMessage = "Error getting Metadata Document for node " + currentNode.getArchiveURI();
                throwWorkspaceExportException(errorMessage, ex);
            }

            File nodeWsFile = new File(currentNode.getWorkspaceURL().getPath());
            File nodeArchiveFile = new File(currentNode.getArchiveURL().getPath());
            StreamResult nodeArchiveStreamResult = workspaceFileHandler.getStreamResultForNodeFile(nodeArchiveFile);
            
            try {
                metadataAPI.writeMetadataDocument(nodeDocument, nodeArchiveStreamResult);
            } catch (IOException ex) {
                String errorMessage = "Error writing file for node " + currentNode.getArchiveURI();
                throwWorkspaceExportException(errorMessage, ex);
            } catch (TransformerException ex) {
                String errorMessage = "Error writing file for node " + currentNode.getArchiveURI();
                throwWorkspaceExportException(errorMessage, ex);
            } catch (MetadataException ex) {
                String errorMessage = "Error writing file for node " + currentNode.getArchiveURI();
                throwWorkspaceExportException(errorMessage, ex);
            }            
            
            //TODO CHECK FOR CHANGES IN DB... OR IS IT TO BE DONE BY THE CRAWLER???
                // check if checksum is different
                    // if not, do nothing
                    // if so
        } else {
            
            //TODO resources
                // they were not copied from the archive to the workspace, so should not be copied back...
                // they might need some database update, due to some possible changes in their information...
                    // the file itself shouldn't have changed, otherwise it's a replaced node
        }
        
        //TODO Update node in corpusstructure?
    }
    
    private void throwWorkspaceExportException(String errorMessage, Exception cause) throws WorkspaceExportException {
        logger.error(errorMessage, cause);
        throw new WorkspaceExportException(errorMessage, workspace.getWorkspaceID(), cause);
    }
    
}
