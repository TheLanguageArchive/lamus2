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
import java.util.Set;
import nl.mpi.lamus.workspace.WorkspaceNode;
import nl.mpi.lamus.workspace.WorkspaceNodeStatus;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class WorkspaceNodeImpl implements WorkspaceNode {
    
    private int workspaceNodeID;
    private int workspaceID;
    private int archiveNodeID;
    private URI profileSchemaURI;
    private String name;
    private String title;
    private URL workspaceURL;
    private URL archiveURL;
    private URL originURL;
    private WorkspaceNodeStatus status;
    private String pid;
    private String format;
    private Set<Integer> parentNodes;

    public int getWorkspaceNodeID() {
        return this.workspaceNodeID;
    }
    
    public void setWorkspaceNodeID(int workspaceNodeID) {
        this.workspaceNodeID = workspaceNodeID;
    }

    public int getWorkspaceID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setWorkspaceID(int workspaceID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getArchiveNodeID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setArchiveNodeID(int archiveNodeID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URI getProfileSchemaURI() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setProfileSchemaURI(URI profileSchemaURI) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setName(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getTitle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTitle(String title) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URL getWorkspaceURL() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setWorkspaceURL(URL workspaceURL) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URL getArchiveURL() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setArchiveURL(URL archiveURL) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URL getOriginURL() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setOriginURL(URL originURL) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public WorkspaceNodeStatus getStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setStatus(WorkspaceNodeStatus status) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPid() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPid(String pid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getFormat() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setFormat(String format) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<Integer> getParentNodes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setParentNodes(Set<Integer> parentNodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
