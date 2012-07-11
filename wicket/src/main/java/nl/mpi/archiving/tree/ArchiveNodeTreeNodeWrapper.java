/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.archiving.tree;

import java.io.Serializable;
import java.util.Enumeration;
import javax.swing.tree.TreeNode;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArchiveNodeTreeNodeWrapper implements TreeNode, Serializable {

    private final ArchiveNode archiveNode;

    public ArchiveNodeTreeNodeWrapper(ArchiveNode archiveNode) {
	this.archiveNode = archiveNode;
    }

    public ArchiveNode getArchiveNode() {
	return archiveNode;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
	return new ArchiveNodeTreeNodeWrapper(archiveNode.getChild(childIndex));
    }

    @Override
    public int getChildCount() {
	return archiveNode.getChildCount();
    }

    @Override
    public TreeNode getParent() {
	final ArchiveNode parent = archiveNode.getParent();
	if (parent == null) {
	    return null;
	} else {
	    return new ArchiveNodeTreeNodeWrapper(parent);
	}
    }

    @Override
    public int getIndex(TreeNode node) {
	return archiveNode.getIndexOfChild(((ArchiveNodeTreeNodeWrapper) node).getArchiveNode());
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
		return new ArchiveNodeTreeNodeWrapper(archiveNode.getChild(index++));
	    }
	};
    }

    @Override
    public String toString() {
	return archiveNode.toString();
    }
}
