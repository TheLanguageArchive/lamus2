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
import java.net.URL;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusArchiveFileLocationProvider implements ArchiveFileLocationProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusArchiveFileLocationProvider.class);
    
    private final ArchiveFileHelper archiveFileHelper;
    
    @Autowired
    public LamusArchiveFileLocationProvider(ArchiveFileHelper archiveFileHelper) {
        this.archiveFileHelper = archiveFileHelper;
    }

//    @Override
//    public File getNextAvailableMetadataFile(URL parentArchiveURL, String childNodeName, URL childOriginURL) {
//        
////        String parentFileNameWithoutExtension = archiveFileHelper.getFileBasenameWithoutExtension(parentArchiveURL.getPath());
//        String parentFileNameWithoutExtension = FilenameUtils.getBaseName(parentArchiveURL.getPath());
//        
////        String parentDirName = archiveFileHelper.getFileDirname(parentArchiveURL.getPath());
//        String parentDirName = FilenameUtils.getFullPathNoEndSeparator(parentArchiveURL.getPath());
//        
//        //TODO ... missing cases ...
//        
//        
////        KeepFileNameOfIMDIFiles ?? - if not, uses parent name
////        MPI Rules ?? - if so, file renaming is different
//                
//        String childFilenameAttempt;
//        
//        if(childOriginURL == null) {
//            //TODO use childNodeName
//            
//            if(archiveFileHelper.fileNameMatchesMpiRules(parentArchiveURL.getPath())) {
//                childFilenameAttempt = parentFileNameWithoutExtension + FilenameUtils.EXTENSION_SEPARATOR_STR + FilenameUtils.getExtension(parentArchiveURL.getPath());
//            } else {
//                String correctedChildNodeName = archiveFileHelper.correctPathElement(childNodeName, "getNextAvailableMetadataFile");
//                childFilenameAttempt = correctedChildNodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + "cmdi";
//                //TODO get extension in a different way
//            }
//        } else {
//            //TODO use childOriginURL
//                //TODO but its usage depends on the name following the "MPI Rules" or not
//            if(archiveFileHelper.fileNameMatchesMpiRules(childOriginURL.getPath())) {
//                childFilenameAttempt = FilenameUtils.getBaseName(childOriginURL.getPath()) + FilenameUtils.EXTENSION_SEPARATOR_STR + FilenameUtils.getExtension(childOriginURL.getPath());
//            } else {
//                String correctedChildNodeName = archiveFileHelper.correctPathElement(childNodeName, "getNextAvailableMetadataFile");
//                childFilenameAttempt = correctedChildNodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + "cmdi";
//                //TODO get extension in a different way
//            }
//        }
//                
//        File childFile = archiveFileHelper.getFinalFile(parentDirName, childFilenameAttempt);
//        try {
//            //TODO Get extension some other way
//
//            archiveFileHelper.createFileAndDirectories(childFile);
//        } catch (IOException ex) {
//            Logger.getLogger(LamusArchiveFileLocationProvider.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        return childFile;
//        
//        
//        //TODO CHECK SESSIONS!!!
//    }

    @Override
    public File getAvailableFile(String parentPath, String filenameAttempt, WorkspaceNodeType nodeType) throws IOException {
        
//        String filename = FilenameUtils.getName(file.getPath());
        
        String correctedFilename = archiveFileHelper.correctPathElement(filenameAttempt, "getAvailableFile");
//        String parentDirectory = FilenameUtils.getFullPath(parentPath);
        String baseDirectoryForFileType = archiveFileHelper.getDirectoryForFileType(parentPath, nodeType);
        File finalFile = archiveFileHelper.getFinalFile(baseDirectoryForFileType, correctedFilename);
        
        archiveFileHelper.createFileAndDirectories(finalFile);
        
        return finalFile;
    }
    
}
