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
import java.net.MalformedURLException;
import java.net.URL;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.exporting.VersioningHandler;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see VersioningHandler
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusVersioningHandler implements VersioningHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusVersioningHandler.class);
    
    private final ArchiveFileHelper archiveFileHelper;
    
    @Autowired
    public LamusVersioningHandler(ArchiveFileHelper fileHelper) {
        
        //TODO check constructor from the trashcan in the old lamus
        
        this.archiveFileHelper = fileHelper;
    }

    /**
     * @see VersioningHandler#moveFileToTrashCanFolder(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public URL moveFileToTrashCanFolder(WorkspaceNode nodeToMove) {
        
        return moveFileTo(nodeToMove, true);
    }

    /**
     * @see VersioningHandler#moveFileToVersioningFolder(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public URL moveFileToVersioningFolder(WorkspaceNode nodeToMove) {
        
        return moveFileTo(nodeToMove, false);
    }
    
    
    private URL moveFileTo(WorkspaceNode nodeToMove, boolean toDelete) {
        
        //TODO consistency checks?
        
        
//        File currentFile = archiveFileHelper.getArchiveLocationForNodeID(nodeToMove.getArchiveNodeID());
        
        File currentFile = FileUtils.toFile(nodeToMove.getArchiveURL());
        
        File targetDirectory = null;
        if(toDelete) {
            targetDirectory = archiveFileHelper.getDirectoryForDeletedNode(nodeToMove.getWorkspaceID());
        } else { // node is replaced
            targetDirectory = archiveFileHelper.getDirectoryForReplacedNode(nodeToMove.getWorkspaceID());
        }

        if(!archiveFileHelper.canWriteTargetDirectory(targetDirectory)) {
            return null;
        }
        
        File targetFile = archiveFileHelper.getTargetFileForReplacedOrDeletedNode(targetDirectory, nodeToMove.getArchiveURI(), nodeToMove.getArchiveURL());
        
        try {
            FileUtils.moveFile(currentFile, targetFile);
        } catch (IOException ex) {
            logger.error("File couldn't be moved from [" + currentFile + "] to [" + targetFile + "]", ex);
            return null;
        }
        
        URL movedFileURL = null;
        try {
            movedFileURL = targetFile.toURI().toURL();
        } catch (MalformedURLException ex) {
            logger.warn("Moved file location is not a URL", ex);
        }
        
        return movedFileURL;
    }
}
