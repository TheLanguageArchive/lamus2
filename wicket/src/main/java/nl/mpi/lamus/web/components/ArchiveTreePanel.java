package nl.mpi.lamus.web.components;

import java.io.Serializable;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import nl.mpi.archiving.tree.ArchiveNode;
import nl.mpi.archiving.tree.ArchiveNodeTreeModelProvider;
import nl.mpi.archiving.tree.ArchiveNodeTreeNodeWrapper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree.LinkType;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.panel.Panel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArchiveTreePanel<T extends ArchiveNode & Serializable> extends Panel {

    private final Tree archiveTree;

    public ArchiveTreePanel(String id, ArchiveNodeTreeModelProvider provider) {
	super(id);
	archiveTree = createArchiveTree("archiveTree", provider);
	add(archiveTree);
    }

    private Tree createArchiveTree(String id, ArchiveNodeTreeModelProvider archiveTreeProvider) {
	final DefaultTreeModel treeModel = new DefaultTreeModel(new ArchiveNodeTreeNodeWrapper(archiveTreeProvider.getRoot()));
	final Tree tree = new Tree(id, treeModel) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node) {
		super.onNodeLinkClicked(target, node);

		// TOOD: Make more robust against other types in model
		final ArchiveNodeTreeNodeWrapper nodeWrapper = (ArchiveNodeTreeNodeWrapper) node;
		final T archiveNode = (T) nodeWrapper.getArchiveNode();
		ArchiveTreePanel.this.onNodeLinkClicked(target, archiveNode);
	    }
	};
	return tree;
    }

    protected abstract void onNodeLinkClicked(AjaxRequestTarget target, T node);

    public void setLinkType(LinkType linkType) {
	archiveTree.setLinkType(linkType);
    }
}
