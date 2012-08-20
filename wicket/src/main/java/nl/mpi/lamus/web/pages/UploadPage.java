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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import nl.mpi.lamus.web.LamusWicketApplication;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.Application;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
@SuppressWarnings("serial")
public class UploadPage extends LamusPage {

    //private final IModel<Workspace> model;

    /**
     * List view for files in upload folder.
     */
    private class FileListView extends ListView<File> {

        /**
         * Construct.
         *
         * @param name Component name
         * @param files The file list model
         */
        public FileListView(String name, final IModel<List<File>> files) {
            super(name, files);
        }

        /**
         * @see ListView#populateItem(ListItem)
         */
        @Override
        protected void populateItem(ListItem<File> listItem) {
            final File file = listItem.getModelObject();
            listItem.add(new Label("file", file.getName()));
            listItem.add(new Link<Void>("delete") {

                @Override
                public void onClick() {
                    Files.remove(file);
                    info("Deleted " + file);
                }
            });
        }
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
            setMaxSize(Bytes.kilobytes(100));
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
                    File newFile = new File(getUploadFolder(), upload.getClientFileName());

                    // Check new file, delete if it already existed
                    checkFileExists(newFile);
                    try {
                        // Save to new file
                        newFile.createNewFile();
                        upload.writeTo(newFile);

                        UploadPage.this.info("saved file: " + upload.getClientFileName());
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
    private static final Logger log = LoggerFactory.getLogger(UploadPage.class);
    /**
     * Reference to listview for easy access.
     */
    private final FileListView fileListView;

    /**
     * Constructor.
     *
     * @param parameters Page parameters
     */
    
    public UploadPage() {
        Folder uploadFolder = getUploadFolder();
        // Create feedback panels
        final FeedbackPanel uploadFeedback = new FeedbackPanel("uploadFeedback");

        // Add uploadFeedback to the page itself
        add(uploadFeedback);


        // Add folder view
        add(new Label("dir", uploadFolder.getAbsolutePath()));
        fileListView = new FileListView("fileList", new LoadableDetachableModel<List<File>>() {

            @Override
            protected List<File> load() {
                return Arrays.asList(getUploadFolder().listFiles());
            }
        });
        add(fileListView);

        // Add upload form with progress bar
        final FileUploadForm progressUploadForm = new FileUploadForm("progressUpload");

        progressUploadForm.add(new UploadProgressBar("progress", progressUploadForm,
                progressUploadForm.fileUploadField));
        add(progressUploadForm);

        // Add upload form that uses HTML5 <input type="file" multiple />, so it can upload
        // more than one file in browsers which support "multiple" attribute
        final FileUploadForm html5UploadForm = new FileUploadForm("html5Upload");
        add(html5UploadForm);

        add(new ButtonPage("buttonpage"));
    }

    /**
     * Check whether the file allready exists, and if so, try to delete it.
     *
     * @param newFile the file to check
     */
    private void checkFileExists(File newFile) {
        if (newFile.exists()) {
            // Try to delete the file
            if (!Files.remove(newFile)) {
                throw new IllegalStateException("Unable to overwrite " + newFile.getAbsolutePath());
            }
        }
    }

    private Folder getUploadFolder() {
        return ((LamusWicketApplication) Application.get()).getUploadFolder();
    }
}
