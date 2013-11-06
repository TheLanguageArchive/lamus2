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

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import nl.mpi.archiving.corpusstructure.core.AccessInfo;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.mock.MockAccessInfo;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.commons.beanutils.BeanUtils;
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

//    private final ArchiveObjectsDB archiveObjectsDB;
    private final CorpusStructureProvider corpusStructureProvider;
    private final AmsBridge amsBridge;
    private final WorkspaceDao workspaceDao;
    
    @Autowired
    private MockAccessInfo defaultAccessInfo;
    
    @Autowired
    public LamusWorkspaceAccessChecker(CorpusStructureProvider csProvider, AmsBridge amsBridge, WorkspaceDao workspaceDao) {
        this.corpusStructureProvider = csProvider;
        this.amsBridge = amsBridge;
        this.workspaceDao = workspaceDao;
    }

    /**
     * @see WorkspaceAccessChecker#canCreateWorkspace(java.lang.String, java.net.URI)
     */
    @Override
    public boolean canCreateWorkspace(String userID, URI archiveNodeURI) {
        
        CorpusNode node = null;
        try {
            node = this.corpusStructureProvider.getNode(archiveNodeURI);
        } catch (UnknownNodeException ex) {
            logger.warn("Node " + archiveNodeURI.toString() + " is not known in the archive", ex);
            return false;
        }
        if(!node.isOnSite()) {
            logger.warn("Node " + archiveNodeURI.toString() + " is not on site (it is an external node)");
            return false;
            //TODO ExternalNodeException
        }
        if(!this.amsBridge.hasWriteAccess(userID, archiveNodeURI)) {
            logger.warn("User " + userID + " has no write access on the node " + archiveNodeURI.toString());
            return false;
            //TODO NoWriteAccessException
        }
        
        //TODO Should it take into account the "sessions" folders, where write access is always true?
        
        if(this.workspaceDao.isNodeLocked(archiveNodeURI)) {
            logger.warn("Node " + archiveNodeURI + " is locked");
            return false;
            //TODO LockedNodeException
        }
        
        //TODO Should it check now if any of the child nodes is locked??
            // maybe that's too much to compute for a large workspace...
            // on the other hand, it can also be a lot of waiting when creating the workspace, in case it ends up being locked
            // if there was a way of checking just the leave nodes, it would be a bit easier

        logger.info("A workspace can be created in node " + archiveNodeURI);
        
        return true;
    }
    
    /**
     * @see WorkspaceAccessChecker#hasAccessToWorkspace(java.lang.String, int)
     */
    @Override
    public boolean hasAccessToWorkspace(String userID, int workspaceID) {
        
        
        Workspace workspace = this.workspaceDao.getWorkspace(workspaceID);
        
        if(workspace == null) {
            logger.error("Can't access a workspace (with ID " + workspaceID + ") that does not exist");
            return false;
        }
        
        if(userID.equals(workspace.getUserID())) {
            logger.info("User with ID " + userID + " has access to workspace with ID " + workspaceID);
            return true;
        } else {
            logger.warn("User with ID " + userID + " does not have access to workspace with ID " + workspaceID);
            return false;
        }
    }
    
    /**
     * @see WorkspaceAccessChecker#getDefaultAccessInfoForUser(java.lang.String)
     */
    @Override
    public AccessInfo getDefaultAccessInfoForUser(String userID) {
        
        List<String> users = new ArrayList<String>();
        users.add(userID);
        
//        AccessInfo defaultAccessRights = AccessInfo.create(AccessInfo.NOBODY, AccessInfo.NOBODY, AccessInfo.ACCESS_LEVEL_NONE);
        MockAccessInfo defaultAccessRights = null;
        try {
            defaultAccessRights = (MockAccessInfo) BeanUtils.cloneBean(this.defaultAccessInfo);
            defaultAccessRights.setReadUsers(users);
            defaultAccessRights.setWriteUsers(users);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Exception not handled yet", ex);
        } catch (InstantiationException ex) {
            throw new UnsupportedOperationException("Exception not handled yet", ex);
        } catch (InvocationTargetException ex) {
            throw new UnsupportedOperationException("Exception not handled yet", ex);
        } catch (NoSuchMethodException ex) {
            throw new UnsupportedOperationException("Exception not handled yet", ex);
        }
        
        return defaultAccessRights;
    }
}
