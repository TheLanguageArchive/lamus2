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

import nl.mpi.archiving.tree.CorpusNode;
import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.archiving.tree.GenericTreeNode;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.components.ArchiveTreePanel;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree.LinkType;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author jeafer
 */
public final class CreateWorkspacePage extends LamusPage {

    // Services to be injected
    @SpringBean
    private WorkspaceService workspaceService;
    @SpringBean(name = "createWorkspaceTreeProvider")
    private GenericTreeModelProvider archiveTreeProvider;
    // Page components
    private final Form nodeIdForm;

    public CreateWorkspacePage() {
	super();
	nodeIdForm = createNodeIdForm("nodeIdForm");
	createArchiveTreePanel("archiveTree");
    }

    /**
     * Creates and adds an archive tree panel
     *
     * @return created tree panel
     */
    private ArchiveTreePanel createArchiveTreePanel(final String id) {
	ArchiveTreePanel tree = new ArchiveTreePanel(id, archiveTreeProvider) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, GenericTreeNode node) {
		nodeIdForm.setModel(new CompoundPropertyModel<GenericTreeNode>(node));

		if (target != null) {
		    // Ajax, refresh nodeIdForm
		    target.addComponent(nodeIdForm);
		}
	    }
	};
	tree.setLinkType(LinkType.AJAX_FALLBACK);
	// Add to page
	add(tree);

	return tree;
    }

    /**
     * Creates and adds node id form
     *
     * @param id component id
     * @return created form
     */
    private Form createNodeIdForm(final String id) {
	final Form<CorpusNode> form = new Form<CorpusNode>(id);
	form.add(new Label("name"));
	form.add(new Label("nodeId"));

	final Button submitButton = new Button("createWorkspace") {

	    @Override
	    public void onSubmit() {
		final String currentUserId = LamusSession.get().getUserId();
		try {
		    final int selectedNodeId = Integer.parseInt(form.getModelObject().getNodeId());
		    // Request a new workspace with workspace service
		    final Workspace createdWorkspace = workspaceService.createWorkspace(currentUserId, selectedNodeId);
		    // Show page for newly created workspace
		    final WorkspacePage resultPage = new WorkspacePage(new WorkspaceModel(createdWorkspace));
		    setResponsePage(resultPage);
		} catch (NumberFormatException nfEx) {
		    error(nfEx.getMessage());
		}
	    }
	};
	form.add(submitButton);

	// Put details/submit form in container for refresh through AJAX 
	final MarkupContainer formContainer = new WebMarkupContainer("formContainer");
	formContainer.add(form);
	// Add container to page
	add(formContainer);

	return form;
    }
}
