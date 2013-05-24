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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.exporting.TrashVersioningHandler;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.versioning.manager.VersioningAPI;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusTrashVersioningHandler implements TrashVersioningHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusTrashVersioningHandler.class);
    
    private final VersioningAPI versioningAPI;
    private final ArchiveFileHelper archiveFileHelper;
    
    @Autowired
    @Qualifier("trashCanBaseDirectory")
    private File trashCanBaseDirectory;
    
    @Autowired
    public LamusTrashVersioningHandler(VersioningAPI vAPI, ArchiveFileHelper afHelper) {
        this.versioningAPI = vAPI;
        this.archiveFileHelper = afHelper;
    }

    public boolean retireNodeVersion(WorkspaceNode node) {
        
        String stringNodeID = NodeIdUtils.TONODEID(node.getArchiveNodeID());
        
        if(NodeIdUtils.isNodeId(stringNodeID)) {
            return versioningAPI.setVersionStatus(stringNodeID, true);
        }
        return false;
    }
    
    public File getDirectoryForNodeVersion(int workspaceID) {
        
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        
        StringBuilder directoryName = new StringBuilder();
        directoryName.append(year);
        directoryName.append("-");
        if(month < 10) {
            directoryName.append("0");
        }
        directoryName.append(month);
        
        File subDirectory = new File(trashCanBaseDirectory, directoryName.toString());
        File subSubDirectory = new File(subDirectory, "" + workspaceID);
        
        return subSubDirectory;
    }
    
    public File getTargetFileForNodeVersion(File baseDirectory, int archiveNodeID, URL archiveNodeURL) {
        
        File archiveNodeFile = new File(archiveNodeURL.getPath());
        String fileBaseName = archiveFileHelper.getFileBasename(archiveNodeFile.getPath());
        StringBuilder fileNameBuilder = new StringBuilder().append("v").append(archiveNodeID).append("__.").append(fileBaseName);
        
        File targetFile = new File(baseDirectory, fileNameBuilder.toString());
        
        return targetFile;
    }

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

    public boolean moveFileToTargetLocation(File currentFile, File targetFile) {
        
        try {
            FileUtils.moveFile(currentFile, targetFile);
        } catch (IOException ex) {
            logger.error("LamusTrashVersioningHandler: File couldn't be moved from [" + currentFile + "] to [" + targetFile + "]", ex);
            return false;
        }
        // FileExistsException?
        // NullPointerException?
        
        return true;
    }
}
