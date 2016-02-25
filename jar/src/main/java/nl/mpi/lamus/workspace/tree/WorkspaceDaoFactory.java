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
package nl.mpi.lamus.workspace.tree;

import java.io.Serializable;
import nl.mpi.lamus.dao.WorkspaceDao;

/**
 * Factory for WorkspaceDao objects.
 * 
 * @author guisil
 */
public interface WorkspaceDaoFactory extends Serializable {
    
    /**
     * Creates a new WorkspaceDao object
     * @return created WorkspaceDao object
     */
    public WorkspaceDao createWorkspaceDao();
}
