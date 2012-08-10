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
package nl.mpi.lamus.web.components;

import java.io.Serializable;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.archiving.tree.GenericTreeNode;
import nl.mpi.archiving.tree.swingtree.GenericTreeSwingTreeNodeWrapper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree.LinkType;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.panel.Panel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArchiveTreePanel<T extends GenericTreeNode & Serializable> extends Panel {

    private final Tree archiveTree;
    private final GenericTreeModelProvider provider;

    public ArchiveTreePanel(String id, GenericTreeModelProvider provider) {
	super(id);
	this.provider = provider;
	this.archiveTree = createArchiveTree("archiveTree", provider);
	add(archiveTree);
    }

    private Tree createArchiveTree(String id, GenericTreeModelProvider archiveTreeProvider) {
	final DefaultTreeModel treeModel = new DefaultTreeModel(new GenericTreeSwingTreeNodeWrapper(archiveTreeProvider.getRoot()));
	final Tree tree = new Tree(id, treeModel) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node) {
		super.onNodeLinkClicked(target, node);

		// TOOD: Make more robust against other types in model
		final GenericTreeSwingTreeNodeWrapper nodeWrapper = (GenericTreeSwingTreeNodeWrapper) node;
		final T archiveNode = (T) nodeWrapper.getContentNode();
		ArchiveTreePanel.this.onNodeLinkClicked(target, archiveNode);
	    }
	};
	return tree;
    }

    protected abstract void onNodeLinkClicked(AjaxRequestTarget target, T node);

    public void setLinkType(LinkType linkType) {
	archiveTree.setLinkType(linkType);
    }

    @Override
    public void onDetach() {
	super.onDetach();
	provider.onDetach();
    }
}
