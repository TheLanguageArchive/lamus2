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
package nl.mpi.lamus.workspace.tree.implementation;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.mpi.archiving.tree.GenericTreeNode;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @see WorkspaceTreeNode
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceTreeNode extends LamusWorkspaceNode implements WorkspaceTreeNode {
    
    private List<WorkspaceTreeNode> childrenTreeNodes;
    private WorkspaceTreeNode parentTreeNode;
    private WorkspaceDao workspaceDao;
    
    public LamusWorkspaceTreeNode(int workspaceNodeID, int workspaceID, int archiveNodeID,
            URI profileSchemaURI, String name, String title, WorkspaceNodeType type,
            URL workspaceURL, URL archiveURL, URL originURL,
            WorkspaceNodeStatus status, String pid, String format,
            WorkspaceTreeNode parent, WorkspaceDao dao) {
        
        super(workspaceNodeID, workspaceID, archiveNodeID, profileSchemaURI,
                name, title, type, workspaceURL, archiveURL, originURL,
                status, pid, format);
        
        if(dao == null) {
            throw new IllegalArgumentException("The WorkspaceService object should not be null.");
        }
        
        this.parentTreeNode = parent;
        this.workspaceDao = dao;
    }
    
    public LamusWorkspaceTreeNode(WorkspaceNode node, WorkspaceTreeNode parent, WorkspaceDao dao) {
        
        super(node.getWorkspaceNodeID(), node.getWorkspaceID(), node.getArchiveNodeID(),
                node.getProfileSchemaURI(), node.getName(), node.getTitle(), node.getType(),
                node.getWorkspaceURL(), node.getArchiveURL(), node.getOriginURL(),
                node.getStatus(), node.getPid(), node.getFormat());
        
        if(dao == null) {
            throw new IllegalArgumentException("The WorkspaceService object should not be null.");
        }
        
        this.parentTreeNode = parent;
        this.workspaceDao = dao;
    }

    /**
     * @see WorkspaceTreeNode#getChild(int)
     */
    @Override
    public WorkspaceTreeNode getChild(int index) {
        return this.getChildren().get(index);
    }

    /**
     * @see WorkspaceTreeNode#getChildCount()
     */
    @Override
    public int getChildCount() {
        return this.getChildren().size();
    }

    /**
     * @see WorkspaceTreeNode#getIndexOfChild(nl.mpi.archiving.tree.GenericTreeNode)
     */
    @Override
    public int getIndexOfChild(GenericTreeNode child) {
        return this.getChildren().indexOf(child);
    }

    /**
     * @see WorkspaceTreeNode#getParent()
     */
    @Override
    public WorkspaceTreeNode getParent() {
        return this.parentTreeNode;
    }
    
    private List<WorkspaceTreeNode> getChildren() {
        
        if(this.childrenTreeNodes == null) {
            
            this.childrenTreeNodes = new ArrayList<WorkspaceTreeNode>();
            Collection<WorkspaceNode> children = this.workspaceDao.getChildWorkspaceNodes(this.getWorkspaceNodeID());
            for(WorkspaceNode child : children) {
                WorkspaceTreeNode treeNode = new LamusWorkspaceTreeNode(
                        child.getWorkspaceNodeID(), child.getWorkspaceID(), child.getArchiveNodeID(),
                        child.getProfileSchemaURI(), child.getName(), child.getTitle(),
                        child.getType(), child.getWorkspaceURL(), child.getArchiveURL(),
                        child.getOriginURL(), child.getStatus(), child.getPid(),
                        child.getFormat(), this, this.workspaceDao);
                this.childrenTreeNodes.add(treeNode);
            }
        }
        return this.childrenTreeNodes;
    }

    
    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .appendSuper(super.hashCode())
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
                .appendSuper(super.equals(obj))
                .append(this.parentTreeNode, other.getParent());
        
        return equalsB.isEquals();
    }
    
    @Override
    public String toString() {
        
        return this.getName();
    }
}
