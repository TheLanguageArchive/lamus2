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
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Class responsible for exporting nodes that were replaced.
 * It takes care of the replacement in the database and in the filesystem,
 * as well as versioning (the old node will be kept as a version).
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class ReplacedNodeExporter implements NodeExporter {

    @Override
    public Workspace getWorkspace() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void setWorkspace(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exportNode(WorkspaceNode parentNode, WorkspaceNode currentNode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        
        //TODO Check method SetIngestLocations.trashDeletedFiles, since Replaced nodes are also treated partially as Deleted
        //TODO Check method DataMoverOut.makeVersionLinks
        //TODO Check method SetIngestLocations.setReplacedNodeURLs
    }
    
}
