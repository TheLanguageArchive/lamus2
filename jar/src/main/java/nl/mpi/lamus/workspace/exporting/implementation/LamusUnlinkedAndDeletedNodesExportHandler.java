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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.util.Collection;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.UnlinkedAndDeletedNodesExportHandler;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceExportPhase;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class that handles the process of exporting all the unlinked and deleted
 * nodes in a workspace.
 * @see UnlinkedAndDeletedNodesExportHandler
 * @author guisil
 */
@Component
public class LamusUnlinkedAndDeletedNodesExportHandler implements UnlinkedAndDeletedNodesExportHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusUnlinkedAndDeletedNodesExportHandler.class);

    private WorkspaceDao workspaceDao;
    private NodeExporterFactory nodeExporterFactory;
    
    @Autowired
    public LamusUnlinkedAndDeletedNodesExportHandler(WorkspaceDao wsDao, NodeExporterFactory neFactory) {
        this.workspaceDao = wsDao;
        this.nodeExporterFactory = neFactory;
    }
    
    /**
     * @see UnlinkedAndDeletedNodesExportHandler#exploreUnlinkedAndDeletedNodes(
     *          nl.mpi.lamus.workspace.model.Workspace, boolean,
     *          nl.mpi.lamus.workspace.model.WorkspaceSubmissionType, nl.mpi.lamus.workspace.model.WorkspaceExportPhase)
     */
    @Override
    public void exploreUnlinkedAndDeletedNodes(
        Workspace workspace, boolean keepUnlinkedFiles,
        WorkspaceSubmissionType submissionType, WorkspaceExportPhase exportPhase)
            throws WorkspaceExportException {
        
        if(WorkspaceExportPhase.TREE_EXPORT.equals(exportPhase)) {
            String errorMessage = "This stage of the export (handling unlinked and deleted nodes) should not be called during the tree export (current 'exportPhase': " + exportPhase.toString();
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        logger.debug("Exploring unlinked and deleted nodes for export; workspaceID: " + workspace.getWorkspaceID() +
                "; submission mode: " + submissionType.name() + "; export phase: " + exportPhase.toString());
        
        Collection<WorkspaceNode> nodesToExport;
        
        if(WorkspaceSubmissionType.SUBMIT_WORKSPACE.equals(submissionType)) {
            nodesToExport = workspaceDao.getUnlinkedAndDeletedTopNodes(workspace.getWorkspaceID());
        } else if(WorkspaceSubmissionType.DELETE_WORKSPACE.equals(submissionType)) {
            nodesToExport = workspaceDao.getUnlinkedNodes(workspace.getWorkspaceID());
        } else {
            throw new IllegalArgumentException("Unknown submission type");
        }
        
        for(WorkspaceNode unlinkedOrDeletedNode : nodesToExport) {
            NodeExporter nodeExporter = nodeExporterFactory.getNodeExporterForNode(workspace, unlinkedOrDeletedNode, exportPhase);
            nodeExporter.exportNode(workspace, null, unlinkedOrDeletedNode, keepUnlinkedFiles, submissionType, exportPhase);
        }
    }
}
