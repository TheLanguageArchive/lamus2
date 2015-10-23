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
import java.util.zip.ZipInputStream;
import nl.mpi.lamus.exception.DisallowedPathException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.pages.LamusPage;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.importing.implementation.FileImportProblem;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.importing.implementation.ImportProblem;
import nl.mpi.lamus.workspace.importing.implementation.LinkImportProblem;
import nl.mpi.lamus.workspace.importing.implementation.MatchImportProblem;
import nl.mpi.lamus.workspace.upload.implementation.ZipUploadResult;
import org.apache.wicket.ajax.AjaxRequestTarget;
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
    

    private class FileUploadForm extends Form<Void> {

        FileUploadField fileUploadField;

        
        public FileUploadForm(String name) {
            super(name);

            // set this form to multipart mode (allways needed for uploads!)
            setMultiPart(true);

            // Add one file input field
            add(fileUploadField = new FileUploadField("fileInput"));

            // Set maximum size to 100K for demo purposes
            //setMaxSize(Bytes.kilobytes(100));
            
            
            add(new AutoDisablingAjaxButton("uploadButton", this) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    target.add(getFeedbackPanel());
                    final List<FileUpload> uploads = fileUploadField.getFileUploads();
                    if (uploads != null) {

                        File uploadDirectory = workspaceService.getWorkspaceUploadDirectory(model.getObject().getWorkspaceID());

                        Collection<File> copiedFiles = new ArrayList<>();
                        
                        Collection<ImportProblem> uploadProblems = new ArrayList<>();
                        int failedUploadsCount = 0;

                        for (FileUpload upload : uploads) {
                            // Create a new file
                            File newFile = new File(uploadDirectory, upload.getClientFileName());

                            if (newFile.isDirectory()) {
                                continue;
                            }

                            if (newFile.getName().endsWith(".zip")) {

                                try {
                                    InputStream newInputStream = upload.getInputStream();
                                    try (ZipInputStream zipInputStream = new ZipInputStream(newInputStream)) {
                                        
                                        ZipUploadResult zipUploadResults =
                                                workspaceService.uploadZipFileIntoWorkspace(LamusSession.get().getUserId(), model.getObject().getWorkspaceID(), zipInputStream, newFile.getName());
                                        
                                        copiedFiles.addAll(zipUploadResults.getSuccessfulUploads());
                                        uploadProblems.addAll(zipUploadResults.getFailedUploads());
                                        failedUploadsCount += uploadProblems.size();
                                    }

                                } catch (IOException | DisallowedPathException ex) {
                                    UploadPanel.this.error(ex.getMessage());
                                }
                            } else {

                                try {
                                    if(newFile.exists()) {
                                        uploadProblems.add(new FileImportProblem(newFile, "Uploaded file with the same path already exists.", null));
                                        failedUploadsCount++;
                                        continue;
                                    }
                                    
                                    File tempCopiedFile =
                                            workspaceService.uploadFileIntoWorkspace(LamusSession.get().getUserId(), model.getObject().getWorkspaceID(), upload.getInputStream(), newFile.getName());
                                    
                                    copiedFiles.add(tempCopiedFile);

                                } catch (IOException | DisallowedPathException ex) {
                                    UploadPanel.this.error(ex.getMessage());
                                }
                            }
                        }
                        
                        if(copiedFiles.isEmpty()) {
                            UploadPanel.this.info(getLocalizer().getString("upload_panel_no_files", this));
                            return;
                        }
                        
                        try {
                            uploadProblems.addAll(workspaceService.processUploadedFiles(LamusSession.get().getUserId(), model.getObject().getWorkspaceID(), copiedFiles));

                            int failedLinksCount = 0;
                            
                            for (ImportProblem problem : uploadProblems) {
                                UploadPanel.this.error("Problem with upload: " + problem.getErrorMessage());
                                if(problem instanceof FileImportProblem) {
                                    failedUploadsCount++;
                                } else if(problem instanceof LinkImportProblem || problem instanceof MatchImportProblem) {
                                    failedLinksCount++;
                                }
                            }

                            
                            StringBuilder feedbackMessage = new StringBuilder();
                            
                            if (uploadProblems.isEmpty() && !copiedFiles.isEmpty()) {
                                feedbackMessage.append(getLocalizer().getString("upload_panel_success_message", this));
                                feedbackMessage.append(getLocalizer().getString("upload_panel_total_successful_files", this));
                                feedbackMessage.append(copiedFiles.size());
                                UploadPanel.this.info(feedbackMessage.toString());
                            }
                            
                            if (!uploadProblems.isEmpty()) {
                                feedbackMessage.append(getLocalizer().getString("upload_panel_fail_message", this));
                                if(failedUploadsCount != 0) {
                                    feedbackMessage.append(getLocalizer().getString("upload_panel_total_failed_files", this));
                                    feedbackMessage.append(failedUploadsCount);
                                    feedbackMessage.append("; ");
                                }
                                if(failedLinksCount != 0) {
                                    feedbackMessage.append(getLocalizer().getString("upload_panel_total_failed_links", this));
                                    feedbackMessage.append(failedLinksCount);
                                    feedbackMessage.append("; ");
                                }
                                feedbackMessage.append(getLocalizer().getString("upload_panel_total_successful_files", this));
                                feedbackMessage.append(copiedFiles.size() - failedUploadsCount);
                                UploadPanel.this.error(feedbackMessage.toString());
                            }

                        } catch (WorkspaceException ex) {
                            UploadPanel.this.error(ex.getMessage());
                        }

                    }
                }
            });
        }
    }
}
