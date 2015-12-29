/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.actions.implementation;

import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class LinkNodesAsInfoActionTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock WorkspaceService mockWorkspaceService;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock NodeUtil mockNodeUtil;
    @Mock WorkspaceTreeNode mockParentNode;
    @Mock WorkspaceTreeNode mockChildNodeOne;
    @Mock WorkspaceTreeNode mockChildNodeTwo;
    @Mock WorkspaceTreeNode mockChildNodeThree;
    
    private WsTreeNodesAction linkNodesAsInfoAction;
    
    private final String expectedActionName = "link_node_info_action";
    private final String userID = "testUser";
    
    public LinkNodesAsInfoActionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        linkNodesAsInfoAction = new LinkNodesAsInfoAction();
        ReflectionTestUtils.setField(linkNodesAsInfoAction, "name", expectedActionName);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getName() {
        
        String retrievedActionName = linkNodesAsInfoAction.getName();
        
        assertEquals("Retrieved name different from expected", expectedActionName, retrievedActionName);
    }
    
    @Test
    public void executeWithNullTreeNodeCollection() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedChildNodes = new ArrayList<>();
        selectedChildNodes.add(mockChildNodeOne);
        String expectedExceptionMessage = "Action for linking nodes as info files requires exactly one tree node; currently null";
        
        linkNodesAsInfoAction.setSelectedUnlinkedNodes(selectedChildNodes);

        try {
            linkNodesAsInfoAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithNullChildNodeCollection() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockParentNode);
        String expectedExceptionMessage = "Action for linking nodes as info files requires at least one selected child node; currently null";
        
        linkNodesAsInfoAction.setSelectedTreeNodes(selectedTreeNodes);

        try {
            linkNodesAsInfoAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithEmptyTreeNodeCollection() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        Collection<WorkspaceTreeNode> selectedChildNodes = new ArrayList<>();
        selectedChildNodes.add(mockChildNodeOne);
        String expectedExceptionMessage = "Action for linking nodes as info files requires exactly one tree node; currently selected " + selectedTreeNodes.size();
        
        linkNodesAsInfoAction.setSelectedTreeNodes(selectedTreeNodes);
        linkNodesAsInfoAction.setSelectedUnlinkedNodes(selectedChildNodes);

        try {
            linkNodesAsInfoAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithEmptyChildNodeCollection() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockParentNode);
        Collection<WorkspaceTreeNode> selectedChildNodes = new ArrayList<>();
        String expectedExceptionMessage = "Action for linking nodes as info files requires at least one selected child node";
        
        linkNodesAsInfoAction.setSelectedTreeNodes(selectedTreeNodes);
        linkNodesAsInfoAction.setSelectedUnlinkedNodes(selectedChildNodes);

        try {
            linkNodesAsInfoAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithNullParameters() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockParentNode);
        Collection<WorkspaceTreeNode> selectedChildNodes = new ArrayList<>();
        selectedChildNodes.add(mockChildNodeOne);
        String expectedExceptionMessage_Service = "WorkspaceService should have been set";
        String expectedExceptionMessage_Dao = "WorkspaceDao should have been set";
        String expectedExceptionMessage_NodeUtil = "NodeUtil should have been set";
        
        linkNodesAsInfoAction.setSelectedTreeNodes(selectedTreeNodes);
        linkNodesAsInfoAction.setSelectedUnlinkedNodes(selectedChildNodes);

        try {
            linkNodesAsInfoAction.execute(userID, null, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage_Service, ex.getMessage());
        }
        
        try {
            linkNodesAsInfoAction.execute(userID, mockWorkspaceService, null, mockNodeUtil);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage_Dao, ex.getMessage());
        }
        
        try {
            linkNodesAsInfoAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, null);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage_NodeUtil, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithMetadata() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockParentNode);
        Collection<WorkspaceTreeNode> selectedChildNodes = new ArrayList<>();
        selectedChildNodes.add(mockChildNodeOne);
        String expectedExceptionMessage = "Some nodes (1) were not linked. Metadata nodes cannot be linked as info files.";
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNodeOne); will(returnValue(Boolean.TRUE));
        }});
        
        linkNodesAsInfoAction.setSelectedTreeNodes(selectedTreeNodes);
        linkNodesAsInfoAction.setSelectedUnlinkedNodes(selectedChildNodes);

        try {
            linkNodesAsInfoAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeOneAction() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockParentNode);
        Collection<WorkspaceTreeNode> selectedChildNodes = new ArrayList<>();
        selectedChildNodes.add(mockChildNodeOne);
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNodeOne); will(returnValue(Boolean.FALSE));
            oneOf(mockChildNodeOne).setType(WorkspaceNodeType.RESOURCE_INFO);
            oneOf(mockWorkspaceService).linkNodes(userID, mockParentNode, mockChildNodeOne);
            oneOf(mockWorkspaceDao).updateNodeType(mockChildNodeOne);
        }});
        
        linkNodesAsInfoAction.setSelectedTreeNodes(selectedTreeNodes);
        linkNodesAsInfoAction.setSelectedUnlinkedNodes(selectedChildNodes);

        linkNodesAsInfoAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
    }
    
    @Test
    public void executeTwoActionsWithMetadata() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockParentNode);
        Collection<WorkspaceTreeNode> selectedChildNodes = new ArrayList<>();
        selectedChildNodes.add(mockChildNodeOne);
        selectedChildNodes.add(mockChildNodeTwo);
        selectedChildNodes.add(mockChildNodeThree);
        String expectedExceptionMessage = "Some nodes (2) were not linked. Metadata nodes cannot be linked as info files.";
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNodeOne); will(returnValue(Boolean.TRUE));
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNodeTwo); will(returnValue(Boolean.FALSE));
            oneOf(mockChildNodeTwo).setType(WorkspaceNodeType.RESOURCE_INFO);
            oneOf(mockWorkspaceService).linkNodes(userID, mockParentNode, mockChildNodeTwo);
            oneOf(mockWorkspaceDao).updateNodeType(mockChildNodeTwo);
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNodeThree); will(returnValue(Boolean.TRUE));
        }});
        
        linkNodesAsInfoAction.setSelectedTreeNodes(selectedTreeNodes);
        linkNodesAsInfoAction.setSelectedUnlinkedNodes(selectedChildNodes);

        try {
            linkNodesAsInfoAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeTwoActions() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockParentNode);
        Collection<WorkspaceTreeNode> selectedChildNodes = new ArrayList<>();
        selectedChildNodes.add(mockChildNodeOne);
        selectedChildNodes.add(mockChildNodeTwo);
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNodeOne); will(returnValue(Boolean.FALSE));
            oneOf(mockChildNodeOne).setType(WorkspaceNodeType.RESOURCE_INFO);
            oneOf(mockWorkspaceService).linkNodes(userID, mockParentNode, mockChildNodeOne);
            oneOf(mockWorkspaceDao).updateNodeType(mockChildNodeOne);
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNodeTwo); will(returnValue(Boolean.FALSE));
            oneOf(mockChildNodeTwo).setType(WorkspaceNodeType.RESOURCE_INFO);
            oneOf(mockWorkspaceService).linkNodes(userID, mockParentNode, mockChildNodeTwo);
            oneOf(mockWorkspaceDao).updateNodeType(mockChildNodeTwo);
        }});

        linkNodesAsInfoAction.setSelectedTreeNodes(selectedTreeNodes);
        linkNodesAsInfoAction.setSelectedUnlinkedNodes(selectedChildNodes);
        
        linkNodesAsInfoAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
    }
    
    @Test
    public void executeActionWorkspaceNotFoundException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        final int workspaceID = 10;
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockParentNode);
        Collection<WorkspaceTreeNode> selectedChildNodes = new ArrayList<>();
        selectedChildNodes.add(mockChildNodeOne);
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNodeOne); will(returnValue(Boolean.FALSE));
            oneOf(mockChildNodeOne).setType(WorkspaceNodeType.RESOURCE_INFO);
            oneOf(mockWorkspaceService).linkNodes(userID, mockParentNode, mockChildNodeOne);
                will(throwException(expectedException));
        }});

        linkNodesAsInfoAction.setSelectedTreeNodes(selectedTreeNodes);
        linkNodesAsInfoAction.setSelectedUnlinkedNodes(selectedChildNodes);
        
        try {
            linkNodesAsInfoAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeActionWorkspaceAccessException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        final int workspaceID = 10;
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockParentNode);
        Collection<WorkspaceTreeNode> selectedChildNodes = new ArrayList<>();
        selectedChildNodes.add(mockChildNodeOne);
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNodeOne); will(returnValue(Boolean.FALSE));
            oneOf(mockChildNodeOne).setType(WorkspaceNodeType.RESOURCE_INFO);
            oneOf(mockWorkspaceService).linkNodes(userID, mockParentNode, mockChildNodeOne);
                will(throwException(expectedException));
        }});
        
        linkNodesAsInfoAction.setSelectedTreeNodes(selectedTreeNodes);
        linkNodesAsInfoAction.setSelectedUnlinkedNodes(selectedChildNodes);

        try {
            linkNodesAsInfoAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeActionWorkspaceException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        final int workspaceID = 10;
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockParentNode);
        Collection<WorkspaceTreeNode> selectedChildNodes = new ArrayList<>();
        selectedChildNodes.add(mockChildNodeOne);
        
        final WorkspaceException expectedException = new WorkspaceException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNodeOne); will(returnValue(Boolean.FALSE));
            oneOf(mockChildNodeOne).setType(WorkspaceNodeType.RESOURCE_INFO);
            oneOf(mockWorkspaceService).linkNodes(userID, mockParentNode, mockChildNodeOne);
                will(throwException(expectedException));
        }});

        linkNodesAsInfoAction.setSelectedTreeNodes(selectedTreeNodes);
        linkNodesAsInfoAction.setSelectedUnlinkedNodes(selectedChildNodes);
        
        try {
            linkNodesAsInfoAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
}
