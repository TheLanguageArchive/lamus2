/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web.test.helpers;

import java.io.Serializable;
import java.util.Collection;
import nl.mpi.archiving.tree.GenericTreeModelProvider;
import nl.mpi.archiving.tree.GenericTreeNode;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreeNodeIconProvider;
import nl.mpi.archiving.tree.wicket.components.ArchiveTreePanel;

/**
 *
 * @author guisil
 */
public class ArchiveTreePanelForTesting<T extends GenericTreeNode & Serializable> extends ArchiveTreePanel {
    
    public ArchiveTreePanelForTesting(String id, GenericTreeModelProvider provider) {
        super(id, provider);
    }
    
    public ArchiveTreePanelForTesting(String id, GenericTreeModelProvider provider, ArchiveTreeNodeIconProvider<T> iconProvider, boolean multipleSelectionAllowed) {
        super(id, provider, iconProvider, multipleSelectionAllowed);
    }
    
    public void selectNode(T node) {
        getTree().getTreeState().selectNode(node, true);
    }
}
