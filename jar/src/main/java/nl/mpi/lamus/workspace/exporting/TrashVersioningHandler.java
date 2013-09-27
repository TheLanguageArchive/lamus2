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
package nl.mpi.lamus.workspace.exporting;

import java.io.File;
import java.net.URI;
import java.net.URL;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Provides methods related with the trash can folder,
 * more specifically with versioning of deleted files.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface TrashVersioningHandler {
    
    /**
     * Retires the given node, as an old version.
     * 
     * @param node Node to be retired
     * @return true if everything went well
     */
//    public boolean retireNodeVersion(WorkspaceNode node);
    
    /**
     * Determines the directory where the versioned node should be located.
     * 
     * @param workspaceID ID of the workspace where the node belongs
     * @return File object corresponding to the directory for the workspace's versioned files
     */
    public File getDirectoryForNodeVersion(int workspaceID);
    
    /**
     * Renames the file for the standard naming of versioned nodes.
     * 
     * @param baseDirectory Base directory where the file will be located
     * @param archiveNodeURI URI of the node to be versioned
     * @param archiveNodeURL URL of the node to be versioned
     * @return File object corresponding to the renamed version file
     */
    public File getTargetFileForNodeVersion(File baseDirectory, URI archiveNodeURI, URL archiveNodeURL);
    
    /**
     * Checks if the target directory is writable.
     * 
     * @param targetDirectory Target directory for the versioned file
     * @return true if the directory is writable
     */
    public boolean canWriteTargetDirectory(File targetDirectory);
    
    /**
     * Moves the versioned file to the target directory.
     * 
     * @param currentFile File to be moved
     * @param targetFile target location
     * @return true if the file was successfully moved
     */
    public boolean moveFileToTargetLocation(File currentFile, File targetFile);
}
