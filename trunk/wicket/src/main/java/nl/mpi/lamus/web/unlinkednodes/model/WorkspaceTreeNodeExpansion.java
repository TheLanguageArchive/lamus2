/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web.unlinkednodes.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

/**
 *
 * @author guisil
 */
public class WorkspaceTreeNodeExpansion implements Set<WorkspaceTreeNode>, Serializable {

    private static final long serialVersionUID = 1L;
    
    private static MetaDataKey<WorkspaceTreeNodeExpansion> KEY = new MetaDataKey<WorkspaceTreeNodeExpansion>()
    {
        private static final long serialVersionUID = 1L;
    };
    
    private Set<Integer> wsNodeIDs = new HashSet<Integer>();

    private boolean inverse;
    
    public void expandAll()
    {
        wsNodeIDs.clear();

        inverse = true;
    }

    public void collapseAll()
    {
        wsNodeIDs.clear();

        inverse = false;
    }
    
    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean contains(Object o) {
        WorkspaceTreeNode node = (WorkspaceTreeNode) o;

        if (inverse)
        {
            return !wsNodeIDs.contains(node.getWorkspaceNodeID());
        }
        else
        {
            return wsNodeIDs.contains(node.getWorkspaceNodeID());
        }
    }

    @Override
    public Iterator<WorkspaceTreeNode> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean add(WorkspaceTreeNode e) {
        if (inverse)
        {
            return wsNodeIDs.remove(e.getWorkspaceNodeID());
        }
        else
        {
            return wsNodeIDs.add(e.getWorkspaceNodeID());
        }
    }

    @Override
    public boolean remove(Object o) {
        WorkspaceTreeNode node = (WorkspaceTreeNode) o;

        if (inverse)
        {
            return wsNodeIDs.add(node.getWorkspaceNodeID());
        }
        else
        {
            return wsNodeIDs.remove(node.getWorkspaceNodeID());
        }
    }

    @Override
    public boolean containsAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(Collection<? extends WorkspaceTreeNode> clctn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean retainAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Get the expansion for the session.
     * 
     * @return expansion
     */
    public static WorkspaceTreeNodeExpansion get()
    {
        WorkspaceTreeNodeExpansion expansion = Session.get().getMetaData(KEY);
        if (expansion == null)
        {
            expansion = new WorkspaceTreeNodeExpansion();

            Session.get().setMetaData(KEY, expansion);
        }
        return expansion;
    }
}
