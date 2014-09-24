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

import java.io.Serializable;
import java.net.URI;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;

/**
 * Interface for the archive node access checking.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceAccessChecker extends Serializable {

    /**
     * Checks if a workspace can be created in the given node.
     * This is true only when the node exists, is on site, is not locked
     * and the user has write access on it.
     * @param userID ID of the user who needs to create a workspace
     * @param archiveNodeURI URI of the node in which the workspace is supposed to be created
     */
    public void ensureWorkspaceCanBeCreated(String userID, URI archiveNodeURI)
            throws NodeAccessException, NodeNotFoundException;
    
    /**
     * Checks if the given node is accessible to the user
     * (i.e. the user has permissions to access the node
     * and the node is not locked in some other workspace already)
     * @param userID ID of the user
     * @param archiveNodeURI URI of the node
     */
    public void ensureBranchIsAccessible(String userID, URI archiveNodeURI);
    
    /**
     * Checks if the given user has access to the given workspace
     * (the user has access if he/she was the creator of the workspace).
     * @param userID ID of the user
     * @param workspaceID ID of the workspace
     */
    public void ensureUserHasAccessToWorkspace(String userID, int workspaceID)
            throws WorkspaceNotFoundException, WorkspaceAccessException;

    /**
     * Checks if the given user can delete the given workspace.
     * This is true if the user is the owner of the workspace or
     * if the user is a manager.
     * @param userID ID of the user
     * @param workspaceID  ID of the workspace
     */
    public void ensureUserCanDeleteWorkspace(String userID, int workspaceID)
            throws WorkspaceNotFoundException, WorkspaceAccessException;
}
