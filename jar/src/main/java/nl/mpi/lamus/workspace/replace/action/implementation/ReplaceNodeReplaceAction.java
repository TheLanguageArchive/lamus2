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
 * Implementation of the action to replace a node as part of the tree replacement.
 * 
 * @author guisil
 */
public class ReplaceNodeReplaceAction extends NodeReplaceAction {
    
    private WorkspaceNode newNode;
    private boolean alreadyLinked;
    private WorkspaceNode parentNode;

    public ReplaceNodeReplaceAction(WorkspaceNode affectedNode, WorkspaceNode oldNodeParent,
            WorkspaceNode newNode, boolean alreadyLinked) {
        this.setAffectedNode(affectedNode);
        this.parentNode = oldNodeParent;
        this.newNode = newNode;
        this.alreadyLinked = alreadyLinked;
    }

    /**
     * @return new version of the affected node
     */
    public WorkspaceNode getNewNode() {
        return newNode;
    }

    /**
     * @param newNode new version of the affected node
     */
    public void setNewNode(WorkspaceNode newNode) {
        this.newNode = newNode;
    }

    /**
     * @return true if the new node is already linked to the parent
     */
    public boolean isAlreadyLinked() {
        return alreadyLinked;
    }

    /**
     * @param alreadyLinked true if the new node is already linked to the parent
     */
    public void setAlreadyLinked(boolean alreadyLinked) {
        this.alreadyLinked = alreadyLinked;
    }

    /**
     * @return parent node
     */
    public WorkspaceNode getParentNode() {
        return parentNode;
    }

    /**
     * @param parentNode parent node
     */
    public void setParentNode(WorkspaceNode parentNode) {
        this.parentNode = parentNode;
    }

    /**
     * @see NodeReplaceAction#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 41).
                appendSuper(super.hashCode()).
                append(this.newNode).
                append(this.alreadyLinked).
                append(this.parentNode).
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
        if (!(obj instanceof ReplaceNodeReplaceAction)) {
            return false;
        }
        ReplaceNodeReplaceAction other = (ReplaceNodeReplaceAction) obj;
        
        return new EqualsBuilder().
                appendSuper(super.equals(other)).
                append(this.newNode, other.getNewNode()).
                append(this.alreadyLinked, other.isAlreadyLinked()).
                append(this.parentNode, other.getParentNode()).
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
                .append("New node: ").append(newNode.getWorkspaceNodeID()).append("; ")
                .append("Already linked: ").append(alreadyLinked).append("; ")
                .append("Parent node: ").append(parentNode.getWorkspaceNodeID());
        return builder.toString();
    }
}
