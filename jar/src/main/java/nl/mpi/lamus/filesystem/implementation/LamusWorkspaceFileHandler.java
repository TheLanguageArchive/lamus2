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
import java.util.logging.Level;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.WorkspaceNodeFilesystemException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceFileHandler
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceFileHandler implements WorkspaceFileHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceFileHandler.class);
    
    @Autowired
    @Qualifier("workspaceBaseDirectory")
    private File workspaceBaseDirectory;

    /**
     * @see WorkspaceFileHandler#copyMetadataFileToWorkspace(nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.metadata.api.MetadataAPI, nl.mpi.metadata.api.model.MetadataDocument, java.io.File, javax.xml.transform.stream.StreamResult)
     */
    @Override
    public void copyMetadataFile(WorkspaceNode workspaceNode,
            MetadataAPI metadataAPI, MetadataDocument metadataDocument, File originNodeFile, StreamResult targetNodeFileStreamResult)
            throws WorkspaceNodeFilesystemException {
        
        try {
            metadataAPI.writeMetadataDocument(metadataDocument, targetNodeFileStreamResult);
        } catch(IOException ioex) {
            String errorMessage = "Problem writing file " + originNodeFile.getAbsolutePath();
            logger.error(errorMessage, ioex);
            throw new WorkspaceNodeFilesystemException(errorMessage, workspaceNode, ioex);
        } catch(TransformerException tex) {
            String errorMessage = "Problem writing file " + originNodeFile.getAbsolutePath();
            logger.error(errorMessage, tex);
            throw new WorkspaceNodeFilesystemException(errorMessage, workspaceNode, tex);
        } catch(MetadataException mdex) {
            String errorMessage = "Problem writing file " + originNodeFile.getAbsolutePath();
            logger.error(errorMessage, mdex);
            throw new WorkspaceNodeFilesystemException(errorMessage, workspaceNode, mdex);
        }
    }
    
    @Override
    public void copyResourceFile(WorkspaceNode workspaceNode,
            File originNodeFile, File targetNodeFile) throws WorkspaceNodeFilesystemException {
        try {
            FileUtils.copyFile(originNodeFile, targetNodeFile);
        } catch (IOException ioex) {
            String errorMessage = "Problem writing file " + originNodeFile.getAbsolutePath();
            logger.error(errorMessage, ioex);
            throw new WorkspaceNodeFilesystemException(errorMessage, workspaceNode, ioex);
        }
    }
    
    /**
     * @see WorkspaceFileHandler#getStreamResultForWorkspaceNodeFile(java.io.File)
     */
    @Override
    public StreamResult getStreamResultForNodeFile(File nodeFile) {
            StreamResult streamResult = new StreamResult(nodeFile);
            return streamResult;
    }

    /**
     * @see WorkspaceFileHandler#getFileForImportedWorkspaceNode(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public File getFileForImportedWorkspaceNode(WorkspaceNode workspaceNode) {
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceNode.getWorkspaceID());
        String nodeFilename = FilenameUtils.getName(workspaceNode.getArchiveURL().toString());
        File workspaceNodeFile = new File(workspaceDirectory, nodeFilename);
        return workspaceNodeFile;
    }
}
