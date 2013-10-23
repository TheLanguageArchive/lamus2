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
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.pages.WorkspacePage;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author guisil
 */
public class WsNodeActionsPanel extends GenericPanel<Collection<WorkspaceTreeNode>> {
    
    @SpringBean
    private WorkspaceService workspaceService;
    
//    private IModel<WorkspaceNode> model;
    private final Form<Collection<WorkspaceTreeNode>> form;
    
    public WsNodeActionsPanel(String id, IModel<Collection<WorkspaceTreeNode>> model) {
	super(id, model);
        
//        this.model = model;
        
        form = new Form<Collection<WorkspaceTreeNode>>("wsNodeActionsForm", model);
        
        final Button deleteNodeButton = new Button("deleteNodeButton") {

            @Override
            public void onSubmit() {
                if(WsNodeActionsPanel.this.getModelObject().iterator().hasNext()) {
                    final String currentUserId = LamusSession.get().getUserId();
                    workspaceService.deleteNode(currentUserId, WsNodeActionsPanel.this.getModelObject().iterator().next());
                    
                    
                    IModel<Collection<WorkspaceTreeNode>> newModel = new CollectionModel<WorkspaceTreeNode>();
                    
                    
                    form.setModel(newModel);
                    
                    
                    WsNodeActionsPanel.this.setModel(newModel);
                    
                }
            }

//            @Override
//            public boolean isVisible() {
//                return form.getModelObject() != null;
//            }
            
        };
        
        form.add(deleteNodeButton);
        add(form);
        
    }

//    set
//    
//    @Override
//    protected void onModelChanged() {
//        super.onModelChanged(); //To change body of generated methods, choose Tools | Templates.
//        
//        this.model = getModel();
//        
//        form.setModel(this.model);
//    }
    
}
