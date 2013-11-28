/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.tree;

import java.util.List;
import nl.mpi.archiving.tree.LinkedTreeNode;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Interface that extends both LinkedTreeNode and WorkspaceNode.
 * It is intended to be used as the source for
 * the graphical representation of the workspace tree.
 * @see GenericTreeNode
 * @see WorkspaceNode
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceTreeNode extends LinkedTreeNode, WorkspaceNode {
    
    /**
     * @see LinkedTreeNode#getChild(int)
     */
    @Override
    public WorkspaceTreeNode getChild(int index);
    
    /**
     * @see LinkedTreeNode#getChildCount()
     */
    @Override
    public int getChildCount();
    
    /**
     * @see LinkedTreeNode#getIndexOfChild(nl.mpi.archiving.tree.GenericTreeNode)
     */
    @Override
    public int getIndexOfChild(LinkedTreeNode child); 
    
    /**
     * @see LinkedTreeNode#getParent()
     */
    @Override
    public WorkspaceTreeNode getParent();
    
    /**
     * 
     * @return children of the node
     */
    public List<WorkspaceTreeNode> getChildren();
}
