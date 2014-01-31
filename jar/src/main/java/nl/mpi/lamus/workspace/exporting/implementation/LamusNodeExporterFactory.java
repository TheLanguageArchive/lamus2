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

import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.exporting.TrashCanHandler;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.metadata.api.MetadataAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see NodeExporterFactory
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusNodeExporterFactory implements NodeExporterFactory {

    @Autowired
    private CorpusStructureProvider corpusStructureProvider;
    
    @Autowired
    private SearchClientBridge searchClientBridge;
    
    @Autowired
    private ArchiveFileLocationProvider archiveFileLocationProvider;
    
    @Autowired
    private WorkspaceFileHandler workspaceFileHandler;
    
    @Autowired
    private MetadataAPI metadataAPI;
    
    @Autowired
    private WorkspaceTreeExporter workspaceTreeExporter;
    
    @Autowired
    private WorkspaceDao workspaceDao;
    
    @Autowired
    private NodeDataRetriever nodeDataRetriever;
    
    @Autowired
    private NodeResolver nodeResolver;
    
    @Autowired
    private TrashCanHandler trashCanHandler;
    
    @Autowired
    private ArchiveFileHelper archiveFileHelper;
    
    private AddedNodeExporter addedNodeExporter;
    private DeletedNodeExporter deletedNodeExporter;
    private GeneralNodeExporter generalNodeExporter;
    private UnlinkedNodeExporter unlinkedNodeExporter;
    
    /**
     * @see NodeExporterFactory#getNodeExporterForNode(nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public NodeExporter getNodeExporterForNode(Workspace workspace, WorkspaceNode node) {

        if(workspace.getTopNodeID() != node.getWorkspaceNodeID()) {
            if(workspaceDao.getParentWorkspaceNodes(node.getWorkspaceNodeID()).isEmpty() &&
                    !WorkspaceNodeStatus.NODE_DELETED.equals(node.getStatus())) { // unlinked node, but not deleted
                return getUnlinkedNodeExporter(workspace);
            }

            if(WorkspaceNodeStatus.NODE_UPLOADED.equals(node.getStatus())) {
                return getAddedNodeExporter(workspace);
            }
            if(WorkspaceNodeStatus.NODE_CREATED.equals(node.getStatus())) {
                return getAddedNodeExporter(workspace);
            }
            if(WorkspaceNodeStatus.NODE_DELETED.equals(node.getStatus())) {
                return getDeletedNodeExporter(workspace);
            }
        }
        
        return getGeneralNodeExporter(workspace); //TODO create other possible exporter types
    }

    private AddedNodeExporter getAddedNodeExporter(Workspace workspace) {
        if(addedNodeExporter == null) {
            addedNodeExporter = new AddedNodeExporter(archiveFileLocationProvider, workspaceFileHandler,
                    metadataAPI, workspaceDao, searchClientBridge, workspaceTreeExporter,
                    nodeDataRetriever, corpusStructureProvider, nodeResolver);
        }
        addedNodeExporter.setWorkspace(workspace);
        return addedNodeExporter;
    }

    private NodeExporter getDeletedNodeExporter(Workspace workspace) {
        if(deletedNodeExporter == null) {
            deletedNodeExporter = new DeletedNodeExporter(trashCanHandler, searchClientBridge);
        }
        deletedNodeExporter.setWorkspace(workspace);
        return deletedNodeExporter;
    }
    
    private NodeExporter getGeneralNodeExporter(Workspace workspace) {
        if(generalNodeExporter == null) {
            generalNodeExporter = new GeneralNodeExporter(metadataAPI, workspaceFileHandler,
                    workspaceTreeExporter, corpusStructureProvider, archiveFileHelper);
        }
        generalNodeExporter.setWorkspace(workspace);
        return generalNodeExporter;
    }
    
    private NodeExporter getUnlinkedNodeExporter(Workspace workspace) {
        if(unlinkedNodeExporter == null) {
            unlinkedNodeExporter = new UnlinkedNodeExporter(trashCanHandler, searchClientBridge);
        }
        unlinkedNodeExporter.setWorkspace(workspace);
        return unlinkedNodeExporter;
    }
}
