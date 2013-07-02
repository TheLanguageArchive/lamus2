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
package nl.mpi.lamus.workspace.importing.implementation;

import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeLinker;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.WorkspaceParentNodeReference;
import nl.mpi.metadata.api.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusWorkspaceNodeLinker implements WorkspaceNodeLinker {
    
    private final WorkspaceParentNodeReferenceFactory workspaceParentNodeReferenceFactory;
    private final WorkspaceNodeLinkFactory workspaceNodeLinkFactory;
    private final WorkspaceDao workspaceDao;
    
    @Autowired
    public LamusWorkspaceNodeLinker(WorkspaceParentNodeReferenceFactory parentNodeReferenceFactory,
            WorkspaceNodeLinkFactory nodeLinkFactory, WorkspaceDao wDao) {
        
        this.workspaceParentNodeReferenceFactory = parentNodeReferenceFactory;
        this.workspaceNodeLinkFactory = nodeLinkFactory;
        this.workspaceDao = wDao;
    }

    public void linkNodes(WorkspaceNode parentNode, WorkspaceNode childNode, Reference childLink) {
        
        
        
        WorkspaceParentNodeReference parentNodeReference =
		workspaceParentNodeReferenceFactory.getNewWorkspaceParentNodeReference(parentNode, childLink);

	//TODO set top node ID in workspace (if reference is null), set workspace status / Save workspace
	if (parentNodeReference == null) { //TODO find a better way of indicating this
            
            Workspace workspace = workspaceDao.getWorkspace(childNode.getWorkspaceID());
            workspace.setTopNodeID(childNode.getWorkspaceNodeID());
	    workspace.setTopNodeArchiveID(childNode.getArchiveNodeID());
	    workspace.setTopNodeArchiveURL(childNode.getArchiveURL());
	    workspaceDao.updateWorkspaceTopNode(workspace);
	} else {
	    //TODO add information about parent link
	    // add the link in the database
	    WorkspaceNodeLink nodeLink = workspaceNodeLinkFactory.getNewWorkspaceNodeLink(
		    parentNodeReference.getParentWorkspaceNodeID(), childNode.getWorkspaceNodeID(), childLink.getURI());
	    workspaceDao.addWorkspaceNodeLink(nodeLink);
	    //TODO possible problems with adding the link? if the link already exists?
	}
    }
    
}
