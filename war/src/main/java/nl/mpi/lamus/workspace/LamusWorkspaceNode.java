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

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceNode implements WorkspaceNode {
    
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
        return this.workspaceID;
    }
    
    public void setWorkspaceID(int workspaceID) {
        this.workspaceID = workspaceID;
    }

    public int getArchiveNodeID() {
        return this.archiveNodeID;
    }
    
    public void setArchiveNodeID(int archiveNodeID) {
        this.archiveNodeID = archiveNodeID;
    }

    public URI getProfileSchemaURI() {
        return this.profileSchemaURI;
    }
    
    public void setProfileSchemaURI(URI profileSchemaURI) {
        this.profileSchemaURI = profileSchemaURI;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public URL getWorkspaceURL() {
        return this.workspaceURL;
    }
    
    public void setWorkspaceURL(URL workspaceURL) {
        this.workspaceURL = workspaceURL;
    }

    public URL getArchiveURL() {
        return this.archiveURL;
    }
    
    public void setArchiveURL(URL archiveURL) {
        this.archiveURL = archiveURL;
    }

    public URL getOriginURL() {
        return this.originURL;
    }
    
    public void setOriginURL(URL originURL) {
        this.originURL = originURL;
    }

    public WorkspaceNodeStatus getStatus() {
        return this.status;
    }

    public void setStatus(WorkspaceNodeStatus status) {
        this.status = status;
    }

    public String getPid() {
        return this.pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getFormat() {
        return this.format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }

    public Set<Integer> getParentNodes() {
        return this.parentNodes;
    }

    public void setParentNodes(Set<Integer> parentNodes) {
        this.parentNodes = parentNodes;
    }
    
}
