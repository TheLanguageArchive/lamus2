/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.TransformerException;
import net.handle.hdllib.HandleException;
import nl.mpi.lamus.archive.ArchiveHandleHelper;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.VersioningHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author guisil
 */
@Component
public class UnlinkedNodeExporter implements NodeExporter{
    
    private static final Logger logger = LoggerFactory.getLogger(UnlinkedNodeExporter.class);

    @Autowired
    private VersioningHandler versioningHandler;
    @Autowired
    private ArchiveHandleHelper archiveHandleHelper;
    

    /**
     * @see NodeExporter#exportNode(nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode, boolean)
     */
    @Override
    public void exportNode(Workspace workspace, WorkspaceNode parentNode, WorkspaceNode currentNode, boolean keepUnlinkedFiles) throws WorkspaceExportException {

        //TODO FOR NOW SIMILAR TO DeletedNodeExporter, but will have to be changed
            // when new functionality is added regarding unlinked nodes
        
        
        if (workspace == null) {
	    String errorMessage = "Workspace not set";
	    logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
	}
        
        logger.debug("Exporting unlinked node to archive; workspaceID: " + workspace.getWorkspaceID() + "; currentNodeID: " + currentNode.getWorkspaceNodeID());
        
        if(currentNode.isProtected()) { // a protected node should remain intact after the workspace submission
            logger.info("Node " + currentNode.getWorkspaceNodeID() + " is protected; skipping export of this node to keep it intact in the archive");
            return;
        }
        
        
        URI currentArchiveUri = currentNode.getArchiveURI();
        
        if(keepUnlinkedFiles) {
            
            versioningHandler.moveFileToOrphansFolder(workspace, currentNode);
            
            if(currentArchiveUri != null) {
                try {
                    archiveHandleHelper.deleteArchiveHandle(currentNode, Boolean.FALSE);
                } catch (HandleException | IOException | TransformerException | MetadataException ex) {
                    logger.warn("There was a problem while deleting the handle for node " + currentNode.getArchiveURL());
                }
            }
        } else {
            
            if(currentArchiveUri != null) {
                
                URL trashedNodeArchiveURL = this.versioningHandler.moveFileToTrashCanFolder(currentNode);
                currentNode.setArchiveURL(trashedNodeArchiveURL);
            }
            
            logger.debug("Node " + currentNode.getWorkspaceNodeID() + " was not in the archive previously; will be skipped and eventually deleted with the workspace folder");
            // if there is no archiveURI, the node was never in the archive, so it can actually be deleted;
            // to make it easier, that node can simply be skipped and eventually will be deleted together with the whole workspace folder
        }
        
        //TODO is this necessary?
//        searchClientBridge.removeNode(currentNode.getArchiveURI());
        
        //TODO REMOVE LINK FROM PARENT (IF THERE IS ONE)
    }
}
