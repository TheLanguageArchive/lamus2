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

import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
public final class ButtonPage extends Panel {

    //private final IModel<Workspace> model;
    
    public ButtonPage(String id) {
        super(id);
        //this.model = model;
        add(new WorkspaceActionsForm("workspaceActionsForm"));
    }

    
       /**
     * Form that allows user to select actions on the current workspace
     */
    private class WorkspaceActionsForm extends Form<Workspace> {

        public WorkspaceActionsForm(String id) {
            super(id);

            final Button uploadFilesButton = new Button("uploadFilesButton") {

                @Override
                public void onSubmit() {
                    final UploadPage resultPage = new UploadPage();
		setResponsePage(resultPage);
                }
            };
            add(uploadFilesButton);

            final Button requestStorageButton = new Button("requestStorageButton") {

                @Override
                public void onSubmit() {
                    handleRequestStorage();
                }
            };
            add(requestStorageButton);

            final Button unlinkedFilesButton = new Button("unlinkedFilesButton") {

                @Override
                public void onSubmit() {
                    handleUnlinkedFiles();
                }
            };
            add(unlinkedFilesButton);
            
            final Button indexPageButton = new Button("indexPageButton") {

                @Override
                public void onSubmit() {
                    final IndexPage resultPage = new IndexPage();
		setResponsePage(resultPage);
                }
            };
            add(indexPageButton);
        }
    }

    private void handleUploadFiles() {
    }

    private void handleRequestStorage() {
    }

    private void handleUnlinkedFiles() {
    }
}
