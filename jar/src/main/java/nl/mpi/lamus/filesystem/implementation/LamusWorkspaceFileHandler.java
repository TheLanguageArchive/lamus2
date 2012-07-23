/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.filesystem.implementation;

import java.io.File;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceNodeFileException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceFileHandler implements WorkspaceFileHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceFileHandler.class);
    
    @Autowired
    @Qualifier("workspaceBaseDirectory")
    private File workspaceBaseDirectory;

    public void copyMetadataFileToWorkspace(Workspace workspace, WorkspaceNode workspaceNode,
            MetadataAPI metadataAPI, MetadataDocument metadataDocument, File nodeFile, StreamResult nodeFileStreamResult)
            throws FailedToCreateWorkspaceNodeFileException {
        
        try {
            metadataAPI.writeMetadataDocument(metadataDocument, nodeFileStreamResult);
        } catch(IOException ioex) {
            String errorMessage = "Problem writing file " + nodeFile.getAbsolutePath();
            logger.error(errorMessage, ioex);
            throw new FailedToCreateWorkspaceNodeFileException(errorMessage, workspace, workspaceNode, ioex);
        } catch(TransformerException tex) {
            String errorMessage = "Problem writing file " + nodeFile.getAbsolutePath();
            logger.error(errorMessage, tex);
            throw new FailedToCreateWorkspaceNodeFileException(errorMessage, workspace, workspaceNode, tex);
        } catch(MetadataException mdex) {
            String errorMessage = "Problem writing file " + nodeFile.getAbsolutePath();
            logger.error(errorMessage, mdex);
            throw new FailedToCreateWorkspaceNodeFileException(errorMessage, workspace, workspaceNode, mdex);
        }
    }
    
    public StreamResult getStreamResultForWorkspaceNodeFile(Workspace workspace, WorkspaceNode workspaceNode, File nodeFile) {
            StreamResult streamResult = new StreamResult(nodeFile);
            return streamResult;
    }

    public File getFileForWorkspaceNode(WorkspaceNode workspaceNode) {
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceNode.getWorkspaceID());
        File workspaceNodeFile = new File(workspaceDirectory, "" + workspaceNode.getWorkspaceNodeID());
        return workspaceNodeFile;
    }
}
