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
package nl.mpi.lamus.workspace.importing;

import java.util.Collection;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;

/**
 * Explorer of the tree/branch containing the nodes that
 * will be imported into the workspace.
 * 
 * @author guisil
 */
public interface WorkspaceNodeExplorer {
    
    /**
     * Explores, recursively, the tree/branch where the import process is
     * currently working on, triggering the import of its nodes.
     * 
     * @param workspace workspace where to import the nodes
     * @param nodeToExplore node where the import proccess is working on currently
     * @param nodeDocument metadata document corresponding to the current node
     * @param linksInNode references contained in the metadata document corresponding to the current node
     */
    public void explore(Workspace workspace, WorkspaceNode nodeToExplore,
            ReferencingMetadataDocument nodeDocument, Collection<Reference> linksInNode)
                throws WorkspaceImportException;
}
