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
package nl.mpi.lamus.archive.implementation;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.mpi.archiving.corpusstructure.core.FileInfo;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.util.Checksum;
import nl.mpi.util.OurURL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see ArchiveFileHelper
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusArchiveFileHelper implements ArchiveFileHelper {
    
    //TODO based on the class 'ArchiveUtils' from the old Lamus
    
    private static final Logger logger = LoggerFactory.getLogger(LamusArchiveFileHelper.class);
    
    @Autowired
    @Qualifier("maxDirectoryNameLength")
    private int maxDirectoryNameLength;
    @Autowired
    @Qualifier("corpusDirectoryBaseName")
    private String corpusDirectoryBaseName;
    @Autowired
    @Qualifier("orphansDirectoryBaseName")
    private String orphansDirectoryBaseName;
    @Autowired
    @Qualifier("typeRecheckSizeLimitInBytes")
    private long typeRecheckSizeLimitInBytes;
    
    @Autowired
    @Qualifier("metadataDirectoryName")
    private String metadataDirectoryName;
    @Autowired
    @Qualifier("resourcesDirectoryName")
    private String resourcesDirectoryName;
    
    @Autowired
    @Qualifier("trashCanBaseDirectory")
    private File trashCanBaseDirectory;
    @Autowired
    @Qualifier("versioningBaseDirectory")
    private File versioningBaseDirectory;
    
    /**
     * 
     * @param fullname
     * @return the offset of the last slash in fullname
     */
    private int lastSlashPos(String fullname) {
        int lastSlash = fullname.lastIndexOf('/');
        int lastBackslash = fullname.lastIndexOf('\\');
        if (lastSlash > lastBackslash) return lastSlash;
        return lastBackslash;
    }
    
    /**
     * @see ArchiveFileHelper#getFileBasename(java.lang.String) 
     */
    @Override
    public String getFileBasename(String fullname) {
        int sp = lastSlashPos(fullname);
        if ((sp+1)==fullname.length()) logger.warn("getFileBasename: None for "+fullname);
        return fullname.substring(sp+1);
    }
    
    @Override
    public String getFileBasenameWithoutExtension(String fullname) {
        String basename = getFileBasename(fullname);
        return basename.substring(0, basename.lastIndexOf("."));
    }

    /**
     * @see ArchiveFileHelper#getFileTitle(java.lang.String) 
     */
    @Override
    public String getFileTitle(String fullname) {
        String name = "";
        if (fullname.length()!=(lastSlashPos(fullname)+1)) name = getFileBasename(fullname);
        if (name.length()==0) {
            name = fullname.replaceAll("^[a-zA-Z0-9]+:[/]+", ""); // remove protocol / schema part
            int preSlash = name.indexOf('/'); // if easy, clip to domain name
            if (preSlash > 0) name = name.substring(0, preSlash);
            name = correctPathElement(name, "getFileTitle"); // remove slashes etc
            name = name.replaceAll("[.]", "_"); // avoid the impression of having a file name extension
        }
        return name;
    }
    
    /**
     * @see ArchiveFileHelper#correctPathElement(java.lang.String, java.lang.String) 
     */
    @Override
    public String correctPathElement(String pathElement, String reason) {
        String temp = pathElement.replaceAll("\\&[^;]+;","_"); // replace xml variables
        // 20..2c: space ! " # $ % &amp;  ' ( ) * +   3a..40: : ; &lt; = > ? @
        // 5b..60: [ \\ ] ^ _    7b..7f: { | } ~
        // temp=temp.replaceAll("[\\x00-\\x2C\\x2F\\x3A-\\x40\\x5B-\\x60\\x7B-\\xFF]", "_"); // replace special chars
        // temp=temp.replaceAll("[\\u0100-\\uffff]", "_"); // replace special chars
        // safe minimal MPI names may only contain [A-Za-z0-9._-], but URI can also contain
        // ! ~ * ' ( ) "unreserved" and : @ &amp; = + $ , "reserved minus ; / ?" at many
        // places. Reserved ; / ? : @ &amp; = + $ , have special meaning, see RFC2396.
        // Whitespace  is never allowed in URI, but %nn hex escapes can often be used.
        temp = temp.replaceAll("[^A-Za-z0-9._-]", "_"); // replace all except known good chars
        
        String result = temp;
        
        // in case the pathElement already contained "__" (without any invalid characters), it should stay unchanged
        if(!temp.equals(pathElement)) {
	        Pattern pat = Pattern.compile("__");  // shorten double replacements
	        Matcher mat = pat.matcher(temp);
	        while (mat.find(0)) mat.reset(mat.replaceFirst("_"));
	        result = mat.replaceFirst("_");
        }
        
        if (result.length()>maxDirectoryNameLength) { // truncate but try to keep extension
            int dot = result.lastIndexOf('.');
            String suffix = "...";
            if (dot>=0 && (result.length()-dot)<=7) // at most '.123456'
                suffix += result.substring(dot); // otherwise: no extension to preserve!
            // archivable files all have (2) / 3 / 4 char extensions, '.class' has 5 chars
            result = result.substring(0, maxDirectoryNameLength-suffix.length()) + suffix; // suffix.length: 3..10
        }
        if (!result.equals(pathElement)) {
            if ("getFileTitle".equals(reason)) { // log noise reduction ;-)
                logger.info("correctPathElement: "+reason+": "+pathElement+" -> "+result);
            } else {
                logger.warn("correctPathElement: "+reason+": "+pathElement+" -> "+result);
            }
        }
        return result;

    }

    /**
     * @see ArchiveFileHelper#getOrphansDirectory(java.net.URI)
     */
    @Override
    public File getOrphansDirectory(URI topNodeURI) {
        String topNodePath = topNodeURI.getPath();
        int index=topNodePath.indexOf(File.separator + corpusDirectoryBaseName + File.separator);
        File orphansFolder = null;
        if(index > -1) {
            orphansFolder = new File(topNodePath.substring(0, index + 1) + orphansDirectoryBaseName);
        } else {
            File temp=new File(topNodePath);
            while((orphansFolder == null) && (temp != null)) {
                File cs = new File (temp, corpusDirectoryBaseName);
                if(cs.exists() && cs.isDirectory()) {
                    orphansFolder = new File(temp, orphansDirectoryBaseName);
                }
                temp=temp.getParentFile();
            }
        }
        return orphansFolder; 
    }
    
    /**
     * @see ArchiveFileHelper#isFileSizeAboveTypeReCheckSizeLimit(java.io.File)
     */
    @Override
    public boolean isFileSizeAboveTypeReCheckSizeLimit(File fileToCheck) {
        if(fileToCheck.length() > typeRecheckSizeLimitInBytes) {
            return true;
        }
        return false;
    }
    
    /**
     * @see ArchiveFileHelper#isFileInOrphansDirectory(java.io.File)
     */
    @Override
    public boolean isFileInOrphansDirectory(File fileToCheck) {
        
        //TODO This method should be more robust
            // it should not only check if the file's path contains the directory name
            // it should check if the path is actually the same as the complete path for the orphans of that workspace

        
        if (orphansDirectoryBaseName != null &&
            fileToCheck.getAbsolutePath().toString().contains(orphansDirectoryBaseName)) {
            return true;
        }
        return false;
    }

    /**
     * @see ArchiveFileHelper#isUrlLocal(nl.mpi.util.OurURL)
     */
    @Override
    public boolean isUrlLocal(OurURL urlToCheck) {
        
        //TODO Should this check be done in some different way?
            // currently the CS database indicates if a file is local or not,
            // but that's not always correct
        //TODO check if protocol is "file" and if there is no host...?
        
        if("file".equals(urlToCheck.getProtocol())) {
            return true;
        }
        return false;
    }

    /**
     * @see ArchiveFileHelper#getFinalFile(java.lang.String, java.lang.String)
     */
    @Override
    public File getFinalFile(String baseDirectory, String fileNameAttempt) {
        
        int suffix = 1;
        String attemptSuffix = "";
        
        File fileAttempt =
                new File(baseDirectory, FilenameUtils.getBaseName(fileNameAttempt) + attemptSuffix + FilenameUtils.EXTENSION_SEPARATOR_STR + FilenameUtils.getExtension(fileNameAttempt));
        
        while ((fileAttempt.exists()) && (suffix < 10000)) {
            suffix++;

            fileAttempt = new File(baseDirectory,
                    FilenameUtils.getBaseName(fileNameAttempt) + "_" + suffix + FilenameUtils.EXTENSION_SEPARATOR_STR + FilenameUtils.getExtension(fileNameAttempt));
        }
        if (suffix >= 10000) {
            return null;
            //TODO Throw some exception instead? does it make sense to stop here?
        } // give up

        
        return fileAttempt;
    }
    
    /**
     * @see ArchiveFileHelper#createFileAndDirectories(java.io.File)
     */
    @Override
    public void createFileAndDirectories(File fileToCreate) throws IOException {
        
        File parentFile = fileToCreate.getAbsoluteFile().getParentFile();
        FileUtils.forceMkdir(parentFile);
        FileUtils.touch(fileToCreate);
    }
    
    /**
     * @see ArchiveFileHelper#getDirectoryForFileType(java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNodeType)
     */
    @Override
    public String getDirectoryForFileType(String parentPath, WorkspaceNodeType nodeType) {
        
        String metadataFolderPlusFilename = this.metadataDirectoryName + File.separator + FilenameUtils.getName(parentPath);
        String parentBaseDirectory;
        if(parentPath.endsWith(metadataFolderPlusFilename)) {
            parentBaseDirectory = parentPath.replace(metadataFolderPlusFilename, "");
        } else {
            parentBaseDirectory = FilenameUtils.getFullPathNoEndSeparator(parentPath);
        }
        
        if(WorkspaceNodeType.METADATA.equals(nodeType)) {
            return FilenameUtils.concat(parentBaseDirectory, this.metadataDirectoryName);
        } else {
            return FilenameUtils.concat(parentBaseDirectory, this.resourcesDirectoryName);
        }
    }

    /**
     * @see ArchiveFileHelper#hasArchiveFileChanged(nl.mpi.archiving.corpusstructure.core.FileInfo, java.io.File)
     */
    @Override
    public boolean hasArchiveFileChanged(FileInfo archiveFileInfo, File workspaceFile) {
        
        if(archiveFileInfo.getSize() != workspaceFile.length()) {
            return true;
        }
        
        String archiveChecksum = archiveFileInfo.getChecksum();
        String workspaceChecksum = Checksum.create(workspaceFile.getPath());
        if(!workspaceChecksum.equals(archiveChecksum)) {
            return true;
        }
        
        return false;
    }

    /**
     * @see ArchiveFileHelper#getDirectoryForReplacedNode(int)
     */
    @Override
    public File getDirectoryForReplacedNode(int workspaceID) {
        
        return getSubDirectoryFor(workspaceID, false);
    }

    /**
     * @see ArchiveFileHelper#getDirectoryForDeletedNode(int)
     */
    @Override
    public File getDirectoryForDeletedNode(int workspaceID) {
        
        return getSubDirectoryFor(workspaceID, true);
    }

    /**
     * @see ArchiveFileHelper#getTargetFileForReplacedOrDeletedNode(java.io.File, java.net.URI, java.net.URL)
     */
    @Override
    public File getTargetFileForReplacedOrDeletedNode(File baseDirectory, URI archiveNodeURI, URL archiveNodeURL) {
        
        File archiveNodeFile = new File(archiveNodeURL.getPath());
        String fileBaseName = getFileBasename(archiveNodeFile.getPath());
        StringBuilder fileNameBuilder = new StringBuilder().append("v").append(archiveNodeURI).append("__.").append(fileBaseName);
        
        File targetFile = new File(baseDirectory, fileNameBuilder.toString());
        
        return targetFile;
    }

    /**
     * @see ArchiveFileHelper#canWriteTargetDirectory(java.io.File)
     */
    @Override
    public boolean canWriteTargetDirectory(File targetDirectory) {
        
        if(!targetDirectory.exists()) {
            if(!targetDirectory.mkdirs()) {
                logger.warn("LamusTrashVersioningHandler: Failed to create directories for " + targetDirectory);
                return false;
            }
        }
        if(!targetDirectory.isDirectory()) {
            logger.error("LamusTrashVersioningHandler: target directory isn't a directory (?)");
            return false;
        }
        if(!targetDirectory.canWrite()) {
            logger.error("LamusTrashVersioningHandler: Cannot write directory " + targetDirectory);
            return false;
        }
        
        return true;
    }
    
    
    private File getSubDirectoryFor(int workspaceID, boolean isNodeDeleted) {
        
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        
        StringBuilder directoryName = new StringBuilder();
        directoryName.append(year);
        directoryName.append("-");
        if(month < 10) {
            directoryName.append("0");
        }
        directoryName.append(month);
        
        File baseDirectoryToUse = null;
        if(isNodeDeleted) {
            baseDirectoryToUse = trashCanBaseDirectory;
        } else { // node is replaced
            baseDirectoryToUse = versioningBaseDirectory;
        }
        
        File subDirectory = new File(baseDirectoryToUse, directoryName.toString());
        File subSubDirectory = new File(subDirectory, "" + workspaceID);
        
        return subSubDirectory;
    }
}
