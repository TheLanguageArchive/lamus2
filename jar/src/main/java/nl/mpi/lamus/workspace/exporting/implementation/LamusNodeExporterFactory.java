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
package nl.mpi.lamus.workspace.exporting.implementation;

import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceExportPhase;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see NodeExporterFactory
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusNodeExporterFactory implements NodeExporterFactory {

    @Autowired
    private WorkspaceDao workspaceDao;
    
    @Autowired
    private AddedNodeExporter addedNodeExporter;
    @Autowired
    private ReplacedOrDeletedNodeExporter replacedOrDeletedNodeExporter;
    @Autowired
    private GeneralNodeExporter generalNodeExporter;
    @Autowired
    private UnlinkedNodeExporter unlinkedNodeExporter;
    
    /**
     * @see NodeExporterFactory#getNodeExporterForNode(nl.mpi.lamus.workspace.model.Workspace,
     *          nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceExportPhase)
     */
    @Override
    public NodeExporter getNodeExporterForNode(Workspace workspace,
        WorkspaceNode node, WorkspaceExportPhase exportPhase) {

        if(workspace.getTopNodeID() != node.getWorkspaceNodeID()) {
            
            boolean hasParents = !workspaceDao.getParentWorkspaceNodes(node.getWorkspaceNodeID()).isEmpty();
            
            if(WorkspaceExportPhase.UNLINKED_NODES_EXPORT.equals(exportPhase) &&
                    !WorkspaceNodeStatus.DELETED.equals(node.getStatus()) &&
                    !WorkspaceNodeStatus.EXTERNAL_DELETED.equals(node.getStatus()) &&
                    !WorkspaceNodeStatus.REPLACED.equals(node.getStatus())) {
                return unlinkedNodeExporter;
            }

            if(WorkspaceNodeStatus.UPLOADED.equals(node.getStatus())) {
                return addedNodeExporter;
            }
            if(WorkspaceNodeStatus.CREATED.equals(node.getStatus())) {
                return addedNodeExporter;
            }
            if(WorkspaceNodeStatus.DELETED.equals(node.getStatus()) ||
                    WorkspaceNodeStatus.EXTERNAL_DELETED.equals(node.getStatus()) ||
                    WorkspaceNodeStatus.REPLACED.equals(node.getStatus())) {
                return replacedOrDeletedNodeExporter;
            }
        }
        
        return generalNodeExporter;
    }
}
