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
import java.net.URL;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;

/**
 * Factory for WorkspaceNode objects.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceNodeFactory {
    
    /**
     * Creates a WorkspaceNode object with the given values, while some others are
     * injected or set as a default value.
     * 
     * @param workspaceID ID of the workspace to which the node should be connected
     * @param archiveNodeURI archive URI of the node
     * @param archiveNodeURL archive URL of the node
     * @return created WorkspaceNode object
     */
    public WorkspaceNode getNewWorkspaceNode(int workspaceID, URI archiveNodeURI, URL archiveNodeURL);
    
    /**
     * Creates a metadata WorkspaceNode with the given values.
     * 
     * @param workspaceID ID of the workspace to which the node should be connected
     * @param archiveNodeURI archive URI of the node
     * @param archiveNodeURL archive URL of the node
     * @param document MetadataDocument object corresponding to the file
     * @param name Name of the node
     * @return created WorkspaceNode object
     */
    public WorkspaceNode getNewWorkspaceMetadataNode(
            int workspaceID, URI archiveNodeURI, URL archiveNodeURL, MetadataDocument document, String name);

    /**
     * Creates a resource WorkspaceNode with the given values.
     * 
     * @param workspaceID ID of the workspace to which the node should be connected
     * @param archiveNodeURI archive URI of the node
     * @param archiveNodeURL archive URL of the node
     * @param resourceReference Reference to the node, in the parent metadata file
     * @param mimetype Mimetype of the file
     * @param name name of the node
     * @return created WorkspaceNode object
     */
    public WorkspaceNode getNewWorkspaceResourceNode(int workspaceID, URI archiveNodeURI, URL archiveNodeURL,
            Reference resourceReference, String mimetype, String name);

    /**
     * Creates a WorkspaceNode with the given values.
     * 
     * @param workspaceID ID of the workspace to which the node should be connected
     * @param originURL URL of the original file location (mostly for uploaded files)
     * @param workspaceURL URL of the file location in the workspace
     * @param mimetype Mimetype of the file
     * @param status Status of the node
     * @return 
     */
    public WorkspaceNode getNewWorkspaceNodeFromFile(int workspaceID, URL originURL, URL workspaceURL,
            String mimetype, WorkspaceNodeStatus status);
}
