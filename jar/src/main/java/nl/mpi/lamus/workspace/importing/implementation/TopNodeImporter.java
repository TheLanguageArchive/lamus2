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

import java.net.URI;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceFileImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeExplorer;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.metadata.api.MetadataAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Importer specific for the top node of the workspace.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class TopNodeImporter {
    
    private MetadataNodeImporter metadataNodeImporter;
    
    @Autowired
    public TopNodeImporter(CorpusStructureProvider csProvider, NodeResolver nodeResolver,
            WorkspaceDao wsDao, MetadataAPI mAPI,
	    WorkspaceNodeLinkManager nodeLinkManager, WorkspaceFileImporter fileImporter,
            WorkspaceNodeFactory nodeFactory, WorkspaceNodeExplorer workspaceNodeExplorer) {

	metadataNodeImporter = new MetadataNodeImporter(csProvider, nodeResolver, wsDao, mAPI,
                nodeLinkManager, fileImporter, nodeFactory, workspaceNodeExplorer);
    }
    
    /**
     * Imports the top node into the workspace, by invoking the correct importer
     * with only the parameters that matter in this case.
     * 
     * @param workspace workspace where to import the node
     * @param childNodeArchiveURI archive URI of the current node
     */
    public void importNode(Workspace workspace, URI childNodeArchiveURI) throws WorkspaceImportException {
        workspace.setTopNodeArchiveURI(childNodeArchiveURI);
        metadataNodeImporter.importNode(workspace, null, null, null);
    }
    
}
