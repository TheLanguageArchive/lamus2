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
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;

/**
 *
 * @author guisil
 */
public class SortableWorkspaceDataProvider extends SortableDataProvider<Workspace, String> {
    
    private final WorkspaceService workspaceService;
            

    public SortableWorkspaceDataProvider(WorkspaceService wsService) {
        this.workspaceService = wsService;
        
        setSort("workspaceID", SortOrder.ASCENDING);
    }
    
    @Override
    public Iterator<? extends Workspace> iterator(long first, long count) {
        
        //The signature of this method requires long, but the subList method used afterwards requires int.
        //It should never be a problem (how many workspaces will there ever be?), but anyway will throw an exception if this assumption proves to be wrong.
        int firstInt = (int) first;
        assureLongAndIntHaveSameValue(first, firstInt);
        int countInt = (int) count;
        assureLongAndIntHaveSameValue(count, countInt);
        
        return getAllWorkspaces().subList(firstInt, firstInt + countInt).iterator();
    }

    @Override
    public long size() {
        return getAllWorkspaces().size();
    }

    @Override
    public IModel<Workspace> model(final Workspace object) {

        return new WorkspaceModel(object);
    }
 
    
    private List<Workspace> getAllWorkspaces() {
        return workspaceService.listAllWorkspaces();
    }
    
    private boolean assureLongAndIntHaveSameValue(long original, int converted) {
        if(original != (long) converted) {
            throw new ArithmeticException("Casting " + original + " to int cannot be done without changing its value");
        }
        return true;
    }
}
