/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web.model.mock;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import nl.mpi.archiving.tree.LinkedTreeNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceParentNodeReference;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;

/**
 * implements interface which is intended to be used as the source for the
 * graphical representation of the workspace tree.
 *
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
public class MockWorkspaceTreeNode implements WorkspaceTreeNode {

    private WorkspaceTreeNode parent;
    private int workspaceNodeID;
    private int workspaceID;
    private URI profileSchemaURI;
    private String name;
    private String title;
    private WorkspaceNodeType type;
    private URL workspaceURL;
    private URI archiveURI;
    private URL archiveURL;
    private URL originURL;
    private WorkspaceNodeStatus status;
    private String format;
    private Collection<WorkspaceParentNodeReference> parentNodesReferences;

    private List<WorkspaceTreeNode> children= Collections.emptyList();
    
    public void setChildren(List<WorkspaceTreeNode> children) {
        this.children = children;
    }
    
    @Override
    public WorkspaceTreeNode getChild(int index) {
        return children.get(index); 
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public int getIndexOfChild(LinkedTreeNode child) {
        return children.indexOf(child);
    }

    @Override
    public WorkspaceTreeNode getParent() {
        return parent;
    }
    
    public void setParent(WorkspaceTreeNode parent) {
        this.parent = parent;
    }


    @Override
    public int getWorkspaceNodeID() {
        return workspaceNodeID;
    }
    
    @Override
    public void setWorkspaceNodeID(int workspaceNodeID) {
        this.workspaceNodeID = workspaceNodeID;
    }

    @Override
    public int getWorkspaceID() {
        return workspaceID;
    }
    
    @Override
    public void setWorkspaceID(int workspaceID) {
        this.workspaceID = workspaceID;
    }

    @Override
    public URI getProfileSchemaURI() {
        return profileSchemaURI;
    }
    
    @Override
    public void setProfileSchemaURI(URI profileSchemaURI) {
        this.profileSchemaURI = profileSchemaURI;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public WorkspaceNodeType getType() {
        return type;
    }
    
    @Override
    public boolean isMetadata() {
        return WorkspaceNodeType.METADATA.equals(this.type);
    }
    
    @Override
    public void setType(WorkspaceNodeType type) {
        this.type = type;
    }

    @Override
    public URL getWorkspaceURL() {
        return workspaceURL;
    }
    
    @Override
    public void setWorkspaceURL(URL workspaceURL) {
        this.workspaceURL = workspaceURL;
    }

    @Override
    public URI getArchiveURI() {
        return archiveURI;
    }
    
    @Override
    public void setArchiveURI(URI archiveURI) {
        this.archiveURI = archiveURI;
    }
    
    @Override
    public URL getArchiveURL() {
        return archiveURL;
    }
    
    @Override
    public void setArchiveURL(URL archiveURL) {
        this.archiveURL = archiveURL;
    }

    @Override
    public URL getOriginURL() {
        return originURL;
    }
    
    @Override
    public void setOriginURL(URL originURL) {
        this.originURL = originURL;
    }

    @Override
    public WorkspaceNodeStatus getStatus() {
        return status;
    }

    @Override
    public boolean isExternal() {
        return WorkspaceNodeStatus.NODE_EXTERNAL.equals(status);
    }

    @Override
    public void setStatus(WorkspaceNodeStatus status) {
        this.status = status;
    }

    @Override
    public String getFormat() {
        return format;
    }
    
    @Override
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public Collection<WorkspaceParentNodeReference> getParentNodesReferences() {
        return Collections.unmodifiableCollection(parentNodesReferences);
    }

    @Override
    public void setParentNodesReferences(Collection<WorkspaceParentNodeReference> parentNodesReferences) {
        this.parentNodesReferences = parentNodesReferences;
    }
    
    @Override
    public void addParentNodeReference(WorkspaceParentNodeReference parentNodeReference) {
        if(parentNodesReferences == null) {
            parentNodesReferences = new ArrayList<WorkspaceParentNodeReference>();
        }
        parentNodesReferences.add(parentNodeReference);
    }

    @Override
    public String toString() {
	return getName();
    }

    @Override
    public List<WorkspaceTreeNode> getChildren() {
        return children;
    }
    
}
