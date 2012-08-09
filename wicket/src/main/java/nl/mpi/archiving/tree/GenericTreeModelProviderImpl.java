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
package nl.mpi.archiving.tree;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class GenericTreeModelProviderImpl implements GenericTreeModelProvider, Serializable {

    private final GenericTreeNode rootNode;
    private final Set<GenericTreeModelListener> treeModelListeners;

    /**
     *
     * @param rootNode root node of tree model. Must implement {@link Serializable}.
     */
    public GenericTreeModelProviderImpl(GenericTreeNode rootNode) {
	this.rootNode = rootNode;
	this.treeModelListeners = new CopyOnWriteArraySet<GenericTreeModelListener>();
    }

    public GenericTreeNode getRoot() {
	return rootNode;
    }

    public GenericTreeNode getChild(GenericTreeNode parent, int index) {
	return parent.getChild(index);
    }

    public int getChildCount(GenericTreeNode parent) {
	return parent.getChildCount();
    }

    public boolean isLeaf(GenericTreeNode node) {
	return node.getChildCount() == 0;
    }

    public int getIndexOfChild(GenericTreeNode parent, GenericTreeNode child) {
	return parent.getIndexOfChild(child);
    }

    @Override
    public void addTreeModelListener(GenericTreeModelListener l) {
	treeModelListeners.add(l);
    }

    @Override
    public void removeTreeModelListener(GenericTreeModelListener l) {
	treeModelListeners.remove(l);
    }
}
