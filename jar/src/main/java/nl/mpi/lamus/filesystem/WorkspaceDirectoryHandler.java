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
import java.io.IOException;
import nl.mpi.lamus.exception.DisallowedPathException;
import nl.mpi.lamus.workspace.model.Workspace;

/**
 * Handler for operations related with the lamus directories.
 * 
 * @author guisil
 */
public interface WorkspaceDirectoryHandler {
    
    /**
     * Creates the directory for the given workspace.
     * 
     * @param workspaceID ID of the workspace for which the directory should be created
     */
    public void createWorkspaceDirectory(int workspaceID) throws IOException;
    
    /**
     * Deletes the directory for the given workspace.
     * 
     * @param workspaceID ID of the workspace for which the directory should be deleted
     */
    public void deleteWorkspaceDirectory(int workspaceID) throws IOException;
    
    /**
     * Checks if a directory for the given workspace exists.
     * 
     * @param workspace Workspace for which the existence of the directory is checked
     * @return true if the directory exists
     */
    public boolean workspaceDirectoryExists(Workspace workspace);
    
    /**
     * Retrieves the directory for the given workspace.
     * @param workspaceID ID of the workspace
     * @return File object corresponding to the workspace directory
     */
    public File getDirectoryForWorkspace(int workspaceID);

    /**
     * Retrieves the upload directory for the given workspace.
     * @param workspaceID ID of the workspace
     * @return File object corresponding to the upload directory for the workspace
     */
    public File getUploadDirectoryForWorkspace(int workspaceID);
    
    /**
     * Creates upload directory for the given workspace
     * @param workspaceID ID of the workspace
     */
    public void createUploadDirectoryForWorkspace(int workspaceID) throws IOException;
    
    /**
     * Creates directory inside the given workspace
     * @param workspaceID ID of the workspace
     * @param directoryName name of the directory to create
     * @return File object corresponding to the directory
     */
    public File createDirectoryInWorkspace(int workspaceID, String directoryName) throws IOException;
    
    /**
     * Checks any folder in the given path has one of the reserved folder names
     * @param path Path to check
     */
    public void ensurePathIsAllowed(String path) throws DisallowedPathException;
}
