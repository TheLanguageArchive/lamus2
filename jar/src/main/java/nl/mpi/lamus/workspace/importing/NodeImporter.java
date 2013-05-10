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

import nl.mpi.lamus.workspace.exception.NodeExplorerException;
import nl.mpi.lamus.workspace.exception.NodeImporterException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;

/**
 * Generic node importer.
 * The correct implementation should be chosen in the workspace file explorer
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface NodeImporter<R extends Reference> {
    
    /**
     * Setter for the workspace where the import is being performed.
     * 
     * @param workspace workspace where the import is being performed
     */
    public void setWorkspace(Workspace workspace);
    
    /**
     * Imports the given file into the workspace, taking into account the
     * information in the archive database as well as in the file itself and
     * the parent node.
     * 
     * @param parentNode parent of the node to be imported
     * @param parentDocument metadata document corresponding to the parent node
     * @param childLink reference corresponding to the current node
     * @param childNodeArchiveID archive ID of the current node
     * @throws FileImporterException if there is a problem during the import
     * @throws FileExplorerException if there is a problem in the recursive exploration of the tree
     */
    public void importNode(WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument,
            Reference childLink, int childNodeArchiveID) throws NodeImporterException, NodeExplorerException;
}
