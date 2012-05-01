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

import java.net.URL;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceNodeFactory implements WorkspaceNodeFactory {

    public WorkspaceNode getNewWorkspaceNode(int workspaceID, int archiveNodeID, URL archiveNodeURL) {
        
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeID, archiveNodeURL, archiveNodeURL);
        //TODO add other values as well

        return node;
    }
    
}
