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

import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceDirectoryException;
import nl.mpi.lamus.workspace.model.Workspace;

/**
 * Handler for operations related with the lamus directories.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceDirectoryHandler {
    
    /**
     * Creates the directory for the given workspace.
     * 
     * @param workspace Workspace for which the directory should be created
     * @throws FailedToCreateWorkspaceDirectoryException if the directory creation fails
     */
    public void createWorkspaceDirectory(Workspace workspace) throws FailedToCreateWorkspaceDirectoryException;
    
    /**
     * Checks if a directory for the given workspace exists.
     * 
     * @param workspace Workspace for which the existence of the directory is checked
     * @return true if the directory exists
     */
    public boolean workspaceDirectoryExists(Workspace workspace);
}
