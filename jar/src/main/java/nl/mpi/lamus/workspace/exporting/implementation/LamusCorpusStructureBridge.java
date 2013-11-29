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
import java.net.URL;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.util.DateTimeHelper;
import nl.mpi.lamus.workspace.exporting.CorpusStructureBridge;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.util.Checksum;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see CorpusStructureBridge
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusCorpusStructureBridge implements CorpusStructureBridge {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusCorpusStructureBridge.class);
    
    private final CorpusStructureProvider corpusStructureProvider;
    private final DateTimeHelper dateTimeHelper;
    private final ArchiveFileHelper archiveFileHelper;
    private final WorkspaceAccessChecker workspaceAccessChecker;
    
    @Autowired
    public LamusCorpusStructureBridge(
            CorpusStructureProvider csProvider,
            DateTimeHelper dtHelper, ArchiveFileHelper afHelper, WorkspaceAccessChecker wsAccessChecker) {
        
        this.corpusStructureProvider = csProvider;
        this.dateTimeHelper = dtHelper;
        this.archiveFileHelper = afHelper;
        this.workspaceAccessChecker = wsAccessChecker;
    }


    /**
     * @see CorpusStructureBridge#getChecksum(java.net.URL)
     */
    @Override
    public String getChecksum(URL nodeURL) {
        File nodeArchiveFile = FileUtils.toFile(nodeURL);
        String checksum = null;
        if(nodeArchiveFile.exists() && nodeArchiveFile.canRead() && nodeArchiveFile.isFile()) {
            checksum = Checksum.create(nodeArchiveFile.getPath());
        } else {
            throw new UnsupportedOperationException("LamusCorpusStructureBridge.getChecksum (when nodeArchiveFile doesn't exist OR can't be read OR is not file) not implemented yet");
        }
        return checksum;
    }

}
