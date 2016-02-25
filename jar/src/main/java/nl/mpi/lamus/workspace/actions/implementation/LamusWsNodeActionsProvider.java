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
import java.util.Collections;
import java.util.List;
import nl.mpi.lamus.cmdi.profile.AllowedCmdiProfiles;
import nl.mpi.lamus.cmdi.profile.CmdiProfile;
import nl.mpi.lamus.workspace.actions.WsNodeActionsProvider;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WsNodeActionsProvider
 * @author guisil
 */
@Component
public class LamusWsNodeActionsProvider implements WsNodeActionsProvider {

    private final NodeUtil nodeUtil;
    private final AllowedCmdiProfiles allowedCmdiProfiles;
    
    private final List<WsTreeNodesAction> resourcesActions;;
    private final List<WsTreeNodesAction> metadataActions;
    private final List<WsTreeNodesAction> latMetadataActions;
    private final List<WsTreeNodesAction> externalActions;
    private final List<WsTreeNodesAction> protectedActions;
    private final List<WsTreeNodesAction> topNodeActions;
    private final List<WsTreeNodesAction> multipleNodesActions;
    
    private final List<WsTreeNodesAction> emptyActions;
    
    
    @Autowired
    public LamusWsNodeActionsProvider(NodeUtil nodeUtil, AllowedCmdiProfiles profiles) {
        
        this.nodeUtil = nodeUtil;
        this.allowedCmdiProfiles = profiles;
        
        resourcesActions = new ArrayList<>();
        resourcesActions.add(new DeleteNodesAction());
        resourcesActions.add(new UnlinkNodesAction());
        resourcesActions.add(new ReplaceNodesAction());
                
        metadataActions = new ArrayList<>();
        metadataActions.add(new DeleteNodesAction());
        metadataActions.add(new UnlinkNodesAction());
        metadataActions.add(new LinkNodesAction());
        metadataActions.add(new ReplaceNodesAction());
        
        latMetadataActions = new ArrayList<>();
        latMetadataActions.add(new DeleteNodesAction());
        latMetadataActions.add(new UnlinkNodesAction());
        latMetadataActions.add(new LinkNodesAction());
        latMetadataActions.add(new LinkNodesAsInfoAction());
        latMetadataActions.add(new ReplaceNodesAction());
        
        externalActions = new ArrayList<>();
        externalActions.add(new DeleteNodesAction());
        externalActions.add(new UnlinkNodesAction());
        externalActions.add(new ReplaceNodesAction());
        
        protectedActions = new ArrayList<>();
        protectedActions.add(new UnlinkNodesAction());
        
        topNodeActions = new ArrayList<>();
        topNodeActions.add(new LinkNodesAction());
        topNodeActions.add(new ReplaceNodesAction());
        
        multipleNodesActions = new ArrayList<>();
        multipleNodesActions.add(new DeleteNodesAction());
        multipleNodesActions.add(new UnlinkNodesAction());
        
        emptyActions = new ArrayList<>();
    }
    
    /**
     * @see WsNodeActionsProvider#getActions(java.util.Collection)
     */
    @Override
    public List<WsTreeNodesAction> getActions(Collection<WorkspaceTreeNode> nodes) {
        
        if(nodes.isEmpty()) {
            return new ArrayList<>();
        } else if(nodes.size() == 1) {
            WorkspaceTreeNode next = nodes.iterator().next();
            
            if(next.isTopNodeOfWorkspace()) {
                return Collections.unmodifiableList(topNodeActions);
            } else if(next.isProtected()) {
                return Collections.unmodifiableList(protectedActions);
            } else if(next.isExternal()) {
                return Collections.unmodifiableList(externalActions);
            } else if(!nodeUtil.isNodeMetadata(next)) {
                return Collections.unmodifiableList(resourcesActions);
            } else {
                CmdiProfile nextProfile = allowedCmdiProfiles.getProfile(next.getProfileSchemaURI().toString());
                if(nodeUtil.isProfileLatCorpusOrSession(nextProfile)) {
                    return Collections.unmodifiableList(latMetadataActions);
                }
                return Collections.unmodifiableList(metadataActions);
            }
        } else {
            
            for(WorkspaceTreeNode node : nodes) {
                if(node.isTopNodeOfWorkspace()) {
                    return Collections.unmodifiableList(emptyActions);
                }
            }
            
            return Collections.unmodifiableList(multipleNodesActions);
        }
    }
    
}
