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
package nl.mpi.lamus.tree.implementation;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.mpi.archiving.tree.GenericTreeNode;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceParentNodeReference;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceTreeNode implements WorkspaceTreeNode {
    
    private WorkspaceNode workspaceNode;
    private List<WorkspaceTreeNode> childrenTreeNodes;
    private WorkspaceTreeNode parentTreeNode;
    private WorkspaceDao workspaceDao;
    
    public LamusWorkspaceTreeNode(WorkspaceNode node, WorkspaceTreeNode parent, WorkspaceDao dao) {
        
        if(dao == null) {
            throw new IllegalArgumentException("Cannot create LamusWorkspaceTreeNode from a null WorkspaceDao.");
        }
        if(node == null) {
            throw new IllegalArgumentException("Cannot create LamusWorkspaceTreeNode from a null WorkspaceNode.");
        }
        
        this.workspaceNode = node;
        this.parentTreeNode = parent;
        this.workspaceDao = dao;
    }

    public WorkspaceTreeNode getChild(int index) {
        return this.getChildren().get(index);
    }

    public int getChildCount() {
        return this.getChildren().size();
    }

    public int getIndexOfChild(GenericTreeNode child) {
        return this.getChildren().indexOf(child);
    }

    public WorkspaceTreeNode getParent() {
        return this.parentTreeNode;
    }
    
    private List<WorkspaceTreeNode> getChildren() {
        
        if(this.childrenTreeNodes == null) {
            this.childrenTreeNodes = new ArrayList<WorkspaceTreeNode>();
            Collection<WorkspaceNode> children = this.workspaceDao.getChildWorkspaceNodes(this.workspaceNode.getWorkspaceNodeID());
            for(WorkspaceNode child : children) {
                WorkspaceTreeNode treeNode = new LamusWorkspaceTreeNode(child, this, this.workspaceDao);
                this.childrenTreeNodes.add(treeNode);
            }
        }
        return this.childrenTreeNodes;
    }

    public int getWorkspaceNodeID() {
        return this.workspaceNode.getWorkspaceNodeID();
    }

    public void setWorkspaceNodeID(int workspaceNodeID) {
        this.workspaceNode.setWorkspaceNodeID(workspaceNodeID);
    }

    public int getWorkspaceID() {
        return this.workspaceNode.getWorkspaceID();
    }

    public void setWorkspaceID(int workspaceID) {
        this.workspaceNode.setWorkspaceID(workspaceID);
    }

    public int getArchiveNodeID() {
        return this.workspaceNode.getArchiveNodeID();
    }

    public void setArchiveNodeID(int archiveNodeID) {
        this.workspaceNode.setArchiveNodeID(archiveNodeID);
    }

    public URI getProfileSchemaURI() {
        return this.workspaceNode.getProfileSchemaURI();
    }

    public void setProfileSchemaURI(URI profileSchemaURI) {
        this.workspaceNode.setProfileSchemaURI(profileSchemaURI);
    }

    public String getName() {
        return this.workspaceNode.getName();
    }

    public void setName(String name) {
        this.workspaceNode.setName(name);
    }

    public String getTitle() {
        return this.workspaceNode.getTitle();
    }

    public void setTitle(String title) {
        this.workspaceNode.setTitle(title);
    }

    public WorkspaceNodeType getType() {
        return this.workspaceNode.getType();
    }

    public void setType(WorkspaceNodeType type) {
        this.workspaceNode.setType(type);
    }

    public URL getWorkspaceURL() {
        return this.workspaceNode.getWorkspaceURL();
    }

    public void setWorkspaceURL(URL workspaceURL) {
        this.workspaceNode.setWorkspaceURL(workspaceURL);
    }

    public URL getArchiveURL() {
        return this.workspaceNode.getArchiveURL();
    }

    public void setArchiveURL(URL archiveURL) {
        this.workspaceNode.setArchiveURL(archiveURL);
    }

    public URL getOriginURL() {
        return this.workspaceNode.getOriginURL();
    }

    public void setOriginURL(URL originURL) {
        this.workspaceNode.setOriginURL(originURL);
    }

    public WorkspaceNodeStatus getStatus() {
        return this.workspaceNode.getStatus();
    }

    public void setStatus(WorkspaceNodeStatus status) {
        this.workspaceNode.setStatus(status);
    }

    public String getPid() {
        return this.workspaceNode.getPid();
    }

    public void setPid(String pid) {
        this.workspaceNode.setPid(pid);
    }

    public String getFormat() {
        return this.workspaceNode.getFormat();
    }

    public void setFormat(String format) {
        this.workspaceNode.setFormat(format);
    }

    public Collection<WorkspaceParentNodeReference> getParentNodesReferences() {
        return this.workspaceNode.getParentNodesReferences();
    }

    public void setParentNodesReferences(Collection<WorkspaceParentNodeReference> parentNodeReferences) {
        this.workspaceNode.setParentNodesReferences(parentNodeReferences);
    }

    public void addParentNodeReference(WorkspaceParentNodeReference parentNodeReference) {
        this.workspaceNode.addParentNodeReference(parentNodeReference);
    }

    public WorkspaceNode getWorkspaceNode() {
        return this.workspaceNode;
    }
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .append(this.workspaceNode)
                .append(this.parentTreeNode);
                
        return hashCodeB.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof LamusWorkspaceTreeNode)) {
            return false;
        }
        LamusWorkspaceTreeNode other = (LamusWorkspaceTreeNode) obj;
        
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .append(this.workspaceNode, other.getWorkspaceNode())
                .append(this.parentTreeNode, other.getParent());
        
        return equalsB.isEquals();
    }
    
    @Override
    public String toString() {
        
        return this.workspaceNode.toString() + "\nWorkspace Tree Parent: " + this.parentTreeNode;
    }
}
