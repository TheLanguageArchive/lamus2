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
import java.net.URI;
import nl.mpi.util.OurURL;

/**
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
    
    /**
     * @return the part of fullname up to but excluding the last slash
     */
    public String getFileDirname(String fullname);
    
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
    
    public boolean isFileSizeAboveTypeReCheckSizeLimit(File fileToCheck);
    
    public boolean isFileInOrphansDirectory(File fileToCheck);
    
    public boolean isUrlLocal(OurURL urlToCheck);
}
