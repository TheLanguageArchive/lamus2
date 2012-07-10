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
package nl.mpi.lamus.filesystem;

import java.io.File;
import java.io.OutputStream;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceNodeFileException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.model.MetadataDocument;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceFileHandler {
    
    public void copyMetadataFileToWorkspace(Workspace workspace, WorkspaceNode workspaceNode,
            MetadataAPI metadataAPI, MetadataDocument metadataDocument, File nodeFile,
            StreamResult nodeFileStreamResult)
            throws FailedToCreateWorkspaceNodeFileException;

    public StreamResult getStreamResultForWorkspaceNodeFile(Workspace workspace, WorkspaceNode workspaceNode, File nodeFile);
            
    public File getFileForWorkspaceNode(WorkspaceNode workspaceNode);
}
