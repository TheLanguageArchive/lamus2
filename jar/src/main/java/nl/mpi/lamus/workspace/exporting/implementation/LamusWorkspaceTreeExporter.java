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
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceTreeExporter
 * @author guisil
 */
@Component
public class LamusWorkspaceTreeExporter implements WorkspaceTreeExporter {

    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceTreeExporter.class);
    
    private WorkspaceDao workspaceDao;
    private NodeExporterFactory nodeExporterFactory;
    
    @Autowired
    public LamusWorkspaceTreeExporter(WorkspaceDao wsDao, NodeExporterFactory exporterFactory) {
        this.workspaceDao = wsDao;
        this.nodeExporterFactory = exporterFactory;
    }
    
    /**
     * @see WorkspaceTreeExporter#explore(nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void explore(Workspace workspace, WorkspaceNode node)
            throws WorkspaceExportException {
        
        logger.debug("Exploring references in metadata node to export; workspaceID: " + workspace.getWorkspaceID() + "; nodeID: " + node.getWorkspaceNodeID());
        
        Collection<WorkspaceNode> children = workspaceDao.getChildWorkspaceNodes(node.getWorkspaceNodeID());
        
        for(WorkspaceNode child : children) {
            
            if(!child.isExternal()) {
                NodeExporter childNodeExporter = nodeExporterFactory.getNodeExporterForNode(workspace, child);
                childNodeExporter.exportNode(node, child);
            }
        }
    }
    
}
