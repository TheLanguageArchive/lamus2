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
import java.util.logging.Level;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.WorkspaceNodeFilesystemException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author guisil
 */
public class GeneralNodeExporter implements NodeExporter {
    
    private final static Logger logger = LoggerFactory.getLogger(GeneralNodeExporter.class);

    private Workspace workspace;
    
    private MetadataAPI metadataAPI;
    private WorkspaceFileHandler workspaceFileHandler;
    private WorkspaceTreeExporter workspaceTreeExporter;
    
    public GeneralNodeExporter(MetadataAPI mAPI, WorkspaceFileHandler wsFileHandler, WorkspaceTreeExporter wsTreeExporter) {
        
        this.metadataAPI = mAPI;
        this.workspaceFileHandler = wsFileHandler;
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
        
        MetadataDocument nodeDocument = null;
        try {
            nodeDocument = metadataAPI.getMetadataDocument(currentNode.getWorkspaceURL());
        } catch (IOException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        } catch (MetadataException ex) {
            throw new UnsupportedOperationException("exception not handled yet", ex);
        }
        
        File nodeWsFile = new File(currentNode.getWorkspaceURL().getPath());
        File nodeArchiveFile = new File(currentNode.getArchiveURL().getPath());
        StreamResult nodeArchiveStreamResult = workspaceFileHandler.getStreamResultForNodeFile(nodeArchiveFile);
        
        if(WorkspaceNodeType.METADATA.equals(currentNode.getType())) {
        
            try {
                workspaceFileHandler.copyMetadataFile(currentNode, metadataAPI, nodeDocument, nodeWsFile, nodeArchiveStreamResult);
            } catch (WorkspaceNodeFilesystemException ex) {
                throw new UnsupportedOperationException("exception not handled yet", ex);
            }
        
            workspaceTreeExporter.explore(workspace, currentNode);
        } else {
            
            //TODO resources
        }
    }
    
}
