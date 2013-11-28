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
import nl.mpi.archiving.tree.LinkedTreeNode;
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

    private WorkspaceTreeNode parentTreeNode;
    protected WorkspaceDao workspaceDao;
    
    public LamusWorkspaceTreeNode() {
        super();
    }

    public LamusWorkspaceTreeNode(int workspaceNodeID, int workspaceID,
	    URI profileSchemaURI, String name, String title, WorkspaceNodeType type,
	    URL workspaceURL, URI archiveURI, URL archiveURL, URL originURL,
	    WorkspaceNodeStatus status, String format,
	    WorkspaceTreeNode parent, WorkspaceDao dao) {

	super(workspaceNodeID, workspaceID, profileSchemaURI,
		name, title, type, workspaceURL, archiveURI, archiveURL, originURL,
		status, format);

	if (dao == null) {
	    throw new IllegalArgumentException("The WorkspaceService object should not be null.");
	}

	this.parentTreeNode = parent;
	this.workspaceDao = dao;
    }

    public LamusWorkspaceTreeNode(WorkspaceNode node, WorkspaceTreeNode parent, WorkspaceDao dao) {

	super(node.getWorkspaceNodeID(), node.getWorkspaceID(),
		node.getProfileSchemaURI(), node.getName(), node.getTitle(), node.getType(),
		node.getWorkspaceURL(), node.getArchiveURI(), node.getArchiveURL(), node.getOriginURL(),
		node.getStatus(), node.getFormat());

	if (dao == null) {
	    throw new IllegalArgumentException("The WorkspaceDao object should not be null.");
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
     * @see WorkspaceTreeNode#getIndexOfChild(nl.mpi.archiving.tree.LinkedTreeNode)
     */
    @Override
    public int getIndexOfChild(LinkedTreeNode child) {
	return this.getChildren().indexOf(child);
    }

    /**
     * @see WorkspaceTreeNode#getParent()
     */
    @Override
    public WorkspaceTreeNode getParent() {
	return this.parentTreeNode;
    }

    @Override
    public List<WorkspaceTreeNode> getChildren() {
	Collection<WorkspaceNode> children = this.workspaceDao.getChildWorkspaceNodes(this.getWorkspaceNodeID());
	List<WorkspaceTreeNode> childrenTreeNodes = new ArrayList<WorkspaceTreeNode>(children.size());
	for (WorkspaceNode child : children) {
	    WorkspaceTreeNode treeNode = new LamusWorkspaceTreeNode(
		    child.getWorkspaceNodeID(), child.getWorkspaceID(),
		    child.getProfileSchemaURI(), child.getName(), child.getTitle(),
		    child.getType(), child.getWorkspaceURL(), child.getArchiveURI(),
		    child.getArchiveURL(), child.getOriginURL(), child.getStatus(),
		    child.getFormat(), this, this.workspaceDao);
	    childrenTreeNodes.add(treeNode);
	}
	return childrenTreeNodes;
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

	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof LamusWorkspaceTreeNode)) {
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
