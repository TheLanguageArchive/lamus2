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
package nl.mpi.lamus.web.unlinkednodes.providers;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import nl.mpi.archiving.tree.GenericTreeModelListener;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.unlinkednodes.model.UnlinkedWorkspaceNode;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

/**
 * Model provider for the unlinked nodes tree.
 * @author guisil
 */
public class UnlinkedNodesModelProvider implements MultiRootTreeModelProvider<WorkspaceTreeNode> {

    private UnlinkedWorkspaceNode unlinkedRootWsNode;
    
    
    private final Set<GenericTreeModelListener> treeModelListeners;
    
    private final WorkspaceTreeService workspaceService;
    
    private final int workspaceID;
    
    
    public UnlinkedNodesModelProvider(WorkspaceTreeService wsService, int wsID) {
        this.workspaceService = wsService;
        this.treeModelListeners = new CopyOnWriteArraySet<>();
        this.workspaceID = wsID;
    }
    
    
    @Override
    public WorkspaceTreeNode getChild(WorkspaceTreeNode parent, int index) {
        return parent.getChild(index);
    }

    @Override
    public int getChildCount(WorkspaceTreeNode parent) {
        return parent.getChildCount();
    }

    @Override
    public boolean isLeaf(WorkspaceTreeNode node) {
        return node.getChildCount() == 0;
    }

    @Override
    public int getIndexOfChild(WorkspaceTreeNode parent, WorkspaceTreeNode child) {
        return parent.getIndexOfChild(child);
    }

    @Override
    public void addTreeModelListener(GenericTreeModelListener l) {
        this.treeModelListeners.add(l);
    }

    @Override
    public void removeTreeModelListener(GenericTreeModelListener l) {
        this.treeModelListeners.remove(l);
    }

    @Override
    public void onDetach() {
        // Do no thing by default, can be overridden
    }
    

    @Override
    public Iterator<? extends WorkspaceTreeNode> getRoots() {
        
        return this.workspaceService.listUnlinkedTreeNodes("", workspaceID).iterator();
    }

    @Override
    public boolean hasChildren(WorkspaceTreeNode t) {
        return !t.getChildren().isEmpty();
    }

    @Override
    public Iterator<? extends WorkspaceTreeNode> getChildren(WorkspaceTreeNode t) {
        return t.getChildren().iterator();
    }

    @Override
    public IModel<WorkspaceTreeNode> model(WorkspaceTreeNode t) {
        return new CompoundPropertyModel<>(t);
    }

    @Override
    public void detach() {
        // Do no thing by default, can be overridden
    }

    @Override
    public ISortState<String> getSortState() {
        
        return new SingleSortState<>();
    }


}
