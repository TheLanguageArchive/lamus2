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
import java.util.List;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.pages.LamusPage;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author guisil
 */
public class UploadPanel extends Panel {
    
    public static final PackageResourceReference DELETE_IMAGE_RESOURCE_REFERENCE = new PackageResourceReference(LamusPage.class, "delete.gif");

    @SpringBean
    private WorkspaceService workspaceService;
    
    private final IModel<Workspace> model;
    

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
        }

        /**
         * @see org.apache.wicket.markup.html.form.Form#onSubmit()
         */
        @Override
        protected void onSubmit() {
            final List<FileUpload> uploads = fileUploadField.getFileUploads();
            if (uploads != null) {
                for (FileUpload upload : uploads) {
                    // Create a new file
//                    File newFile = new File(getUploadFolder(UploadPage.this.model.getObject().getWorkspaceID()), upload.getClientFileName());

                    File newFile = new File(upload.getClientFileName());
                    
                    if(newFile.isDirectory()) {
                        continue;
                    }

                    try {
                        
                        workspaceService.uploadFileIntoWorkspace(
                                LamusSession.get().getUserId(), model.getObject().getWorkspaceID(), upload.getInputStream(), upload.getClientFileName());

                        UploadPanel.this.info("saved file: " + upload.getClientFileName());
                    } catch (Exception e) {
                        throw new IllegalStateException("Unable to write file", e);
                    }
                }
            }
        }
    }
    /**
     * Log.
     */
    private static final Logger log = LoggerFactory.getLogger(UploadPanel.class);
    /**
     * Reference to listview for easy access.
     */
//    private final WorkspaceNodeListView nodeListView;

    /**
     * Constructor.
     *
     * @param parameters Page parameters
     */
    public UploadPanel(String id, IModel<Workspace> model) {
        
        super(id, model);
        
        this.model = model;
        
//        final String currentUserID = LamusSession.get().getUserId();

        // Create feedback panels
        final FeedbackPanel uploadFeedback = new FeedbackPanel("uploadFeedback");

        // Add uploadFeedback to the page itself
        add(uploadFeedback);


        // Add folder view
     
//        add(new UnlinkedNodesPanel("unlinkedNodesPanel", model));
        
        
        

        // Add upload form with progress bar that uses HTML <input type="file" multiple />, so it can upload
        // more than one file in browsers which support "multiple" attribute
        final FileUploadForm progressUploadForm = new FileUploadForm("progressUpload");

        progressUploadForm.add(new UploadProgressBar("progress", progressUploadForm,
                progressUploadForm.fileUploadField));
        add(progressUploadForm);
    }
}
