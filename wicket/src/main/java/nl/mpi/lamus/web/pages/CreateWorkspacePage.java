package nl.mpi.lamus.web.pages;

import nl.mpi.archiving.tree.ArchiveNode;
import nl.mpi.archiving.tree.ArchiveNodeTreeModelProvider;
import nl.mpi.archiving.tree.CorpusArchiveNode;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.components.ArchiveTreePanel;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree.LinkType;
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
    private final Form nodeIdForm;

    public CreateWorkspacePage() {
	super();
	nodeIdForm = createNodeIdForm("nodeIdForm");
	createArchiveTreePanel("archiveTree");
    }

    /**
     * Creates and adds an archive tree panel
     *
     * @return created tree panel
     */
    private ArchiveTreePanel createArchiveTreePanel(final String id) {
	ArchiveTreePanel tree = new ArchiveTreePanel(id, archiveTreeProvider) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, ArchiveNode node) {
		nodeIdForm.setModel(new CompoundPropertyModel<ArchiveNode>(node));

		if (target != null) {
		    // Ajax, refresh nodeIdForm
		    target.addComponent(nodeIdForm);
		}
	    }
	};
	tree.setLinkType(LinkType.AJAX_FALLBACK);
	// Add to page
	add(tree);

	return tree;
    }

    /**
     * Creates and adds node id form
     *
     * @param id component id
     * @return created form
     */
    private Form createNodeIdForm(final String id) {
	final Form<CorpusArchiveNode> form = new Form<CorpusArchiveNode>(id);
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

	// Put details/submit form in container for refresh through AJAX 
	final MarkupContainer formContainer = new WebMarkupContainer("formContainer");
	formContainer.add(form);
	// Add container to page
	add(formContainer);

	return form;
    }
}
