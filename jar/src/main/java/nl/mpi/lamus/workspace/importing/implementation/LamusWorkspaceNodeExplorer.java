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

import java.util.Collection;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeExplorer;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceNodeExplorer
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceNodeExplorer implements WorkspaceNodeExplorer {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceNodeExplorer.class);
    
    private final WorkspaceDao workspaceDao;
    private final NodeImporterFactoryBean nodeImporterFactoryBean;
    private final ArchiveFileHelper archiveFileHelper;
    
    @Autowired
    public LamusWorkspaceNodeExplorer(WorkspaceDao wsDao,
        NodeImporterFactoryBean nodeImporterFactoryBean, ArchiveFileHelper aFileHelper) {
        this.workspaceDao = wsDao;
        this.nodeImporterFactoryBean = nodeImporterFactoryBean;
        this.archiveFileHelper = aFileHelper;
    }

    /**
     * @see WorkspaceNodeExplorer#explore(
     *          nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode,
     *          nl.mpi.metadata.api.model.ReferencingMetadataDocument, java.util.Collection)
     */
    @Override
    public void explore(Workspace workspace, WorkspaceNode nodeToExplore, ReferencingMetadataDocument nodeDocument, Collection<Reference> linksInNode)
        throws WorkspaceImportException {
        
        logger.debug("Exploring references in metadata node to import; workspaceID: " + workspace.getWorkspaceID() + "; nodeID: " + nodeToExplore.getWorkspaceNodeID());
        
        for(Reference currentLink : linksInNode) {
        
            //TODO check if the file does exist

            //TODO check here if it's already locked or not?
            
            //TODO check if it is Metadata or Resource node
            
            nodeImporterFactoryBean.setNodeImporterTypeForReference(currentLink);
            NodeImporter linkImporterToUse = null;
            try {
                linkImporterToUse = nodeImporterFactoryBean.getObject();
            } catch (Exception ex) {
                String errorMessage = "Error getting file importer";
                throw new WorkspaceImportException(errorMessage, workspace.getWorkspaceID(), ex);
            }
            
            linkImporterToUse.importNode(workspace, nodeToExplore, nodeDocument, currentLink);
        }
    }
    
}
