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

import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceParentNodeReference;
import nl.mpi.metadata.api.model.Reference;

/**
 * Factory for WorkspaceParentNodeReference objects.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceParentNodeReferenceFactory {
    
    /**
     * Creates a WorkspaceParentNodeReference object with the given values.
     * 
     * @param parentNode parent node
     * @param childLink reference for the child node, included in the parent metadata document
     * @return created WorkspaceParentNodeReference object
     */
    public WorkspaceParentNodeReference getNewWorkspaceParentNodeReference(WorkspaceNode parentNode, Reference childLink);
}
