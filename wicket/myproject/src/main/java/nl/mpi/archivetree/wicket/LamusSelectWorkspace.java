/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.archivetree.wicket;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
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
    private int workspaceID;
    private String userID;
    private int topNodeID;
    private URL topNodeArchiveURL;
    private Date startDate;
    private Date endDate;
    private Date sessionStartDate;
    private Date sessionEndDate;
    private long usedStorageSpace;
    private long maxStorageSpace;
    private WorkspaceStatus status;
    private String message;
    private String archiveInfo;

    public LamusSelectWorkspace() throws MalformedURLException {
        super();
        
        add(new HeaderPanel("headerpanel", "Welcome To Wicket"));
        //add(new ButtonPage("buttonpage"));
        Form nodeIdForm = new Form("workspaceForm");
        final TextField wsid = new TextField("wsId", new Model<Integer>(workspaceID));
        final TextField userid = new TextField("userId", new Model<String>(userID));
        final TextField topnodeid = new TextField("topnodeId", new Model<Integer>(topNodeID));
        //final TextField topnodearchiveurl = new TextField("topnodearchiveurl", new Model<URL>(topNodeArchiveURL));
        final TextField<URL> topnodearchiveurl = new TextField<URL>("topnodearchiveurl",new Model<URL>(topNodeArchiveURL));
        nodeIdForm.add(wsid);
        nodeIdForm.add(userid);
        nodeIdForm.add(topnodeid);
        nodeIdForm.add(topnodearchiveurl);
        add(nodeIdForm);
        Button submitButton = new Button("OpenWorkspace") {

            @Override
            public void onSubmit() {
                int wosid = Integer.parseInt(wsid.getValue());
                String userId = userid.toString();
                int topNodeId = Integer.parseInt(topnodeid.getValue());
                URL topurl = topnodearchiveurl.getConvertedInput();
                ws = new LamusWorkspace(wosid, userId, topNodeId, topurl, null, null, null, null, LATEST_VERSION, LATEST_VERSION, WorkspaceStatus.REFUSED, PARENT_PATH, PARENT_PATH);
                //int nodeid = Integer.parseInt(nodeIdField.getValue());
                //wsm.createWorkspace("jeafer", nodeid);
                //System.out.println("OnSubmit, name = " + nodeId);
            }
        };
        nodeIdForm.add(submitButton);
        add(nodeIdForm);
    }

    public LamusSelectWorkspace(PageParameters params) {
        //TODO:  process page parameters
    }
}
