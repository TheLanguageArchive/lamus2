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
import nl.mpi.lamus.workspace.model.WorkspaceExportPhase;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Factory for node exporters.
 * 
 * @author guisil
 */
public interface NodeExporterFactory {
    
    /**
     * Returns the NodeExporter of the appropriate type for the given node.
     * 
     * @param workspace current workspace
     * @param node node to be exporter
     * @param exportPhase indicates whether the workspace export is currently in
     * the first stage, in which the tree is exported, or in the second stage,
     * in which the unlinked nodes are exported
     * @return NodeExporter object of the appropriate subtype
     */
    public NodeExporter getNodeExporterForNode(Workspace workspace,
            WorkspaceNode node, WorkspaceExportPhase exportPhase);
    
}
