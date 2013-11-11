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
package nl.mpi.lamus.web.pages;

import java.util.Collection;
import nl.mpi.lamus.web.components.LinkNodesPanel;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.actions.implementation.LinkNodesAction;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author guisil
 */
public class LinkNodesPage extends WorkspacePage {
    
    private final IModel<Workspace> model;
    
    private Collection<WorkspaceNode> selectedUnlinkedNodes;
    
    private LinkNodesPanel linkNodesPanel;
    
    public LinkNodesPage(IModel<Workspace> model) {
        
        super(model);
        
        this.model = model;
        
        LinkNodesForm linkNodesForm = new LinkNodesForm("linkNodesForm", new LoadableDetachableModel<LinkNodesAction>() {

            @Override
            protected LinkNodesAction load() {
                return new LinkNodesAction(workspaceTreeService);
            }
            
            
        });
        
        
        linkNodesPanel = new LinkNodesPanel("linkNodesPanel", model);
        
        linkNodesForm.add(linkNodesPanel);
        
        add(linkNodesForm);
    }

    @Override
    protected void refreshStuff() {
        super.refreshStuff(); //To change body of generated methods, choose Tools | Templates.
        
//        linkNodesPanel.render();
    }
    
    private class LinkNodesForm extends Form<LinkNodesAction> {
        
        LinkNodesForm(String id, IModel<LinkNodesAction> model) {
            super(id, model);
        }

        @Override
        protected void onSubmit() {
            
            getModelObject().execute(LamusSession.get().getUserId(), getSelectedNode(), LinkNodesPage.this.linkNodesPanel.getSelectedUnlinkedNodes());
            LinkNodesPage.this.refreshStuff();
            
            //TODO REMOVE SELECTION FROM MODEL...
            addOrReplace(new LinkNodesPanel("linkNodesPanel", model));
        }
    }
}
