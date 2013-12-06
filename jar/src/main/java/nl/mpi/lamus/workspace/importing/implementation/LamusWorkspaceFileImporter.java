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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.importing.WorkspaceFileImporter;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceFileImporter
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceFileImporter implements WorkspaceFileImporter {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceFileImporter.class);
    
    private final WorkspaceFileHandler workspaceFileHandler;
    private final WorkspaceDao workspaceDao;
    private final MetadataAPI metadataAPI;
    
    @Autowired
    public LamusWorkspaceFileImporter(WorkspaceFileHandler wFileHandler, WorkspaceDao wDao, MetadataAPI mAPI) {
        
        this.workspaceFileHandler = wFileHandler;
        this.workspaceDao = wDao;
        this.metadataAPI = mAPI;
    }

    /**
     * @see WorkspaceFileImporter#importMetadataFileToWorkspace(java.net.URL, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.metadata.api.model.MetadataDocument)
     */
    @Override
    public void importMetadataFileToWorkspace(URL archiveNodeURL, WorkspaceNode workspaceNode, MetadataDocument metadataDocument)
            throws MalformedURLException, IOException, TransformerException, MetadataException {
        
	File nodeFile = workspaceFileHandler.getFileForImportedWorkspaceNode(archiveNodeURL, workspaceNode);
	StreamResult streamResult = workspaceFileHandler.getStreamResultForNodeFile(nodeFile);

        metadataAPI.writeMetadataDocument(metadataDocument, streamResult);
        
        workspaceNode.setWorkspaceURL(nodeFile.toURI().toURL());
        this.workspaceDao.updateNodeWorkspaceURL(workspaceNode);
    }
}
