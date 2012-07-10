/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.archivetree.wicket;

import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceImportRunner;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.springframework.core.task.TaskExecutor;

/**
 *
 * @author jeafer
 */
public final class CreateWorkspacePage extends WebPage {

    
    private String nodeId;
    private TaskExecutor mockexecutor;
    private WorkspaceFactory workspaceFactory;
    private WorkspaceDao workspaceDao;
    private WorkspaceDirectoryHandler workspaceDirectoryHandler;
//    private final FileImporterFactory importerFactory;
    private WorkspaceImportRunner workspaceImportRunner;
    private WorkspaceManager wsm;
    
    
    public CreateWorkspacePage() {
        super();
        this.wsm = new MokLamusWorkspaceManager(mockexecutor, workspaceFactory, workspaceDao, workspaceDirectoryHandler, workspaceImportRunner);
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
