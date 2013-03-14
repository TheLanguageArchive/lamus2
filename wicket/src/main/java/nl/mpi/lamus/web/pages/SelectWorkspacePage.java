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

import java.util.ArrayList;
import java.util.List;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Page that displays list of workspaces that can be opened
 *
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
public final class SelectWorkspacePage extends LamusPage {

    // service to be injected
    @SpringBean
    private WorkspaceService workspaceService;
    final String currentUserId = LamusSession.get().getUserId();

    public SelectWorkspacePage() {
        super();
        createNodeIdForm("workspaceForm");
    }

    /**
     * Create Form that will show a list of Workspaces to be opened by a
     * specific user
     *
     * @param id
     * @return created form
     */
    private Form createNodeIdForm(String id) {

        IModel<Workspace> workspaceModel = new WorkspaceModel(null);
        List<Workspace> myWSList = new ArrayList<Workspace>(workspaceService.listUserWorkspaces(currentUserId));
        ListChoice<Workspace> listWorkspaces = new ListChoice<Workspace>("workspace", workspaceModel, myWSList);
        listWorkspaces.setMaxRows(5);
        final Form<Workspace> form = new Form<Workspace>(id, workspaceModel);

        Button submitButton = new Button("OpenWorkspace") {

            @Override
            public void onSubmit() {

                // Request a workspace with workspace service
                final Workspace openSelectedWorkspace = workspaceService.openWorkspace(currentUserId, form.getModelObject().getWorkspaceID());
                // Show page for newly created workspace
                final WorkspacePage resultPage = new WorkspacePage(new WorkspaceModel(openSelectedWorkspace));
                setResponsePage(resultPage);
            }
        };
        form.add(submitButton);
        form.add(listWorkspaces);

        // Put details/submit form in container for refresh through AJAX 
        final MarkupContainer formContainer = new WebMarkupContainer("formContainer");
        formContainer.add(form);
        // Add container to page
        add(formContainer);
        return form;

    }
}
