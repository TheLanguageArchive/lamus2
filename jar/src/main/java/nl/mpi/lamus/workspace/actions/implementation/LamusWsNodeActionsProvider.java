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
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.springframework.stereotype.Component;

/**
 *
 * @author guisil
 */
@Component
public class LamusWsNodeActionsProvider implements WsNodeActionsProvider {

    private final List<WsTreeNodesAction> resourcesActions;;
    private final List<WsTreeNodesAction> metadataActions;
    private final List<WsTreeNodesAction> externalActions;
    private final List<WsTreeNodesAction> protectedActions;
    
    //TODO actions with a different format?
    private final List<WsTreeNodesAction> multipleNodesActions;
    
    
    public LamusWsNodeActionsProvider() {
        resourcesActions = new ArrayList<>();
        resourcesActions.add(new DeleteNodesAction());
        resourcesActions.add(new UnlinkNodesAction());
        resourcesActions.add(new ReplaceNodesAction());
                
        metadataActions = new ArrayList<>();
        metadataActions.add(new DeleteNodesAction());
        metadataActions.add(new UnlinkNodesAction());
        metadataActions.add(new LinkNodesAction());
        metadataActions.add(new ReplaceNodesAction());
        
        externalActions = new ArrayList<>();
        externalActions.add(new DeleteNodesAction());
        externalActions.add(new UnlinkNodesAction());
        externalActions.add(new ReplaceNodesAction());
        
        protectedActions = new ArrayList<>();
        protectedActions.add(new UnlinkNodesAction());
        
        multipleNodesActions = new ArrayList<>();
    }
    
    @Override
    public List<WsTreeNodesAction> getActions(Collection<WorkspaceTreeNode> nodes) {
        
        if(nodes.isEmpty()) {
            return new ArrayList<>();
        } else if(nodes.size() == 1) {
            WorkspaceTreeNode next = nodes.iterator().next();
            if(next.isProtected()) {
                return this.protectedActions;
            } else if(next.isExternal()) {
                return this.externalActions;
            } else if(!next.isMetadata()) {
                return this.resourcesActions;
            } else {
                return this.metadataActions;
            }
        } else {
        
            //TODO multiple node actions
            return this.multipleNodesActions;
        }
    }
    
}
