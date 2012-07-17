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
import nl.mpi.archiving.tree.ArchiveNode;
import nl.mpi.archiving.tree.ArchiveNodeTreeModelProvider;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArchiveNodeTreeModel implements TreeModel, Serializable {

    private final ArchiveNodeTreeModelProvider provider;

    /**
     *
     * @param provider provider to use in tree model. Must implement {@link Serializable}.
     */
    public ArchiveNodeTreeModel(ArchiveNodeTreeModelProvider provider) {
	this.provider = provider;
    }

    public Object getRoot() {
	return provider.getRoot();
    }

    public Object getChild(Object parent, int index) {
	return provider.getChild((ArchiveNode) parent, index);
    }

    public int getChildCount(Object parent) {
	return provider.getChildCount((ArchiveNode) parent);
    }

    public boolean isLeaf(Object node) {
	return provider.isLeaf((ArchiveNode) node);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
	throw new UnsupportedOperationException("ArchiveNodeTreeModel does not support changing values from the tree");
    }

    public int getIndexOfChild(Object parent, Object child) {
	return provider.getIndexOfChild((ArchiveNode) parent, (ArchiveNode) child);
    }

    public void addTreeModelListener(TreeModelListener l) {
	provider.addTreeModelListener(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
	provider.removeTreeModelListener(l);
    }
}
