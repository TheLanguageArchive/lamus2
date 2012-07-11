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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.event.TreeModelListener;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArchiveNodeTreeModelProviderImpl implements ArchiveNodeTreeModelProvider {
    
    private final ArchiveNode rootNode;
    private final Set<TreeModelListener> treeModelListeners;
    
    public ArchiveNodeTreeModelProviderImpl(ArchiveNode rootNode) {
	this.rootNode = rootNode;
	this.treeModelListeners = new CopyOnWriteArraySet<TreeModelListener>();
    }
    
    public ArchiveNode getRoot() {
	return rootNode;
    }
    
    public ArchiveNode getChild(ArchiveNode parent, int index) {
	return parent.getChild(index);
    }
    
    public int getChildCount(ArchiveNode parent) {
	return parent.getChildCount();
    }
    
    public boolean isLeaf(ArchiveNode node) {
	return node.getChildCount() == 0;
    }
    
    public int getIndexOfChild(ArchiveNode parent, ArchiveNode child) {
	return parent.getIndexOfChild(child);
    }
    
    public void addTreeModelListener(TreeModelListener l) {
	treeModelListeners.add(l);
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
	treeModelListeners.remove(l);
    }
}
