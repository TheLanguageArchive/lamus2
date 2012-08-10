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
import nl.mpi.lamus.typechecking.FileTypeHandlerFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.FileImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceFileExplorer;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ResourceReference;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * FactoryBean for the file importers.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class FileImporterFactoryBean implements FactoryBean<FileImporter> {

    @Autowired
    @Qualifier("ArchiveObjectsDB")
    private ArchiveObjectsDB archiveObjectsDB;
    @Autowired
    private WorkspaceDao workspaceDao;
    @Autowired
    private MetadataAPI metadataApi;
    @Autowired
    private WorkspaceNodeFactory workspaceNodeFactory;
    @Autowired
    private WorkspaceParentNodeReferenceFactory workspaceParentNodeReferenceFactory;
    @Autowired
    private WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    @Autowired
    private WorkspaceFileHandler workspaceFileHandler;
    @Autowired
    private WorkspaceFileExplorer workspaceFileExplorer;
    @Autowired
    private ArchiveFileHelper archiveFileHelper;
    @Autowired
    private FileTypeHandlerFactory fileTypeHandlerFactory;
    
    private Class<? extends FileImporter> fileImporterType; 
    
    /**
     * Returns the right file importer for the current defined type.
     * 
     * @return Instance of the file importer
     * @throws Exception in case of creation errors
     */
    @Override
    public FileImporter getObject() throws Exception {
        if(ResourceFileImporter.class.equals(fileImporterType)) {
            return new ResourceFileImporter(
                    archiveObjectsDB, workspaceDao, archiveFileHelper,
                    fileTypeHandlerFactory, workspaceNodeFactory,
                    workspaceParentNodeReferenceFactory, workspaceNodeLinkFactory);
        } else {
            return new MetadataFileImporter(
                    archiveObjectsDB, workspaceDao, metadataApi, workspaceNodeFactory,
                    workspaceParentNodeReferenceFactory, workspaceNodeLinkFactory, workspaceFileHandler, workspaceFileExplorer);
        }
    }

    /**
     * Returns the type of object created by the FactoryBean
     * @return type of the file importer
     */
    @Override
    public Class<? extends FileImporter> getObjectType() {
        return fileImporterType;
    }
    
    /**
     * Setter for the file importer type,
     * according to the given metadata reference.
     * 
     * @param reference reference included in the parent metadata document
     */
    public void setFileImporterTypeForReference(Reference reference) {
        fileImporterType = MetadataFileImporter.class;
        if(reference instanceof ResourceReference) {
            fileImporterType = ResourceFileImporter.class;
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
