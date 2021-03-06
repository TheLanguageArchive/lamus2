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
import nl.mpi.metadata.api.model.Reference;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author guisil
 */
public class MatchImportProblem extends ImportProblem {
    
    private final WorkspaceNode parentNode;
    private final Reference childReference;
    
    public MatchImportProblem(WorkspaceNode parentNode, Reference childReference, String errorMessage, Exception exception) {
        super(errorMessage, exception);
        this.parentNode = parentNode;
        this.childReference = childReference;
    }
    
    public WorkspaceNode getParentNode() {
        return parentNode;
    }
    
    public Reference getChildReference() {
        return childReference;
    }
    
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.parentNode)
                .append(this.childReference);
        
        return hashCodeB.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof MatchImportProblem)) {
            return false;
        }
        MatchImportProblem other = (MatchImportProblem) obj;
        
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.parentNode, other.getParentNode())
                .append(this.childReference, other.getChildReference());
        
        return equalsB.isEquals();
    }
}
