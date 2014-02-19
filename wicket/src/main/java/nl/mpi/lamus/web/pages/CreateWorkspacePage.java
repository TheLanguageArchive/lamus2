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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.archiving.tree.LinkedTreeNode;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanel;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanelListener;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.web.components.NavigationPanel;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.extensions.markup.html.tree.LinkType;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Create a workspace by adding a tree and display frame on selected node
 *
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
public class CreateWorkspacePage extends LamusPage {

    // Services to be injected
    @SpringBean
    private WorkspaceService workspaceService;
    
    @SpringBean(name = "createWorkspaceTreeProvider")
    private GenericTreeModelProvider archiveTreeProvider;
    
    @SpringBean
    private LamusWicketPagesProvider pagesProvider;
    
    // Page components
    private final Form nodeIdForm;

    public CreateWorkspacePage() {
	super();
        
        createNavigationPanel("navigationPanel");
        
	nodeIdForm = createNodeIdForm("nodeIdForm");
	createArchiveTreePanel("archiveTree");
    }

    
    private NavigationPanel createNavigationPanel(final String id) {
        NavigationPanel navPanel = new NavigationPanel(id);
        add(navPanel);
        return navPanel;
    }
    
    
    /**
     * Creates and adds an archive tree panel
     *
     * @return created tree panel
     */
    private ArchiveTreePanel createArchiveTreePanel(final String id) {
	ArchiveTreePanel tree = new ArchiveTreePanel(id, archiveTreeProvider);
	tree.addArchiveTreePanelListener(new ArchiveTreePanelListener() {
	    @Override
	    public void nodeSelectionChanged(AjaxRequestTarget target, ArchiveTreePanel treePanel) {
		final LinkedTreeNode node = (LinkedTreeNode) treePanel.getSelectedNodes().iterator().next();
		nodeIdForm.setModel(new CompoundPropertyModel<LinkedTreeNode>(node));

		if (target != null) {
		    // Ajax, refresh nodeIdForm
		    target.add(nodeIdForm);
		}
	    }
	});
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
	form.add(new Label("nodeURI"));

	final Button submitButton = new Button("createWorkspace") {
	    @Override
	    public void onSubmit() {
		final String currentUserId = LamusSession.get().getUserId();
		final URI selectedNodeURI = form.getModelObject().getNodeURI();
		// Request a new workspace with workspace service
                try {
                    Workspace createdWorkspace = workspaceService.createWorkspace(currentUserId, selectedNodeURI);
                    setResponsePage(pagesProvider.getWorkspacePage(createdWorkspace));
                } catch (UnknownNodeException ex) {
                    Session.get().error(ex.getMessage());
                } catch (NodeAccessException ex) {
                    Session.get().error(ex.getMessage());
                } catch (WorkspaceImportException ex) {
                    Session.get().error(ex.getMessage());
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
