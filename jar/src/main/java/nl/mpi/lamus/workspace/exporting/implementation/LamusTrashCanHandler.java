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
import java.net.MalformedURLException;
import java.net.URL;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.exporting.TrashCanHandler;
import nl.mpi.lamus.workspace.exporting.TrashVersioningHandler;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see TrashCanHandler
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusTrashCanHandler implements TrashCanHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusTrashCanHandler.class);
    
    private final TrashVersioningHandler trashVersioningHandler;
    private final ArchiveFileHelper archiveFileHelper;
    
    @Autowired
    public LamusTrashCanHandler(TrashVersioningHandler vHandler, ArchiveFileHelper fileHelper) {
        
        //TODO check constructor from the trashcan in the old lamus
        
        this.trashVersioningHandler = vHandler;
        this.archiveFileHelper = fileHelper;
    }

    /**
     * @see TrashCanHandler#moveFileToTrashCan(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public URL moveFileToTrashCan(WorkspaceNode nodeToMove) {
        
        //TODO consistency checks?
        
        
        File currentFile = archiveFileHelper.getArchiveLocationForNodeID(nodeToMove.getArchiveNodeID());
        
        File versionDirectory = trashVersioningHandler.getDirectoryForNodeVersion(nodeToMove.getWorkspaceID());

        if(!trashVersioningHandler.canWriteTargetDirectory(versionDirectory)) {
            return null;
        }
        
        File versionFile = trashVersioningHandler.getTargetFileForNodeVersion(versionDirectory, nodeToMove.getArchiveNodeID(), nodeToMove.getArchiveURL());
        
        if(!trashVersioningHandler.moveFileToTargetLocation(currentFile, versionFile)) {
            return null;
        }
        
        URL versionURL = null;
        try {
            versionURL = versionFile.toURI().toURL();
        } catch (MalformedURLException ex) {
            logger.warn("Versioned node file location is not a URL", ex);
        }
        
        return versionURL;
    }
    
}
