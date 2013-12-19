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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.web.unlinkednodes.providers.UnlinkedNodesModelProviderFactory;
import nl.mpi.lamus.workspace.actions.implementation.LinkNodesAction;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.actions.implementation.LinkExternalNodesAction;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
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
    
    @SpringBean
    private WorkspaceNodeFactory workspaceNodeFactory;
    
    @SpringBean(name = "unlinkedNodesProviderFactory")
    private UnlinkedNodesModelProviderFactory providerFactory;
    
    private IModel<WorkspaceTreeNode> model;
    
    private UnlinkedNodesPanel unlinkedNodesPanel;
    private Button linkNodesButton;
    private TextField<String> externalNodeLocation;
    private Button linkExternalNodeButton;
    
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
        
        linkNodesButton = new Button("linkNodesButton", new Model("Link")) {

            @Override
            protected void onConfigure() {
                super.onConfigure(); //To change body of generated methods, choose Tools | Templates.
                
                if(LinkNodesPanel.this.getModelObject() != null && LinkNodesPanel.this.getModelObject().isMetadata()) {
                    setVisible(true);
                } else {
                    setVisible(false);
                }
            }
        };
        
        linkNodesForm.add(linkNodesButton);

        add(linkNodesForm);
        
        
        Form<LinkExternalNodesAction> linkExternalNodesForm = new LinkExternalNodesForm("linkExternalNodesForm", new LoadableDetachableModel<LinkExternalNodesAction>() {

            //TODO Maybe it's not a bad idea to just create another LinkNodesForm,
                // create a workspace node for the inserted external URL
                    // and execute normally the link action...
            
            
            
            @Override
            protected LinkExternalNodesAction load() {
                return new LinkExternalNodesAction();
            }
        });
        
        externalNodeLocation = new TextField<String>("externalNodeLocation", new Model(""));
        linkExternalNodesForm.add(externalNodeLocation);
        
        linkExternalNodeButton = new Button("linkExternalNodeButton", new Model("Link External Node")) {

            @Override
            protected void onConfigure() {
                super.onConfigure(); //To change body of generated methods, choose Tools | Templates.
                
                if(LinkNodesPanel.this.getModelObject() != null) {
                    setVisible(true);
                } else {
                    setVisible(false);
                }
            }
        };
        linkExternalNodesForm.add(linkExternalNodeButton);
        
        add(linkExternalNodesForm);
    }
    
    
    // to override from the page where the panel is included
    protected void refreshStuff() {

    }
    
    private class LinkNodesForm extends Form<LinkNodesAction> {
        
        boolean linkExternalNodes;
        
        LinkNodesForm(String id, IModel<LinkNodesAction> model) {
            super(id, model);
            this.linkExternalNodes = linkExternalNodes;
        }

        @Override
        protected void onSubmit() {
            try {
                
                //TODO if unlinked nodes are not selected, show a warning message and do nothing else
                
                getModelObject().execute(
                        LamusSession.get().getUserId(), model.getObject(),
                            unlinkedNodesPanel.getSelectedUnlinkedNodes() , workspaceService);
                
            } catch (WorkspaceNotFoundException ex) {
                Session.get().error(ex.getMessage());
            } catch (WorkspaceAccessException ex) {
                Session.get().error(ex.getMessage());
            } catch (WorkspaceException ex) {
                Session.get().error(ex.getMessage());
            }
            LinkNodesPanel.this.refreshStuff();            
            
            unlinkedNodesPanel = new UnlinkedNodesPanel(
                        "unlinkedNodesPanel",
                        new Model<Workspace>(currentWorkspace), providerFactory.createTreeModelProvider(workspaceService, currentWorkspace.getWorkspaceID()));
            
            addOrReplace(unlinkedNodesPanel);
        }
    }
    
    private class LinkExternalNodesForm extends Form<LinkExternalNodesAction> {
        
        LinkExternalNodesForm(String id, IModel<LinkExternalNodesAction> model) {
            super(id, model);
        }

        @Override
        protected void onSubmit() {
            try {
                
                //has to be a valid URL?
                URL externalNodeUrl = new URL(externalNodeLocation.getValue());
                
                WorkspaceNode externalNode =
                        workspaceNodeFactory.getNewExternalNode(
                            model.getObject().getWorkspaceID(), externalNodeUrl);
                
                getModelObject().execute(LamusSession.get().getUserId(), model.getObject(), externalNode , workspaceService);
                
            } catch (MalformedURLException ex) {
                Session.get().error(ex.getMessage());
            } catch (WorkspaceNotFoundException ex) {
                Session.get().error(ex.getMessage());
            } catch (WorkspaceAccessException ex) {
                Session.get().error(ex.getMessage());
            } catch (WorkspaceException ex) {
                Session.get().error(ex.getMessage());
            }
            LinkNodesPanel.this.refreshStuff();
        }
    }
    

    @Override
    protected void onModelChanged() {
        super.onModelChanged(); //To change body of generated methods, choose Tools | Templates.
        
        if(getModelObject() != null) {
            linkNodesButton.setVisible(true);
            linkExternalNodeButton.setVisible(true);
        } else {
            linkNodesButton.setVisible(false);
            linkExternalNodeButton.setVisible(false);
        }
    }
}
