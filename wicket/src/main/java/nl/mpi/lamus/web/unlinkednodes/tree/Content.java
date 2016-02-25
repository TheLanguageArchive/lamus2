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
package nl.mpi.lamus.web.unlinkednodes.tree;

import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

/**
 * Content to be used as base in the unlinked nodes tree.
 * 
 * Based on the example class from Wicket
 * @author Sven Meier
 */
public abstract class Content implements IDetachable {

    
    /**
     * Create new content.
     */
    public abstract Component newContentComponent(String id, AbstractTree<WorkspaceTreeNode> tree,
            IModel<WorkspaceTreeNode> model);
    
    
    /**
     * @see IDetachable#detach()
     */
    @Override
    public void detach() {
        
    }
    
}
