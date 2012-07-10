/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.archivetree.wicket;

import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.management.implementation.LamusWorkspaceManager;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

/**
 *
 * @author jeafer
 */
public final class CreateWorkspacePage extends WebPage {

    
    private String nodeId;
    private WorkspaceManager wsm;
    private WorkspaceFactory wsFactory; // = new MockWSFactory()
    
    public CreateWorkspacePage() {
        super();
        this.wsm = (WorkspaceManager) wsFactory;
//        add(new Button("createWorkspace"){
//                public void onSubmit() {
//                    //wsm.createWorkspace(nodeId, FLAG_RESERVED1);
//                    info("OK was pressed!");
//                }
//            });
        Form nodeIdForm = new Form("nodeIdForm");
        final TextField nodeIdField = new TextField("nodeId", new Model<String>(nodeId));
        nodeIdForm.add(nodeIdField);
        add(nodeIdForm);
        Button submitButton = new Button("createWorkspace"){
        @Override
                public void onSubmit() {
            int nodeid = Integer.parseInt(nodeIdField.getValue());
                    wsm.createWorkspace("jeafer", nodeid);
        //System.out.println("OnSubmit, name = " + nodeId);
                }
        };
                nodeIdForm.add(submitButton);
                add(nodeIdForm);
}
}
