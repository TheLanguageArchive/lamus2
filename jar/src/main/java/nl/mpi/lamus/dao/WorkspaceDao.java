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
package nl.mpi.lamus.dao;

import java.util.Collection;
import javax.sql.DataSource;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceDao {
    
    
    /**
     * 
     * @param datasource 
     */
    public void setDataSource(DataSource datasource);
    
    /**
     * 
     * @param workspace
     * @return 
     */
    public void addWorkspace(Workspace workspace);
    
    /**
     * 
     * @param workspace 
     */
    public void updateWorkspaceTopNode(Workspace workspace);
    
    /**
     * 
     * @param workspace 
     */
    public void updateWorkspaceSessionDates(Workspace workspace);
    
    /**
     * 
     * @param workspace 
     */
    public void updateWorkspaceStorageSpace(Workspace workspace);

    /**
     * 
     * @param workspace 
     */
    public void updateWorkspaceStatusMessage(Workspace workspace);
    
    /**
     * 
     * @param workspaceID
     * @return 
     */
    public Workspace getWorkspace(int workspaceID);

    /**
     * 
     * @param archiveNodeID
     * @return 
     */
    public boolean isNodeLocked(int archiveNodeID);
    
    /**
     * 
     * @param node
     * @return 
     */
    public void addWorkspaceNode(WorkspaceNode node);
    
    /**
     * 
     * @param workspaceNodeID
     * @return 
     */
    public WorkspaceNode getWorkspaceNode(int workspaceNodeID);
    
    /**
     * 
     * @param workspaceNodeID
     * @return 
     */
    public Collection<WorkspaceNode> getChildWorkspaceNodes(int workspaceNodeID);
    
    /**
     * 
     * @param link 
     */
    public void addWorkspaceNodeLink(WorkspaceNodeLink nodeLink);
}
