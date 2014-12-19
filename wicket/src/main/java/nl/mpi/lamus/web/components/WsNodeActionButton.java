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
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.web.unlinkednodes.model.ClearSelectedUnlinkedNodes;
import nl.mpi.lamus.web.unlinkednodes.model.SelectedUnlinkedNodesWrapper;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.Session;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

/**
 * Based on the NodeActionButton class of the Metadata Browser
 * 
 * @author guisil
 */
public class WsNodeActionButton extends Button {
    
    private final Collection<WorkspaceTreeNode> selectedTreeNodes;
    private Collection<WorkspaceTreeNode> selectedChildNodes;
    private final WsTreeNodesAction action;
    private final WorkspaceService workspaceService;
    
    private final FeedbackPanel feedbackPanel;
    
    public WsNodeActionButton(
            String id, Collection<WorkspaceTreeNode> selectedTreeNodes,
            Collection<WorkspaceTreeNode> selectedChildNodes,
            WsTreeNodesAction action, WorkspaceService wsService, FeedbackPanel feedbackPanel) {
        super(id, new Model<>(action.getName()));
        this.selectedTreeNodes = selectedTreeNodes;
        this.selectedChildNodes = selectedChildNodes;
        this.action = action;
        this.workspaceService = wsService;
        
        this.feedbackPanel = feedbackPanel;
    }

    @Override
    public void onSubmit() {
        
        final String currentUserId = LamusSession.get().getUserId();
        try {
            
            this.action.setSelectedTreeNodes(selectedTreeNodes);
            this.action.setSelectedUnlinkedNodes(selectedChildNodes);
            
            this.action.execute(currentUserId, workspaceService);
            
            //tell the unlinked nodes panel to clear the selected nodes
            send(this, Broadcast.BUBBLE, new ClearSelectedUnlinkedNodes());
            
        } catch(WorkspaceException | IllegalArgumentException | ProtectedNodeException ex) {
            Session.get().error(ex.getMessage());
        }
        
        refreshStuff();
    }
	
    
    public void refreshStuff() {
        
    }
    
    public void refreshSelectedUnlinkedNodes() {
        
    }

    @Override
    public void onEvent(IEvent<?> event) {
        
        if(event.getPayload() instanceof SelectedUnlinkedNodesWrapper) {
            this.selectedChildNodes = ((SelectedUnlinkedNodesWrapper) event.getPayload()).getSelectedUnlinkedNodes();
        }
    }
    
}
