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

/**
 * Interface for the archive node access checking.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceAccessChecker extends Serializable {

    /**
     * Checks if a given user has write access to the given archive node.
     * @param userID ID of the user who needs to create a workspace
     * @param archiveNodeURI URI of the node in which the workspace is supposed to be created
     * @return true if the given user has write access to the given node
     */
    public boolean canCreateWorkspace(String userID, URI archiveNodeURI);
    
    /**
     * Checks if the given user has access to the given workspace
     * (the user has access if he/she was the creator of the workspace).
     * @param userID ID of the user
     * @param workspaceID ID of the workspace
     * @return true if the given user has access to the given workspace
     */
    public boolean hasAccessToWorkspace(String userID, int workspaceID);

}
