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
package nl.mpi.lamus.workspace.replace.action.implementation;

import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
import org.springframework.stereotype.Component;

/**
 * @see ReplaceActionFactory
 * 
 * @author guisil
 */
@Component
public class LamusReplaceActionFactory implements ReplaceActionFactory {

    /**
     * @see ReplaceActionFactory#getDeleteAction(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public DeleteNodeReplaceAction getDeleteAction(WorkspaceNode affectedNode) {
        return new DeleteNodeReplaceAction(affectedNode);
    }

    /**
     * @see ReplaceActionFactory#getLinkAction(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public LinkNodeReplaceAction getLinkAction(WorkspaceNode affectedNode, WorkspaceNode parentNode) {
        return new LinkNodeReplaceAction(affectedNode, parentNode);
    }

    /**
     * @see ReplaceActionFactory#getMoveLinkLocationAction(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public MoveLinkLocationNodeReplaceAction getMoveLinkLocationAction(WorkspaceNode affectedNode, WorkspaceNode parentNode) {
        return new MoveLinkLocationNodeReplaceAction(affectedNode, parentNode);
    }

    /**
     * @see ReplaceActionFactory#getReplaceAction(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, boolean)
     */
    @Override
    public ReplaceNodeReplaceAction getReplaceAction(WorkspaceNode affectedNode, WorkspaceNode affectedNodeParent, WorkspaceNode newNode, boolean newNodeAlreadyLinked) {
        return new ReplaceNodeReplaceAction(affectedNode, affectedNodeParent, newNode, newNodeAlreadyLinked);
    }

    /**
     * @see ReplaceActionFactory#getUnlinkAction(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public UnlinkNodeReplaceAction getUnlinkAction(WorkspaceNode affectedNode, WorkspaceNode parentNode) {
        return new UnlinkNodeReplaceAction(affectedNode, parentNode);
    }
    
}
