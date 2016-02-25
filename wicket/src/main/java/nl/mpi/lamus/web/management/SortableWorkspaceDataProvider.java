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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * Data provider to be used by the management table listing workspaces.
 * @author guisil
 */
public class SortableWorkspaceDataProvider extends SortableDataProvider<Workspace, String> implements IFilterStateLocator<WorkspaceFilter> {

    private final WorkspaceService workspaceService;
    private final SortableDataProviderComparator comparator = new SortableDataProviderComparator();
    private WorkspaceFilter workspaceFilter = new WorkspaceFilter();

    public SortableWorkspaceDataProvider(WorkspaceService wsService) {
        this.workspaceService = wsService;
        
        setSort("workspaceID", SortOrder.ASCENDING);
    }
    
    /**
     * @see SortableDataProvider#iterator(long, long)
     */
    @Override
    public Iterator<? extends Workspace> iterator(long first, long count) {
        
        //The signature of this method requires long, but the subList method used afterwards requires int.
        //It should never be a problem (how many workspaces will there ever be?), but anyway will throw an exception if this assumption proves to be wrong.
        int firstInt = (int) first;
        assureLongAndIntHaveSameValue(first, firstInt);
        int countInt = (int) count;
        assureLongAndIntHaveSameValue(count, countInt);
        
        List<Workspace> allWs = getAllWorkspaces();
        List<Workspace> filteredWs = filterWorkspaces(allWs);
        Collections.sort(filteredWs, comparator);
        
        return filteredWs.subList(firstInt, firstInt + countInt).iterator();
    }

    /**
     * @see SortableDataProvider#size()
     */
    @Override
    public long size() {
        return filterWorkspaces(getAllWorkspaces()).size();
    }

    /**
     * @see SortableDataProvider#model(java.lang.Object)
     */
    @Override
    public IModel<Workspace> model(final Workspace object) {

        return new WorkspaceModel(object);
    }
    
    /**
     * @see IFilterStateLocator#getFilterState()
     */
    @Override
    public WorkspaceFilter getFilterState() {
        return workspaceFilter;
    }

    /**
     * @see IFilterStateLocator#setFilterState(java.lang.Object)
     */
    @Override
    public void setFilterState(WorkspaceFilter state) {
        workspaceFilter = state;
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
    
    private List<Workspace> filterWorkspaces(List<Workspace> initialList) {
        
        List<Workspace> result = new ArrayList<>();
        
        for(Workspace ws : initialList) {
            if(includeInFilteredResults(ws)) {
                result.add(ws);
            }
        }
        
        return result;
    }
    
    private boolean includeInFilteredResults(Workspace ws) {
        
        boolean matchesUserIDFilter = workspaceFilter.getUserID() == null || ws.getUserID().toLowerCase().contains(workspaceFilter.getUserID().toLowerCase());
        boolean matchesTopNodeUriFilter = workspaceFilter.getTopNodeURI() == null || ws.getTopNodeArchiveURI().toString().toLowerCase().contains(workspaceFilter.getTopNodeURI().toLowerCase());
        boolean matchesTopNodeUrlFilter = workspaceFilter.getTopNodeURL() == null || ws.getTopNodeArchiveURL().toString().toLowerCase().contains(workspaceFilter.getTopNodeURL().toLowerCase());
        boolean matchesStatusFilter = workspaceFilter.getStatus() == null || ws.getStatus().toString().toLowerCase().contains(workspaceFilter.getStatus().toLowerCase());
        boolean matchesExcludeSuccessfulFilter = !workspaceFilter.getExcludeSuccessful() || (workspaceFilter.getExcludeSuccessful() && !WorkspaceStatus.SUCCESS.equals(ws.getStatus()));
        
        return matchesUserIDFilter && matchesTopNodeUriFilter && matchesTopNodeUrlFilter && matchesStatusFilter && matchesExcludeSuccessfulFilter;
    }
    
    /**
     * Comparator for the data provider, to be used in the sorting of the data.
     */
    class SortableDataProviderComparator implements Comparator<Workspace>, Serializable {
        
        @Override
        public int compare(final Workspace o1, final Workspace o2) {
            PropertyModel<Comparable> model1 = new PropertyModel<>(o1, getSort().getProperty());
            PropertyModel<Comparable> model2 = new PropertyModel<>(o2, getSort().getProperty());
 
            int result = model1.getObject().compareTo(model2.getObject());
 
            if (!getSort().isAscending()) {
                result = -result;
            }
 
            return result;
        }
    }
}
