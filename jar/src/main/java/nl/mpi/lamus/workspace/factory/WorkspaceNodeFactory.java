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
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
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
     * @param onSite true if node is on site (in the local archive)
     * @param isProtected true if node is to be protected from changes (e.g. has multiple parents)
     * @return created WorkspaceNode object
     */
    public WorkspaceNode getNewWorkspaceMetadataNode(
            int workspaceID, URI archiveNodeURI, URL archiveNodeURL,
            MetadataDocument document, String name, boolean onSite, boolean isProtected);

    /**
     * Creates a resource WorkspaceNode with the given values.
     * 
     * @param workspaceID ID of the workspace to which the node should be connected
     * @param archiveNodeURI archive URI of the node
     * @param archiveNodeURL archive URL of the node
     * @param resourceReference Reference to the node, in the parent metadata file
     * @param mimetype Mimetype of the file
     * @param nodeType node type for the file
     * @param name name of the node
     * @param onSite true if node is on site (in the local archive)
     * @param isProtected true if node is to be protected from changes (e.g. has multiple parents)
     * @return created WorkspaceNode object
     */
    public WorkspaceNode getNewWorkspaceNode(int workspaceID, URI archiveNodeURI, URL archiveNodeURL,
            Reference resourceReference, String mimetype, WorkspaceNodeType nodeType,
            String name, boolean onSite, boolean isProtected);

    /**
     * Creates a WorkspaceNode with the given values.
     * 
     * @param workspaceID ID of the workspace to which the node should be connected
     * @param archiveURI URI (handle) from the archive, if the node comes from there
     * @param originURI URI of the original file location (mostly for uploaded files)
     * @param workspaceURL URL of the file location in the workspace
     * @param profileSchemaURI URI of the profile schema, in case it's a metadata file
     * @param name Name to use as node name
     * @param mimetype Mimetype of the file
     * @param nodeType node type for the file
     * @param status Status of the node
     * @param isProtected true if node is to be protected from changes (e.g. has multiple parents)
     * @return created WorkspaceNode object
     */
    public WorkspaceNode getNewWorkspaceNodeFromFile(int workspaceID, URI archiveURI,
            URI originURI, URL workspaceURL, URI profileSchemaURI, String name,
            String mimetype, WorkspaceNodeType nodeType,
            WorkspaceNodeStatus status, boolean isProtected);
    
    /**
     * Creates an external WorkspaceNode with the given values
     * 
     * @param workpaceID ID of the workspace to which the node should be connected
     * @param originURI URI referencing the file
     * @return created WorkspaceNode object
     */
    public WorkspaceNode getNewExternalNode(int workpaceID, URI originURI);
    
    /**
     * Creates an external WorkspaceNode which is a pointer to an archive node.
     * 
     * @param workspaceID ID of the workspace to which the node should be connected
     * @param archiveNode CorpusNode object corresponding to the archive node
     * @param archivePID PID of the node in the archive
     * @param archiveURL URL of the node in the archive
     * @return created WorkspaceNode object
     */
    public WorkspaceNode getNewExternalNodeFromArchive(int workspaceID, CorpusNode archiveNode, URI archivePID, URL archiveURL);
}
