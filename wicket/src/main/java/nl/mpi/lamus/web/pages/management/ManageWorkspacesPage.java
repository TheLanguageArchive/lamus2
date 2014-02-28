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
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.management.SortableWorkspaceDataProvider;
import nl.mpi.lamus.web.pages.LamusPage;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Session;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author guisil
 */
public class ManageWorkspacesPage extends LamusPage {
    
    @SpringBean
    private WorkspaceService workspaceService;
    
    private DataView<Workspace> dataView;
    
    public ManageWorkspacesPage() {
        
//        add(new PagingNavigator("navigator", dataView));
        
        List<IColumn<Workspace, String>> columns = new ArrayList<IColumn<Workspace, String>>();

        columns.add(new AbstractColumn<Workspace, String>(new Model<String>("Actions")) {
            
            @Override
            public void populateItem(Item<ICellPopulator<Workspace>> cellItem, String componentId, IModel<Workspace> model) {
                
                Link<Workspace> deleteLink =new Link<Workspace>(componentId, model) {

                    @Override
                    public void onClick() {
                        try {
                            //TODO Add confirmation dialog
                            
                            workspaceService.deleteWorkspace(LamusSession.get().getUserId(), getModelObject().getWorkspaceID());
                            
                            refreshDataView();
                            
                        } catch (WorkspaceNotFoundException ex) {
                            Session.get().error(ex.getMessage());
                        } catch (WorkspaceAccessException ex) {
                            Session.get().error(ex.getMessage());
                        } catch (IOException ex) {
                            Session.get().error(ex.getMessage());
                        }
                    }
                    
                };
                deleteLink.add(AttributeModifier.append("class", new Model<String>("managementActionLink")));
                cellItem.add(deleteLink);
            }
        });

        columns.add(new PropertyColumn<Workspace, String>(new Model<String>("ID"), "workspaceID"/*, "workspaceID"*/));
        columns.add(new PropertyColumn<Workspace, String>(new Model<String>("User"), "userID"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<String>("Top node"), "topNodeArchiveURI"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<String>("Start date"), "startDate"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<String>("End date"), "endDate"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<String>("Session start"), "sessionStartDate"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<String>("Session end"), "sessionEndDate"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<String>("Used storage"), "usedStorageSpace"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<String>("Max storage"), "maxStorageSpace"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<String>("Status"), "status"));
        columns.add(new PropertyColumn<Workspace, String>(new Model<String>("Message"), "message"));
        

        DataTable<Workspace, String> dataTable = new AjaxFallbackDefaultDataTable<Workspace, String>("table", columns,
            new SortableWorkspaceDataProvider(workspaceService), 8) {

            @Override
            protected Item<Workspace> newRowItem(String id, int index, IModel<Workspace> model) {
                return new OddEvenItem<Workspace>(id, index, model);
            }
        };
        dataTable.setOutputMarkupId(true);
        add(dataTable);
        
    }
    
    private void refreshDataView() {

    }
}
