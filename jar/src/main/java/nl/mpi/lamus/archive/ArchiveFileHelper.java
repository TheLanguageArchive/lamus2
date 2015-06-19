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
package nl.mpi.lamus.archive;

import java.io.File;
import java.io.IOException;
import nl.mpi.archiving.corpusstructure.core.FileInfo;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.util.OurURL;

/**
 * Contains some helper methods regarding some archive operations.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface ArchiveFileHelper {
    
//    public int lastSlashPos(String fullname);
    
    /**
     * 
     * @param fullname
     * @return the part of fullname after the last slash
     */
    public String getFileBasename(String fullname);
    
//    public String getFileSystemId(String fullname);
    
    public String getFileBasenameWithoutExtension(String fullname);
    
    /**
     * 
     * @param fullname
     * @return the part of fullname after but excluding the last slash,
     * or if the file name ends in a slash, derive a name from the full
     * name. That usually clips the name to the part up to the first
     * slash, after removing the protocol if the name looks like an URL.
     * In other words, if the name is an URL, it returns the domain name
     * of that URL if no file name is visible in the URL.
     */
    public String getFileTitle(String fullname);
    
    /**
     * replaces all special characters from the given String substitutes them to "_"
     * it substitutes also the "/" - so use it only for parts for file- or directory
     * names not whole paths. Note that this might clip strings to a maximum length,
     * but will try to preserve file name extensions up to a reasonable length then.
     * @param pathElement the original String
     * @param reason describes the nature of the to be corrected element, for logging.
     * @return the String with substituted special characters
     */
    public String correctPathElement(String pathElement, String reason);
    
    /**
     * Checks if the given file's size is above the size limit of the typechecker
     * @param fileToCheck File to be checked
     * @return true if the file is larger than the typechecker size limit
     */
    public boolean isFileSizeAboveTypeReCheckSizeLimit(File fileToCheck);
    
    /**
     * Checks if the given URL is our internal archive
     * @param urlToCheck URL to be checked
     * @return true if URL is local
     */
    public boolean isUrlLocal(OurURL urlToCheck);
    
    /**
     * Adjusts the given filename, if necessary, depending on the files already
     * existing in the directory, so it can be created without conflicts
     * @param baseDirectory Directory where the file is supposed to be created
     * @param fileNameAttempt Attempted filename, which can be adjusted if there are conflicts
     * @return File object for the adjusted (or not) filename
     */
    public File getFinalFile(File baseDirectory, String fileNameAttempt);
    
    /**
     * Creates the file and parent directories, if needed, for the given File object
     * @param fileToCreate File to be created
     * @throws IOException if there are problems with the creation of the file or directories
     */
    public void createFileAndDirectories(File fileToCreate) throws IOException;
    
    /**
     * Determines the path for a certain node, based on the path of the
     * parent node, as well as the node type and/or profile.
     * @param parentPath Path of the parent node
     * @param node WorkspaceNode object for the node
     * @return Directory where the node should be located
     */
    public String getDirectoryForNode(String parentPath, WorkspaceNode node);
    
    /**
     * Given the FileInfo of an archive node and the location of the corresponding
     * file in the workspace, checks if there were any changes in the file.
     * @param archiveFileInfo FileInfo of the archive node to be checked
     * @param workspaceFile Path of the file in the workspace
     * @return true if the file has changed
     */
    public boolean hasArchiveFileChanged(FileInfo archiveFileInfo, File workspaceFile);
    
    /**
     * Retrieves the directory where a replaced node in the given workspace
     * should be located.
     * @param workspaceID
     * @return Directory for the replaced node
     */
    public File getDirectoryForReplacedNode(int workspaceID);
    
    /**
     * Retrieves the directory where a deleted node in the given workspace
     * should be located.
     * @param workspaceID
     * @return Directory for the deleted node
     */
    public File getDirectoryForDeletedNode(int workspaceID);
    
    /**
     * Retrieves the file location for the given base directory and path.
     * @param baseDirectory
     * @param archiveNodeUriStr
     * @param archiveLocalFile
     * @return target file
     */
    public File getTargetFileForReplacedOrDeletedNode(File baseDirectory, String archiveNodeUriStr, File archiveLocalFile);
    
    /**
     * Checks if target directory can be written to.
     * If it doesn't exist, an attempt is made to create the directory (and and its ancestors, if needed).
     * @param targetDirectory
     * @return true if target directory can be written to
     */
    public boolean canWriteTargetDirectory(File targetDirectory);
}
