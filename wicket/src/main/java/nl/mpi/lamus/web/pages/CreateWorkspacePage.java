/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.lamus.web.pages;

import nl.mpi.archiving.tree.ArchiveNodeTreeModel;
import nl.mpi.archiving.tree.ArchiveNodeTreeModelProvider;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.components.WicketArchiveNodeTree;
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

    @SpringBean
    private WorkspaceService workspaceService;
    @SpringBean
    private ArchiveNodeTreeModelProvider archiveTreeProvider;
    private String nodeId;

    public CreateWorkspacePage() {
	super();

	Form nodeIdForm = new Form("nodeIdForm");

	final ArchiveNodeTreeModel treeModel = new ArchiveNodeTreeModel(archiveTreeProvider);
	final WicketArchiveNodeTree archiveTree = new WicketArchiveNodeTree("archiveTree", treeModel);
	archiveTree.setLinkType(WicketArchiveNodeTree.LinkType.REGULAR);
	nodeIdForm.add(archiveTree);

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
