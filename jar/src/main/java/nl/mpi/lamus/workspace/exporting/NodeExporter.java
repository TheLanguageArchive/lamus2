/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.exporting;

import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Generic interface for a node exporter.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface NodeExporter {
    
    /**
     * Gets the workspace to which the exporter is associated
     * @return Workspace object
     */
    public Workspace getWorkspace();
    
    /**
     * Sets the workspace to which the exporter is associated
     * @param workspace Workspace object
     */
    public void setWorkspace(Workspace workspace);
    
    /**
     * Exports the given node from the workspace to the archive
     * @param parentNode Parent of the node to export
     * @param currentNode Node to export
     */
    public void exportNode(WorkspaceNode parentNode, WorkspaceNode currentNode);
    
}
