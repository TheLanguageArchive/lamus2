/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.archivetree.wicket;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import nl.mpi.archivetree.model.mock.MockWorkspace;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

/**
 *
 * @author jeafer
 */
public final class LamusSelectWorkspace extends WebPage {

    private Workspace ws;
    private String userID;
    private int topNodeID;

    
    private WorkspaceFactory wsFactory; // = new MockWSFactory()

    public LamusSelectWorkspace(WorkspaceFactory workspaceFactory) throws MalformedURLException {
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

    public LamusSelectWorkspace(PageParameters params) {
        //TODO:  process page parameters
        add(new HeaderPanel("headerpanel", "Welcome To Wicket"));
    }
}
