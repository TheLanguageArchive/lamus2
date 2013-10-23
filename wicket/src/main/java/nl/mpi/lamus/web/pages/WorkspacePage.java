/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanel;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanelListener;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.components.WsNodeActionsPanel;
import nl.mpi.lamus.web.components.NodeInfoPanel;
import nl.mpi.lamus.web.model.WorkspaceTreeNodeModel;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.tree.implementation.WorkspaceTreeModelProviderFactory;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.extensions.markup.html.tree.LinkType;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class WorkspacePage extends LamusPage {

    // Services to be injected
    @SpringBean
    private WorkspaceTreeService workspaceTreeService;
    @SpringBean(name = "workspaceTreeProviderFactory")
    private WorkspaceTreeModelProviderFactory workspaceTreeProviderFactory;
    private LinkedTreeModelProvider workspaceTreeProvider;
    // Page model
    private final IModel<Workspace> model;
    private final Form nodeIdForm;
//    private final WebMarkupContainer nodeInfo;
//    private NodeInfoPanel nodeInfoPanel;
    private ArchiveTreePanel wsTreePanel;
    private final WsNodeActionsPanel wsNodeActionsPanel;

    public WorkspacePage(final IModel<Workspace> model) {
	super();
	this.model = model;
	nodeIdForm = (Form) createNodeInfoForm("nodeInfoForm");

	this.workspaceTreeProvider = this.workspaceTreeProviderFactory.createTreeModelProvider(
		this.workspaceTreeService.getTreeNode(this.model.getObject().getTopNodeID(), null));

	add(createWorkspaceInfo("workspaceInfo"));
//        nodeInfo = createNodeInfo("nodeInfo");
//        add(nodeInfo);
        
        wsTreePanel = createWorkspaceTreePanel("workspaceTree");
        add(wsTreePanel);
	add(new ButtonPage("buttonpage", model));
        
        
        wsNodeActionsPanel = new WsNodeActionsPanel("wsNodeActionsPanel", new CollectionModel<WorkspaceTreeNode>(wsTreePanel.getSelectedNodes())) {

            @Override
            protected void onModelChanged() {
                super.onModelChanged(); //To change body of generated methods, choose Tools | Templates.
                
//                if(getModel() == null) {
//                    
//                    
//                    
//                    wsTreePanel = new ArchiveTreePanel("workspaceTree", workspaceTreeProviderFactory.createTreeModelProvider(
//                            workspaceTreeService.getTreeNode(model.getObject().getTopNodeID(), null)));
//                    
                    wsTreePanel.getTree().updateTree();
//                }
            }
            
            
        };
                
	wsNodeActionsPanel.setOutputMarkupId(true);
	add(wsNodeActionsPanel);

//        nodeInfoPanel.setOutputMarkupId(Boolean.TRUE);
//        add(nodeInfoPanel);
    }

    /**
     * Creates and adds an tree panel to be display in the opened/created workspace
     *
     * @param id
     * @return ArchiveTreePanel
     */
    private ArchiveTreePanel createWorkspaceTreePanel(String id) {
	ArchiveTreePanel treePanel = new ArchiveTreePanel(id, workspaceTreeProvider);
	treePanel.addArchiveTreePanelListener(new ArchiveTreePanelListener() {
	    @Override
	    public void nodeSelectionChanged(AjaxRequestTarget target, ArchiveTreePanel treePanel) {
		final WorkspaceTreeNode node = (WorkspaceTreeNode) treePanel.getSelectedNodes().iterator().next();
		nodeIdForm.setModel(new CompoundPropertyModel<WorkspaceTreeNode>(node));
//                nodeIdForm.setModel(new WorkspaceTreeNodeModel(node, node.getParent()));
//                nodeInfo.get
//                nodeInfo.setDefaultModel(new WorkspaceTreeNodeModel(node, node.getParent()));
//                nodeInfoPanel.setDefaultModel(new WorkspaceTreeNodeModel(node, node.getParent()));
                
                wsNodeActionsPanel.setModelObject(wsTreePanel.getSelectedNodes());
                
		if (target != null) {
		    // Ajax, refresh nodeIdForm
		    target.add(nodeIdForm);

                    target.add(wsNodeActionsPanel);
                    
//                    target.add(nodeInfo);
//                    target.add(nodeInfoPanel);
		}
	    }
	});
	treePanel.setLinkType(LinkType.REGULAR);
	return treePanel;
    }

    /**
     * Collect information about the workspace
     *
     * @param id
     * @return WebMarkupContainer
     */
    private WebMarkupContainer createWorkspaceInfo(String id) {
	WebMarkupContainer wsInfo = new WebMarkupContainer(id, new CompoundPropertyModel<Workspace>(model));
	wsInfo.add(new Label("userID"));
	wsInfo.add(new Label("workspaceID"));
	wsInfo.add(new Label("status"));
	return wsInfo;
    }

    /**
     * Creates and adds node id form
     *
     * @param id
     * @return Form
     */
    private Form createNodeInfoForm(final String id) {
	final Form<WorkspaceTreeNode> form = new Form<WorkspaceTreeNode>(id);
//        WebMarkupContainer nInfo = new WebMarkupContainer(id, new CompoundPropertyModel<WorkspaceTreeNode>(workspaceTreeService.getTreeNode(model.getObject().getTopNodeID(), null)));
        
//        WebMarkupContainer nInfo = new WebMarkupContainer(id, new WorkspaceTreeNodeModel(null, null));
        
//        WebMarkupContainer nInfo = new WebMarkupContainer(id);
        
	form.add(new Label("name"));
	form.add(new Label("archiveURI"));
	form.add(new Label("archiveURL"));
	form.add(new Label("workspaceID"));
	form.add(new Label("type"));
        
	// Put details/submit form in container for refresh through AJAX 
	final MarkupContainer formContainer = new WebMarkupContainer("nodeInfoContainer");
	formContainer.add(form);
	// Add container to page
	add(formContainer);

	return form;
    }
}
