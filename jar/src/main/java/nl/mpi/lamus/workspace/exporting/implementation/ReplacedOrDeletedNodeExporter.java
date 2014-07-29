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
import net.handle.hdllib.HandleException;
import nl.mpi.handle.util.HandleManager;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.VersioningHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for exporting nodes that were replaced
 * (should be moved to the versioining folder) or deleted
 * (should be moved to the trash can) and have
 * their record in the database updated accordingly.
 * @see NodeExporter
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class ReplacedOrDeletedNodeExporter implements NodeExporter {

    private final static Logger logger = LoggerFactory.getLogger(ReplacedOrDeletedNodeExporter.class);
    
    private final VersioningHandler versioningHandler;
    private final WorkspaceDao workspaceDao;
    private final HandleManager handleManager;
    private final ArchiveFileLocationProvider archiveFileLocationProvider;
    
    private Workspace workspace;
    
    public ReplacedOrDeletedNodeExporter(VersioningHandler versioningHandler,
            WorkspaceDao wsDao, HandleManager handleManager,
            ArchiveFileLocationProvider locationProvider) {

        this.versioningHandler = versioningHandler;
        this.workspaceDao = wsDao;
        this.handleManager = handleManager;
        this.archiveFileLocationProvider = locationProvider;
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
        
        logger.debug("Exporting deleted or replaced node to archive; workspaceID: " + workspace.getWorkspaceID() + "; currentNodeID: " + currentNode.getWorkspaceNodeID());
        
        if(currentNode.getArchiveURL() == null) { //Assuming that if archiveURL is null, so is archiveURI
            
            logger.debug("Node " + currentNode.getWorkspaceNodeID() + " was not in the workspace previously; will be skipped and eventually deleted with the workspace folder");
            // if there is no archiveURL, the node was never in the archive, so it can actually be deleted;
            // to make it easier, that node can simply be skipped and eventually will be deleted together with the whole workspace folder
            return;
            
        }

        moveNodeToAppropriateLocationInArchive(currentNode);
        
        
//        if(WorkspaceNodeStatus.NODE_REPLACED.equals(currentNode.getStatus())) {
//        
//            WorkspaceNode newerVersion;
//            try {
//                newerVersion = this.workspaceDao.getNewerVersionOfNode(workspace.getWorkspaceID(), currentNode.getWorkspaceNodeID());
//            } catch (WorkspaceNodeNotFoundException ex) {
//                String errorMessage = "Error getting newer version of node " + currentNode.getWorkspaceNodeID() + " from workspace " + workspace.getWorkspaceID();
//                throwWorkspaceExportException(errorMessage, ex);
//            }    
//        }
        
        
        //TODO TAKE CARE OF NEW NODES THAT WERE REPLACED
        //TODO ALSO NODES REPLACING NODES WHICH HAD REPLACED OTHER NODES AND SO ON...
        // THESE SHOULD BE EVENTUALLY REMOVED FROM THE REPLACEMENTS TABLE SO THAT THEY DON'T GET ADDED TO THE CS DB
        
        //TODO is this necessary?
//        searchClientBridge.removeNode(currentNode.getArchiveURI());
        
        updateHandleLocation(currentNode);
        
    }
    
    
    private void moveNodeToAppropriateLocationInArchive(WorkspaceNode currentNode) {
        
        URL targetArchiveURL = null;
        if(WorkspaceNodeStatus.NODE_DELETED.equals(currentNode.getStatus())) {
            targetArchiveURL = this.versioningHandler.moveFileToTrashCanFolder(currentNode);
        } else if(WorkspaceNodeStatus.NODE_REPLACED.equals(currentNode.getStatus())) {
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
    
    private void updateHandleLocation(WorkspaceNode currentNode) throws WorkspaceExportException {
        
        if(WorkspaceNodeStatus.NODE_DELETED.equals(currentNode.getStatus())) {
            try {
                handleManager.deleteHandle(new URI(currentNode.getArchiveURI().getSchemeSpecificPart()));
                
                //TODO Should these exceptions cause the export to stop? Maybe a notification would be enough...
                
            } catch (HandleException ex) {
                String errorMessage = "Error deleting handle for node " + currentNode.getArchiveURL();
                throwWorkspaceExportException(errorMessage, ex);
            } catch (IOException ex) {
                String errorMessage = "Error deleting handle for node " + currentNode.getArchiveURL();
                throwWorkspaceExportException(errorMessage, ex);
            } catch (URISyntaxException ex) {
                String errorMessage = "Error deleting handle for node " + currentNode.getArchiveURL();
                throwWorkspaceExportException(errorMessage, ex);
            }
            
            currentNode.setArchiveURI(null);
            workspaceDao.updateNodeArchiveUri(currentNode);
            
            if(currentNode.isMetadata()) {
                
                //TODO IF NODES ARE METADATA, ALSO THE SELF LINK HAS TO BE UPDATED
                throw new UnsupportedOperationException("not implemented yet");
            }
            
        } else if(WorkspaceNodeStatus.NODE_REPLACED.equals(currentNode.getStatus())) {
            URI newTargetUri = null;
            try {
                 newTargetUri = archiveFileLocationProvider.getUriWithHttpsRoot(currentNode.getArchiveURL().toURI());
            } catch (URISyntaxException ex) {
                String errorMessage = "Error getting new target URI for node " + currentNode.getArchiveURL();
                throwWorkspaceExportException(errorMessage, ex);
            }
            try {
                handleManager.updateHandle(new File(currentNode.getArchiveURL().getPath()),
                        new URI(currentNode.getArchiveURI().getSchemeSpecificPart()), newTargetUri);
                
                //TODO Should these exceptions cause the export to stop? Maybe a notification would be enough...
                
            } catch (HandleException ex) {
                String errorMessage = "Error updating handle for node " + currentNode.getArchiveURL();
                throwWorkspaceExportException(errorMessage, ex);
            } catch (IOException ex) {
                String errorMessage = "Error updating handle for node " + currentNode.getArchiveURL();
                throwWorkspaceExportException(errorMessage, ex);
            } catch (URISyntaxException ex) {
                String errorMessage = "Error updating handle for node " + currentNode.getArchiveURL();
                throwWorkspaceExportException(errorMessage, ex);
            }
            
        }
    }
    
    private void throwWorkspaceExportException(String errorMessage, Exception cause) throws WorkspaceExportException {
        logger.error(errorMessage, cause);
        throw new WorkspaceExportException(errorMessage, workspace.getWorkspaceID(), cause);
    }
}
