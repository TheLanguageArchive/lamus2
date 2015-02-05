/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.importing.implementation;

import java.io.File;
import java.util.Collection;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.importing.OrphanNodesImportHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author guisil
 */
@Component
public class LamusOrphanNodesImportHandler implements OrphanNodesImportHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusOrphanNodesImportHandler.class);

    private final WorkspaceFileHandler workspaceFileHandler;
    private final WorkspaceUploader workspaceUploader;
    
    @Autowired
    public LamusOrphanNodesImportHandler(WorkspaceFileHandler wsFileHandler, WorkspaceUploader wsUploader) {
        workspaceFileHandler = wsFileHandler;
        workspaceUploader = wsUploader;
    }
    
    /**
     * @see OrphanNodesImportHandler#exploreOrphanNodes(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public Collection<ImportProblem> exploreOrphanNodes(Workspace workspace) throws WorkspaceException {
        
        logger.info("Importing files from the orphans directory (if any).");
        
        Collection<File> orphanFiles = workspaceFileHandler.getFilesInOrphanDirectory(workspace);
        logger.info("Found " + orphanFiles.size() + " available orphan files");
        
        return workspaceUploader.processUploadedFiles(workspace.getWorkspaceID(), orphanFiles);
    }
    
}
