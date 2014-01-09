/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.upload;

import java.util.Collection;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 *
 * @author guisil
 */
public interface WorkspaceUploadHelper {
    
    /**
     * Checks if there are links among the nodes in the given collection
     * and adds them in the database.
     * @param workspaceID ID of the workspace
     * @param nodesToCheck Collection of nodes to be checked
     */
    public void assureLinksInWorkspace(int workspaceID, Collection<WorkspaceNode> nodesToCheck);
}
