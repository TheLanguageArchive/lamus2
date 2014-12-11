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
package nl.mpi.lamus.web.components;

import java.io.IOException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Button page that allows navigation
 * 
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */

public final class ButtonPanel extends Panel {
// Services to be injected
    @SpringBean
    private WorkspaceService workspaceService;
    
    @SpringBean
    private LamusWicketPagesProvider pagesProvider;
    
    
    public ButtonPanel(String id, IModel<Workspace> model) {
        super(id, model);
        add(new WorkspaceActionsForm("workspaceActionsForm", model));
    }
    
    public ButtonPanel(String id, Workspace workspace) {
        this(id, new WorkspaceModel(workspace));
    }

    
    /**
     * Form that allows user to select actions on the current workspace
     */
    private class WorkspaceActionsForm extends Form<Workspace> {
        
        public WorkspaceActionsForm(String id, final IModel<Workspace> model) {
            super(id, model);

            final IndicatingAjaxButton submitWorkspaceButton = new IndicatingAjaxButton("submitWorkspaceButton") {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        workspaceService.submitWorkspace(model.getObject().getUserID(), model.getObject().getWorkspaceID());
                    } catch (WorkspaceNotFoundException | WorkspaceAccessException | WorkspaceExportException ex) {
                        Session.get().error(ex.getMessage());
                    }
                    
                    
                    //TODO SHOW SUCCESS MESSAGE
                    //TODO SHOW SUCCESS MESSAGE
                    //TODO SHOW SUCCESS MESSAGE
                    
                    
                    setResponsePage(pagesProvider.getIndexPage());

                }
            };
            submitWorkspaceButton.add(new AttributeModifier("onclick",
                    "if(!confirm('If there are unlinked nodes in the workspace,"
                    + " they will be deleted. Are you sure you want to proceed?'))return false;"));
            add(submitWorkspaceButton);
            
            final IndicatingAjaxButton deleteWorkspaceButton = new IndicatingAjaxButton("deleteWorkspaceButton") {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        workspaceService.deleteWorkspace(model.getObject().getUserID(), model.getObject().getWorkspaceID());
                    } catch (WorkspaceNotFoundException | WorkspaceAccessException | IOException ex) {
                        Session.get().error(ex.getMessage());
                    }
                    setResponsePage(pagesProvider.getIndexPage());
                }
            };
            deleteWorkspaceButton.add(new AttributeModifier("onclick", "if(!confirm('are you sure?'))return false;"));
            add(deleteWorkspaceButton);
        }
    }
}
