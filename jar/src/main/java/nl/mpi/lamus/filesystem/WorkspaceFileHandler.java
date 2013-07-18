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
package nl.mpi.lamus.filesystem;

import java.io.File;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.workspace.exception.WorkspaceNodeFilesystemException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.model.MetadataDocument;

/**
 * Handler for operations related with the lamus files.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceFileHandler {
    
    /**
     * Copies the given metadata file into the workspace directory.
     * 
     * @param workspace workspace where the file should copied to
     * @param workspaceNode node corresponding to the file
     * @param metadataAPI instance of the MetadataAPI
     * @param metadataDocument MetadataDocument corresponding to the file
     * @param originNodeFile File object corresponding to the file
     * @param targetNodeFileStreamResult StreamResult object corresponding to the file
     * @throws FailedToCreateWorkspaceNodeFileException if there is some problem with the file copy
     */
    
    //TODO REVIEW THESE METHODS
    
    
    public void copyMetadataFile(WorkspaceNode workspaceNode,
            MetadataAPI metadataAPI, MetadataDocument metadataDocument, File originNodeFile,
            StreamResult targetNodeFileStreamResult)
            throws WorkspaceNodeFilesystemException;
    
    public void copyResourceFile(WorkspaceNode workspaceNode,
            File originNodeFile, File targetNodeFile)
            throws WorkspaceNodeFilesystemException;
    
    /**
     * Gets a StreamResult object based on the given file.
     * 
     * @param nodeFile file with which to construct the StreamResult
     * @return StreamResult object
     */
    public StreamResult getStreamResultForNodeFile(File nodeFile);
    
    /**
     * Construct a File object corresponding to the given node.
     * 
     * @param workspaceNode node with which to construct the File object.
     * @return File object
     */
    public File getFileForImportedWorkspaceNode(WorkspaceNode workspaceNode);
}
