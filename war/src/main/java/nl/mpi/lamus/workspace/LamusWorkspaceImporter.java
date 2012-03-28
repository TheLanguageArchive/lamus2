/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceImporter implements WorkspaceImporter, Runnable{

    public void importWorkspace(Workspace workspaceToImport) {
        
        //TODO get the information about the top node with the CorpusStructure API
        //TODO start using MetadataAPI to get the document corresponding to the top node
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void run() {
        
        //TODO explore all the references in the metadata files and copy them to the workspace directory / workspace database
        
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
