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
import nl.mpi.lamus.configuration.Configuration;
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
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class FileImporterFactoryBean implements FactoryBean<FileImporter> {

    @Autowired
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
    private Configuration configuration;
    @Autowired
    private ArchiveFileHelper archiveFileHelper;
    @Autowired
    private FileTypeHandlerFactory fileTypeHandlerFactory;
    
    private Class<? extends FileImporter> fileImporterType; 
    
    @Override
    public FileImporter getObject() throws Exception {
        if(ResourceFileImporter.class.equals(fileImporterType)) {
            return new ResourceFileImporter(
                    archiveObjectsDB, workspaceDao, configuration, archiveFileHelper,
                    fileTypeHandlerFactory, workspaceNodeFactory,
                    workspaceParentNodeReferenceFactory, workspaceNodeLinkFactory);
        } else {
            return new MetadataFileImporter(
                    archiveObjectsDB, workspaceDao, metadataApi, workspaceNodeFactory,
                    workspaceParentNodeReferenceFactory, workspaceNodeLinkFactory, workspaceFileHandler, workspaceFileExplorer);
        }
    }

    @Override
    public Class<? extends FileImporter> getObjectType() {
        return fileImporterType;
    }
    
    public void setFileImporterTypeForReference(Reference reference) {
        fileImporterType = MetadataFileImporter.class;
        if(reference instanceof ResourceReference) {
            fileImporterType = ResourceFileImporter.class;
        }
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
    
}
