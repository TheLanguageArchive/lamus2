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
import java.util.zip.ZipInputStream;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.lamus.archive.ArchiveHandleHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.CrawlerInvocationException;
import nl.mpi.lamus.exception.DisallowedPathException;
import nl.mpi.lamus.exception.MetadataValidationException;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.exception.PreLockedNodeException;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.exporting.WorkspaceCorpusStructureExporter;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.management.WorkspaceNodeManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.implementation.LamusNodeReplaceManager;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import nl.mpi.lamus.workspace.importing.implementation.ImportProblem;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.upload.implementation.ZipUploadResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

/**
 * @see WorkspaceService
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceService implements WorkspaceService {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceService.class);

    private final WorkspaceAccessChecker nodeAccessChecker;
    private final ArchiveHandleHelper archiveHandleHelper;
    private final WorkspaceManager workspaceManager;
    protected final WorkspaceDao workspaceDao;
    private final WorkspaceUploader workspaceUploader;
    private final WorkspaceNodeLinkManager workspaceNodeLinkManager;
    private final WorkspaceNodeManager workspaceNodeManager;
    private final LamusNodeReplaceManager nodeReplaceManager;
    private final WorkspaceCorpusStructureExporter workspaceCorpusStructureExporter;

    public LamusWorkspaceService(WorkspaceAccessChecker aChecker, ArchiveHandleHelper aHandleHelper,
            WorkspaceManager wsManager, WorkspaceDao wsDao, WorkspaceUploader wsUploader,
            WorkspaceNodeLinkManager wsnLinkManager, WorkspaceNodeManager wsNodeManager,
            LamusNodeReplaceManager topNodeReplaceManager, WorkspaceCorpusStructureExporter wsCsExporter) {
        this.nodeAccessChecker = aChecker;
        this.archiveHandleHelper = aHandleHelper;
        this.workspaceManager = wsManager;
        this.workspaceDao = wsDao;
        this.workspaceUploader = wsUploader;
        this.workspaceNodeLinkManager = wsnLinkManager;
        this.workspaceNodeManager = wsNodeManager;
        this.nodeReplaceManager = topNodeReplaceManager;
        this.workspaceCorpusStructureExporter = wsCsExporter;
    }
    
    
    /**
     * @see WorkspaceService#createWorkspace(java.lang.String, java.net.URI)
     */
    @Override
    public Workspace createWorkspace(String userID, URI archiveNodeURI)
            throws NodeAccessException, WorkspaceImportException, NodeNotFoundException {
        
        logger.debug("Triggered creation of workspace; userID: " + userID + "; archiveNodeURI: " + archiveNodeURI);
         
        if(userID == null || archiveNodeURI == null) {
            throw new IllegalArgumentException("Both userID and archiveNodeURI should not be null");
        }
        
        URI archiveUriToUse = this.archiveHandleHelper.getArchiveHandleForNode(archiveNodeURI);
        if(archiveUriToUse == null) {
            archiveUriToUse = archiveNodeURI;
        }
        
        try {
            workspaceDao.preLockNode(archiveUriToUse);
            this.nodeAccessChecker.ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            return this.workspaceManager.createWorkspace(userID, archiveUriToUse);
        } catch(DuplicateKeyException ex) {
            throw new PreLockedNodeException("Node " + archiveUriToUse + " already pre-locked", archiveUriToUse);
        } finally {
            workspaceDao.removeNodePreLock(archiveUriToUse);
        }
    }
    
    /**
     * @see WorkspaceService#deleteWorkspace(java.lang.String, int, boolean)
     */
    @Override
    public void deleteWorkspace(String userID, int workspaceID, boolean keepUnlinkedFiles)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceExportException, IOException {
        
        logger.debug("Triggered deletion of workspace; userID: " + userID + "; workspaceID: " + workspaceID);
        
        this.nodeAccessChecker.ensureUserCanDeleteWorkspace(userID, workspaceID);
        
        this.workspaceManager.deleteWorkspace(workspaceID, keepUnlinkedFiles);
    }

    /**
     * @see WorkspaceService#submitWorkspace(java.lang.String, int, boolean)
     */
    @Override
    public void submitWorkspace(String userID, int workspaceID, boolean keepUnlinkedFiles)
            throws WorkspaceNotFoundException, WorkspaceAccessException,
            WorkspaceExportException, MetadataValidationException {
        
        logger.debug("Triggered submission of workspace; userID: " + userID + "; workspaceID: " + workspaceID);
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, workspaceID);
        
        this.workspaceManager.submitWorkspace(workspaceID, keepUnlinkedFiles);
    }

    /**
     * @see WorkspaceService#triggerCrawlForWorkspace(java.lang.String, int)
     */
    @Override
    public void triggerCrawlForWorkspace(String userID, int workspaceID)
            throws WorkspaceNotFoundException, WorkspaceAccessException, CrawlerInvocationException {
        
        logger.debug("Triggered crawl of workspace; userID: " + userID + "; workspaceID: " + workspaceID);
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, workspaceID);
        
        this.workspaceCorpusStructureExporter.triggerWorkspaceCrawl(this.workspaceDao.getWorkspace(workspaceID));
    }

    /**
     * @see WorkspaceService#getWorkspace(int)
     */
    @Override
    public Workspace getWorkspace(int workspaceID)
            throws WorkspaceNotFoundException {
        
        logger.debug("Triggered retrieval of workspace; workspaceID: " + workspaceID);
        
        return this.workspaceDao.getWorkspace(workspaceID);
    }
    
    /**
     * @see WorkspaceService#listUserWorkspaces(java.lang.String)
     */
    @Override
    public Collection<Workspace> listUserWorkspaces(String userID) {
        
        logger.debug("Triggered retrieval of all workspaces for user; userID: " + userID);
        
        return this.workspaceDao.getWorkspacesForUser(userID);
    }

    /**
     * @see WorkspaceService#listAllWorkspaces()
     */
    @Override
    public List<Workspace> listAllWorkspaces() {
        
        logger.debug("Triggered retrieval of all workspaces");
        
        return this.workspaceDao.getAllWorkspaces();
    }
    
    /**
     * @see WorkspaceService#userHasWorkspaces(java.lang.String)
     */
    @Override
    public boolean userHasWorkspaces(String userID) {
        return !listUserWorkspaces(userID).isEmpty();
    }

    /**
     * @see WorkspaceService#openWorkspace(java.lang.String, int)
     */
    @Override
    public Workspace openWorkspace(String userID, int workspaceID)
            throws WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
        logger.debug("Triggered opening of workspace; userID: " + userID + "; workspaceID: " + workspaceID);
        
        if(userID == null) {
            throw new IllegalArgumentException("userID should not be null");
        }
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, workspaceID);
        
        return this.workspaceManager.openWorkspace(workspaceID);
    }

    /**
     * @see WorkspaceService#getNode(int)
     */
    @Override
    public WorkspaceNode getNode(int nodeID)
            throws WorkspaceNodeNotFoundException {
        
        logger.debug("Triggered retrieval of node; nodeID: " + nodeID);
        
        return this.workspaceDao.getWorkspaceNode(nodeID);
    }

    /**
     * @see WorkspaceService#getChildNodes(int)
     */
    @Override
    public Collection<WorkspaceNode> getChildNodes(int nodeID) {
        
        logger.debug("Triggered retrieval of child nodes; nodeID: " + nodeID);
        
        return this.workspaceDao.getChildWorkspaceNodes(nodeID);
    }

    /**
     * @see WorkspaceService#addNode(java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void addNode(String userID, WorkspaceNode node) throws WorkspaceNotFoundException, WorkspaceAccessException {
        
        logger.debug("Triggered node addition; userID: " + userID + "nodeWorkspaceURL: " + node.getWorkspaceURL());
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, node.getWorkspaceID());
        
        this.workspaceDao.addWorkspaceNode(node);
    }
    
    /**
     * @see WorkspaceService#linkNodes(java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void linkNodes(String userID, WorkspaceNode parentNode, WorkspaceNode childNode)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        logger.debug("Triggered node linking; userID: " + userID + "; parentNodeID: " + parentNode.getWorkspaceNodeID() + "; childNodeID: " + childNode.getWorkspaceNodeID());
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, parentNode.getWorkspaceID());
        
        this.workspaceNodeLinkManager.removeArchiveUriFromUploadedNodeRecursively(childNode, true);
        
        this.workspaceNodeLinkManager.linkNodes(parentNode, childNode, WorkspaceNodeType.RESOURCE_INFO.equals(childNode.getType()));
    }
    
    /**
     * @see WorkspaceService#unlinkNodes(java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void unlinkNodes(String userID, WorkspaceNode parentNode, WorkspaceNode childNode)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        logger.debug("Triggered node unlinking; userID: " + userID + "; parentNodeID: " + parentNode.getWorkspaceNodeID() + "; childNodeID: " + childNode.getWorkspaceNodeID());
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, parentNode.getWorkspaceID());
        
        this.workspaceNodeLinkManager.unlinkNodes(parentNode, childNode);
    }
    
    /**
     * @see WorkspaceService#deleteNode(java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void deleteNode(String userID, WorkspaceNode node)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        logger.debug("Triggered node deletion; userID: " + userID + "; nodeID: " + node.getWorkspaceNodeID());
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, node.getWorkspaceID());
        
        this.workspaceNodeManager.deleteNodesRecursively(node);
    }

    /**
     * @see WorkspaceService#replaceTree(java.lang.String, nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void replaceTree(String userID, WorkspaceNode oldTreeTopNode, WorkspaceNode newTreeTopNode, WorkspaceNode parentNode)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        logger.debug("Triggered tree replacement; userID: " + userID + "; oldNodeID: " + oldTreeTopNode.getWorkspaceNodeID() + "; newNodeID: " + newTreeTopNode.getWorkspaceNodeID());
        
        this.nodeAccessChecker.ensureUserHasAccessToWorkspace(userID, oldTreeTopNode.getWorkspaceID());
        
        this.nodeReplaceManager.replaceTree(oldTreeTopNode, newTreeTopNode, parentNode);
    }

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
    public File uploadFileIntoWorkspace(String userID, int workspaceID, InputStream inputStream, String filename)
            throws IOException, DisallowedPathException {
        
        logger.debug("Triggered upload of file into workspace; userID: " + userID + "; workspaceID: " + workspaceID + "; filename: " + filename);
        
        return this.workspaceUploader.uploadFileIntoWorkspace(workspaceID, inputStream, filename);
    }
    
    /**
     * @see WorkspaceService#uploadZipFileIntoWorkspace(java.lang.String, int, java.util.zip.ZipInputStream, java.lang.String)
     */
    @Override
    public ZipUploadResult uploadZipFileIntoWorkspace(String userID, int workspaceID, ZipInputStream zipInputStream, String filename)
            throws IOException, DisallowedPathException {
        
        logger.debug("Triggered upload of zip file into workspace; userID: " + userID + "; workspaceID: " + workspaceID + "; filename: " + filename);
        
        return this.workspaceUploader.uploadZipFileIntoWorkspace(workspaceID, zipInputStream);
    }
    
    /**
     * @see WorkspaceService#processUploadedFiles(java.lang.String, int, java.util.Collection)
     */
    @Override
    public Collection<ImportProblem> processUploadedFiles(String userID, int workspaceID, Collection<File> uploadedFiles)
            throws WorkspaceException {
        
        logger.debug("Triggered processing of uploaded files; userID: " + userID + "; workspaceID: " + workspaceID);
        
        return this.workspaceUploader.processUploadedFiles(workspaceID, uploadedFiles);
    }
    
    /**
     * @see WorkspaceService#getUnlinkedNodes(java.lang.String, int)
     */
    @Override
    public List<WorkspaceNode> listUnlinkedNodes(String userID, int workspaceID) {
        
        logger.debug("Triggered retrieval of unlinked nodes list; userID: " + userID + "; workspaceID: " + workspaceID);
        
        return this.workspaceDao.getUnlinkedNodes(workspaceID);
    }
    
}
