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
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanel;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanelListener;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.web.components.AutoDisablingAjaxButton;
import nl.mpi.lamus.web.components.NavigationPanel;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
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
 * @author guisil
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
    private Button createWorkspaceButton;
    private Button addTopNodeButton;
    private Label warningMessage;
    private Model<String> warningMessageModel;

    
    public CreateWorkspacePage() {
    	super();
        
        createNavigationPanel("navigationPanel");
        
		nodeIdForm = createNodeIdForm("nodeIdForm");
		createArchiveTreePanel("archiveTree");
		
		if(!LamusSession.get().isAuthenticated()) {
			LamusSession.get().invalidateNow();
		}	
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
                    createWorkspaceButton.setEnabled(false);
                    warningMessageModel.setObject("Please select a metadata node as top node of the workspace");
                    addTopNodeButton.setEnabled(false);
                    info("Please select a metadata node as top node of the workspace");
                    nodeIdForm.setModelObject(null);
                } else if(treePanel.getSelectedNodes().size() > 1) {
                    createWorkspaceButton.setEnabled(false);
                    warningMessageModel.setObject("Please select only one node as top node of the workspace");
                    addTopNodeButton.setEnabled(false);
                    info("Please select only one node as top node of the workspace");
                    nodeIdForm.setModelObject(null);
                } else {
                    final CorpusNode node = (CorpusNode) treePanel.getSelectedNodes().iterator().next();
                    if(CorpusNodeType.COLLECTION != node.getType() && CorpusNodeType.METADATA != node.getType()) { //only metadata should be selectable
                        createWorkspaceButton.setEnabled(false);
                        warningMessageModel.setObject("Please select a metadata node as top node of the workspace");
                        addTopNodeButton.setEnabled(false);
                        info("Please select a metadata node as top node of the workspace");
                    } else {
                        createWorkspaceButton.setEnabled(true);
                        
                        if(archiveTreeProvider.getRoot().equals(node)) {
                            addTopNodeButton.setEnabled(true);
                        } else {
                            addTopNodeButton.setEnabled(false);
                        }
                    }
                    nodeIdForm.setModel(new CompoundPropertyModel<>(node));
                }
                if (target != null) {
                    // Ajax, refresh nodeIdForm
                    target.add(nodeIdForm);
                    target.add(getFeedbackPanel());
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
	final Form<CorpusNode> createWsForm = new Form<>(id);
	createWsForm.add(new Label("name"));
	createWsForm.add(new Label("nodeURI"));
        createWsForm.add(new Label("type"));

	createWorkspaceButton = new AutoDisablingAjaxButton("createWorkspace") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                
                target.add(getFeedbackPanel());

                final String currentUserId = LamusSession.get().getUserId();
		final URI selectedNodeURI = createWsForm.getModelObject().getNodeURI();
		// Request a new workspace with workspace service
                try {
                    Workspace createdWorkspace = workspaceService.createWorkspace(currentUserId, selectedNodeURI);
                    setResponsePage(pagesProvider.getWorkspacePage(createdWorkspace));
                } catch (NodeNotFoundException | NodeAccessException | WorkspaceImportException ex) {
                    Session.get().error(ex.getMessage());
                }
            }
	};
        createWorkspaceButton.setEnabled(false);
	createWsForm.add(createWorkspaceButton);
        
        warningMessageModel = Model.of("Please select a metadata node as top node of the workspace");
        
        warningMessage = new Label("warning_message", warningMessageModel);
        warningMessage.setVisible(false);
        createWsForm.add(warningMessage);

        
        addTopNodeButton = new IndicatingAjaxButton("addTopNode") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form); //To change body of generated methods, choose Tools | Templates.
            }
        };
        addTopNodeButton.setEnabled(false);
        
        
        //TODO This should be removed once the functionality is implemented
        addTopNodeButton.setVisible(false);
                
        
        createWsForm.add(addTopNodeButton);
        
        
        add(createWsForm);

	return createWsForm;
    }
}
