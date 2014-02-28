/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web.management;

import java.util.Iterator;
import java.util.List;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 *
 * @author guisil
 */
public class SortableWorkspaceDataProvider extends SortableDataProvider<Workspace, String> {
    
    private WorkspaceService workspaceService;
    
    private List<Workspace> allWorkspaces;
            

    public SortableWorkspaceDataProvider(WorkspaceService wsService) {
        this.workspaceService = wsService;
    }
    
    @Override
    public Iterator<? extends Workspace> iterator(long l, long l1) {
        
        //TODO NOT USING VALUES FOR THE MOMENT
        
        
        callListAllWorkspaces();
        return allWorkspaces.iterator();
    }

    @Override
    public long size() {
        if(allWorkspaces == null) {
            callListAllWorkspaces();
        }
        return allWorkspaces.size();
    }

    @Override
    public IModel<Workspace> model(final Workspace t) {
        return new LoadableDetachableModel<Workspace>(t) {
            @Override
            protected Workspace load() {
                
                
                
                return t;
                
                //TODO SHOULD BE CHANGED
            }
        };
    }
 
    
    private void callListAllWorkspaces() {
        allWorkspaces = workspaceService.listAllWorkspaces();
    }
}
