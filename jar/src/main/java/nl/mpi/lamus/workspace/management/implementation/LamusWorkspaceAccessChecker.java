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
package nl.mpi.lamus.workspace.management.implementation;

import java.net.URI;
import java.util.Collection;
import javax.annotation.Resource;
import nl.mpi.archiving.corpusstructure.adapter.AdapterUtils;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.CorpusNodeType;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.CorpusStructureAccessChecker;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ExternalNodeException;
import nl.mpi.lamus.exception.LockedNodeException;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.exception.UnauthorizedNodeException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Checker for node access. If a user needs access to a certain node in the
 * archive in order to create a workspace, this is the class to use.
 * @see WorkspaceAccessChecker
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceAccessChecker implements WorkspaceAccessChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceAccessChecker.class);    

    private final CorpusStructureProvider corpusStructureProvider;
    private final NodeResolver nodeResolver;
    private final WorkspaceDao workspaceDao;
    private final CorpusStructureAccessChecker corpusStructureAccessChecker;
    
    @Resource(name = "managerUsers")
    private Collection<String> managerUsers;
    
    
    @Autowired
    public LamusWorkspaceAccessChecker(CorpusStructureProvider csProvider, NodeResolver nodeResolver,
            WorkspaceDao workspaceDao, CorpusStructureAccessChecker csAccessChecker) {
        this.corpusStructureProvider = csProvider;
        this.nodeResolver = nodeResolver;
        this.workspaceDao = workspaceDao;
        this.corpusStructureAccessChecker = csAccessChecker;
    }

    /**
     * @see WorkspaceAccessChecker#ensureWorkspaceCanBeCreated(java.lang.String, java.net.URI)
     */
    @Override
    public void ensureWorkspaceCanBeCreated(String userID, URI archiveNodeURI)
            throws NodeAccessException, NodeNotFoundException {
        
        CorpusNode node = corpusStructureProvider.getNode(archiveNodeURI);

        if(node == null) {
            String message = "Archive node not found: " + archiveNodeURI;
            NodeNotFoundException ex = new NodeNotFoundException(archiveNodeURI, message);
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
        if(!node.isOnSite()) {
            NodeAccessException ex = new ExternalNodeException(archiveNodeURI);
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
        if(CorpusNodeType.COLLECTION != node.getType() && CorpusNodeType.METADATA != node.getType()) {
            throw new IllegalArgumentException("Selected node should be Metadata: " + archiveNodeURI);
            
        }

        String nodeID = nodeResolver.getId(node);
        URI nodeID_URI = AdapterUtils.toNodeUri(Integer.parseInt(nodeID));
        
        logger.debug("Ensuring that node '{}' is accessible to user {}", archiveNodeURI, userID);
        ensureWriteAccessToNode(userID, nodeID_URI);
        
        logger.debug("Ensuring that node '{}' is not locked", archiveNodeURI);
        ensureNodeIsNotLocked(archiveNodeURI);
        
        Collection<CorpusNode> descendants = corpusStructureProvider.getDescendantNodes(archiveNodeURI);
        
        logger.debug("Ensuring that the descendants of node '{}' (count = {}) are not locked and accessible to user {}", archiveNodeURI, descendants.size(), userID);
        
        for(CorpusNode descendant : descendants) {
            if(descendant.isOnSite()) {
                
                String descendantID = nodeResolver.getId(descendant);
                URI descendantID_URI = AdapterUtils.toNodeUri(Integer.parseInt(descendantID));
                ensureWriteAccessToNode(userID, descendantID_URI);
                
                URI descendantPid = nodeResolver.getPID(descendant);
                ensureNodeIsNotLocked(descendantPid);
            }
        }
        

        logger.debug("A workspace can be created in node " + archiveNodeURI + " by user " + userID);
        
    }

    /**
     * @see WorkspaceAccessChecker#ensureBranchIsAccessible(java.lang.String, java.net.URI)
     */
    @Override
    public void ensureBranchIsAccessible(String userID, URI archiveNodeURI) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * @see WorkspaceAccessChecker#ensureUserHasAccessToWorkspace(java.lang.String, int)
     */
    @Override
    public void ensureUserHasAccessToWorkspace(String userID, int workspaceID)
            throws WorkspaceNotFoundException, WorkspaceAccessException {
        
        Workspace workspace = workspaceDao.getWorkspace(workspaceID);
        
        if(!userID.equals(workspace.getUserID())) {
            String errorMessage = "User with ID " + userID + " does not have access to workspace with ID " + workspaceID;
            logger.error(errorMessage);
            throw new WorkspaceAccessException(errorMessage, workspaceID, null);
        }
        
        logger.debug("User " + userID + " has access to workspace " + workspaceID);
    }

    /**
     * @see WorkspaceAccessChecker#ensureUserCanDeleteWorkspace(java.lang.String, int)
     */
    @Override
    public void ensureUserCanDeleteWorkspace(String userID, int workspaceID)
            throws WorkspaceNotFoundException, WorkspaceAccessException {
        
        Workspace workspace = workspaceDao.getWorkspace(workspaceID);
        
        if(!userID.equals(workspace.getUserID()) && !managerUsers.contains(userID)) {
            String errorMessage = "User with ID " + userID + " cannot delete workspace with ID " + workspaceID;
            logger.error(errorMessage);
            throw new WorkspaceAccessException(errorMessage, workspaceID, null);
        }
        
        logger.debug("User " + userID + " can delete workspace " + workspaceID);
    }
    
    /**
     * @see WorkspaceAccessChecker#ensureWriteAccessToNode(java.lang.String, java.net.URI)
     */
    @Override
    public void ensureWriteAccessToNode(String userID, URI archiveNodeURI) throws NodeAccessException, NodeNotFoundException {
        if(!this.corpusStructureAccessChecker.hasWriteAccess(userID, archiveNodeURI)) {
            NodeAccessException ex = new UnauthorizedNodeException(archiveNodeURI, userID);
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
    }
    
    /**
     * @see WorkspaceAccessChecker#ensureNodeIsNotLocked(java.net.URI)
     */
    @Override
    public void ensureNodeIsNotLocked(URI archiveNodeURI) throws NodeAccessException {
        if(this.workspaceDao.isNodeLocked(archiveNodeURI)) {
            Collection<WorkspaceNode> lockedNodes = workspaceDao.getWorkspaceNodeByArchiveURI(archiveNodeURI);
            int workspaceID = -1;
            if(lockedNodes.size() == 1) {
                workspaceID = lockedNodes.iterator().next().getWorkspaceID();
            }
            NodeAccessException ex = new LockedNodeException(archiveNodeURI, workspaceID);
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}
