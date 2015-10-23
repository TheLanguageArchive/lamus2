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
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;

/**
 * Interface for the handler of unlinked and deleted nodes export.
 * @author guisil
 */
public interface UnlinkedAndDeletedNodesExportHandler {
    
    /**
     * Triggers the export of all the unlinked and deleted nodes in the workspace
     * @param workspace Workspace to explore
     * @param keepUnlinkedFiles true if unlinked files (orphans) are to be kept for future use
     * @param submissionType indicates whether the method is being executed
     * during a workspace submission or deletion
     * @param exportPhase indicates whether the workspace export is currently in
     * the first stage, in which the tree is exported, or in the second stage,
     * in which the unlinked nodes are exported
     */
    public void exploreUnlinkedAndDeletedNodes(
        Workspace workspace, boolean keepUnlinkedFiles,
        WorkspaceSubmissionType submissionType, WorkspaceExportPhase exportPhase)
            throws WorkspaceExportException;
}
