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
package nl.mpi.lamus.workspace.replace.action;

import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.action.implementation.DeleteNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.LinkNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.MoveLinkLocationNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.RemoveArchiveUriReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.ReplaceNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.UnlinkNodeReplaceAction;

/**
 * Factory for the actions involved in node / tree replacement
 * 
 * @author guisil
 */
public interface ReplaceActionFactory {
   
    /**
     * @param affectedNode node to be deleted
     * @return new "delete node" action
     */
    public DeleteNodeReplaceAction getDeleteAction(WorkspaceNode affectedNode);
    
    /**
     * @param affectedNode child node
     * @param parentNode parent node
     * @return new "link node" action
     */
    public LinkNodeReplaceAction getLinkAction(WorkspaceNode affectedNode, WorkspaceNode parentNode);
    
    /**
     * @param affectedNode child node
     * @param parentNode parent node
     * @return new "move link location" action
     */
    public MoveLinkLocationNodeReplaceAction getMoveLinkLocationAction(WorkspaceNode affectedNode, WorkspaceNode parentNode);
    
    /**
     * @param affectedNode old node
     * @param affectedNodeParent parent node
     * @param newNode new node
     * @param newNodeAlreadyLinked true if the new node is already linked to the parent
     * @return new "replace node" action
     */
    public ReplaceNodeReplaceAction getReplaceAction(WorkspaceNode affectedNode, WorkspaceNode affectedNodeParent, WorkspaceNode newNode, boolean newNodeAlreadyLinked);
    
    /**
     * @param affectedNode child node
     * @param parentNode parent node
     * @return new "unlink node" action
     */
    public UnlinkNodeReplaceAction getUnlinkAction(WorkspaceNode affectedNode, WorkspaceNode parentNode);
    
    /**
     * @param affectedNode node to have its URI removed
     * @param parentNode parent node
     * @return  new "remove archive uri" action
     */
    public RemoveArchiveUriReplaceAction getRemoveArchiveUriAction(WorkspaceNode affectedNode, WorkspaceNode parentNode);
}
