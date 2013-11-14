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

import nl.mpi.lamus.web.components.ButtonPanel;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanel;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanelListener;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.components.WsTreeNodeActionsPanel;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.tree.implementation.WorkspaceTreeModelProviderFactory;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tree.LinkType;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class WorkspacePage extends LamusPage {

    // Services to be injected
    @SpringBean
    protected WorkspaceTreeService workspaceTreeService;
    @SpringBean(name = "workspaceTreeProviderFactory")
    private WorkspaceTreeModelProviderFactory workspaceTreeProviderFactory;
    // Page model
    private final IModel<Workspace> model;
    private final Form nodeIdForm;
    private ArchiveTreePanel wsTreePanel;
    private final WsTreeNodeActionsPanel wsNodeActionsPanel;
    
    //TODO Make it possible to have multiple selection
    private WorkspaceTreeNode selectedNode;

    public WorkspacePage(final IModel<Workspace> model) {
	super();
	this.model = model;
	nodeIdForm = (Form) createNodeInfoForm("nodeInfoForm");

	add(createWorkspaceInfo("workspaceInfo"));

	wsTreePanel = createWorkspaceTreePanel("workspaceTree");
	add(wsTreePanel);
	add(new ButtonPanel("buttonpage", model));

	wsNodeActionsPanel = new WsTreeNodeActionsPanel("wsNodeActionsPanel", new CollectionModel<WorkspaceTreeNode>(wsTreePanel.getSelectedNodes())) {
	    @Override
	    public void refreshStuff() {
		WorkspacePage.this.refreshStuff();
	    }
            
	};

	wsNodeActionsPanel.setOutputMarkupId(true);
	add(wsNodeActionsPanel);
    }

    /**
     * Creates and adds an tree panel to be display in the opened/created workspace
     *
     * @param id
     * @return ArchiveTreePanel
     */
    private ArchiveTreePanel createWorkspaceTreePanel(String id) {
	LinkedTreeModelProvider workspaceTreeProvider;
	workspaceTreeProvider = this.workspaceTreeProviderFactory.createTreeModelProvider(
		this.workspaceTreeService.getTreeNode(this.model.getObject().getTopNodeID(), null));

	ArchiveTreePanel treePanel = new ArchiveTreePanel(id, workspaceTreeProvider);
	treePanel.addArchiveTreePanelListener(new ArchiveTreePanelListener() {
	    @Override
	    public void nodeSelectionChanged(AjaxRequestTarget target, ArchiveTreePanel treePanel) {
		
                final WorkspaceTreeNode node = (WorkspaceTreeNode) treePanel.getSelectedNodes().iterator().next();
                
                setSelectedNode(node);
		
                
                nodeIdForm.setModel(new CompoundPropertyModel<WorkspaceTreeNode>(node));
		wsNodeActionsPanel.setModelObject(wsTreePanel.getSelectedNodes());

		if (target != null) {
		    target.add(nodeIdForm);
		    target.add(wsNodeActionsPanel);
		}
	    }
	});
	treePanel.setLinkType(LinkType.AJAX_FALLBACK);
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
    
    protected WorkspaceTreeNode getSelectedNode() {
        return this.selectedNode;
    }
    
    private void setSelectedNode(WorkspaceTreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }
    
    protected void refreshStuff() {
        wsTreePanel = createWorkspaceTreePanel("workspaceTree");
        addOrReplace(wsTreePanel);
    }
}
