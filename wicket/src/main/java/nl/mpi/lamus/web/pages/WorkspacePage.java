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

import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.archiving.tree.GenericTreeModelProviderFactory;
import nl.mpi.archiving.tree.GenericTreeNode;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.components.ArchiveTreePanel;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.tree.LinkType;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public final class WorkspacePage extends LamusPage {

    // Services to be injected
    
    @SpringBean
    private WorkspaceTreeService workspaceTreeService;
    
    @SpringBean(name = "workspaceTreeProviderFactory")
    private GenericTreeModelProviderFactory workspaceTreeProviderFactory;

    private GenericTreeModelProvider workspaceTreeProvider;
    // Page model
    private final IModel<Workspace> model;
    private final Form nodeIdForm;

    public WorkspacePage(IModel<Workspace> model) {
        super();
        this.model = model;
        nodeIdForm = (Form) createNodeInfoForm("nodeInfoForm");
        
        this.workspaceTreeProvider = this.workspaceTreeProviderFactory.createTreeModelProvider(
                this.workspaceTreeService.getTreeNode(this.model.getObject().getTopNodeID(), null));
        
        add(createWorkspaceInfo("workspaceInfo"));
        add(createWorkspaceTreePanel("workspaceTree"));
        add(new ButtonPage("buttonpage"));
        
    }

    private ArchiveTreePanel createWorkspaceTreePanel(String id) {
        ArchiveTreePanel treePanel = new ArchiveTreePanel(id, workspaceTreeProvider) {

            @Override
            protected void onNodeLinkClicked(AjaxRequestTarget target, GenericTreeNode node) {
                nodeIdForm.setModel(new CompoundPropertyModel<GenericTreeNode>(node));

                if (target != null) {
                    // Ajax, refresh nodeIdForm
                    target.addComponent(nodeIdForm);
                }
            }
        };
        treePanel.setLinkType(LinkType.AJAX_FALLBACK);
        return treePanel;
    }

    private WebMarkupContainer createWorkspaceInfo(String id) {
        WebMarkupContainer wsInfo = new WebMarkupContainer(id, new CompoundPropertyModel<Workspace>(model));
        wsInfo.add(new Label("userID"));
        wsInfo.add(new Label("workspaceID"));
        wsInfo.add(new Label("status"));
        return wsInfo;
    }
    
    private Form createNodeInfoForm(final String id) {
	final Form<WorkspaceTreeNode> form = new Form<WorkspaceTreeNode>(id);
	form.add(new Label("name"));
	form.add(new Label("nodeId"));

	// Put details/submit form in container for refresh through AJAX 
	final MarkupContainer formContainer = new WebMarkupContainer("formContainer");
	formContainer.add(form);
	// Add container to page
	add(formContainer);

	return form;
    }

 
}
