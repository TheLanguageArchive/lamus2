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
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.CorpusNodeType;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ArchiveNodeNotFoundException;
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
    private final AmsBridge amsBridge;
    private final WorkspaceDao workspaceDao;
    
    
    @Autowired
    public LamusWorkspaceAccessChecker(CorpusStructureProvider csProvider, AmsBridge amsBridge, WorkspaceDao workspaceDao) {
        this.corpusStructureProvider = csProvider;
        this.amsBridge = amsBridge;
        this.workspaceDao = workspaceDao;
    }

    /**
     * @see WorkspaceAccessChecker#ensureWorkspaceCanBeCreated(java.lang.String, java.net.URI)
     */
    @Override
    public void ensureWorkspaceCanBeCreated(String userID, URI archiveNodeURI)
            throws NodeAccessException {
        
        CorpusNode node = this.corpusStructureProvider.getNode(archiveNodeURI);

        if(node == null) {
            String message = "Archive node not found: " + archiveNodeURI;
            ArchiveNodeNotFoundException ex = new ArchiveNodeNotFoundException(message, archiveNodeURI, null);
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
        if(!this.amsBridge.hasWriteAccess(userID, archiveNodeURI)) {
            NodeAccessException ex = new UnauthorizedNodeException(archiveNodeURI, userID);
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
        
        //TODO Should it take into account the "sessions" folders, where write access is always true?
        
        if(this.workspaceDao.isNodeLocked(archiveNodeURI)) {
            Collection<WorkspaceNode> lockedNodes = this.workspaceDao.getWorkspaceNodeByArchiveURI(archiveNodeURI);
            int workspaceID = -1;
            if(lockedNodes.size() == 1) {
                workspaceID = lockedNodes.iterator().next().getWorkspaceID();
            }
            NodeAccessException ex = new LockedNodeException(archiveNodeURI, workspaceID);
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
        
        //TODO Should it check now if any of the child nodes is locked??
            // maybe that's too much to compute for a large workspace...
            // on the other hand, it can also be a lot of waiting when creating the workspace, in case it ends up being locked
            // if there was a way of checking just the leave nodes, it would be a bit easier

        logger.info("A workspace can be created in node " + archiveNodeURI);
        
    }
    
    /**
     * @see WorkspaceAccessChecker#ensureUserHasAccessToWorkspace(java.lang.String, int)
     */
    @Override
    public void ensureUserHasAccessToWorkspace(String userID, int workspaceID)
            throws WorkspaceNotFoundException, WorkspaceAccessException {
        
        Workspace workspace = this.workspaceDao.getWorkspace(workspaceID);
        
        if(!userID.equals(workspace.getUserID())) {
            String errorMessage = "User with ID " + userID + " does not have access to workspace with ID " + workspaceID;
            logger.error(errorMessage);
            throw new WorkspaceAccessException(errorMessage, workspaceID, null);
        }
    }
    
}
