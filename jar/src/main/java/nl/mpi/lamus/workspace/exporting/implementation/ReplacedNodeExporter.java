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

import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for exporting nodes that were replaced.
 * It takes care of the replacement in the database and in the filesystem,
 * as well as versioning (the old node will be kept as a version).
 * @see NodeExporter
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class ReplacedNodeExporter implements NodeExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(ReplacedNodeExporter.class);

    private final SearchClientBridge searchClientBridge;
    
    private Workspace workspace;
    
    
    public ReplacedNodeExporter(SearchClientBridge sClientBridge) {
        searchClientBridge = sClientBridge;
    }
    
    /**
     * @see NodeExporter#getWorkspace()
     */
    @Override
    public Workspace getWorkspace() {
        return this.workspace;
    }
    
    /**
     * @see NodeExporter#setWorkspace(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * @see NodeExporter#exportNode(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void exportNode(WorkspaceNode parentNode, WorkspaceNode currentNode) {
        
        if (workspace == null) {
	    String errorMessage = "Workspace not set";
	    logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
	}
        
        if(currentNode.getArchiveURL() == null) { //Assuming that if archiveURL is null, so is archiveURI
            
            // if there is no archiveURL, the node was never in the archive, so it can actually be deleted;
            // to make it easier, that node can simply be skipped and eventually will be deleted together with the whole workspace folder
            
            
            //TODO DELETE THE NODE...
            
            return;
            
        }
        
        //TODO move file to version location
        //TODO update handle to point to the version location
        //TODO add version information in CS database
        
        
        searchClientBridge.removeNode(currentNode.getArchiveURI());
        
        
        
        
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        
        //TODO Check method SetIngestLocations.trashDeletedFiles, since Replaced nodes are also treated partially as Deleted
        //TODO Check method DataMoverOut.makeVersionLinks
        //TODO Check method SetIngestLocations.setReplacedNodeURLs
    }
    
}
