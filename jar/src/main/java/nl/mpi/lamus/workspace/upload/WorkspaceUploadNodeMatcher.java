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

import java.net.URI;
import java.util.Collection;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.model.Reference;

/**
 * Provides methods that aid in the matching of references with nodes
 * previously uploaded into the workspace.
 * 
 * @author guisil
 */
public interface WorkspaceUploadNodeMatcher {
    
    /**
     * Tries to find a matching node for the given handle.
     * 
     * @param workspaceID ID of the workspace
     * @param nodesToCheck Collection of (uploaded) nodes to check
     * @param handle handle for which a match should be found
     * @return node that matches the given handle, null if none is found
     */
    public WorkspaceNode findNodeForHandle(int workspaceID, Collection<WorkspaceNode> nodesToCheck, URI handle);
    
    /**
     * Tries to find a matching node for the given path.
     * 
     * @param nodesToCheck Collection of (uploaded) nodes to check
     * @param referencePath reference path to match
     * @return node that matches the given path, null if none is found
     */
    public WorkspaceNode findNodeForPath(Collection<WorkspaceNode> nodesToCheck, String referencePath);
    
    /**
     * Checks if the given URI corresponds to an external location,
     * creating the WorkspaceNode for it, if necessary.
     * 
     * @param workspaceID ID of the workspace
     * @param uri URI to be checked
     * @return node corresponding to the matching external location, null if none if found
     */
    public WorkspaceNode findExternalNodeForUri(int workspaceID, URI uri);
}
