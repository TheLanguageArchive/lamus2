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
import java.net.URI;
import java.net.URL;
import nl.mpi.archiving.corpusstructure.core.FileInfo;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
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
     * finds the physical place of a directory in the archive
     * where orphans (unlinked files) are/should be stored
     * @param topNodeURL the archive-URL of the topNode of the workspace
     * @return the directory where orphans are/should be stored (when it doesn't exist it is not created)
     */
    public File getOrphansDirectory(URI topNodeURI);
    
    /**
     * 
     * @return the name of the orphans directory
     */
//    public String getOrphansDirectoryName();
    
//    public String getRelativePath(String parent,String child);
    
//    public String getRelativePath(OurURL parent, OurURL child);
    
//    public OurURL makeNewOurURL(URL url);
    
//    public String fileExtensionToLowerCase(String name);
    
    /**
     * Checks if the given file's size is above the size limit of the typechecker
     * @param fileToCheck File to be checked
     * @return true if the file is larger than the typechecker size limit
     */
    public boolean isFileSizeAboveTypeReCheckSizeLimit(File fileToCheck);
    
    
    /**
     * Checks if the given file is located in the orphans directory
     * @param fileToCheck File object to be checked
     * @return true if file is located in the orphans directory
     */
    public boolean isFileInOrphansDirectory(File fileToCheck);
    
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
    public File getFinalFile(String baseDirectory, String fileNameAttempt);
    
    /**
     * Creates the file and parent directories, if needed, for the given File object
     * @param fileToCreate File to be created
     * @throws IOException if there are problems with the creation of the file or directories
     */
    public void createFileAndDirectories(File fileToCreate) throws IOException;
    
    /**
     * Determines the path for files of the given type, based on the path of the parent node
     * @param parentPath Path of the parent node
     * @param nodeType Type of the node
     * @return Directory where the node should be located
     */
    public String getDirectoryForFileType(String parentPath, WorkspaceNodeType nodeType);
    
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
     * @param archiveNodeURL
     * @return target file
     */
    public File getTargetFileForReplacedOrDeletedNode(File baseDirectory, String archiveNodeUriStr, URL archiveNodeURL);
    
    /**
     * Checks if target directory can be written to.
     * @param targetDirectory
     * @return true if target directory can be written to
     */
    public boolean canWriteTargetDirectory(File targetDirectory);
}
