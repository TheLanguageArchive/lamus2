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

import java.net.URL;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.exporting.TrashCanHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for exporting nodes that were deleted
 * and should be moved to the trash can in the filesystem and have
 * their record in the database updated accordingly.
 * @see NodeExporter
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class DeletedNodeExporter implements NodeExporter {

    private final static Logger logger = LoggerFactory.getLogger(DeletedNodeExporter.class);
    
    private final TrashCanHandler trashCanHandler;
    private final CorpusStructureProvider corpusStructureProvider;
    private final SearchClientBridge searchClientBridge;
    
    private Workspace workspace;
    
    public DeletedNodeExporter(TrashCanHandler trashCanHandler,
            CorpusStructureProvider csProvider, SearchClientBridge sClientBridge) {

        this.trashCanHandler = trashCanHandler;
        this.corpusStructureProvider = csProvider;
        this.searchClientBridge = sClientBridge;
    }
    
    /**
     * @see NodeExporter#getWorkspace()
     */
    @Override
    public Workspace getWorkspace() {
        return this.workspace;
    }
    
    /**
     * @see NodeExporter#setWorkspace(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * @see NodeExporter#exportNode(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void exportNode(WorkspaceNode parentNode, WorkspaceNode currentNode) throws WorkspaceExportException {

        if (workspace == null) {
	    String errorMessage = "Workspace not set";
	    logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
	}
        
        if(currentNode.getArchiveURL() == null) { //Assuming that if archiveURL is null, so is archiveURI
            
            // if there is no archiveURL, the node was never in the archive, so it can actually be deleted;
            // to make it easier, that node can simply be skipped and eventually will be deleted together with the whole workspace folder
            return;
            
        }
        
        
        //TODO What to do with this URL? Update it and use to inform the crawler of the change?
        
        URL trashedNodeArchiveURL = this.trashCanHandler.moveFileToTrashCan(currentNode);
        currentNode.setArchiveURL(trashedNodeArchiveURL);
        
//        CorpusNode node;
//        try {
//            node = this.corpusStructureProvider.getNode(currentNode.getArchiveURI());
//        } catch (UnknownNodeException ex) {
//            String errorMessage = "Node not found in archive database for URI " + currentNode.getArchiveURI();
//            logger.error(errorMessage, ex);
//            throw new WorkspaceExportException(errorMessage, workspace.getWorkspaceID(), ex);
//        }
        
        //TODO Is this needed? Isn't LAMUS only supposed to change things in the filesystem, leaving the database changes for the crawler?
//        this.corpusstructureWriter.deleteNode(node);
        
        searchClientBridge.removeNode(currentNode.getArchiveURI());
        
        //TODO REMOVE LINK FROM PARENT (IF THERE IS ONE)
    }
    
}
