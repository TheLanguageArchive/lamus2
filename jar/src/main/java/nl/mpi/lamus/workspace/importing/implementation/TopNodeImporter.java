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
package nl.mpi.lamus.workspace.importing.implementation;

import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.NodeExplorerException;
import nl.mpi.lamus.workspace.exception.NodeImporterException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.importing.WorkspaceFileImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeExplorer;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeLinker;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.metadata.api.MetadataAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class TopNodeImporter {
    
    private MetadataNodeImporter metadataNodeImporter;
    
    @Autowired
    public TopNodeImporter(@Qualifier("ArchiveObjectsDB") ArchiveObjectsDB aoDB, WorkspaceDao wsDao, MetadataAPI mAPI,
	    NodeDataRetriever nodeDataRetriever, WorkspaceNodeLinker nodeLinker, WorkspaceFileImporter fileImporter,
            WorkspaceNodeFactory nodeFactory, WorkspaceParentNodeReferenceFactory parentNodeReferenceFactory,
	    WorkspaceNodeLinkFactory wsNodelinkFactory, WorkspaceFileHandler fileHandler,
	    WorkspaceNodeExplorer workspaceNodeExplorer) {

	metadataNodeImporter = new MetadataNodeImporter(aoDB, wsDao, mAPI, nodeDataRetriever, nodeLinker, fileImporter, nodeFactory,
                parentNodeReferenceFactory, wsNodelinkFactory, fileHandler, workspaceNodeExplorer);
    }
    
    public void importNode(int workspaceID, int childNodeArchiveID) throws NodeImporterException, NodeExplorerException {
        metadataNodeImporter.importNode(workspaceID, null, null, null, childNodeArchiveID);
    }
    
//    public void setWorkspace(Workspace workspace) {
//        metadataNodeImporter.setWorkspace(workspace);
//    }
}
