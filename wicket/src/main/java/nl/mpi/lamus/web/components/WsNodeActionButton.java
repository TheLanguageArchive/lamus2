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
package nl.mpi.lamus.web.components;

import java.util.Collection;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;

/**
 * Based on the NodeActionButton class of the Metadata Browser
 * 
 * @author guisil
 */
public class WsNodeActionButton extends Button {
    
    private final Collection<WorkspaceTreeNode> selectedTreeNodes;
    private final WsTreeNodesAction action;
    private WorkspaceService workspaceService;
    
    public WsNodeActionButton(String id, Collection<WorkspaceTreeNode> selectedTreeNodes, WsTreeNodesAction action, WorkspaceService wsService) {
        super(id, new Model<String>(action.getName()));
        this.selectedTreeNodes = selectedTreeNodes;
        this.action = action;
        this.workspaceService = wsService;
    }
    
    @Override
    public void onSubmit() {
        final String currentUserId = LamusSession.get().getUserId();
        
        this.action.execute(currentUserId, this.selectedTreeNodes, this.workspaceService);
        
        refreshStuff();
    }
	
    
    public void refreshStuff() {
        
    }
}
