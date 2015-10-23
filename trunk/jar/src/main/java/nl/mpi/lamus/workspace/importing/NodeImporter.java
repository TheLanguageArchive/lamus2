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

import java.net.URI;
import nl.mpi.lamus.exception.WorkspaceImportException;
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
     * Imports the given file into the workspace, taking into account the
     * information in the archive database as well as in the file itself and
     * the parent node.
     * 
     * @param workspace workspace where to import the node
     * @param parentNode parent of the node to be imported
     * @param parentDocument metadata document corresponding to the parent node
     * @param referenceFromParent reference corresponding to the current node
     */
    public void importNode(Workspace workspace, WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument,
            Reference referenceFromParent) throws WorkspaceImportException;
}
