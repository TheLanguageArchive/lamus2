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

import java.io.*;
import javax.xml.transform.TransformerException;
import nl.mpi.lamus.configuration.Configuration;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceNodeFileException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceFileHandler implements WorkspaceFileHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceFileHandler.class);
    
    private final Configuration configuration;
    
    @Autowired
    LamusWorkspaceFileHandler(Configuration configuration) {
        this.configuration = configuration;
    }

    public void copyMetadataFileToWorkspace(Workspace workspace, WorkspaceNode workspaceNode,
            MetadataAPI metadataAPI, MetadataDocument metadataDocument, OutputStream nodeFileOutputStream)
            throws FailedToCreateWorkspaceNodeFileException {
        
        File workspaceNodeFile = getFileForWorkspaceNode(workspaceNode);
        
        try {
            metadataAPI.writeMetadataDocument(metadataDocument, nodeFileOutputStream);
        } catch(IOException ioex) {
            String errorMessage = "Problem writing file " + workspaceNodeFile.getAbsolutePath();
            logger.error(errorMessage, ioex);
            throw new FailedToCreateWorkspaceNodeFileException(errorMessage, workspace, workspaceNode, ioex);
        } catch(TransformerException tex) {
            String errorMessage = "Problem writing file " + workspaceNodeFile.getAbsolutePath();
            logger.error(errorMessage, tex);
            throw new FailedToCreateWorkspaceNodeFileException(errorMessage, workspace, workspaceNode, tex);
        } catch(MetadataException mdex) {
            String errorMessage = "Problem writing file " + workspaceNodeFile.getAbsolutePath();
            logger.error(errorMessage, mdex);
            throw new FailedToCreateWorkspaceNodeFileException(errorMessage, workspace, workspaceNode, mdex);
        } finally {
            IOUtils.closeQuietly(nodeFileOutputStream);
        }
    }
    
    public OutputStream getOutputStreamForWorkspaceNodeFile(Workspace workspace, WorkspaceNode workspaceNode, File nodeFile)
            throws FailedToCreateWorkspaceNodeFileException {

        try {
            OutputStream outputStream = new FileOutputStream(nodeFile);
            return outputStream;
        } catch(FileNotFoundException fnfex) {
            String errorMessage = "Problem with file " + nodeFile.getAbsolutePath();
            logger.error(errorMessage, fnfex);
            throw new FailedToCreateWorkspaceNodeFileException(errorMessage, workspace, workspaceNode, fnfex);
        }
    }

    public File getFileForWorkspaceNode(WorkspaceNode workspaceNode) {
        File workspaceBaseDirectory = configuration.getWorkspaceBaseDirectory();
        File workspaceNodeFile = new File(workspaceBaseDirectory, "" + workspaceNode.getWorkspaceNodeID());
        return workspaceNodeFile;
    }
}
