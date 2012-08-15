/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web.model.mock;

import java.util.Arrays;
import java.util.Collection;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MockWorkspaceService implements WorkspaceService {

    private static Logger logger = LoggerFactory.getLogger(MockWorkspaceService.class);
    private final Workspace workspace;

    public MockWorkspaceService(Workspace workspace) {
	logger.info("call to constructor MockWorkspaceService({})", workspace);
	this.workspace = workspace;
    }

    @Override
    public Workspace createWorkspace(String userID, int archiveNodeID) {
	logger.info("call to createWorkspace({}, {})", userID, archiveNodeID);
	return workspace;
    }

    @Override
    public void submitWorkspace(int workspaceID) {
	logger.info("call to submitWorkspace({})", workspaceID);
    }

    @Override
    public Workspace getWorkspace(int workspaceID) {
	logger.info("call to getWorkspace({})", workspaceID);
	return workspace;
    }

    @Override
    public Collection<Workspace> listUserWorkspaces(String userID) {
        Workspace myNewMockWorkspace = new MockWorkspace();
        myNewMockWorkspace.setWorkspaceID(1);
        Workspace myNewMockWorkspace2 = new MockWorkspace();
        myNewMockWorkspace2.setWorkspaceID(2);
        
	return Arrays.asList(myNewMockWorkspace, myNewMockWorkspace2);
    }

    @Override
    public Workspace openWorkspace(String userID, int workspaceID) {
		logger.info("call to getWorkspace({})", workspaceID);
	return workspace;
    }

    @Override
    public WorkspaceNode getNode(int nodeID) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<WorkspaceNode> getChildNodes(int nodeID) {
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
