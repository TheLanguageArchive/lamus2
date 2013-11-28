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
package nl.mpi.lamus.workspace.actions.implementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.mpi.lamus.workspace.actions.WsNodeActionsProvider;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.springframework.stereotype.Component;

/**
 *
 * @author guisil
 */
@Component
public class LamusWsNodeActionsProvider implements WsNodeActionsProvider {

    private final List<WsTreeNodesAction> writtenResourcesActions;
    private final List<WsTreeNodesAction> mediaResourcesActions;
    private final List<WsTreeNodesAction> lexicalResourcesActions;
    
    private final List<WsTreeNodesAction> metadataActions;
    
    //TODO would this have different actions??
    private final List<WsTreeNodesAction> metadataCollectionActions;
    
    //TODO actions with a different format?
    private final List<WsTreeNodesAction> multipleNodesActions;
    
    
    public LamusWsNodeActionsProvider() {
        writtenResourcesActions = new ArrayList<WsTreeNodesAction>();
        writtenResourcesActions.add(new DeleteNodesAction());
        writtenResourcesActions.add(new UnlinkNodesAction());
        
        mediaResourcesActions = new ArrayList<WsTreeNodesAction>();
        mediaResourcesActions.add(new DeleteNodesAction());
        mediaResourcesActions.add(new UnlinkNodesAction());
        
        lexicalResourcesActions = new ArrayList<WsTreeNodesAction>();
        lexicalResourcesActions.add(new DeleteNodesAction());
        lexicalResourcesActions.add(new UnlinkNodesAction());
        
        metadataActions = new ArrayList<WsTreeNodesAction>();
        metadataActions.add(new DeleteNodesAction());
        metadataActions.add(new UnlinkNodesAction());
        
        
        
        metadataCollectionActions = new ArrayList<WsTreeNodesAction>();
        
        multipleNodesActions = new ArrayList<WsTreeNodesAction>();
    }
    
    @Override
    public List<WsTreeNodesAction> getActions(Collection<WorkspaceTreeNode> nodes) {
        
        if(nodes.isEmpty()) {
            return new ArrayList<WsTreeNodesAction>();
        } else if(nodes.size() == 1) {
            for(WorkspaceTreeNode currentNode : nodes) {
                
                WorkspaceNodeType currentNodeType = currentNode.getType();
                
                if(WorkspaceNodeType.RESOURCE_WR.equals(currentNodeType)) {
                    return this.writtenResourcesActions;
                }
                if(WorkspaceNodeType.RESOURCE_MR.equals(currentNodeType)) {
                    return this.mediaResourcesActions;
                }
                if(WorkspaceNodeType.RESOURCE_LEX.equals(currentNodeType)) {
                    return this.lexicalResourcesActions;
                }
        
                
                if(WorkspaceNodeType.METADATA.equals(currentNodeType)) {
                    return this.metadataActions;
                }
                
                
                //TODO OTHER TYPES...
                
                throw new UnsupportedOperationException("not implemented yet");
            }
        }
        
        //TODO multiple node actions
        throw new UnsupportedOperationException("not implemented yet");
    }
    
}
