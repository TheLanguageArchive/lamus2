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
package nl.mpi.lamus.archive;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Interface providing methods that deal with the location of files in the archive.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface ArchiveFileLocationProvider {
    
    /**
     * Provides an available location for the given node,
     * adjusting the attempted filename if necessary.
     * This method will also trigger the creation of the necessary folders and
     * an empty file in the correct location, so it becomes unavailable for other nodes.
     * @param parentNodePath Path where the parent node is located
     * @param parentCorpusNamePathToClosestTopNode Name of the parent node, to be used possibly as a folder name
     * @param node WorkspaceNode object for the node
     * @param filenameAttempt Attempted filename for the node
     * @return File object corresponding to the available location
     * @throws IOException when there are problems in the creation of file or folders
     */
    public File getAvailableFile(String parentNodePath, String parentCorpusNamePathToClosestTopNode, WorkspaceNode node, String filenameAttempt) throws IOException;
    
    /**
     * Given a parent and a child path, gets the relative path
     * from the parent directory to the child file.
     * 
     * @param parentNodeFile object corresponding to the parent file
     * @param childNodeFile object corresponding to the child file
     * @return relative path between the parent directory and the child file
     */
    public String getChildPathRelativeToParent(File parentNodeFile, File childNodeFile);
    
    /**
     * Retrieves the given URI with the archive HTTPS Root prefix (if the URI
     * starts with the local Root prefix, it is replaced, otherwise the URI
     * is not changed).
     * 
     * @param location
     * @return URI with the archive HTTPS Root prefix
     */
    public URI getUriWithHttpsRoot(URI location) throws URISyntaxException;
    
    /**
     * Retrieves the given URI with the archive Local Root prefix (if the URI
     * starts with the HTTP Root prefix, it is replaced, otherwise the URI
     * is not changed).
     * 
     * @param location
     * @return URI with the archive local Root prefix
     */
    public URI getUriWithLocalRoot(URI location) throws URISyntaxException;
    
    /**
     * finds the physical place of a directory in the archive
     * where orphans (unlinked files) are/should be stored
     * @param topNodeURL the archive-URL of the topNode of the workspace
     * @return the directory where orphans are/should be stored (when it doesn't exist it is not created)
     */
    public File getOrphansDirectory(URI topNodeLocation);
    
    /**
     * Checks if the given file is located in the orphans directory
     * @param fileToCheck File object to be checked
     * @return true if file is located in the orphans directory
     */
    public boolean isFileInOrphansDirectory(File fileToCheck);
}
