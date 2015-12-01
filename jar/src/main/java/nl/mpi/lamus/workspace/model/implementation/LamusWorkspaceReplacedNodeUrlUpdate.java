/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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
import nl.mpi.lamus.workspace.model.WorkspaceReplacedNodeUrlUpdate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceReplacedNodeUrlUpdate implements WorkspaceReplacedNodeUrlUpdate {

    private URI nodeUri;
    private URI updatedUrl;
    private String updateStatus = "";
    private String updateError = "";
    
    
    public LamusWorkspaceReplacedNodeUrlUpdate(URI nodeUri, URI updatedUrl) {
        this.nodeUri = nodeUri;
        this.updatedUrl = updatedUrl;
    }
    
    public LamusWorkspaceReplacedNodeUrlUpdate(URI nodeUri, URI updatedUrl, String updateStatus) {
        this(nodeUri, updatedUrl);
        this.updateStatus = updateStatus;
    }
    
    public LamusWorkspaceReplacedNodeUrlUpdate(URI nodeUri, URI updatedUrl, String updateStatus, String updateError) {
        this(nodeUri, updatedUrl, updateStatus);
        this.updateError = updateError;
    }
   
    @Override
    public URI getNodeUri() {
        return nodeUri;
    }

    @Override
    public void setNodeUri(URI nodeUri) {
        this.nodeUri = nodeUri;
    }

    @Override
    public URI getUpdatedUrl() {
        return updatedUrl;
    }

    @Override
    public void setUpdatedUrl(URI updatedUrl) {
        this.updatedUrl = updatedUrl;
    }

    @Override
    public String getUpdateStatus() {
        return updateStatus;
    }

    @Override
    public void setUpdateStatus(String updateStatus) {
        this.updateStatus = updateStatus;
    }

    @Override
    public String getUpdateError() {
        return updateError;
    }

    @Override
    public void setUpdateError(String updateError) {
        this.updateError = updateError;
    }
    
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .append(this.nodeUri)
                .append(this.updatedUrl)
                .append(this.updateStatus)
                .append(this.updateError);
                
        return hashCodeB.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof LamusWorkspaceReplacedNodeUrlUpdate)) {
            return false;
        }
        LamusWorkspaceReplacedNodeUrlUpdate other = (LamusWorkspaceReplacedNodeUrlUpdate) obj;
        
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .append(this.nodeUri, other.getNodeUri())
                .append(this.updatedUrl, other.getUpdatedUrl())
                .append(this.updateStatus, other.getUpdateStatus())
                .append(this.updateError, other.getUpdateError());
        
        return equalsB.isEquals();
    }
    
    @Override
    public String toString() {
        
        String stringResult = "Node URI: " + this.nodeUri +
                ", Update URL: " + this.updatedUrl +
                ", Update Status: " + this.updateStatus +
                ", Update Error: " + this.updateError;
        
        return stringResult;
    }
}
