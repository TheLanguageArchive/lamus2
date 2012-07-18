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
package nl.mpi.lamus.service;

import java.util.Collection;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceService {
    
    
    
    /**
     * Creates a workspace starting in a given archive node, for a given user.
     * 
     * @param archiveNodeID archive ID of the node
     * @param userID ID of the user
     * @return Workspace object
     */
    public Workspace createWorkspace(String userID, int archiveNodeID);
    
    
    /**
     * Retrieves a workspace from the database.
     * 
     * @param workspaceID ID of the workspace to retrieve
     * @return Retrieved workspace object
     */
    public Workspace getWorkspace(int workspaceID);
    
    /**
     * Submits a workspace back into the archive.
     * 
     * @param workspaceID ID of the workspace
     */
    public void submitWorkspace(int workspaceID);
    
}
