/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.importing;

import java.util.Collection;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.importing.implementation.ImportProblem;
import nl.mpi.lamus.workspace.model.Workspace;

/**
 * Handler of the import process of orphan nodes into the workspace.
 * 
 * @author guisil
 */
public interface OrphanNodesImportHandler {
    
    /**
     * Gets the orphan nodes available to the workspace and redirects them
     * to the appropriate importer.
     * @param workspace Workspace where to import the nodes
     * @return Collection of problems importing these orphan nodes
     * @throws WorkspaceException 
     */
    public Collection<ImportProblem> exploreOrphanNodes(Workspace workspace)
            throws WorkspaceException;
}
