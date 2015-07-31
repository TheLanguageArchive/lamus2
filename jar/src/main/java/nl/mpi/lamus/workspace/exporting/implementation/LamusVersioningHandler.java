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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.workspace.exporting.VersioningHandler;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.Workspace;
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
    private final ArchiveFileLocationProvider archiveFileLocationProvider;
    private final HandleParser handleParser;
    private final CorpusStructureProvider corpusStructureProvider;
    private final NodeResolver nodeResolver;
    private final NodeUtil nodeUtil;
    
    @Autowired
    public LamusVersioningHandler(ArchiveFileHelper fileHelper, ArchiveFileLocationProvider fileLocationProvider,
        HandleParser handleParser, CorpusStructureProvider csProvider, NodeResolver resolver,
        NodeUtil nodeUtil) {
        
        this.archiveFileHelper = fileHelper;
        this.handleParser = handleParser;
        this.corpusStructureProvider = csProvider;
        this.nodeResolver = resolver;
        this.archiveFileLocationProvider = fileLocationProvider;
        this.nodeUtil = nodeUtil;
    }

    /**
     * @see VersioningHandler#moveFileToTrashCanFolder(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public URL moveFileToTrashCanFolder(WorkspaceNode nodeToMove) {
        
        logger.debug("Moving node to trash can folder; workspaceID: " + nodeToMove.getWorkspaceID() + "; nodeID: " + nodeToMove.getWorkspaceNodeID());
        
        return moveFileTo(nodeToMove, true);
    }

    /**
     * @see VersioningHandler#moveFileToVersioningFolder(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public URL moveFileToVersioningFolder(WorkspaceNode nodeToMove) {
        
        logger.debug("Moving node to versioning folder; workspaceID: " + nodeToMove.getWorkspaceID() + "; nodeID: " + nodeToMove.getWorkspaceNodeID());
        
        return moveFileTo(nodeToMove, false);
    }

    /**
     * @see VersioningHandler#moveFileToOrphansFolder(nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public URL moveFileToOrphansFolder(Workspace workspace, WorkspaceNode nodeToMove) {
        
        
        File orphanOldLocation = null;
        
        File archiveLocation = null;
        
        //if file was not in the archive already, then it will be moved from its workspace location
        //if file is in the archive and is a resource, then it will be moved from its archive location
        //if file is metadata, then it will be moved from its workspace location; if it is also in the archive, then it has to be removed from there
        
        if(nodeToMove.getArchiveURI() != null) {
            CorpusNode archiveNode = corpusStructureProvider.getNode(nodeToMove.getArchiveURI());
            archiveLocation = nodeResolver.getLocalFile(archiveNode);
        }
        
        if(nodeToMove.getArchiveURI() == null || nodeUtil.isNodeMetadata(nodeToMove)) {
            URL orphanOldLocationUrl = nodeToMove.getWorkspaceURL();
            orphanOldLocation = new File(orphanOldLocationUrl.getPath());
            if(archiveFileLocationProvider.isFileInOrphansDirectory(orphanOldLocation)) {
                logger.info("File already in orphans directory: " + orphanOldLocation.getAbsolutePath());
                return orphanOldLocationUrl;
            }
        }
        
        if(orphanOldLocation == null) {
            orphanOldLocation = archiveLocation;
        }
        
        String filename;
        if(orphanOldLocation != null) {
            filename = orphanOldLocation.getName();
        } else {
            throw new IllegalStateException("No valid file location was found.");
        }
        
        File orphanNewLocation;
        try {
            File orphansDirectory = archiveFileLocationProvider.getOrphansDirectory(workspace.getTopNodeArchiveURL().toURI());
            if(!archiveFileHelper.canWriteTargetDirectory(orphansDirectory)) {
                logger.error("Problem with write permissions in target directory " + orphansDirectory);
                return null;
            }
            orphanNewLocation = archiveFileHelper.getFinalFile(orphansDirectory, filename);
        } catch (URISyntaxException ex) {
            String errorMessage = "Error retrieving archive location of node " + workspace.getTopNodeArchiveURI();
            logger.error(errorMessage, ex);
            return null;
        }
        
        try {
            FileUtils.moveFile(orphanOldLocation, orphanNewLocation);
            if(archiveLocation != null) {
                Files.deleteIfExists(archiveLocation.toPath());
            }
        } catch (IOException ex) {
            logger.error("File couldn't be moved from [" + orphanOldLocation + "] to [" + orphanNewLocation + "]", ex);
            return null;
        }
        
        URL movedFileUrl = null;
        try {
            movedFileUrl = orphanNewLocation.toURI().toURL();
        } catch (MalformedURLException ex) {
            logger.warn("Moved file location is not a URL", ex);
        }
        
        return movedFileUrl;
    }
    
    
    private URL moveFileTo(WorkspaceNode nodeToMove, boolean toDelete) {
        
        CorpusNode archiveNode = corpusStructureProvider.getNode(nodeToMove.getArchiveURI());
        File currentFile = nodeResolver.getLocalFile(archiveNode);
        
        File targetDirectory;
        if(toDelete) {
            targetDirectory = archiveFileHelper.getDirectoryForDeletedNode(nodeToMove.getWorkspaceID());
        } else { // node is replaced
            targetDirectory = archiveFileHelper.getDirectoryForReplacedNode(nodeToMove.getWorkspaceID());
        }

        if(!archiveFileHelper.canWriteTargetDirectory(targetDirectory)) {
            logger.error("Problem with write permissions in target directory " + targetDirectory);
            return null;
        }
        
        File targetFile = archiveFileHelper.getTargetFileForReplacedOrDeletedNode(
                targetDirectory, handleParser.stripHandleIfPrefixIsKnown(nodeToMove.getArchiveURI()), currentFile);
        
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
