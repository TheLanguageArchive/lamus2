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
package nl.mpi.lamus.workspace.factory;

import java.net.URI;
import nl.mpi.lamus.workspace.model.Workspace;

/**
 * Factory for Workspace objects.
 * 
 * @author guisil
 */
public interface WorkspaceFactory {
    
    /**
     * Creates a Workspace object with the given values, while some others are
     * injected or set as a default value.
     * 
     * @param userID ID of the user who is creating the workspace
     * @param archiveTopNodeURI URI of the archive node to be used as the top node
     * @return created Workspace object
     */
    public Workspace getNewWorkspace(String userID, URI archiveTopNodeURI);
    
}
