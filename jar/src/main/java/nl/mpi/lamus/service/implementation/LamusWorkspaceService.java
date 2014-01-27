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
package nl.mpi.lamus.service.implementation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.management.WorkspaceNodeManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see WorkspaceService
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceService implements WorkspaceService {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceService.class);

    private final WorkspaceAccessChecker nodeAccessChecker;
    private final WorkspaceManager workspaceManager;
    protected final WorkspaceDao workspaceDao;
    private final WorkspaceUploader workspaceUploader;
    private final WorkspaceNodeLinkManager workspaceNodeLinkManager;
    private final WorkspaceNodeManager workspaceNodeManager;

    public LamusWorkspaceService(WorkspaceAccessChecker aChecker, WorkspaceManager wsManager,
            WorkspaceDao wsDao, WorkspaceUploader wsUploader,
            WorkspaceNodeLinkManager wsnLinkManager, WorkspaceNodeManager wsNodeManager) {
        this.nodeAccessChecker = aChecker;
        this.workspaceManager = wsManager;
        this.workspaceDao = wsDao;
        this.workspaceUploader = wsUploader;
        this.workspaceNodeLinkManager = wsnLinkManager;
        this.workspaceNodeManager = wsNodeManager;
    }
    
    
    /**
     * @see WorkspaceService#createWorkspace(java.lang.String, java.net.URI)
     */
    @Override
    public Workspace createWorkspace(String userID, URI archiveNodeURI)
            throws UnknownNodeException, NodeAccessException, WorkspaceImportException {

        this.nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
        
        //TODO what about the browser session? does it make sense to check for a workspace in the session? disconnect it?
        //TODO thread for timeout checking? - WorkspaceTimeoutChecker/WorkspaceDates...
        
        return this.workspaceManager.createWorkspace(userID, archiveNodeURI);
    }
    
    /**
     * @see WorkspaceService#deleteWorkspace(java.lang.String, int)
     */
    @Override
    public void deleteWorkspace(String userID, int workspaceID)
            throws WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, workspaceID);
        
        this.workspaceManager.deleteWorkspace(workspaceID);
    }

    /**
     * @see WorkspaceService#submitWorkspace(String, int)
     */
    @Override
    public void submitWorkspace(String userID, int workspaceID/*, boolean keepUnlinkedFiles*/)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceExportException {

        //TODO requests in this session?
        //TODO workspace should be initialised / connected
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, workspaceID);
        
        this.workspaceManager.submitWorkspace(workspaceID/*, keepUnlinkedFiles*/);
    }

    /**
     * @see WorkspaceService#getWorkspace(int)
     */
    @Override
    public Workspace getWorkspace(int workspaceID)
            throws WorkspaceNotFoundException {
        
        return this.workspaceDao.getWorkspace(workspaceID);
    }
    
    /**
     * @see WorkspaceService#listUserWorkspaces(java.lang.String)
     */
    @Override
    public Collection<Workspace> listUserWorkspaces(String userID) {
        
        return this.workspaceDao.listWorkspacesForUser(userID);
    }

    /**
     * @see WorkspaceService#openWorkspace(java.lang.String, int)
     */
    @Override
    public Workspace openWorkspace(String userID, int workspaceID)
            throws WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, workspaceID);
        
        return this.workspaceManager.openWorkspace(workspaceID);
    }

    /**
     * @see WorkspaceService#getNode(int)
     */
    @Override
    public WorkspaceNode getNode(int nodeID)
            throws WorkspaceNodeNotFoundException {
        
        return this.workspaceDao.getWorkspaceNode(nodeID);
    }

    /**
     * @see WorkspaceService#getChildNodes(int)
     */
    @Override
    public Collection<WorkspaceNode> getChildNodes(int nodeID) {
        
        return this.workspaceDao.getChildWorkspaceNodes(nodeID);
    }

    /**
     * @see WorkspaceService#addNode(java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void addNode(String userID, WorkspaceNode node) throws WorkspaceNotFoundException, WorkspaceAccessException {
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, node.getWorkspaceID());
        
        this.workspaceDao.addWorkspaceNode(node);
    }
    
    /**
     * @see WorkspaceService#linkNodes(java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void linkNodes(String userID, WorkspaceNode parentNode, WorkspaceNode childNode)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, parentNode.getWorkspaceID());
        
        this.workspaceNodeLinkManager.linkNodes(parentNode, childNode);
    }
    
    /**
     * @see WorkspaceService#unlinkNodes(java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void unlinkNodes(String userID, WorkspaceNode parentNode, WorkspaceNode childNode)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, parentNode.getWorkspaceID());
        
        this.workspaceNodeLinkManager.unlinkNodes(parentNode, childNode);
    }
    
    /**
     * @see WorkspaceService#deleteNode(java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void deleteNode(String userID, WorkspaceNode node)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, node.getWorkspaceID());
        
        this.workspaceNodeManager.deleteNodesRecursively(node);
    }

    /**
     * @see WorkspaceService#uploadFilesIntoWorkspace(java.lang.String, int, java.util.Collection)
     */
//    @Override
//    public void uploadFilesIntoWorkspace(String userID, int workspaceID, Collection<FileItem> fileItems) {
//        
//        if(!this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, workspaceID)) {
//            
//            //TODO Inform the user of the reason why the files can't be uploaded
//            //TODO Throw an exception instead?
//            logger.error("Cannot upload files to workspace with ID " + workspaceID);
//        } else {
//        
//            this.workspaceUploader.uploadFiles(workspaceID, fileItems);
//        }
//    }

    /**
     * @see WorkspaceService#getWorkspaceUploadDirectory(int)
     */
    @Override
    public File getWorkspaceUploadDirectory(int workspaceID) {
        return this.workspaceUploader.getWorkspaceUploadDirectory(workspaceID);
    }

    /**
     * @see WorkspaceService#uploadFileIntoWorkspace(java.lang.String, int, java.io.InputStream, java.lang.String)
     */
    @Override
    public void uploadFileIntoWorkspace(String userID, int workspaceID, InputStream inputStream, String filename)
            throws IOException, TypeCheckerException, WorkspaceException {
            
        this.workspaceUploader.uploadFileIntoWorkspace(workspaceID, inputStream, filename);
    }
    
    /**
     * @see WorkspaceService#processUploadedFiles(java.lang.String, int, java.util.Collection)
     */
    @Override
    public Map<File, String> processUploadedFiles(String userID, int workspaceID, Collection<File> uploadedFiles)
            throws IOException, WorkspaceException {
        return this.workspaceUploader.processUploadedFiles(workspaceID, uploadedFiles);
    }
    
    /**
     * @see WorkspaceService#listUnlinkedNodes(java.lang.String, int)
     */
    @Override
    public List<WorkspaceNode> listUnlinkedNodes(String userID, int workspaceID) {
        
        return this.workspaceDao.listUnlinkedNodes(workspaceID);
    }
    
}
