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
package nl.mpi.lamus.workspace.exporting;

import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceExportPhase;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;

/**
 * Class used to explore the workspace tree during the export process.
 * 
 * @author guisil
 */
public interface WorkspaceTreeExporter {
    
    /**
     * Explores the given node, invoking the correct exporter class
     * for each of the children.
     * 
     * @param workspace Workspace being exported
     * @param node Node to be explored
     * @param corpusNamePathToClosestTopNode Path of corpus names up to the closest top node
     * @param keepUnlinkedFiles true if unlinked files are to be kept for future use
     * @param submissionType indicates whether the method is being executed
     * during a workspace submission or deletion
     * @param exportPhase indicates whether the workspace export is currently in
     * the first stage, in which the tree is exported, or in the second stage,
     * in which the unlinked nodes are exported
     */
    public void explore(
            Workspace workspace, WorkspaceNode node,
            String corpusNamePathToClosestTopNode, boolean keepUnlinkedFiles,
            WorkspaceSubmissionType submissionType, WorkspaceExportPhase exportPhase)
            throws WorkspaceExportException;
    
}
