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
 * Class responsible for exporting nodes that were newly added
 * and are supposed to get a new location in the archive.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class AddedNodeExporter implements NodeExporter {

    public void setWorkspace(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void exportNode(WorkspaceNode node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        
        // node
        // virtpaths
        // subnodes
        // keep directory structure?
        
        
        // for each subnode
        
            // metadata?
                // get virt path...?
            // else if deleted or unknown type
                // ...

            // external?
                // ...

            // keepdirstruct && fromarchive && ! wasunlinked ? (i.e. already existed in the archive and - do not rename already archived files / directories...' (allow resource basename rename, though))

            // metadata?
                // recursion
            // else
                // change name (and url) if old and new name are different and, if so
                    // get resource file for url
                    // move file to new location
                    // setWsDbArchiveUrl...
                    // wsdb setnodenametitle...
                    // continue loop
        
            // corpus or catalogue?
                // not in archive before?
                    // calculate url based on parent, if possible
                    // setWsDbArchiveUrl...
                // else
                    // ...
            // else session?
                // not in archive before and protocol is file (?)
                    // oldsessionfile (?)
                    // get next session name available
                    // if ?
                        // if ?
                        // eventually move file and updata wsdb...
                
    }
}
