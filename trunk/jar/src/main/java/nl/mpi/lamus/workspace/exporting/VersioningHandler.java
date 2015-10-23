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

import java.net.URL;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Provides methods related either with the trashcan folder
 * or the versioning folder.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface VersioningHandler {

    /**
     * Moves the given node's file to the trashcan
     * 
     * @param nodeToMove Node to be moved
     * @return URL of the node after being moved
     */
    public URL moveFileToTrashCanFolder(WorkspaceNode nodeToMove);
    
    /**
     * Moves the given node's file to the trashcan
     * 
     * @param nodeToMove Node to be moved
     * @return URL of the node after being moved
     */
    public URL moveFileToVersioningFolder(WorkspaceNode nodeToMove);
    
    /**
     * Moves the given node to the corresponding orphans folder.
     * @param workspace Workspace to which the node belongs
     * @param nodeToMove Node to be moved
     * @return URL of the node after being moved
     */
    public URL moveFileToOrphansFolder(Workspace workspace, WorkspaceNode nodeToMove);
}
