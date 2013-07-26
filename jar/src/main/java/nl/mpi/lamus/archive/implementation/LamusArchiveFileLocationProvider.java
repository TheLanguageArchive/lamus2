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
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see ArchiveFileLocationProvider
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

    /**
     * @see ArchiveFileLocationProvider#getAvailableFile(java.lang.String, java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNodeType)
     */
    @Override
    public File getAvailableFile(String parentPath, String filenameAttempt, WorkspaceNodeType nodeType) throws IOException {
        
        String correctedFilename = archiveFileHelper.correctPathElement(filenameAttempt, "getAvailableFile");
        String baseDirectoryForFileType = archiveFileHelper.getDirectoryForFileType(parentPath, nodeType);
        File finalFile = archiveFileHelper.getFinalFile(baseDirectoryForFileType, correctedFilename);
        
        archiveFileHelper.createFileAndDirectories(finalFile);
        
        return finalFile;
    }
    
}
