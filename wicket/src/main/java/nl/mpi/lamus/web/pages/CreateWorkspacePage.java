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

import java.net.URI;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.CorpusNodeType;
import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.archiving.tree.LinkedTreeNode;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanel;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanelListener;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.web.components.NavigationPanel;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.extensions.markup.html.tree.LinkType;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
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
    private Button submitButton;
    private Label warningMessage;
    private Model<String> warningMessageModel;

    
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
	ArchiveTreePanel tree = new ArchiveTreePanel(id, archiveTreeProvider, false);
	tree.addArchiveTreePanelListener(new ArchiveTreePanelListener() {
	    @Override
	    public void nodeSelectionChanged(AjaxRequestTarget target, ArchiveTreePanel treePanel) {
                
                if(treePanel.getSelectedNodes().isEmpty()) {
                    submitButton.setEnabled(false);
                    warningMessageModel.setObject("Please select a metadata node as top node of the workspace");
                    warningMessage.setVisible(true);
                    
                    nodeIdForm.setModelObject(null);
                } else if(treePanel.getSelectedNodes().size() > 1) {
                    submitButton.setEnabled(false);
                    warningMessageModel.setObject("Please select only one node as top node of the workspace");
                    warningMessage.setVisible(true);
                    
                    nodeIdForm.setModelObject(null);
                } else {
                
                    final CorpusNode node = (CorpusNode) treePanel.getSelectedNodes().iterator().next();

                    if(CorpusNodeType.COLLECTION != node.getType() && CorpusNodeType.METADATA != node.getType()) { //only metadata should be selectable
                        submitButton.setEnabled(false);
                        warningMessageModel.setObject("Please select a metadata node as top node of the workspace");
                        warningMessage.setVisible(true);
                    } else {
                        submitButton.setEnabled(true);
                        warningMessage.setVisible(false);
                    }

                    nodeIdForm.setModel(new CompoundPropertyModel<CorpusNode>(node));
                }
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
        form.add(new Label("type"));

	submitButton = new Button("createWorkspace") {
	    @Override
	    public void onSubmit() {
                final String currentUserId = LamusSession.get().getUserId();
		final URI selectedNodeURI = form.getModelObject().getNodeURI();
		// Request a new workspace with workspace service
                try {
                    Workspace createdWorkspace = workspaceService.createWorkspace(currentUserId, selectedNodeURI);
                    setResponsePage(pagesProvider.getWorkspacePage(createdWorkspace));
                } catch (NodeAccessException ex) {
                    Session.get().error(ex.getMessage());
                } catch (WorkspaceImportException ex) {
                    Session.get().error(ex.getMessage());
                }
	    }
	};
        submitButton.setEnabled(false);
	form.add(submitButton);
        
        warningMessageModel = Model.of("Please select a metadata node as top node of the workspace");
        
        warningMessage = new Label("warning_message", warningMessageModel);
        warningMessage.setVisible(false);
        form.add(warningMessage);

	// Put details/submit form in container for refresh through AJAX 
	final MarkupContainer formContainer = new WebMarkupContainer("formContainer");
	formContainer.add(form);
	// Add container to page
	add(formContainer);

	return form;
    }
}
