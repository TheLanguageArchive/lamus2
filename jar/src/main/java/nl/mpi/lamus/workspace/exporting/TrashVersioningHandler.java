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
package nl.mpi.lamus.workspace.exporting;

import java.io.File;
import java.net.URL;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface TrashVersioningHandler {
    
    public boolean retireNodeVersion(WorkspaceNode node);
    
    public File getDirectoryForNodeVersion(int workspaceID);
    
    public File getTargetFileForNodeVersion(File baseDirectory, int archiveNodeID, URL archiveNodeURL);
    
    public boolean canWriteTargetDirectory(File targetDirectory);
    
    public boolean moveFileToTargetLocation(File currentFile, File targetFile);
}
