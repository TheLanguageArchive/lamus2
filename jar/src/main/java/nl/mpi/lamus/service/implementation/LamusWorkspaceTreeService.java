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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.mpi.lamus.archive.ArchiveHandleHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.management.WorkspaceNodeManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.implementation.LamusNodeReplaceManager;
import nl.mpi.lamus.workspace.tree.implementation.LamusWorkspaceTreeNode;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Service
public class LamusWorkspaceTreeService extends LamusWorkspaceService implements WorkspaceTreeService {

    @Autowired
    public LamusWorkspaceTreeService(WorkspaceAccessChecker aChecker, ArchiveHandleHelper aPidHelper, WorkspaceManager wsManager,
            WorkspaceDao wsDao, WorkspaceUploader wsUploader, WorkspaceNodeLinkManager wsnLinkManager,
            WorkspaceNodeManager wsnManager, LamusNodeReplaceManager topNodeReplaceManager) {
        super(aChecker, aPidHelper, wsManager, wsDao, wsUploader, wsnLinkManager, wsnManager, topNodeReplaceManager);
    }
    
    /**
     * @see WorkspaceTreeService#getTreeNode(int, int)
     */
    @Override
    public WorkspaceTreeNode getTreeNode(int nodeID, WorkspaceTreeNode parentTreeNode)
            throws WorkspaceNodeNotFoundException {
        
        WorkspaceNode child = this.workspaceDao.getWorkspaceNode(nodeID);
        
        WorkspaceTreeNode treeNode = new LamusWorkspaceTreeNode(
                        child.getWorkspaceNodeID(), child.getWorkspaceID(),
                        child.getProfileSchemaURI(), child.getName(), child.getTitle(),
                        child.getType(), child.getWorkspaceURL(), child.getArchiveURI(),
                        child.getArchiveURL(), child.getOriginURI(), child.getStatus(),
                        child.isProtected(), child.getFormat(), parentTreeNode, this.workspaceDao);
        
        return treeNode;
    }
    
    /**
     * @see WorkspaceTreeService#listUnlinkedTreeNodes(java.lang.String, int)
     */
    @Override
    public List<WorkspaceTreeNode> listUnlinkedTreeNodes(String userID, int workspaceID) {
        
        List<WorkspaceNode> nodes = super.listUnlinkedNodes(userID, workspaceID);
        List<WorkspaceTreeNode> treeNodes = new ArrayList<>();
        
        for(WorkspaceNode node : nodes) {
            
            //TODO assume null parent... should it be changed in this case?
            treeNodes.add(new LamusWorkspaceTreeNode(node, null, workspaceDao));
        }
        
        return treeNodes;
    }

    /**
     * @see WorkspaceTreeService#deleteTreeNodes(java.lang.String, java.util.Collection)
     */
    @Override
    public void deleteTreeNodes(String userID, Collection<WorkspaceTreeNode> nodesToDelete)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        for(WorkspaceTreeNode node : nodesToDelete) {
            deleteNode(userID, node);
        }
    }
}
