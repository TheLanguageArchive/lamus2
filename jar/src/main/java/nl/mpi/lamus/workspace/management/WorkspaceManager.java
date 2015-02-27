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
package nl.mpi.lamus.workspace.management;

import java.io.IOException;
import java.net.URI;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.model.Workspace;

/**
 * Interface for some managing operations in a workspace.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceManager {
    
    /**
     * Triggers the creation of a workspace 
     * (copying the corresponding data from the archive).
     * 
     * @param userID ID of the user who is creating the workspace
     * @param topArchiveNodeURI URI of the archive node where the workspace is being created
     * @return the object corresponding to the created workspace
     */
    public Workspace createWorkspace(String userID, URI topArchiveNodeURI)
            throws WorkspaceImportException;
    
    /**
     * Triggers the deletion of a workspace
     * (removing the data both from the database and the filesystem).
     * 
     * @param userID ID of the user who is deleting the workspace
     * @param workspaceID  ID of the workspace to be deleted
     * @param keepUnlinkedFiles true if the unlinked files are to be kept for future use
     */
    public void deleteWorkspace(int workspaceID, boolean keepUnlinkedFiles)
            throws WorkspaceNotFoundException, WorkspaceExportException, IOException;
    
    /**
     * Triggers the submission of a workspace 
     * (copying the corresponding data back to the archive).
     * 
     * @param workspaceID ID of the workspace to submit
     * @param keepUnlinkedFiles true if the unlinked files are to be kept for future use
     */
    public void submitWorkspace(int workspaceID, boolean keepUnlinkedFiles)
            throws WorkspaceNotFoundException, WorkspaceExportException;
    
    /**
     * Opens a workspace, getting the corresponding object from the
     * database, as well as changing its status.
     * 
     * @param workspaceID ID of the workspace to be opened
     * @return the object corresponding to the opened workspace
     */
    public Workspace openWorkspace(int workspaceID)
            throws WorkspaceNotFoundException, IOException;
}
