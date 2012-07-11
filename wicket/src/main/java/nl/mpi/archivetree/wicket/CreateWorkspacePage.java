/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.archivetree.wicket;

import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.management.implementation.LamusWorkspaceManager;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author jeafer
 */
public final class CreateWorkspacePage extends WebPage {

    @SpringBean//(name="workspaceService")
    private WorkspaceService workspaceService;
    private String nodeId;

    public CreateWorkspacePage() {
	super();
	//this.wsm = new MokLamusWorkspaceManager(mockexecutor, workspaceFactory, workspaceDao, workspaceDirectoryHandler, workspaceImportRunner);
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
	Button submitButton = new Button("createWorkspace") {

	    @Override
	    public void onSubmit() {
		int nodeid = Integer.parseInt(nodeIdField.getValue());
		workspaceService.createWorkspace("userId", nodeid);
		//System.out.println("OnSubmit, name = " + nodeId);
	    }
	};
	nodeIdForm.add(submitButton);
	add(nodeIdForm);
    }
}
