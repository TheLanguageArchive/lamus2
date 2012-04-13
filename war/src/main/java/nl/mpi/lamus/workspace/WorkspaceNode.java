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
package nl.mpi.lamus.workspace;

import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceNode {
    
    public int getWorkspaceNodeID();
    
    public void setWorkspaceNodeID(int workspaceNodeID);
    
    public int getWorkspaceID();
    
    public void setWorkspaceID(int workspaceID);
    
    public int getArchiveNodeID();
    
    public void setArchiveNodeID(int archiveNodeID);
    
    public URI getProfileSchemaURI();
    
    public void setProfileSchemaURI(URI profileSchemaURI);
    
    public String getName();
    
    public void setName(String name);
    
    public String getTitle();
    
    public void setTitle(String title);
    
    public String getType();
    
    public void setType(String type);
    
    public URL getWorkspaceURL();
    
    public void setWorkspaceURL(URL workspaceURL);
    
    public URL getArchiveURL();
    
    public void setArchiveURL(URL archiveURL);
    
    public URL getOriginURL();
    
    public void setOriginURL(URL originURL);
    
    public WorkspaceNodeStatus getStatus();
    
    public void setStatus(WorkspaceNodeStatus status);
    
    public String getPid();
    
    public void setPid(String pid);
    
    public String getFormat();
    
    public void setFormat(String format);
    
    public Map<Integer, String> getParentNodesReferences();
    
    public void setParentNodesReferences(Map<Integer, String> parentNodes);
    
}
