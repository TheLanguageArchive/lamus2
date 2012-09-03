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
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.file.Folder;

/**
 *
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
public class FreeNodesPage extends LamusPage {
        public static final PackageResourceReference DELETE_IMAGE_RESOURCE_REFERENCE = new PackageResourceReference(LamusPage.class, "delete.gif");
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
         * Add clickable icon to remove unwanted uploaded files
         */
        @Override
        protected void populateItem(ListItem<File> listItem) {           
            final File file = listItem.getModelObject();           
            listItem.add(new Label("file", file.getName()));
                Link link = new Link("delete"){
                @Override
                public void onClick() {
                    Files.remove(file);
                    info("Deleted " + file);
                }
            };
            link.add(new Image("image2", DELETE_IMAGE_RESOURCE_REFERENCE));
            listItem.add(link);
        }
    }
    private final FileListView fileListView;

    /**
     * Constructor.
     *
     * @param parameters Page parameters
     */
    public FreeNodesPage(IModel<Workspace> model) { 
        
        add(new ButtonPage("buttonpage", model));
        Folder uploadFolder = getUploadFolder();


        // Add folder view
        add(new Label("dir", uploadFolder.getAbsolutePath()));
        fileListView = new FileListView("fileList", new LoadableDetachableModel<List<File>>() {

            @Override
            protected List<File> load() {
                return Arrays.asList(getUploadFolder().listFiles());
            }
        });
        add(fileListView);

    }


    private Folder getUploadFolder() {
        return ((LamusWicketApplication) Application.get()).getUploadFolder();
    }
}
