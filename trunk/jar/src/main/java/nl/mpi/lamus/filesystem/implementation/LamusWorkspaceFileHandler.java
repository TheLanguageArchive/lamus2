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
package nl.mpi.lamus.filesystem.implementation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceFileHandler
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceFileHandler implements WorkspaceFileHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceFileHandler.class);
    
    @Autowired
    @Qualifier("workspaceBaseDirectory")
    private File workspaceBaseDirectory;
    
    private final ArchiveFileLocationProvider archiveFileLocationProvider;
    private final WorkspaceAccessChecker workspaceAccessChecker;
    
    @Autowired
    public LamusWorkspaceFileHandler(ArchiveFileLocationProvider aflProvider, WorkspaceAccessChecker wsAccessChecker) {
        archiveFileLocationProvider = aflProvider;
        workspaceAccessChecker = wsAccessChecker;
    }
    

    /**
     * @see WorkspaceFileHandler#copyFile(java.io.File, java.io.File)
     */
    @Override
    public void copyFile(File originNodeFile, File targetNodeFile)
                throws IOException {
        
        Files.copy(originNodeFile.toPath(), targetNodeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * @see WorkspaceFileHandler#moveFile(java.io.File, java.io.File)
     */
    @Override
    public void moveFile(File originNodeFile, File targetNodeFile)
            throws IOException {
        
        Files.move(originNodeFile.toPath(), targetNodeFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * @see WorkspaceFileHandler#deleteFile(java.io.File)
     */
    @Override
    public void deleteFile(File file)
            throws IOException {
        
        Files.deleteIfExists(file.toPath());
    }
    
    /**
     * @see WorkspaceFileHandler#getStreamResultForWorkspaceNodeFile(java.io.File)
     */
    @Override
    public StreamResult getStreamResultForNodeFile(File nodeFile) {
            StreamResult streamResult = new StreamResult(nodeFile);
            return streamResult;
    }

    /**
     * @see WorkspaceFileHandler#getFileForImportedWorkspaceNode(java.io.File, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public File getFileForImportedWorkspaceNode(File archiveFile, WorkspaceNode workspaceNode) {
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceNode.getWorkspaceID());
        String nodeFilename = FilenameUtils.getName(archiveFile.getPath());
        
        File workspaceNodeFile = new File(workspaceDirectory, nodeFilename);
        
        return workspaceNodeFile;
    }

    /**
     * @see WorkspaceFileHandler#copyInputStreamToTargetFile(java.io.InputStream, java.io.File)
     */
    @Override
    public void copyInputStreamToTargetFile(InputStream inputStream, File targetFile)
            throws IOException {
        
        byte[] buffer = new byte[1024];
        
        try (OutputStream outputStream = new FileOutputStream(targetFile)) {
            int len;
            while((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        }
    }

    /**
     * @see WorkspaceFileHandler#getFilesInOrphanDirectory(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public Collection<File> getFilesInOrphanDirectory(Workspace workspace) {
        
        File orphansDirectory;
        try {
            orphansDirectory = archiveFileLocationProvider.getOrphansDirectory(workspace.getTopNodeArchiveURL().toURI());
        } catch (URISyntaxException ex) {
            logger.warn("Problem while trying to get the location of the orphans directory: " + ex.getMessage());
            return new ArrayList<>();
        }
        
        Collection<File> allFiles = new ArrayList<>();
        if(orphansDirectory.exists()) {
            logger.debug("Listing files for orphans directory: " + orphansDirectory.getAbsolutePath());
            allFiles = FileUtils.listFiles(orphansDirectory, null, true);
        }
        Collection<File> fileAvailableForWorkspace = new ArrayList<>();
        
        for(File currentFile : allFiles) {
            try {
                if(currentFile.isFile()) {
                    workspaceAccessChecker.ensureNodeIsNotLocked(currentFile.toURI());
                    fileAvailableForWorkspace.add(currentFile);
                }
            } catch (NodeAccessException ex) {
                //do nothing, let the loop continue
            }
        }
        
        return fileAvailableForWorkspace;
    }
}
