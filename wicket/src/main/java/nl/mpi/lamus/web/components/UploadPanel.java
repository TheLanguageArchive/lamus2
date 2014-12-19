/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.zip.ZipInputStream;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.pages.LamusPage;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.upload.implementation.FileUploadProblem;
import nl.mpi.lamus.workspace.upload.implementation.LinkUploadProblem;
import nl.mpi.lamus.workspace.upload.implementation.MatchUploadProblem;
import nl.mpi.lamus.workspace.upload.implementation.UploadProblem;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author guisil
 */
public class UploadPanel extends FeedbackPanelAwarePanel<Workspace> {
    
    private static final Logger log = LoggerFactory.getLogger(UploadPanel.class);

    public static final PackageResourceReference DELETE_IMAGE_RESOURCE_REFERENCE = new PackageResourceReference(LamusPage.class, "delete.gif");

    @SpringBean
    private WorkspaceService workspaceService;
    
    private final IModel<Workspace> model;
    
    
    /**
     * Constructor.
     *
     * @param parameters Page parameters
     */
    public UploadPanel(String id, IModel<Workspace> model, FeedbackPanel feedbackPanel) {
        
        super(id, model, feedbackPanel);
        
        this.model = model;

        // Add upload form with progress bar that uses HTML <input type="file" multiple />, so it can upload
        // more than one file in browsers which support "multiple" attribute
        final FileUploadForm progressUploadForm = new FileUploadForm("progressUpload");

        progressUploadForm.add(new UploadProgressBar("progress", progressUploadForm,
                progressUploadForm.fileUploadField));
        add(progressUploadForm);
    }
    

    /**
     * Form for uploads.
     */
    private class FileUploadForm extends Form<Void> {

        FileUploadField fileUploadField;

        /**
         * Construct.
         *
         * @param name Component name
         */
        public FileUploadForm(String name) {
            super(name);

            // set this form to multipart mode (allways needed for uploads!)
            setMultiPart(true);

            // Add one file input field
            add(fileUploadField = new FileUploadField("fileInput"));

            // Set maximum size to 100K for demo purposes
            //setMaxSize(Bytes.kilobytes(100));
            
            
            add(new IndicatingAjaxButton("uploadButton", this) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    target.add(getFeedbackPanel());
                    final List<FileUpload> uploads = fileUploadField.getFileUploads();
                    if (uploads != null) {

                        File uploadDirectory = workspaceService.getWorkspaceUploadDirectory(model.getObject().getWorkspaceID());

                        Collection<File> copiedFiles = new ArrayList<>();

                        for (FileUpload upload : uploads) {
                            // Create a new file
                            File newFile = new File(uploadDirectory, upload.getClientFileName());

                            if (newFile.isDirectory()) {
                                continue;
                            }

                            //TODO a better way of deciding this? typechecker?
                            if (newFile.getName().endsWith(".zip")) {

                                try {
                                    InputStream newInputStream = upload.getInputStream();
                                    try (ZipInputStream zipInputStream = new ZipInputStream(newInputStream)) {

                                        Collection<File> tempCopiedFiles =
                                                workspaceService.uploadZipFileIntoWorkspace(LamusSession.get().getUserId(), model.getObject().getWorkspaceID(), zipInputStream, newFile.getName());
                                        copiedFiles.addAll(tempCopiedFiles);
                                    }

                                } catch (IOException ex) {
                                    UploadPanel.this.error(ex.getMessage());
                                }
                            } else {

                                try {
                                    //TODO PERFORM A "SHALLOW" TYPECHECK BEFORE UPLOADING?

                                    // Save to new file
                                    newFile.createNewFile();
                                    upload.writeTo(newFile);

                                    //TODO ADD UPLOADED FILE TO LIST OF FILES TO PROCESS LATER
                                    copiedFiles.add(newFile);

                                } catch (IOException | MissingResourceException e) {
                                    throw new IllegalStateException(getLocalizer().getString("upload_panel_failure_message", this), e);
                                }
                            }
                        }

                        try {
                            Collection<UploadProblem> uploadProblems = workspaceService.processUploadedFiles(LamusSession.get().getUserId(), model.getObject().getWorkspaceID(), copiedFiles);

                            for (UploadProblem problem : uploadProblems) {

                                if (problem instanceof FileUploadProblem) {

                                    UploadPanel.this.error("Problem with upload: " + problem.getErrorMessage());


                                    //TODO MORE COMPLETE MESSAGE OR GROUP ALL MESSAGES IN ONE

                                    continue;
                                }
                                if (problem instanceof LinkUploadProblem) {

                                    UploadPanel.this.error("Problem with upload: " + problem.getErrorMessage());


                                    //TODO MORE COMPLETE MESSAGE OR GROUP ALL MESSAGES IN ONE

                                    continue;
                                }
                                if (problem instanceof MatchUploadProblem) {

                                    UploadPanel.this.error("Problem with upload: " + problem.getErrorMessage());


                                    //TODO MORE COMPLETE MESSAGE OR GROUP ALL MESSAGES IN ONE

                                    continue;
                                }

                                //TODO COMPLETE EXCEPTION
                                throw new IllegalStateException();
                            }

                            //TODO IF THERE WERE ERRORS, THE SUCCESSFUL FILES SHOULD BE MENTIONED ANYWAY


                            if (uploadProblems.isEmpty()) {
                                UploadPanel.this.info(getLocalizer().getString("upload_panel_success_message", this) + copiedFiles.toString());



                                //TODO PRINT A LIST OF THE FILES? - might be a problem if they're too many

                            }

                        } catch (IOException | WorkspaceException | TypeCheckerException ex) {
                            UploadPanel.this.error(ex.getMessage());
                        }

                    }
                }
            });
        }
    }
}
