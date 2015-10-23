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
 * Base interface for the actions performed during the node / tree replacement.
 * 
 * @author guisil
 */
public abstract class NodeReplaceAction {
    
    private WorkspaceNode affectedNode;

    /**
     * @return node to be affected by the action
     */
    public WorkspaceNode getAffectedNode() {
            return affectedNode;
    }

    /**
     * @param affectedNode node to be affected by the action
     */
    public void setAffectedNode(WorkspaceNode affectedNode) {
            this.affectedNode = affectedNode;
    }
    
    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
            return new HashCodeBuilder(13, 41).
                    append(this.affectedNode).
                    toHashCode();
    }

    /**
     * @see Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
            if (this == obj) {
                    return true;
            }
            if (!(obj instanceof NodeReplaceAction)) {
                    return false;
            }
            NodeReplaceAction other = (NodeReplaceAction) obj;

            return new EqualsBuilder().
                    append(this.affectedNode, other.getAffectedNode()).
                    isEquals();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "Affected Node: " + (affectedNode != null ? affectedNode.getWorkspaceNodeID() : "null");
    }
}
