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
package nl.mpi.lamus.web.pages.management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nl.mpi.lamus.exception.CrawlerInvocationException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.management.SortableWorkspaceDataProvider;
import nl.mpi.lamus.web.management.WorkspaceFilter;
import nl.mpi.lamus.web.pages.LamusPage;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Session;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterToolbar;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Page for the management related actions.
 * @author guisil
 */
public class ManageWorkspacesPage extends LamusPage {
    
    @SpringBean
    private WorkspaceService workspaceService;
    
    
    public ManageWorkspacesPage() {
        
        List<IColumn<Workspace, String>> columns = createColumns();
        
        SortableWorkspaceDataProvider provider = new SortableWorkspaceDataProvider(workspaceService);

        DataTable<Workspace, String> dataTable = createDataTable(columns, provider);
        
        FilterForm<WorkspaceFilter> filterForm = createFilterForm(provider);
        FilterToolbar filterToolbar = new FilterToolbar(dataTable, filterForm, provider);
        
        dataTable.addTopToolbar(filterToolbar);
        
        filterForm.add(dataTable);
    }
    
    
    private List<IColumn<Workspace, String>> createColumns() {
        
        List<IColumn<Workspace, String>> columns = new ArrayList<>();

        columns.add(new AbstractColumn<Workspace, String>(new Model<>(getLocalizer().getString("management_table_column_actions", this))) {
            
            @Override
            public void populateItem(Item<ICellPopulator<Workspace>> cellItem, String componentId, IModel<Workspace> model) {
                
                Link<Workspace> deleteLink = new Link<Workspace>(componentId, model) {

                    @Override
                    public void onClick() {
                        try {
                            workspaceService.deleteWorkspace(LamusSession.get().getUserId(), getModelObject().getWorkspaceID(), false);
                            
                            refreshDataView();
                            
                        } catch (WorkspaceNotFoundException | WorkspaceAccessException | WorkspaceExportException | IOException ex) {
                            Session.get().error(ex.getMessage());
                        }
                    }
                    
                };
                deleteLink.setBody(Model.of(getLocalizer().getString("management_table_delete_button", ManageWorkspacesPage.this)));
                deleteLink.add(AttributeModifier.append("class", new Model<>("tableActionLink")));
                cellItem.add(deleteLink);
            }
        });
        
        columns.add(new AbstractColumn<Workspace, String>(new Model<>("")) {

            @Override
            public void populateItem(Item<ICellPopulator<Workspace>> cellItem, String componentId, IModel<Workspace> model) {
                
                if(WorkspaceStatus.UPDATING_ARCHIVE.equals(model.getObject().getStatus())) {
                    
                    Link<Workspace> reCrawlLink = new Link<Workspace>(componentId, model) {

                        @Override
                        public void onClick() {
                            
                            try {
                                workspaceService.triggerCrawlForWorkspace(LamusSession.get().getUserId(), getModelObject().getWorkspaceID());
                            } catch (WorkspaceNotFoundException | WorkspaceAccessException | CrawlerInvocationException ex) {
                                Session.get().error(ex.getMessage());
                            }
                        }
                    };
                    reCrawlLink.setBody(Model.of(getLocalizer().getString("management_table_recrawl_button", ManageWorkspacesPage.this)));
                    reCrawlLink.add(AttributeModifier.append("class", new Model<>("tableActionLink")));
                    cellItem.add(reCrawlLink);
                } else {
                    cellItem.add(new Label(componentId));
                }
            }
        });

        columns.add(new PropertyColumn<Workspace, String>(new Model<>(getLocalizer().getString("management_table_column_workspace_id", this)), "workspaceID", "workspaceID"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<>(getLocalizer().getString("management_table_column_user_id", this)), "userID", "userID"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<>(getLocalizer().getString("management_table_column_top_node_uri", this)), "topNodeArchiveURI"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<>(getLocalizer().getString("management_table_column_top_node_url", this)), "topNodeArchiveURL"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<>(getLocalizer().getString("management_table_column_start_date", this)), "startDate", "startDateStr"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<>(getLocalizer().getString("management_table_column_end_date", this)), "endDateStr"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<>(getLocalizer().getString("management_table_column_session_start_date", this)), "sessionStartDateStr"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<>(getLocalizer().getString("management_table_column_session_end_date", this)), "sessionEndDateStr"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<>(getLocalizer().getString("management_table_column_status", this)), "status", "status"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<>(getLocalizer().getString("management_table_column_message", this)), "message"));
        
        return columns;
    }
    
    private DataTable<Workspace, String> createDataTable(List<IColumn<Workspace, String>> columns, SortableWorkspaceDataProvider provider) {
        
        DataTable<Workspace, String> dataTable = new AjaxFallbackDefaultDataTable<Workspace, String>("table", columns, provider, 13) {

            @Override
            protected Item<Workspace> newRowItem(String id, int index, IModel<Workspace> model) {
                return new OddEvenItem<>(id, index, model);
            }
        };
        dataTable.setOutputMarkupId(true);
        
        return dataTable;
    }
    
    private FilterForm<WorkspaceFilter> createFilterForm(SortableWorkspaceDataProvider provider) {
        
        FilterForm<WorkspaceFilter> filterForm = new FilterForm<>("filterForm", provider);
        
        filterForm.add(new TextField("userID", new PropertyModel<String>(provider, "filterState.userID")));
        filterForm.add(new TextField("topNodeURI", new PropertyModel<String>(provider, "filterState.topNodeURI")));
        filterForm.add(new TextField("topNodeURL", new PropertyModel<String>(provider, "filterState.topNodeURL")));
        filterForm.add(new TextField("status", new PropertyModel<String>(provider, "filterState.status")));
        filterForm.add(new CheckBox("excludeSuccessful", new PropertyModel<Boolean>(provider, "filterState.excludeSuccessful")));
        
        add(filterForm);
        
        return filterForm;
    }
    
    private void refreshDataView() {

    }
}
