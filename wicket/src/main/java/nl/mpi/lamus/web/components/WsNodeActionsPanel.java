/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web.components;

import java.util.Collection;
import java.util.List;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.actions.WsNodeActionsProvider;
import nl.mpi.lamus.workspace.actions.WsParentMultipleChildNodesAction;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author guisil
 */
public class WsNodeActionsPanel extends GenericPanel<Collection<WorkspaceTreeNode>> {

    @SpringBean
    private WsNodeActionsProvider nodeActionsProvider;
    @SpringBean
    private WorkspaceService workspaceService;
    private final Form<Collection<WorkspaceTreeNode>> form;

    public WsNodeActionsPanel(String id, IModel<Collection<WorkspaceTreeNode>> model) {
	super(id, model);
	form = new Form<Collection<WorkspaceTreeNode>>("wsNodeActionsForm", model);
        
        //TODO should this also be part of the services?
        form.add(createListView(nodeActionsProvider.getActions(model.getObject())));
        
        //TODO Add other buttons
        
        
	add(form);

    }

    @Override
    protected void onModelChanged() {
        super.onModelChanged(); //To change body of generated methods, choose Tools | Templates.
        
        form.setModelObject(getModelObject());
        form.addOrReplace(createListView(nodeActionsProvider.getActions(getModelObject())));
    }

    
    
    private ListView<WsTreeNodesAction> createListView(List<WsTreeNodesAction> nodeActionsList) {
        
        return new ListView<WsTreeNodesAction>("wsNodeActions", nodeActionsList) {

            @Override
            protected void populateItem(ListItem<WsTreeNodesAction> li) {
                
                Button nodeActionButton = new WsNodeActionButton(
                        "nodeActionButton", WsNodeActionsPanel.this.getModelObject(), li.getModelObject(),
                        WsNodeActionsPanel.this.workspaceService) {

                    @Override
                    public void refreshStuff() {
                        WsNodeActionsPanel.this.refreshStuff();
                    }
                    
                    
                };
                
                nodeActionButton.add(AttributeModifier.append("class", new Model<String>("icon-submit_ws")));
                
                li.add(nodeActionButton);
            }
        };
    }
    
    
    
    public void refreshStuff() {
    }
}
