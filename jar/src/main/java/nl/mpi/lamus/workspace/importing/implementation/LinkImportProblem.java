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
package nl.mpi.lamus.workspace.importing.implementation;

import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author guisil
 */
public class LinkImportProblem extends ImportProblem {
    
    private final WorkspaceNode parentNode;
    private final WorkspaceNode childNode;
    
    public LinkImportProblem(WorkspaceNode parentNode, WorkspaceNode childNode, String errorMessage, Exception exception) {
        super(errorMessage, exception);
        this.parentNode = parentNode;
        this.childNode = childNode;
    }
    
    public WorkspaceNode getParentNode() {
        return parentNode;
    }
    
    public WorkspaceNode getChildNode() {
        return childNode;
    }
    
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.parentNode)
                .append(this.childNode);
        
        return hashCodeB.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof LinkImportProblem)) {
            return false;
        }
        LinkImportProblem other = (LinkImportProblem) obj;
        
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.parentNode, other.getParentNode())
                .append(this.childNode, other.getChildNode());
        
        return equalsB.isEquals();
    }
}
