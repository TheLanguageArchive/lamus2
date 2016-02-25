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
package nl.mpi.lamus.archive.implementation;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see ArchiveFileLocationProvider
 * @author guisil
 */
@Component
public class LamusArchiveFileLocationProvider implements ArchiveFileLocationProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusArchiveFileLocationProvider.class);
    
    private final ArchiveFileHelper archiveFileHelper;
    
    @Autowired
    @Qualifier("dbHttpRoot")
    private String dbHttpRoot;
    @Autowired
    @Qualifier("dbHttpsRoot")
    private String dbHttpsRoot;
    @Autowired
    @Qualifier("dbLocalRoot")
    private String dbLocalRoot;
    
    @Autowired
    @Qualifier("corpusstructureDirectoryName")
    private String corpusstructureDirectoryName;
    @Autowired
    @Qualifier("orphansDirectoryName")
    private String orphansDirectoryName;
    
    
    @Autowired
    public LamusArchiveFileLocationProvider(ArchiveFileHelper archiveFileHelper) {
        this.archiveFileHelper = archiveFileHelper;
    }

    /**
     * @see ArchiveFileLocationProvider#getAvailableFile(java.lang.String,
     *  java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNode, java.lang.String)
     */
    @Override
    public File getAvailableFile(String parentNodePath, String parentCorpusNamePathToClosestTopNode, WorkspaceNode node, String filenameAttempt) throws IOException {
        
        String correctedFilename = archiveFileHelper.correctPathElement(filenameAttempt, "getAvailableFile");
        String baseDirectoryForFileType = archiveFileHelper.getDirectoryForNode(parentNodePath, parentCorpusNamePathToClosestTopNode, node);
        File finalFile = archiveFileHelper.getFinalFile(new File(baseDirectoryForFileType), correctedFilename);
        
        archiveFileHelper.createFileAndDirectories(finalFile);
        
        return finalFile;
    }

    /**
     * @see ArchiveFileLocationProvider#getChildPathRelativeToParent(java.io.File, java.lang.String)
     */
    @Override
    public String getChildPathRelativeToParent(File parentNodeFile, File childNodeFile) {
        
        if(parentNodeFile.equals(childNodeFile)) {
            throw new IllegalStateException("Parent and child files should be different");
        }
        
        String parentDirectory = FilenameUtils.getFullPath(parentNodeFile.getAbsolutePath());
        Path parentDirPath = Paths.get(parentDirectory);
        Path childFilePath = Paths.get(childNodeFile.getAbsolutePath());
        Path relativePath = parentDirPath.relativize(childFilePath);
        
        return relativePath.toString();
    }

    /**
     * @see ArchiveFileLocationProvider#getUriWithHttpsRoot(java.net.URI)
     */
    @Override
    public URI getUriWithHttpsRoot(URI location) throws URISyntaxException{
        
        if(location.toString().startsWith(dbLocalRoot)) {
            return new URI(location.toString().replace(dbLocalRoot, dbHttpsRoot));
        }
        
        // in other cases (including when the httpRoot is already present, returns the same file
        return location;
    }

    @Override
    public URI getUriWithLocalRoot(URI location) throws URISyntaxException {
        
        if(location.toString().startsWith(dbHttpsRoot)) {
            return new URI(location.toString().replace(dbHttpsRoot, dbLocalRoot));
        }
        if(location.toString().startsWith(dbHttpRoot)) {
            return new URI(location.toString().replace(dbHttpRoot, dbLocalRoot));
        }
        
        // in other cases (including when the httpRoot is already present, returns the same file
        return location;
    }
    

    /**
     * @see ArchiveFileLocationProvider#getOrphansDirectory(java.net.URI)
     */
    @Override
    public File getOrphansDirectory(URI topNodeLocation) {
        String topNodePath = topNodeLocation.getPath();
        int index=topNodePath.indexOf(File.separator + corpusstructureDirectoryName + File.separator);
        File orphansFolder = null;
        if(index > -1) {
            orphansFolder = new File(topNodePath.substring(0, index + 1) + orphansDirectoryName);
        } else {
            File temp=new File(topNodePath);
            while((orphansFolder == null) && (temp != null)) {
                File cs = new File (temp, corpusstructureDirectoryName);
                if(cs.exists() && cs.isDirectory()) {
                    orphansFolder = new File(temp, orphansDirectoryName);
                }
                temp=temp.getParentFile();
            }
        }
        return orphansFolder; 
    }
    
    /**
     * @see ArchiveFileLocationProvider#isFileInOrphansDirectory(java.io.File)
     */
    @Override
    public boolean isFileInOrphansDirectory(File fileToCheck) {
        
        if (orphansDirectoryName != null &&
            fileToCheck.getAbsolutePath().contains(orphansDirectoryName)) {
            return true;
        }
        return false;
    }
}
