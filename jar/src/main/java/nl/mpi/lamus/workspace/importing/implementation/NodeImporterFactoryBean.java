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
package nl.mpi.lamus.workspace.importing.implementation;

import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceFileImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeExplorer;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeLinker;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ResourceReference;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * FactoryBean for the node importers.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class NodeImporterFactoryBean implements FactoryBean<NodeImporter> {

    @Autowired
    @Qualifier("ArchiveObjectsDB")
    private ArchiveObjectsDB archiveObjectsDB;
    @Autowired
    private WorkspaceDao workspaceDao;
    @Autowired
    private MetadataAPI metadataApi;
    @Autowired
    private NodeDataRetriever nodeDataRetriever;
    @Autowired
    private WorkspaceNodeLinker workspaceNodeLinker;
    @Autowired
    private WorkspaceFileImporter workspaceFileImporter;
    @Autowired
    private WorkspaceNodeFactory workspaceNodeFactory;
    @Autowired
    private WorkspaceParentNodeReferenceFactory workspaceParentNodeReferenceFactory;
    @Autowired
    private WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    @Autowired
    private WorkspaceFileHandler workspaceFileHandler;
    @Autowired
    private WorkspaceNodeExplorer workspaceNodeExplorer;
    @Autowired
    private ArchiveFileHelper archiveFileHelper;
    @Autowired
    private FileTypeHandler fileTypeHandler;
    
    private Class<? extends NodeImporter> nodeImporterType; 
    
    /**
     * Returns the right node importer for the current defined type.
     * 
     * @return Instance of the node importer
     * @throws Exception in case of creation errors
     */
    @Override
    public NodeImporter getObject() throws Exception {
        if(ResourceNodeImporter.class.equals(nodeImporterType)) {
            return new ResourceNodeImporter(archiveObjectsDB, workspaceDao, nodeDataRetriever,
                    archiveFileHelper, fileTypeHandler, workspaceNodeFactory,
                    workspaceParentNodeReferenceFactory, workspaceNodeLinkFactory);
        } else {
            return new MetadataNodeImporter(
                    archiveObjectsDB, workspaceDao, metadataApi, nodeDataRetriever, workspaceNodeLinker, workspaceFileImporter,
                    workspaceNodeFactory, workspaceParentNodeReferenceFactory, workspaceNodeLinkFactory,
                    workspaceFileHandler, workspaceNodeExplorer);
        }
    }

    /**
     * Returns the type of object created by the FactoryBean
     * @return type of the node importer
     */
    @Override
    public Class<? extends NodeImporter> getObjectType() {
        return nodeImporterType;
    }
    
    /**
     * Setter for the node importer type,
     * according to the given metadata reference.
     * 
     * @param reference reference included in the parent metadata document
     */
    public void setNodeImporterTypeForReference(Reference reference) {
        nodeImporterType = MetadataNodeImporter.class;
        if(reference instanceof ResourceReference) {
            nodeImporterType = ResourceNodeImporter.class;
        }
    }

    /**
     * @return true if the created object is a singleton
     */
    @Override
    public boolean isSingleton() {
        return false;
    }
    
}
