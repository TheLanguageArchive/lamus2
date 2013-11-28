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

import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.web.unlinkednodes.providers.UnlinkedNodesModelProviderFactory;
import nl.mpi.lamus.workspace.actions.implementation.LinkNodesAction;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author guisil
 */
public class LinkNodesPanel extends GenericPanel<WorkspaceTreeNode> {
    
    @SpringBean
    protected WorkspaceTreeService workspaceService;
    
    @SpringBean(name = "unlinkedNodesProviderFactory")
    private UnlinkedNodesModelProviderFactory providerFactory;
    
    private IModel<WorkspaceTreeNode> model;
    
    private UnlinkedNodesPanel unlinkedNodesPanel;
    
    private Workspace currentWorkspace;
    
    
    public LinkNodesPanel(String id, IModel<WorkspaceTreeNode> model, Workspace currentWs) {
        super(id, model);
        this.model = model;
        
        this.currentWorkspace = currentWs;
        
        LinkNodesForm linkNodesForm = new LinkNodesForm("linkNodesForm", new LoadableDetachableModel<LinkNodesAction>() {

            @Override
            protected LinkNodesAction load() {
                return new LinkNodesAction();
            }
        });
        
        unlinkedNodesPanel =
                new UnlinkedNodesPanel(
                    "unlinkedNodesPanel",
                    new Model<Workspace>(currentWorkspace), providerFactory.createTreeModelProvider(workspaceService, currentWs.getWorkspaceID()));
        
        linkNodesForm.add(unlinkedNodesPanel);
        
        add(linkNodesForm);
    }
    
    
    // to override from the page where the panel is included
    protected void refreshStuff() {

    }
    
    private class LinkNodesForm extends Form<LinkNodesAction> {
        
        LinkNodesForm(String id, IModel<LinkNodesAction> model) {
            super(id, model);
        }

        @Override
        protected void onSubmit() {

            getModelObject().execute(LamusSession.get().getUserId(), model.getObject(), unlinkedNodesPanel.getSelectedUnlinkedNodes(), workspaceService);
            LinkNodesPanel.this.refreshStuff();            
            
            unlinkedNodesPanel = new UnlinkedNodesPanel(
                        "unlinkedNodesPanel",
                        new Model<Workspace>(currentWorkspace), providerFactory.createTreeModelProvider(workspaceService, currentWorkspace.getWorkspaceID()));
            
            addOrReplace(unlinkedNodesPanel);
        }
    }
}
