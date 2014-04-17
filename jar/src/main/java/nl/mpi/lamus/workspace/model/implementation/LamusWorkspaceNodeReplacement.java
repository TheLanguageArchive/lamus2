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
 *
 * @author guisil
 */
public class LamusWorkspaceNodeReplacement implements WorkspaceNodeReplacement {

    private URI oldArchiveNodeURI;
    private URI newArchiveNodeURI;
    private String replacementStatus = "";
    private String replacementError = "";
    
    public LamusWorkspaceNodeReplacement(URI oldArchiveNodeURI, URI newArchiveNodeURI) {
        this.oldArchiveNodeURI = oldArchiveNodeURI;
        this.newArchiveNodeURI = newArchiveNodeURI;
    }
    
    public LamusWorkspaceNodeReplacement(URI oldArchiveNodeURI, URI newArchiveNodeURI, String replacementStatus) {
        this.oldArchiveNodeURI = oldArchiveNodeURI;
        this.newArchiveNodeURI = newArchiveNodeURI;
        this.replacementStatus = replacementStatus;
    }
    
    public LamusWorkspaceNodeReplacement(URI oldArchiveNodeURI, URI newArchiveNodeURI, String replacementStatus, String replacementError) {
        this.oldArchiveNodeURI = oldArchiveNodeURI;
        this.newArchiveNodeURI = newArchiveNodeURI;
        this.replacementStatus = replacementStatus;
        this.replacementError = replacementError;
    }
    

    @Override
    public URI getOldArchiveNodeURI() {
        return this.oldArchiveNodeURI;
    }

    @Override
    public void setOldArchiveNodeURI(URI oldNodeURI) {
        this.oldArchiveNodeURI = oldNodeURI;
    }

    @Override
    public URI getNewArchiveNodeURI() {
        return this.newArchiveNodeURI;
    }

    @Override
    public void setNewArchiveNodeURI(URI newNodeURI) {
        this.newArchiveNodeURI = newNodeURI;
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
                .append(this.oldArchiveNodeURI)
                .append(this.newArchiveNodeURI)
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
                .append(this.oldArchiveNodeURI, other.getOldArchiveNodeURI())
                .append(this.newArchiveNodeURI, other.getNewArchiveNodeURI())
                .append(this.replacementStatus, other.getReplacementStatus())
                .append(this.replacementError, other.replacementError);
        
        return equalsB.isEquals();
    }
    
    @Override
    public String toString() {
        
        String stringResult = "Old Node Archive URI: " + this.oldArchiveNodeURI +
                ", New Node Archive URI: " + this.newArchiveNodeURI +
                ", Replacement Status: " + this.replacementStatus +
                ", Replacement Error: " + this.replacementError;
        
        return stringResult;
    }
}
