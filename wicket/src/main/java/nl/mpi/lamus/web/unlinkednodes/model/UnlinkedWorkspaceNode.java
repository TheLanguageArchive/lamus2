/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.web.unlinkednodes.model;

import java.net.URI;
import java.net.URL;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.tree.implementation.LamusWorkspaceTreeNode;

/**
 *
 * @author guisil
 */
public class UnlinkedWorkspaceNode extends LamusWorkspaceTreeNode {

    public UnlinkedWorkspaceNode() {
        super();
    }
    
    public UnlinkedWorkspaceNode(int workspaceNodeID, int workspaceID,
	    URI profileSchemaURI, String name, String title, WorkspaceNodeType type,
	    URL workspaceURL, URI archiveURI, URL archiveURL, URL originURL,
	    WorkspaceNodeStatus status, boolean isProtected, String format,
	    WorkspaceTreeNode parent, WorkspaceDao dao) {
        
        super(workspaceNodeID, workspaceID, profileSchemaURI, name, title, type,
                workspaceURL, archiveURI, archiveURL, originURL, status,
                isProtected, format, parent, dao);
    }
    
    public UnlinkedWorkspaceNode(WorkspaceNode node, WorkspaceTreeNode parent, WorkspaceDao dao) {
        
        super(node, parent, dao);
    }

}
