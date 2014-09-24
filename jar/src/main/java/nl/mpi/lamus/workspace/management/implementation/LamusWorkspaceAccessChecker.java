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
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.CorpusNodeType;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.archiving.corpusstructure.core.database.dao.ArchiveObjectDao;
import nl.mpi.archiving.corpusstructure.core.database.pojo.ArchiveObject;
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
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final ArchiveObjectDao archiveObjectDao;
    private final WorkspaceDao workspaceDao;
    private final CorpusStructureAccessChecker corpusStructureAccessChecker;
    
    @Resource(name = "managerUsers")
    private Collection<String> managerUsers;
    
    
    @Autowired
    public LamusWorkspaceAccessChecker(CorpusStructureProvider csProvider, ArchiveObjectDao aoDao,
            WorkspaceDao workspaceDao, CorpusStructureAccessChecker csAccessChecker) {
        this.corpusStructureProvider = csProvider;
        this.archiveObjectDao = aoDao;
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
        
        //TODO replaced the usage of AMS Bridge (and the associated service because it wasn't working properly due to some hibernate lazy loading issues of the users...)
        //TODO should use some service instead (maybe provided by the corpus structure)
        
        logger.debug("Ensuring that node '{}' is accessible to user {}", archiveNodeURI, userID);
        ensureWriteAccessToNode(userID, archiveNodeURI);
        
        //TODO Should it take into account the "sessions" folders, where write access is always true?
        
        logger.debug("Ensuring that node '{}' is not locked", archiveNodeURI);
        ensureNodeIsNotLocked(archiveNodeURI);
        
        //TODO Should it check now if any of the child nodes is locked??
            // maybe that's too much to compute for a large workspace...
            // on the other hand, it can also be a lot of waiting when creating the workspace, in case it ends up being locked
            // if there was a way of checking just the leave nodes, it would be a bit easier
        
        long nodeId = archiveObjectDao.select(archiveNodeURI).getId();
        Collection<ArchiveObject> descendants = archiveObjectDao.selectDescendants(nodeId);
        
        logger.debug("Ensuring that the descendants of node '{}' (count = {}) are not locked and accessible to user {}", archiveNodeURI, descendants.size(), userID);
        
        for(ArchiveObject descendant : descendants) {
            if(descendant.isOnsite()) {
                URI descendantURI = descendant.getCanonicalUri();
                ensureWriteAccessToNode(userID, descendantURI);
                ensureNodeIsNotLocked(descendantURI);
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
    
    
    private void ensureWriteAccessToNode(String userID, URI archiveNodeURI) throws NodeAccessException, NodeNotFoundException {
        if(!this.corpusStructureAccessChecker.hasWriteAccess(userID, archiveNodeURI)) {
            NodeAccessException ex = new UnauthorizedNodeException(archiveNodeURI, userID);
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
    }
    
    private void ensureNodeIsNotLocked(URI archiveNodeURI) throws NodeAccessException {
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
