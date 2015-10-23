/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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

import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.Session;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Workspace model that allows detaching, loading via {@link WorkspaceService}
 * by {@link Workspace#getWorkspaceID() workspaceId}
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author guisil
 * @see WorkspaceService
 */
public class WorkspaceModel extends Model<Workspace> { // LoadableDetachableModel<Workspace> {

    // Services to be injected
    @SpringBean
    private WorkspaceService workspaceService;
    // Workspace identifier
    private final Integer workspaceId;

    @SuppressWarnings("LeakingThisInConstructor")
    public WorkspaceModel(int workspaceId) {
        super();
        this.workspaceId = workspaceId;
        // Get workspaceService injected
        Injector.get().inject(this);
    }

    @Override
    public Workspace getObject() {
        if (workspaceId == null) {
            return null;
        } else {
            try {
                return workspaceService.getWorkspace(workspaceId);
            } catch (WorkspaceNotFoundException ex) {
                Session.get().error(ex.getMessage());
                return null;
            }
        }
    }
}
