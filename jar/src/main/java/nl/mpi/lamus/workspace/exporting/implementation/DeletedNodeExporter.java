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

import java.net.URL;
import nl.mpi.lamus.workspace.exporting.ArchiveObjectsBridge;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.exporting.TrashCanHandler;
import nl.mpi.lamus.workspace.exporting.TrashVersioningHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class DeletedNodeExporter implements NodeExporter {

    private final TrashVersioningHandler trashVersioningHandler;
    private final TrashCanHandler trashCanHandler;
    private final ArchiveObjectsBridge archiveObjectsBridge;
    private final SearchClientBridge searchClientBridge;
    
    private Workspace workspace = null;
    
    public DeletedNodeExporter(TrashVersioningHandler tvHandler, TrashCanHandler tcHandler, ArchiveObjectsBridge aoBridge, SearchClientBridge sClientBridge) {
        
        this.trashVersioningHandler = tvHandler;
        this.trashCanHandler = tcHandler;
        this.archiveObjectsBridge = aoBridge;
        this.searchClientBridge = sClientBridge;
    }
    
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void exportNode(WorkspaceNode node) {
        
        if(!trashVersioningHandler.retireNodeVersion(node)) {
            //TODO log error / throw exception
        }
        
        URL trashedNodeArchiveURL = trashCanHandler.moveFileToTrashCan(node);
        
        //TODO check if URL is good
        
        if(!archiveObjectsBridge.updateArchiveObjectsNodeURL(node.getArchiveNodeID(), node.getArchiveURL(), trashedNodeArchiveURL)) {
            //TODO log error / throw exception
        }
        
        searchClientBridge.removeNode(node.getArchiveNodeID());
    }
    
}
