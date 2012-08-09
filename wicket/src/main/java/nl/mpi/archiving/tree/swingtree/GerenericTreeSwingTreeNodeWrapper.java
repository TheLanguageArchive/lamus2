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
package nl.mpi.archiving.tree.swingtree;

import java.io.Serializable;
import java.util.Enumeration;
import javax.swing.tree.TreeNode;
import nl.mpi.archiving.tree.GenericTreeNode;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class GerenericTreeSwingTreeNodeWrapper implements TreeNode, Serializable {

    private final GenericTreeNode archiveNode;
    private final TreeNode parent;

    /**
     * Creates a parentless tree node ({@link #getParent() } will return null)
     */
    public GerenericTreeSwingTreeNodeWrapper(GenericTreeNode archiveNode) {
	this(archiveNode, null);
    }

    public GerenericTreeSwingTreeNodeWrapper(GenericTreeNode archiveNode, TreeNode parent) {
	this.archiveNode = archiveNode;
	this.parent = parent;
    }

    public GenericTreeNode getArchiveNode() {
	return archiveNode;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
	return new GerenericTreeSwingTreeNodeWrapper(archiveNode.getChild(childIndex), this);
    }

    @Override
    public int getChildCount() {
	return archiveNode.getChildCount();
    }

    @Override
    public TreeNode getParent() {
	return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
	return archiveNode.getIndexOfChild(((GerenericTreeSwingTreeNodeWrapper) node).getArchiveNode());
    }

    @Override
    public boolean getAllowsChildren() {
	return true;
    }

    @Override
    public boolean isLeaf() {
	return archiveNode.getChildCount() == 0;
    }

    @Override
    public Enumeration children() {
	return new Enumeration() {

	    private int index = 0;

	    @Override
	    public boolean hasMoreElements() {
		return index + 1 < archiveNode.getChildCount();
	    }

	    @Override
	    public Object nextElement() {
		return new GerenericTreeSwingTreeNodeWrapper(archiveNode.getChild(index++), GerenericTreeSwingTreeNodeWrapper.this);
	    }
	};
    }

    @Override
    public String toString() {
	return archiveNode.toString();
    }
}
