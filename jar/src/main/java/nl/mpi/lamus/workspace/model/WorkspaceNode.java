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
package nl.mpi.lamus.workspace.model;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;

/**
 * Represents a node within a workspace.
 * @author guisil
 */
public interface WorkspaceNode extends Serializable {
    
    public int getWorkspaceNodeID();
    
    public void setWorkspaceNodeID(int workspaceNodeID);
    
    public int getWorkspaceID();
    
    public void setWorkspaceID(int workspaceID);
    
    public URI getProfileSchemaURI();
    
    public void setProfileSchemaURI(URI profileSchemaURI);
    
    public String getName();
    
    public void setName(String name);
    
    public String getTitle();
    
    public void setTitle(String title);
    
    public WorkspaceNodeType getType();
    
    public void setType(WorkspaceNodeType type);
    
    public URL getWorkspaceURL();
    
    public void setWorkspaceURL(URL workspaceURL);
    
    public URI getArchiveURI();
    
    public void setArchiveURI(URI archiveURI);
    
    public URL getArchiveURL();
    
    public void setArchiveURL(URL archiveURL);
    
    public URI getOriginURI();
    
    public void setOriginURI(URI originURI);
    
    public WorkspaceNodeStatus getStatus();
    
    public String getStatusAsString();
    
    public boolean isExternal();
    
    public void setStatus(WorkspaceNodeStatus status);
    
    public boolean isProtected();
    
    public void setProtected(boolean isProtected);
    
    public String getFormat();
    
    public void setFormat(String format);
    
}
