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
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.web.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.components.AutoDisablingAjaxButton;
import nl.mpi.lamus.web.components.NavigationPanel;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Page that displays list of workspaces that can be opened
 *
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
public class SelectWorkspacePage extends LamusPage {

    // service to be injected
    @SpringBean
    private WorkspaceService workspaceService;
    
    @SpringBean
    private LamusWicketPagesProvider pagesProvider;
    
    final String currentUserId = LamusSession.get().getUserId();

    public SelectWorkspacePage() {
        super();
        createNodeIdForm("workspaceForm");
        
        createNavigationPanel("navigationPanel");
    }

    private NavigationPanel createNavigationPanel(final String id) {
        NavigationPanel navPanel = new NavigationPanel(id);
        add(navPanel);
        return navPanel;
    }
    
    /**
     * Create Form that will show a list of Workspaces to be opened by a
     * specific user
     *
     * @param id
     * @return created form
     */
    private Form createNodeIdForm(String id) {

        boolean showPanel = true;
        Workspace defaultSelectedWs = null;
        List<Workspace> myWSList = new ArrayList<>(workspaceService.listUserWorkspaces(currentUserId));
        if(!myWSList.isEmpty()) {
            defaultSelectedWs = myWSList.iterator().next();
        } else {
            showPanel = false;
        }
        IModel<Workspace> workspaceModel = new WorkspaceModel(defaultSelectedWs);
        
        ListChoice<Workspace> listWorkspaces = new ListChoice<>("workspaceSelection", workspaceModel, myWSList, new ChoiceRenderer<Workspace>("workspaceSelectionDisplayString"));
        listWorkspaces.setMaxRows(5);
        listWorkspaces.setNullValid(false);
        listWorkspaces.setRequired(true);
        final Form<Workspace> openWsForm = new Form<>(id, workspaceModel);

        Button submitButton = new AutoDisablingAjaxButton("openWorkspace") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                
                target.add(getFeedbackPanel());
                
                try {
                    if(form.getModelObject() != null) {
                        Workspace openSelectedWorkspace = workspaceService.openWorkspace(currentUserId, openWsForm.getModelObject().getWorkspaceID());
                        setResponsePage(pagesProvider.getWorkspacePage(openSelectedWorkspace));
                    } else {
                        //TODO MESSAGE IN FEEDBACK PANEL?
                    }
                } catch (WorkspaceNotFoundException | WorkspaceAccessException | IOException ex) {
                    Session.get().error(ex.getMessage());
                }
            }
        };
        openWsForm.add(submitButton);
        openWsForm.add(listWorkspaces);

        // Put details/submit form in container for refresh through AJAX 
        final MarkupContainer formContainer = new WebMarkupContainer("formContainer");
        formContainer.add(openWsForm);
        
        if(!showPanel) {
            formContainer.setVisible(false);
            Session.get().info(getLocalizer().getString("select_workspace_no_open_workspaces", this));
        }
        
        // Add container to page
        add(formContainer);
        return openWsForm;

    }
}
