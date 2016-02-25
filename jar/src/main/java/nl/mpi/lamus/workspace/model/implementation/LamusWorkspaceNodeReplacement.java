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
package nl.mpi.lamus.workspace.model.implementation;

import java.net.URI;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @see WorkspaceNodeReplacement
 * @author guisil
 */
public class LamusWorkspaceNodeReplacement implements WorkspaceNodeReplacement {

    private URI oldNodeURI;
    private URI newNodeURI;
    private String replacementStatus = "";
    private String replacementError = "";
    
    
    public LamusWorkspaceNodeReplacement(URI oldArchiveNodeURI, URI newArchiveNodeURI) {
        this.oldNodeURI = oldArchiveNodeURI;
        this.newNodeURI = newArchiveNodeURI;
    }
    
    public LamusWorkspaceNodeReplacement(URI oldArchiveNodeURI, URI newArchiveNodeURI, String replacementStatus) {
        this(oldArchiveNodeURI, newArchiveNodeURI);
        this.replacementStatus = replacementStatus;
    }
    
    public LamusWorkspaceNodeReplacement(URI oldArchiveNodeURI, URI newArchiveNodeURI, String replacementStatus, String replacementError) {
        this(oldArchiveNodeURI, newArchiveNodeURI, replacementStatus);
        this.replacementError = replacementError;
    }
    

    @Override
    public URI getOldNodeURI() {
        return this.oldNodeURI;
    }

    @Override
    public void setOldNodeURI(URI oldNodeURI) {
        this.oldNodeURI = oldNodeURI;
    }

    @Override
    public URI getNewNodeURI() {
        return this.newNodeURI;
    }

    @Override
    public void setNewNodeURI(URI newNodeURI) {
        this.newNodeURI = newNodeURI;
    }

    @Override
    public String getReplacementStatus() {
        return this.replacementStatus;
    }

    @Override
    public void setReplacementStatus(String replacementStatus) {
        this.replacementStatus = replacementStatus;
    }

    @Override
    public String getReplacementError() {
        return this.replacementError;
    }

    @Override
    public void setReplacementError(String replacementError) {
        this.replacementError = replacementError;
    }
    
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .append(this.oldNodeURI)
                .append(this.newNodeURI)
                .append(this.replacementStatus)
                .append(this.replacementError);
                
        return hashCodeB.toHashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof LamusWorkspaceNodeReplacement)) {
            return false;
        }
        LamusWorkspaceNodeReplacement other = (LamusWorkspaceNodeReplacement) obj;
        
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .append(this.oldNodeURI, other.getOldNodeURI())
                .append(this.newNodeURI, other.getNewNodeURI())
                .append(this.replacementStatus, other.getReplacementStatus())
                .append(this.replacementError, other.getReplacementError());
        
        return equalsB.isEquals();
    }
    
    @Override
    public String toString() {
        
        String stringResult = "Old Node Archive URI: " + this.oldNodeURI +
                ", New Node Archive URI: " + this.newNodeURI +
                ", Replacement Status: " + this.replacementStatus +
                ", Replacement Error: " + this.replacementError;
        
        return stringResult;
    }
}
