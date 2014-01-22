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
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;

/**
 * Provides functionality related with references in uploaded files.
 * 
 * @author guisil
 */
public interface WorkspaceUploadReferenceHandler {
    
    /**
     * Tries to match the references in the current document
     * with nodes from the given list of (uploaded) nodes.
     * When a match is found, the proper link is created in the database.
     * If a reference has no match, it is removed.
     * 
     * @param workspaceID ID of the workspace
     * @param nodesToCheck Collection of uploaded nodes which are going to be checked for links between themselves
     * @param currentNode Node whose references need to be matched
     * @param currentDocument MetadataDocument corresponding to the current node
     * @param failedLinks Collection containing the references which have been matched but failed to be linked
     */
    public void matchReferencesWithNodes(
            int workspaceID, Collection<WorkspaceNode> nodesToCheck,
            WorkspaceNode currentNode, ReferencingMetadataDocument currentDocument,
            Collection<Reference> failedLinks);
}
