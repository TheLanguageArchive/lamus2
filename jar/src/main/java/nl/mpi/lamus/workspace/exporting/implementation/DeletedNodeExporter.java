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
import nl.mpi.lamus.workspace.exporting.CorpusStructureBridge;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.exporting.TrashCanHandler;
import nl.mpi.lamus.workspace.exporting.TrashVersioningHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Class responsible for exporting nodes that were deleted
 * and should be moved to the trash can in the filesystem and have
 * their record in the database updated accordingly.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class DeletedNodeExporter implements NodeExporter {

    private final TrashVersioningHandler trashVersioningHandler;
    private final TrashCanHandler trashCanHandler;
    private final CorpusStructureBridge corpusStructureBridge;
    private final SearchClientBridge searchClientBridge;
    
    private Workspace workspace;
    
    public DeletedNodeExporter(TrashVersioningHandler tvHandler, TrashCanHandler tcHandler, CorpusStructureBridge csBridge, SearchClientBridge sClientBridge) {
        
        this.trashVersioningHandler = tvHandler;
        this.trashCanHandler = tcHandler;
        this.corpusStructureBridge = csBridge;
        this.searchClientBridge = sClientBridge;
    }
    
    @Override
    public Workspace getWorkspace() {
        return this.workspace;
    }
    
    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public void exportNode(WorkspaceNode parentNode, WorkspaceNode currentNode) {
        
        if(!trashVersioningHandler.retireNodeVersion(currentNode)) {
            //TODO log error / throw exception
        }
        
        URL trashedNodeArchiveURL = trashCanHandler.moveFileToTrashCan(currentNode);
        
        //TODO check if URL is good
        
        if(!corpusStructureBridge.updateArchiveObjectsNodeURL(currentNode.getArchiveNodeID(), currentNode.getArchiveURL(), trashedNodeArchiveURL)) {
            //TODO log error / throw exception
        }
        
        searchClientBridge.removeNode(currentNode.getArchiveNodeID());
        
        //TODO REMOVE LINK FROM PARENT (IF THERE IS ONE)
    }
    
}
