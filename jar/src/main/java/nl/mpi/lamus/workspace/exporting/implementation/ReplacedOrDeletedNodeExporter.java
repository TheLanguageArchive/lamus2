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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.transform.TransformerException;
import net.handle.hdllib.HandleException;
import nl.mpi.handle.util.HandleManager;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.archive.ArchiveHandleHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.VersioningHandler;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceExportPhase;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;
import nl.mpi.metadata.api.MetadataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class responsible for exporting nodes that were replaced
 * (should be moved to the versionining folder) or deleted
 * (should be moved to the trash can) and have
 * their record in the database updated accordingly.
 * @see NodeExporter
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class ReplacedOrDeletedNodeExporter implements NodeExporter {

    private final static Logger logger = LoggerFactory.getLogger(ReplacedOrDeletedNodeExporter.class);

    @Autowired
    private VersioningHandler versioningHandler;
    @Autowired
    private WorkspaceDao workspaceDao;
    @Autowired
    private HandleManager handleManager;
    @Autowired
    private ArchiveFileLocationProvider archiveFileLocationProvider;
    @Autowired
    private WorkspaceTreeExporter workspaceTreeExporter;
    @Autowired
    private MetadataApiBridge metadataApiBridge;
    @Autowired
    private ArchiveHandleHelper archiveHandleHelper;
    @Autowired
    private NodeUtil nodeUtil;
    

    /**
     * @see NodeExporter#exportNode(
     *          nl.mpi.lamus.workspace.model.Workspace, nl.mpi.lamus.workspace.model.WorkspaceNode,
     *          nl.mpi.lamus.workspace.model.WorkspaceNode, boolean,
     *          nl.mpi.lamus.workspace.model.WorkspaceSubmissionType, nl.mpi.lamus.workspace.model.WorkspaceExportPhase)
     */
    @Override
    public void exportNode(
        Workspace workspace, WorkspaceNode parentNode, WorkspaceNode currentNode,
        boolean keepUnlinkedFiles,
        WorkspaceSubmissionType submissionType, WorkspaceExportPhase exportPhase)
            throws WorkspaceExportException {

        if (workspace == null) {
	    String errorMessage = "Workspace not set";
	    logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
	}
        
        if(WorkspaceNodeStatus.REPLACED.equals(currentNode.getStatus()) &&
                WorkspaceSubmissionType.DELETE_WORKSPACE.equals(submissionType)) {
            String errorMessage = "This exporter (for nodes with status " + currentNode.getStatus().name() + ") should only be used when submitting the workspace, not when deleting";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        if(WorkspaceNodeStatus.REPLACED.equals(currentNode.getStatus()) &&
                WorkspaceExportPhase.UNLINKED_NODES_EXPORT.equals(exportPhase)) {
            String errorMessage = "This exporter (for nodes with status " + currentNode.getStatus().name() + ") should only be used when exporting the tree, not for unlinked nodes";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        if(WorkspaceNodeStatus.DELETED.equals(currentNode.getStatus()) &&
                WorkspaceSubmissionType.DELETE_WORKSPACE.equals(submissionType)) {
            String errorMessage = "This exporter (for nodes with status " + currentNode.getStatus().name() + ") should only be used when submitting the workspace, not when deleting";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        if(WorkspaceNodeStatus.DELETED.equals(currentNode.getStatus()) &&
                WorkspaceExportPhase.TREE_EXPORT.equals(exportPhase)) {
            String errorMessage = "This exporter (for nodes with status " + currentNode.getStatus().name() + ") should only be used when exporting unlinked nodes, not for the tree";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        int workspaceID = workspace.getWorkspaceID();
        
        logger.debug("Exporting deleted or replaced node to archive; workspaceID: " + workspaceID + "; currentNodeID: " + currentNode.getWorkspaceNodeID());
        
        if(currentNode.isExternal()) { // Nothing to do here. If an external node was removed, just let it go when the workspace is also removed.
            
            logger.debug("Node " + currentNode.getWorkspaceNodeID() + "is external; will be skipped and eventually deleted with the workspace");
            return;
        }
        
        if(currentNode.getArchiveURI() == null) {
            
            logger.debug("Node " + currentNode.getWorkspaceNodeID() + " was not in the workspace previously; will be skipped and eventually deleted with the workspace folder");
            // if there is no archiveURL, the node was never in the archive, so it can actually be deleted;
            // to make it easier, that node can simply be skipped and eventually will be deleted together with the whole workspace folder
            return;
        }
        
        if(currentNode.isProtected()) { // a protected node should remain intact after the workspace submission
            logger.info("Node " + currentNode.getWorkspaceNodeID() + " is protected; skipping export of this node to keep it intact in the archive");
            return;
        }
        
        if(nodeUtil.isNodeMetadata(currentNode) && WorkspaceNodeStatus.REPLACED.equals(currentNode.getStatus())) {
            workspaceTreeExporter.explore(workspace, currentNode, keepUnlinkedFiles, submissionType, exportPhase);
        }

        moveNodeToAppropriateLocationInArchive(currentNode);
        
        //TODO is this necessary?
//        searchClientBridge.removeNode(currentNode.getArchiveURI());
        
        updateHandleLocation(workspaceID, currentNode);
        
    }
    
    
    private void moveNodeToAppropriateLocationInArchive(WorkspaceNode currentNode) {
        
        URL targetArchiveURL = null;
        if(WorkspaceNodeStatus.DELETED.equals(currentNode.getStatus())) {
            targetArchiveURL = this.versioningHandler.moveFileToTrashCanFolder(currentNode);
        } else if(WorkspaceNodeStatus.REPLACED.equals(currentNode.getStatus())) {
            targetArchiveURL = this.versioningHandler.moveFileToVersioningFolder(currentNode);
        } else {
            throw new IllegalStateException("This exporter only supports deleted or replaced nodes. Current node status: " + currentNode.getStatusAsString());
        }
        currentNode.setArchiveURL(targetArchiveURL);
        
        if(targetArchiveURL == null) {
            //TODO send some sort of notification at this point
                //the fact that the file didn't make it to the trash or versioning archive shouldn't cause the export to stop
            //TODO have exceptions thrown instead of returning null from the move method?
            
            logger.warn("Problem moving file to its target location");
        }
    }
    
    private void updateHandleLocation(int workspaceID, WorkspaceNode currentNode) throws WorkspaceExportException {
        
        if(WorkspaceNodeStatus.DELETED.equals(currentNode.getStatus())) {
            try {
                archiveHandleHelper.deleteArchiveHandle(currentNode, currentNode.getArchiveURL());
            } catch (HandleException | IOException | TransformerException | MetadataException ex) {
                logger.warn("There was a problem while deleting the handle for node " + currentNode.getArchiveURL());
            }
        } else if(WorkspaceNodeStatus.REPLACED.equals(currentNode.getStatus())) {
            URI newTargetUri = null;
            try {
                 newTargetUri = archiveFileLocationProvider.getUriWithHttpsRoot(currentNode.getArchiveURL().toURI());
            } catch (URISyntaxException ex) {
                String errorMessage = "Error getting new target URI for node " + currentNode.getArchiveURL();
                throwWorkspaceExportException(workspaceID, errorMessage, ex);
            }
            try {
                handleManager.updateHandle(new File(currentNode.getArchiveURL().getPath()),
                        URI.create(currentNode.getArchiveURI().getSchemeSpecificPart()), newTargetUri);
                
                //TODO Should these exceptions cause the export to stop? Maybe a notification would be enough...
                
            } catch (HandleException | IOException ex) {
                String errorMessage = "Error updating handle for node " + currentNode.getArchiveURL();
                throwWorkspaceExportException(workspaceID, errorMessage, ex);
            }
            
        }
    }
    
    private void throwWorkspaceExportException(int workspaceID, String errorMessage, Exception cause) throws WorkspaceExportException {
        logger.error(errorMessage, cause);
        throw new WorkspaceExportException(errorMessage, workspaceID, cause);
    }
}
