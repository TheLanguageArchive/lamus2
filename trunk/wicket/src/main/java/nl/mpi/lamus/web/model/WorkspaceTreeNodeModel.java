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
package nl.mpi.lamus.web.model;

import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.Session;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author guisil
 */
public class WorkspaceTreeNodeModel extends LoadableDetachableModel<WorkspaceTreeNode> {

    // Services to be injected
    @SpringBean
    private WorkspaceTreeService workspaceTreeService;
    // Workspace identifier
    private final Integer workspaceNodeId;
    private final WorkspaceTreeNode parentNode;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public WorkspaceTreeNodeModel(WorkspaceTreeNode node, WorkspaceTreeNode nodeParent) {
        super(node);
        if (node == null) {
            this.workspaceNodeId = null;
            this.parentNode = null;
        } else {
            this.workspaceNodeId = node.getWorkspaceNodeID();
            this.parentNode = nodeParent;
        }
        // Get workspaceService injected
        Injector.get().inject(this);
    }
    
    @SuppressWarnings("LeakingThisInConstructor")
    public WorkspaceTreeNodeModel(int nodeId, WorkspaceTreeNode nodeParent) {
        super();
        this.workspaceNodeId = nodeId;
        this.parentNode = nodeParent;
        
        // Get workspaceService injected
        Injector.get().inject(this);
    }
    
    @Override
    protected WorkspaceTreeNode load() {
        if (this.workspaceNodeId == null) {
            return null;
        } else {
            try {
                return this.workspaceTreeService.getTreeNode(this.workspaceNodeId, parentNode);
            } catch (WorkspaceNodeNotFoundException ex) {
                Session.get().error(ex.getMessage());
                return null;
            }
        }
    }
    
}
