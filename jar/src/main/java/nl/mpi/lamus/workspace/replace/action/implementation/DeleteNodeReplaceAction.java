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
 * Implementation of the action to delete a node as part of the tree replacement.
 * 
 * @author guisil
 */
public class DeleteNodeReplaceAction extends NodeReplaceAction {
    
    public DeleteNodeReplaceAction(WorkspaceNode affectedNode) {
        this.setAffectedNode(affectedNode);
    }
    
    /**
     * @see NodeReplaceAction#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 41).
                appendSuper(super.hashCode()).
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
            if (!(obj instanceof DeleteNodeReplaceAction)) {
                    return false;
            }
            DeleteNodeReplaceAction other = (DeleteNodeReplaceAction) obj;

            return new EqualsBuilder().
                    appendSuper(super.equals(other)).
                    isEquals();
    }

    /**
     * @see NodeReplaceAction#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("Action type: ").append(getClass()).append("; ")
                .append(super.toString());
        return builder.toString();
    }
}
