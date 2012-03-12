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

import javax.sql.DataSource;
import nl.mpi.lamus.workspace.Workspace;

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
    
}
