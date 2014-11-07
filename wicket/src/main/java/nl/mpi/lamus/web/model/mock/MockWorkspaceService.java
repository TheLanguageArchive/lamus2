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

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.upload.implementation.UploadProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Collection of service methods that are used to retrieve nodes with the
 * intent of representing them graphically (using a WorkspaceTreeNode instead of
 * just a WorkspaceNode)
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MockWorkspaceService implements WorkspaceTreeService {

//@SpringBean(name = "workspaceTreeNode")
    private WorkspaceTreeNode workspaceTreeNode;
    private static Logger logger = LoggerFactory.getLogger(MockWorkspaceService.class);
    private final Workspace workspace;

    public MockWorkspaceService(Workspace workspace, WorkspaceTreeNode workspaceTreeNode) {
	logger.info("call to constructor MockWorkspaceService({})", workspace);
	this.workspace = workspace;
	this.workspaceTreeNode = workspaceTreeNode;
    }

    /**
     * Creates a workspace starting in a given archive node, for a given user.
     *
     * @param archiveNodeURI archive URI of the node
     * @param userID ID of the user
     * @return Workspace object
     */
    @Override
    public Workspace createWorkspace(String userID, URI archiveNodeURI) {
	logger.info("call to createWorkspace({}, {})", userID, archiveNodeURI);
	return workspace;
    }

    /**
     * Submits a workspace back into the archive.
     *
     * @param userID ID of the user
     * @param workspaceID ID of the workspace
     */
    @Override
    public void submitWorkspace(String userID, int workspaceID) {
	logger.info("call to submitWorkspace({}, {})", userID, workspaceID);
    }

    /**
     * Retrieves a workspace with the given ID.
     *
     * @param workspaceID ID of the workspace to retrieve
     * @return Retrieved workspace object
     */
    @Override
    public Workspace getWorkspace(int workspaceID) {
	logger.info("call to getWorkspace({})", workspaceID);
	return workspace;
    }

    /**
     * Retrieves a collection containing the active workspaces belonging to the
     * given user.
     *
     * @param userID ID of the user
     * @return Collection with the user's active workspaces
     */
    @Override
    public Collection<Workspace> listUserWorkspaces(String userID) {
	Workspace myNewMockWorkspace = new MockWorkspace();
	myNewMockWorkspace.setWorkspaceID(1);
	Workspace myNewMockWorkspace2 = new MockWorkspace();
	myNewMockWorkspace2.setWorkspaceID(2);

	return Arrays.asList(myNewMockWorkspace, myNewMockWorkspace2);
    }

    /**
     * Opens a workspace, retrieving the corresponding object from the database.
     * While doing so, it also updates the session start date of the workspace.
     *
     * @param workspaceID ID of the workspace to open
     * @return Retrieved workspace object
     */
    @Override
    public Workspace openWorkspace(String userID, int workspaceID) {
	logger.info("call to getWorkspace({})", workspaceID);
	return workspace;
    }

    /**
     * Retrieves a workspace node with the given ID.
     *
     * @param nodeID ID of the node to retrieve
     * @return corresponding workspace node
     */
    @Override
    public WorkspaceNode getNode(int nodeID) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Retrieves a collection containing the child nodes of the node with the
     * given ID.
     *
     * @param nodeID ID of the parent node
     * @return child nodes of the given node
     */
    @Override
    public Collection<WorkspaceNode> getChildNodes(int nodeID) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Deletes the workspace with the given ID.
     *
     * @param userID ID of the user who is trying to delete the workspace
     * @param workspaceID ID of the workspace to be deleted
     */
    @Override
    public void deleteWorkspace(String userID, int workspaceID) {
	//DO something;
    }

    /**
     * Retrieves a workspace tree node with the given ID.
     *
     * @param nodeID ID of the node to retrieve
     * @param parentNode WorkspaceTreeNode object of the parent node
     * @return corresponding workspace tree node
     */
    @Override
    public WorkspaceTreeNode getTreeNode(int nodeID, WorkspaceTreeNode parentNode) {
	return workspaceTreeNode;


    }

    @Override
    public void addNode(String userID, WorkspaceNode node) throws WorkspaceNotFoundException, WorkspaceAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void linkNodes(String userID, WorkspaceNode parentNode, WorkspaceNode childNode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void unlinkNodes(String userID, WorkspaceNode parentNode, WorkspaceNode childNode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteNode(String userID, WorkspaceNode node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<WorkspaceNode> listUnlinkedNodes(String userID, int workspaceID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public File getWorkspaceUploadDirectory(int workspaceID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void uploadFileIntoWorkspace(String userID, int workspaceID, InputStream inputStream, String filename) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<WorkspaceTreeNode> listUnlinkedTreeNodes(String userID, int workspaceID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<UploadProblem> processUploadedFiles(String userID, int workspaceID, Collection<File> uploadedFiles) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean userHasWorkspaces(String userID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Workspace> listAllWorkspaces() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void replaceTree(String userID, WorkspaceNode oldTreeTopNode, WorkspaceNode newTreeTopNode, WorkspaceNode parentNode)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
