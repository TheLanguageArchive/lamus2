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
package nl.mpi.lamus.workspace.importing;

import java.net.URL;
import nl.mpi.lamus.workspace.exception.WorkspaceNodeFilesystemException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.model.MetadataDocument;

/**
 * Handles the import of files from the archive to the workspace.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceFileImporter {
    
    /**
     * Imports metadata files from the archive to the workspace
     * @param archiveNodeURL URL of the node in the archive
     * @param node WorkspaceNode object corresponding to the node being imported
     * @param document MetadataDocument object corresponding to the node being imported
     * @throws WorkspaceNodeFilesystemException when there is an issue with the import
     */
    public void importMetadataFileToWorkspace(URL archiveNodeURL, WorkspaceNode node, MetadataDocument document)
            throws WorkspaceNodeFilesystemException;
    
}
