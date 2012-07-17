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

import java.net.MalformedURLException;
import java.net.URL;
import nl.mpi.lamus.web.components.HeaderPanel;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;

/**
 *
 * @author jeafer
 */
public final class SelectWorkspacePage extends WebPage {

    private Workspace ws;
    private String userID;
    private int topNodeID;

    
    private WorkspaceFactory wsFactory; // = new MockWSFactory()

    public SelectWorkspacePage(WorkspaceFactory workspaceFactory) throws MalformedURLException {
        super();
        this.wsFactory = workspaceFactory;
        
        add(new HeaderPanel("headerpanel", "Welcome To Wicket"));
        //add(new ButtonPage("buttonpage"));
        Form nodeIdForm = new Form("workspaceForm");
        final TextField wsid = new TextField("workspaceId");
        final TextField userid = new TextField("userId");
        final TextField topnodeid = new TextField("topnodeId");
        //final TextField topnodearchiveurl = new TextField("topnodearchiveurl", new Model<URL>(topNodeArchiveURL));
        final TextField<URL> topnodearchiveurl = new TextField<URL>("topnodearchiveurl");
        nodeIdForm.add(wsid);
        nodeIdForm.add(userid);
        nodeIdForm.add(topnodeid);
        nodeIdForm.add(topnodearchiveurl);
        add(nodeIdForm);
        Button submitButton = new Button("OpenWorkspace") {

            @Override
            public void onSubmit() {
                Workspace newWorkspace = wsFactory.getNewWorkspace(userID, topNodeID);
            }
        };
        nodeIdForm.add(submitButton);
        add(nodeIdForm);
    }

    public SelectWorkspacePage(PageParameters params) {
        //TODO:  process page parameters
        add(new HeaderPanel("headerpanel", "Welcome To Wicket"));
    }
}
