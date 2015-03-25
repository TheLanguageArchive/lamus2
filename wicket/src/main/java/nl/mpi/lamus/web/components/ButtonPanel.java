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
import nl.mpi.lamus.exception.MetadataValidationException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.typechecking.implementation.MetadataValidationIssue;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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
    
    
    public ButtonPanel(String id, IModel<Workspace> model, FeedbackPanel feedbackPanel) {
        super(id, model, feedbackPanel);
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
            
            final ModalWindow modalConfirmSubmit = createConfirmationModalWindow(WorkspaceSubmissionType.SUBMIT_WORKSPACE);
            add(modalConfirmSubmit);
            final ModalWindow modalConfirmDelete = createConfirmationModalWindow(WorkspaceSubmissionType.DELETE_WORKSPACE);
            add(modalConfirmDelete);
            
            final IndicatingAjaxButton submitWorkspaceButton = new AutoDisablingAjaxButton("submitWorkspaceButton") {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                    target.add(getFeedbackPanel());
                    modalConfirmSubmit.show(target);
                }
            };
            
            add(submitWorkspaceButton);
            
            final IndicatingAjaxButton deleteWorkspaceButton = new AutoDisablingAjaxButton("deleteWorkspaceButton") {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    target.add(getFeedbackPanel());
                    modalConfirmDelete.show(target);
                }
            };
            
            add(deleteWorkspaceButton);
        }
    }
    
    
    private void onSubmitConfirm(AjaxRequestTarget target, boolean keepUnlinkedFiles) {
        
        boolean success = false;
        boolean showInitialPage = true;
        try {
            workspaceService.submitWorkspace(getModelObject().getUserID(), getModelObject().getWorkspaceID(), keepUnlinkedFiles);
            success = true;
        } catch (WorkspaceNotFoundException | WorkspaceAccessException | WorkspaceExportException ex) {
            Session.get().error(ex.getMessage());
        } catch(MetadataValidationException ex) {
            StringBuilder errorMessage = new StringBuilder();
            for(MetadataValidationIssue issue : ex.getValidationIssues()) {
                errorMessage.append(issue.toString()).append(" ");
            }
            Session.get().error(errorMessage);
            
            showInitialPage = false;
        }
        
        if(success) {
            Session.get().info("Workspace successfully submitted");
        }
        if(showInitialPage) {
            setResponsePage(pagesProvider.getIndexPage());
        }
        
        target.add(getFeedbackPanel());
    }
    
    private void onDeleteConfirm(AjaxRequestTarget target, boolean keepUnlinkedFiles) {
        try {
            workspaceService.deleteWorkspace(getModelObject().getUserID(), getModelObject().getWorkspaceID(), keepUnlinkedFiles);
        } catch (WorkspaceNotFoundException | WorkspaceAccessException | WorkspaceExportException | IOException ex) {
            Session.get().error(ex.getMessage());
        }
        
        Session.get().info("Workspace successfully deleted");
        
        setResponsePage(pagesProvider.getIndexPage());
    }
    
    private void onCancel(AjaxRequestTarget target) {
        // do nothing
    }
    
    private ModalWindow createConfirmationModalWindow(WorkspaceSubmissionType submissionType) {
        
        String id;
        String title;
        String cookieName;
        String confirmationText;
        if(WorkspaceSubmissionType.SUBMIT_WORKSPACE.equals(submissionType)) {
            id = "modalConfirmSubmit";
            title = "Submit Workspace";
            cookieName = "modal-confirm-submit";
            confirmationText = getLocalizer().getString("submit_workspace_confirm", this);
        } else if (WorkspaceSubmissionType.DELETE_WORKSPACE.equals(submissionType)) {
            id = "modalConfirmDelete";
            title = "Delete Workspace";
            cookieName = "modal-confirm-delete";
            confirmationText = getLocalizer().getString("delete_workspace_confirm", this);
        } else {
            throw new UnsupportedOperationException("Submission type not supported");
        }
        
        final ConfirmationOptions options = new ConfirmationOptions(false, true, submissionType, confirmationText);
        
        ModalWindow modalConfirm = new ModalWindow(id);
        modalConfirm.setContent(new ConfirmPanel(modalConfirm.getContentId(), modalConfirm, options));
        modalConfirm.setTitle(title);
        modalConfirm.setCookieName(cookieName);
        modalConfirm.setWindowClosedCallback((new ModalWindow.WindowClosedCallback() {
            @Override
            public void onClose(AjaxRequestTarget art) {
                if (options.isConfirmed()) {
                    if(WorkspaceSubmissionType.SUBMIT_WORKSPACE.equals(options.getWorkspaceSubmissionType())) {
                        onSubmitConfirm(art, options.isKeepUnlinkedFiles());
                    } else if(WorkspaceSubmissionType.DELETE_WORKSPACE.equals(options.getWorkspaceSubmissionType())) {
                        onDeleteConfirm(art, options.isKeepUnlinkedFiles());
                    } else {
                        throw new UnsupportedOperationException("Confirmation type not supported");
                    }
                } else {
                    onCancel(art);
                }
            }
        }));
        
        return modalConfirm;
    }
    
    
    public class ConfirmationOptions implements Serializable {
        
        
        private boolean confirmed;
        private boolean keepUnlinkedFiles;
        private WorkspaceSubmissionType submissionType;
        private String confirmationText;
        
        public ConfirmationOptions(boolean confirmed, boolean keepUnlinkedFiles,
                WorkspaceSubmissionType type, String confirmationText) {
            this.confirmed = confirmed;
            this.keepUnlinkedFiles = keepUnlinkedFiles;
            this.submissionType = type;
            this.confirmationText = confirmationText;
        }
        
        
        public boolean isConfirmed() {
            return confirmed;
        }
        public void setConfirmed(boolean confirmed) {
            this.confirmed = confirmed;
        }
        
        public boolean isKeepUnlinkedFiles() {
            return keepUnlinkedFiles;
        }
        public void setKeepUnlinkedFiles(boolean keepUnlinkedFiles) {
            this.keepUnlinkedFiles = keepUnlinkedFiles;
        }
        
        public WorkspaceSubmissionType getWorkspaceSubmissionType() {
            return submissionType;
        }
        
        public String getConfirmationText() {
            return confirmationText;
        }
    }
}
