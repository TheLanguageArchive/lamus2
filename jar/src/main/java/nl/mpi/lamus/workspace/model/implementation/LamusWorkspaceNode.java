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

import java.net.URI;
import java.net.URL;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @see WorkspaceNode
 * @author guisil
 */
public class LamusWorkspaceNode implements WorkspaceNode {
    
    private int workspaceNodeID;
    private int workspaceID;
    private URI profileSchemaURI;
    private String name;
    private String title;
    private WorkspaceNodeType type;
    private URL workspaceURL;
    private URI archiveURI;
    private URL archiveURL;
    private URI originURI;
    private WorkspaceNodeStatus status;
    private boolean isProtected;
    private String format;
    
    public LamusWorkspaceNode() {
        
    }
    
    public LamusWorkspaceNode(int workspaceID, URI archiveURI, URL archiveURL) {
        this.workspaceID = workspaceID;
        this.archiveURI = archiveURI;
        this.archiveURL = archiveURL;
    }
    
    public LamusWorkspaceNode(int workspaceNodeID, int workspaceID,
            URI profileSchemaURI, String name, String title, WorkspaceNodeType type,
            URL workspaceURL, URI archiveURI, URL archiveURL, URI originURI,
            WorkspaceNodeStatus status, boolean isProtected, String format) {
        
        this.workspaceNodeID = workspaceNodeID;
        this.workspaceID = workspaceID;
        this.profileSchemaURI = profileSchemaURI;
        this.name = name;
        this.title = title;
        this.type = type;
        this.workspaceURL = workspaceURL;
        this.archiveURI = archiveURI;
        this.archiveURL = archiveURL;
        this.originURI = originURI;
        this.status = status;
        this.isProtected = isProtected;
        this.format = format;
    }
    

    @Override
    public int getWorkspaceNodeID() {
        return this.workspaceNodeID;
    }
    
    @Override
    public void setWorkspaceNodeID(int workspaceNodeID) {
        this.workspaceNodeID = workspaceNodeID;
    }

    @Override
    public int getWorkspaceID() {
        return this.workspaceID;
    }
    
    @Override
    public void setWorkspaceID(int workspaceID) {
        this.workspaceID = workspaceID;
    }

    @Override
    public URI getProfileSchemaURI() {
        return this.profileSchemaURI;
    }
    
    @Override
    public void setProfileSchemaURI(URI profileSchemaURI) {
        this.profileSchemaURI = profileSchemaURI;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public WorkspaceNodeType getType() {
        return this.type;
    }
    
    @Override
    public void setType(WorkspaceNodeType type) {
        this.type = type;
    }

    @Override
    public URL getWorkspaceURL() {
        return this.workspaceURL;
    }
    
    @Override
    public void setWorkspaceURL(URL workspaceURL) {
        this.workspaceURL = workspaceURL;
    }

    @Override
    public URI getArchiveURI() {
        return this.archiveURI;
    }
    
    @Override
    public void setArchiveURI(URI archiveURI) {
        this.archiveURI = archiveURI;
    }
    
    @Override
    public URL getArchiveURL() {
        return this.archiveURL;
    }
    
    @Override
    public void setArchiveURL(URL archiveURL) {
        this.archiveURL = archiveURL;
    }

    @Override
    public URI getOriginURI() {
        return this.originURI;
    }
    
    @Override
    public void setOriginURI(URI originURI) {
        this.originURI = originURI;
    }

    @Override
    public WorkspaceNodeStatus getStatus() {
        return this.status;
    }

    @Override
    public String getStatusAsString() {
        return this.status.toString();
    }

    @Override
    public boolean isExternal() {
        return WorkspaceNodeStatus.EXTERNAL.equals(this.status) ||
                WorkspaceNodeStatus.EXTERNAL_DELETED.equals(this.status);
    }
    
    @Override
    public void setStatus(WorkspaceNodeStatus status) {
        this.status = status;
    }

    @Override
    public boolean isProtected() {
        return this.isProtected;
    }
    
    @Override
    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }
    
    @Override
    public String getFormat() {
        return this.format;
    }
    
    @Override
    public void setFormat(String format) {
        this.format = format;
    }
    

    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .append(this.workspaceNodeID)
                .append(this.workspaceID)
                .append(this.profileSchemaURI)
                .append(this.name)
                .append(this.title)
                .append(this.type)
                .append(this.workspaceURL)
                .append(this.archiveURI)
                .append(this.archiveURL)
                .append(this.originURI)
                .append(this.status)
                .append(this.isProtected)
                .append(this.format);
                
        return hashCodeB.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof LamusWorkspaceNode)) {
            return false;
        }
        LamusWorkspaceNode other = (LamusWorkspaceNode) obj;
        
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .append(this.workspaceNodeID, other.getWorkspaceNodeID())
                .append(this.workspaceID, other.getWorkspaceID())
                .append(this.profileSchemaURI, other.getProfileSchemaURI())
                .append(this.name, other.getName())
                .append(this.title, other.getTitle())
                .append(this.type, other.getType())
                .append(this.workspaceURL, other.getWorkspaceURL())
                .append(this.archiveURI, other.getArchiveURI())
                .append(this.archiveURL, other.getArchiveURL())
                .append(this.originURI, other.getOriginURI())
                .append(this.status, other.getStatus())
                .append(this.isProtected, other.isProtected())
                .append(this.format, other.getFormat());
        
        return equalsB.isEquals();
    }
    
    @Override
    public String toString() {
        
        String stringResult = "Workspace Node ID: " + this.workspaceNodeID + ", Workspace ID: " + this.workspaceID +
                ", Profile Schema URI: " + this.profileSchemaURI +
                ", Name: " + this.name + ", Title: " + this.title +
                ", Type: " + this.type + ", Workspace URL: " + this.workspaceURL +
                ", Archive URI: " + this.archiveURI + ", Archive URL: " + this.archiveURL + ", Origin URL: " + this.originURI +
                ", Status: " + this.status.name() + ", Protected: " + this.isProtected + ", Format: " + this.format;
        
        return stringResult;
    }
}
