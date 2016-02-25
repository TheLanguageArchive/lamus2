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
package nl.mpi.lamus.web.model;

import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.model.IModel;
import org.springframework.stereotype.Component;

/**
 * Provider for the workspace model.
 * @author guisil
 */
@Component
public class WorkspaceModelProvider {
    
    /**
     * Retrieves the workspace model
     * @param workspaceId ID of the workspace
     * @return WorkspaceModel for the given workspace ID
     */
    public IModel<Workspace> getWorkspaceModel(int workspaceId) {
        return new WorkspaceModel(workspaceId);
    }
}
