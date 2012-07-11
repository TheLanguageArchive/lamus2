package nl.mpi.lamus.web.pages;

import java.io.Serializable;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import nl.mpi.archiving.tree.ArchiveNodeTreeModelProvider;
import nl.mpi.archiving.tree.ArchiveNodeTreeNodeWrapper;
import nl.mpi.archiving.tree.CorpusArchiveNode;
import nl.mpi.lamus.service.WorkspaceService;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author jeafer
 */
public final class CreateWorkspacePage extends WebPage {

    // Services to be injected
    @SpringBean
    private WorkspaceService workspaceService;
    @SpringBean
    private ArchiveNodeTreeModelProvider archiveTreeProvider;
    // Page components
    private final Tree archiveTree;
    private final Form nodeIdForm;

    public <T extends CorpusArchiveNode & Serializable> CreateWorkspacePage() {
	super();

	// Create archive tree
	archiveTree = createArchiveTree("archiveTree");
	add(archiveTree);

	// Create details/submit form
	// Put in container for refresh through AJAX 
	final MarkupContainer formContainer = new WebMarkupContainer("formContainer");
	nodeIdForm = createNodeIdForm("nodeIdForm");
	formContainer.add(nodeIdForm);
	add(formContainer);
    }

    private <T> Tree createArchiveTree(String id) {
	final DefaultTreeModel treeModel = new DefaultTreeModel(new ArchiveNodeTreeNodeWrapper(archiveTreeProvider.getRoot()));
	final Tree tree = new Tree(id, treeModel) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node) {
		super.onNodeLinkClicked(target, node);

		// TOOD: Make more robust against other types in model
		final ArchiveNodeTreeNodeWrapper nodeWrapper = (ArchiveNodeTreeNodeWrapper) node;
		final T archiveNode = (T) nodeWrapper.getArchiveNode();
		nodeIdForm.setModel(new CompoundPropertyModel<T>(archiveNode));

		if (target != null) {
		    // Ajax, refresh nodeIdForm
		    target.addComponent(nodeIdForm);
		}
	    }
	};
	tree.setLinkType(Tree.LinkType.AJAX_FALLBACK);
	return tree;
    }

    private <T extends CorpusArchiveNode> Form<T> createNodeIdForm(String id) {
	final Form<T> form = new Form<T>(id);
	form.add(new Label("name"));
	form.add(new Label("nodeId"));

	final Button submitButton = new Button("createWorkspace") {

	    @Override
	    public void onSubmit() {
		// TODO: Get userId out of session through some service
		workspaceService.createWorkspace("userId", form.getModelObject().getNodeId());
	    }
	};
	form.add(submitButton);
	return form;
    }
}
