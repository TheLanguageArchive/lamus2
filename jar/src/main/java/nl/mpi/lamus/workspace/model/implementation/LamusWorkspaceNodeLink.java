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
package nl.mpi.lamus.workspace.model.implementation;

import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @see WorkspaceNodeLink
 * @author guisil
 */
public class LamusWorkspaceNodeLink implements WorkspaceNodeLink {

    private final int parentWorkspaceNodeID;
    private final int childWorkspaceNodeID;
    
    public LamusWorkspaceNodeLink(int parentWorkspaceNodeID, int childWorkspaceNodeID) {
        this.parentWorkspaceNodeID = parentWorkspaceNodeID;
        this.childWorkspaceNodeID = childWorkspaceNodeID;
    }
    
    
    @Override
    public int getParentWorkspaceNodeID() {
        return this.parentWorkspaceNodeID;
    }

    @Override
    public int getChildWorkspaceNodeID() {
        return this.childWorkspaceNodeID;
    }
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .append(this.parentWorkspaceNodeID)
                .append(this.childWorkspaceNodeID);
                
        return hashCodeB.toHashCode();
    }

    
    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof LamusWorkspaceNodeLink)) {
            return false;
        }
        LamusWorkspaceNodeLink other = (LamusWorkspaceNodeLink) obj;
        
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .append(this.parentWorkspaceNodeID, other.getParentWorkspaceNodeID())
                .append(this.childWorkspaceNodeID, other.getChildWorkspaceNodeID());
        
        return equalsB.isEquals();
    }
    
    @Override
    public String toString() {
        
        String stringResult = "Parent Workspace Node ID: " + this.parentWorkspaceNodeID +
                ", Child Workspace Node ID: " + this.childWorkspaceNodeID;
        
        return stringResult;
    }
}
