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

import nl.mpi.lamus.workspace.exporting.ArchiveObjectsBridge;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.exporting.TrashCanHandler;
import nl.mpi.lamus.workspace.exporting.TrashVersioningHandler;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusNodeExporterFactory implements NodeExporterFactory {

    private TrashVersioningHandler trashVersioningHandler;
    private TrashCanHandler trashCanHandler;
    private ArchiveObjectsBridge archiveObjectsBridge;
    private SearchClientBridge searchClientBridge;
    
    private AddedNodeExporter addedNodeExporter;
    private DeletedNodeExporter deletedNodeExporter;
    
    @Autowired
    public LamusNodeExporterFactory(TrashVersioningHandler tvHandler, TrashCanHandler tcHandler,
            ArchiveObjectsBridge aoBridge, SearchClientBridge scBridge) {
        this.trashVersioningHandler = tvHandler;
        this.trashCanHandler = tcHandler;
        this.archiveObjectsBridge = aoBridge;
        this.searchClientBridge = scBridge;
    }
    
    public NodeExporter getNodeExporterForNode(WorkspaceNode node) {
        
        if(WorkspaceNodeStatus.NODE_UPLOADED.equals(node.getStatus())) {
            return getAddedNodeExporter();
        }
        if(WorkspaceNodeStatus.NODE_CREATED.equals(node.getStatus())) {
            return getAddedNodeExporter();
        }
        if(WorkspaceNodeStatus.NODE_DELETED.equals(node.getStatus())) {
            return getDeletedNodeExporter();
        }
        
        return null; //TODO create other possible exporter types
    }

    private AddedNodeExporter getAddedNodeExporter() {
        if(addedNodeExporter == null) {
            addedNodeExporter = new AddedNodeExporter();
        }
        return addedNodeExporter;
    }

    private NodeExporter getDeletedNodeExporter() {
        if(deletedNodeExporter == null) {
            deletedNodeExporter = new DeletedNodeExporter(trashVersioningHandler, trashCanHandler, archiveObjectsBridge, searchClientBridge);
        }
        return deletedNodeExporter;
    }
    
}
