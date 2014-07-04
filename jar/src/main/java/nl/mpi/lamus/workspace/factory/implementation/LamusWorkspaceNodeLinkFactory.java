/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.factory.implementation;

import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceNodeLinkFactory
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceNodeLinkFactory implements WorkspaceNodeLinkFactory {

    /**
     * @see WorkspaceNodeLinkFactory#getNewWorkspaceNodeLink(int, int)
     */
    @Override
    public WorkspaceNodeLink getNewWorkspaceNodeLink(int parentWorkspaceNodeID, int childWorkspaceNodeID) {
        return new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
    }
    
}
