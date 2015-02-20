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
import java.io.Serializable;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Button page that allows navigation
 * 
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 * @author guisil
 */

public final class ButtonPanel extends FeedbackPanelAwarePanel<Workspace> {

    @SpringBean
    private WorkspaceService workspaceService;
    
    @SpringBean
    private LamusWicketPagesProvider pagesProvider;
    
    private ModalWindow modalConfirmSubmit;
    private SubmitConfirmationOptions submitConfirmationOptions;
    
    
    public ButtonPanel(String id, IModel<Workspace> model, FeedbackPanel feedbackPanel) {
        super(id, model, feedbackPanel);
        submitConfirmationOptions = new SubmitConfirmationOptions(false, false);
        add(new WorkspaceActionsForm("workspaceActionsForm", model));
    }
    
    public ButtonPanel(String id, Workspace workspace, FeedbackPanel feedbackPanel) {
        this(id, new WorkspaceModel(workspace), feedbackPanel);
    }

    
    /**
     * Form that allows user to select actions on the current workspace
     */
    private class WorkspaceActionsForm extends Form<Workspace> {
        
        public WorkspaceActionsForm(String id, final IModel<Workspace> model) {
            super(id, model);
            
            modalConfirmSubmit = createConfirmationModalWindow();
            add(modalConfirmSubmit);
            
            final Button submitWorkspaceButton = new AutoDisablingAjaxButton("submitWorkspaceButton") {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                    target.add(getFeedbackPanel());
                    modalConfirmSubmit.show(target);
                }
            };
            
            add(submitWorkspaceButton);
            
            final String deleteConfirmationMessage = "Are you sure you want to proceed with the deletion of the workspace?";
            final IndicatingAjaxButton deleteWorkspaceButton = new ConfirmationAjaxButton("deleteWorkspaceButton", deleteConfirmationMessage) {

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
            
            add(deleteWorkspaceButton);
        }
    }
    
    
    private void onSubmitConfirm(AjaxRequestTarget target, boolean keepUnlinkedFiles) {
        try {
            workspaceService.submitWorkspace(getModelObject().getUserID(), getModelObject().getWorkspaceID(), keepUnlinkedFiles);
        } catch (WorkspaceNotFoundException | WorkspaceAccessException | WorkspaceExportException ex) {
            Session.get().error(ex.getMessage());
        }
        
        Session.get().info("Workspace successfully submitted");
                    
        setResponsePage(pagesProvider.getIndexPage());
    }
    
    private void onSubmitCancel(AjaxRequestTarget target) {
        // do nothing
    }
    
    private ModalWindow createConfirmationModalWindow() {
        
        modalConfirmSubmit = new ModalWindow("modalConfirmSubmit");
        modalConfirmSubmit.setContent(new ConfirmSubmitPanel(modalConfirmSubmit.getContentId(), modalConfirmSubmit, submitConfirmationOptions));
        modalConfirmSubmit.setTitle("Submit Workspace");
        modalConfirmSubmit.setCookieName("modal-confirm-submit");
        modalConfirmSubmit.setWindowClosedCallback((new ModalWindow.WindowClosedCallback() {
            @Override
            public void onClose(AjaxRequestTarget art) {
                if (submitConfirmationOptions.isSubmitConfirmed()) {
                    onSubmitConfirm(art, submitConfirmationOptions.isKeepUnlinkedFiles());
                } else {
                    onSubmitCancel(art);
                }
            }
        }));
        
        return modalConfirmSubmit;
    }
    
    
    public class SubmitConfirmationOptions implements Serializable {
        
        private boolean submitConfirmed;
        private boolean keepUnlinkedFiles;
        
        public SubmitConfirmationOptions(boolean submitConfirmed, boolean keepUnlinkedFiles) {
            this.submitConfirmed = submitConfirmed;
            this.keepUnlinkedFiles = keepUnlinkedFiles;
        }
        
        
        public boolean isSubmitConfirmed() {
            return submitConfirmed;
        }
        public void setSubmitConfirmed(boolean submitConfirmed) {
            this.submitConfirmed = submitConfirmed;
        }
        
        public boolean isKeepUnlinkedFiles() {
            return keepUnlinkedFiles;
        }
        public void setKeepUnlinkedFiles(boolean keepUnlinkedFiles) {
            this.keepUnlinkedFiles = keepUnlinkedFiles;
        }
    }
}
