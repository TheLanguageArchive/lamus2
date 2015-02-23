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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Handler for operations related with the lamus files.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceFileHandler {

    /**
     * Copies the given resource file into the given location.
     * If a file already exists in the target location, it will be replaced.
     * 
     * @param originNodeFile File object corresponding to the file origin
     * @param targetNodeFile File object corresponding to the target file
     */
    public void copyFile(File originNodeFile, File targetNodeFile)
                throws IOException;
    
    /**
     * Moves the given file into the given location.
     * If a file already exists in the target location, it will be replaced.
     * 
     * @param originNodeFile
     * @param targetNodeFile 
     */
    public void moveFile(File originNodeFile, File targetNodeFile)
            throws IOException;
    
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
     * @param archiveFile archive file from which to retrieve the filename
     * @param workspaceNode node with which to construct the File object
     * @return File object
     */
    public File getFileForImportedWorkspaceNode(File archiveFile, WorkspaceNode workspaceNode);
    
    /**
     * Copies given input stream to the given file location
     * @param inputStream input stream to copy
     * @param targetFile File object corresponding to the target location
     */
    public void copyInputStreamToTargetFile(InputStream inputStream, File targetFile)
            throws IOException;
    
    /**
     * Gets the files from the orphan directory corresponding to the given workspace.
     * @param workspace
     * @return Collection of orphan files
     */
    public Collection<File> getFilesInOrphanDirectory(Workspace workspace);
}
