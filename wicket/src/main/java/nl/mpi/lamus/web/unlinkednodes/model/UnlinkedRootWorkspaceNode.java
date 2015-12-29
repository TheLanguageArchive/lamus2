/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web.unlinkednodes.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;

/**
 *
 * @author guisil
 */
public class UnlinkedRootWorkspaceNode extends UnlinkedWorkspaceNode {
    
    public UnlinkedRootWorkspaceNode() {
        super();
    }
    
    public UnlinkedRootWorkspaceNode(int workspaceID, WorkspaceDao dao) {
        super(-1, workspaceID, null, "UnlinkedRootNode", "", WorkspaceNodeType.UNKNOWN, null, null, null, null, WorkspaceNodeStatus.CREATED, false, "", null, dao);
    }
    
    @Override
    public List<WorkspaceTreeNode> getChildren() {
        Collection<WorkspaceNode> children = this.workspaceDao.getUnlinkedNodes(this.getWorkspaceID());
	List<WorkspaceTreeNode> childrenTreeNodes = new ArrayList<>(children.size());
	for (WorkspaceNode child : children) {
	    UnlinkedWorkspaceNode treeNode = new UnlinkedWorkspaceNode(
		    child.getWorkspaceNodeID(), child.getWorkspaceID(),
		    child.getProfileSchemaURI(), child.getName(), child.getTitle(),
		    child.getType(), child.getWorkspaceURL(), child.getArchiveURI(),
		    child.getArchiveURL(), child.getOriginURI(), child.getStatus(),
		    child.isProtected(), child.getFormat(), this, this.workspaceDao);
	    childrenTreeNodes.add(treeNode);
	}
	return childrenTreeNodes;
    }
}
