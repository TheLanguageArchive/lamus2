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
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.archiving.tree.GenericTreeNode;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class GenericTreeSwingTreeModel implements TreeModel, Serializable {

    private final GenericTreeModelProvider provider;

    /**
     *
     * @param provider provider to use in tree model. Must implement {@link Serializable}.
     */
    public GenericTreeSwingTreeModel(GenericTreeModelProvider provider) {
	this.provider = provider;
    }

    public Object getRoot() {
	return provider.getRoot();
    }

    public Object getChild(Object parent, int index) {
	return provider.getChild((GenericTreeNode) parent, index);
    }

    public int getChildCount(Object parent) {
	return provider.getChildCount((GenericTreeNode) parent);
    }

    public boolean isLeaf(Object node) {
	return provider.isLeaf((GenericTreeNode) node);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
	throw new UnsupportedOperationException("ArchiveNodeTreeModel does not support changing values from the tree");
    }

    public int getIndexOfChild(Object parent, Object child) {
	return provider.getIndexOfChild((GenericTreeNode) parent, (GenericTreeNode) child);
    }

    public void addTreeModelListener(TreeModelListener l) {
	//TOOD: Wrap in GenericTreeModelListener
	//provider.addTreeModelListener(l);
	throw new UnsupportedOperationException();
    }

    public void removeTreeModelListener(TreeModelListener l) {
	//TOOD: Wrap in GenericTreeModelListener 
	//provider.removeTreeModelListener(l);
	throw new UnsupportedOperationException();
    }
}
