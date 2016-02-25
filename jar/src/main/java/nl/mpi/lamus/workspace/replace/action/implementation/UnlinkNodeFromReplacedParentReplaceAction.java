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
package nl.mpi.lamus.workspace.replace.action.implementation;

import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Implementation of the action to unlink nodes as part of the tree replacement.
 * 
 * @author guisil
 */
public class UnlinkNodeFromReplacedParentReplaceAction extends NodeReplaceAction {
    
        private WorkspaceNode newParentNode;
	
    public UnlinkNodeFromReplacedParentReplaceAction(WorkspaceNode affectedNode, WorkspaceNode newParentNode) {
        this.setAffectedNode(affectedNode);
        this.newParentNode = newParentNode;
    }
    
    /**
     * @return parent node which replaced the parent from which the affected node will be unlinked
     */
    public WorkspaceNode getNewParentNode() {
        return newParentNode;
    }

    /**
     * @param newParentNode parent node which replaced the parent from which the affected node will be unlinked
     */
    public void setNewParentNode(WorkspaceNode newParentNode) {
        this.newParentNode = newParentNode;
    }

    /**
     * @see NodeReplaceAction#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 41).
                appendSuper(super.hashCode()).
                append(this.newParentNode).
                toHashCode();
    }

    /**
     * @see NodeReplaceAction#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
                return true;
        }
        if (!(obj instanceof UnlinkNodeFromReplacedParentReplaceAction)) {
                return false;
        }
        UnlinkNodeFromReplacedParentReplaceAction other = (UnlinkNodeFromReplacedParentReplaceAction) obj;

        return new EqualsBuilder().
                appendSuper(super.equals(other)).
                append(this.newParentNode, other.getNewParentNode()).
                isEquals();
    }
    
    /**
     * @see NodeReplaceAction#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("Action type: ").append(getClass()).append("; ")
                .append(super.toString()).append("; ")
                .append("Parent node: ").append(newParentNode.getWorkspaceNodeID());
        return builder.toString();
    }
}
