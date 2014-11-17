/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.replace.action.implementation;

import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.management.WorkspaceNodeManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionExecutor;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;

/**
 *
 * @author guisil
 */
public class LamusReplaceActionExecutorTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock WorkspaceNodeLinkManager mockWorkspaceNodeLinkManager;
    @Mock WorkspaceNodeManager mockWorkspaceNodeManager;
    
    @Mock LinkNodeReplaceAction mockLinkAction;
    @Mock UnlinkNodeReplaceAction mockUnlinkAction;
    @Mock DeleteNodeReplaceAction mockDeleteAction;
    @Mock ReplaceNodeReplaceAction mockReplaceAction;
    @Mock RemoveArchiveUriReplaceAction mockRemoveArchiveUriAction;
    @Mock WorkspaceNode mockParentNode;
    @Mock WorkspaceNode mockChildNode;
    @Mock WorkspaceNode mockNewChildNode;
    
    private ReplaceActionExecutor replaceActionExecutor;
    
    
    public LamusReplaceActionExecutorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        replaceActionExecutor = new LamusReplaceActionExecutor(mockWorkspaceNodeLinkManager, mockWorkspaceNodeManager);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void executeLinkActionSuccessful() throws WorkspaceException, ProtectedNodeException {
        
        context.checking(new Expectations() {{
            
            oneOf(mockLinkAction).getParentNode(); will(returnValue(mockParentNode));
            oneOf(mockLinkAction).getAffectedNode(); will(returnValue(mockChildNode));
            
            oneOf(mockWorkspaceNodeLinkManager).linkNodes(mockParentNode, mockChildNode);
        }});
        
        replaceActionExecutor.execute(mockLinkAction);
    }
    
    @Test
    public void executeLinkActionUnsuccessful() throws WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 10;
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockLinkAction).getParentNode(); will(returnValue(mockParentNode));
            oneOf(mockLinkAction).getAffectedNode(); will(returnValue(mockChildNode));
            
            oneOf(mockWorkspaceNodeLinkManager).linkNodes(mockParentNode, mockChildNode);
                will(throwException(expectedException));
        }});
        
        try {
            replaceActionExecutor.execute(mockLinkAction);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeUnlinkActionSuccessful() throws WorkspaceException, ProtectedNodeException {
        
        context.checking(new Expectations() {{
            
            oneOf(mockUnlinkAction).getParentNode(); will(returnValue(mockParentNode));
            oneOf(mockUnlinkAction).getAffectedNode(); will(returnValue(mockChildNode));
            
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodes(mockParentNode, mockChildNode);
        }});
        
        replaceActionExecutor.execute(mockUnlinkAction);
    }
    
    @Test
    public void executeUnlinkActionUnsuccessful() throws WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 10;
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockUnlinkAction).getParentNode(); will(returnValue(mockParentNode));
            oneOf(mockUnlinkAction).getAffectedNode(); will(returnValue(mockChildNode));
            
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodes(mockParentNode, mockChildNode);
                will(throwException(expectedException));
        }});
        
        try {
            replaceActionExecutor.execute(mockUnlinkAction);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeDeleteActionSuccessful() throws WorkspaceException, ProtectedNodeException {
        
        context.checking(new Expectations() {{
            
            oneOf(mockDeleteAction).getAffectedNode(); will(returnValue(mockChildNode));
            
            oneOf(mockWorkspaceNodeManager).deleteNodesRecursively(mockChildNode);
        }});
        
        replaceActionExecutor.execute(mockDeleteAction);
    }
    
    @Test
    public void executeDeleteActionUnsuccessful() throws WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 10;
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockDeleteAction).getAffectedNode(); will(returnValue(mockChildNode));
            
            oneOf(mockWorkspaceNodeManager).deleteNodesRecursively(mockChildNode);
                will(throwException(expectedException));
        }});
        
        try {
            replaceActionExecutor.execute(mockDeleteAction);
            fail("should have thrown an exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeReplaceActionSuccessful() throws WorkspaceException, ProtectedNodeException {
        
        final boolean isAlreadyLinked = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            oneOf(mockReplaceAction).getParentNode(); will(returnValue(mockParentNode));
            oneOf(mockReplaceAction).getAffectedNode(); will(returnValue(mockChildNode));
            oneOf(mockReplaceAction).getNewNode(); will(returnValue(mockNewChildNode));
            oneOf(mockReplaceAction).isAlreadyLinked(); will(returnValue(isAlreadyLinked));
            
            oneOf(mockWorkspaceNodeLinkManager).replaceNode(mockParentNode, mockChildNode, mockNewChildNode, isAlreadyLinked);
        }});
        
        replaceActionExecutor.execute(mockReplaceAction);
    }
    
    @Test
    public void executeReplaceActionUnsuccessful() throws WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 10;
        final boolean isAlreadyLinked = Boolean.FALSE;
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockReplaceAction).getParentNode(); will(returnValue(mockParentNode));
            oneOf(mockReplaceAction).getAffectedNode(); will(returnValue(mockChildNode));
            oneOf(mockReplaceAction).getNewNode(); will(returnValue(mockNewChildNode));
            oneOf(mockReplaceAction).isAlreadyLinked(); will(returnValue(isAlreadyLinked));
            
            oneOf(mockWorkspaceNodeLinkManager).replaceNode(mockParentNode, mockChildNode, mockNewChildNode, isAlreadyLinked);
                will(throwException(expectedException));
        }});
        
        try {
            replaceActionExecutor.execute(mockReplaceAction);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeRemoveArchiveUriActionSuccessful() throws WorkspaceException, ProtectedNodeException {
        
        context.checking(new Expectations() {{
            
            oneOf(mockRemoveArchiveUriAction).getParentNode(); will(returnValue(mockParentNode));
            oneOf(mockRemoveArchiveUriAction).getAffectedNode(); will(returnValue(mockChildNode));
            
            oneOf(mockWorkspaceNodeLinkManager).removeArchiveUriFromChildNode(mockParentNode, mockChildNode);
        }});
        
        replaceActionExecutor.execute(mockRemoveArchiveUriAction);
    }
    
    @Test
    public void executeRemoveArchiveUriActionUnsuccessful() throws WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 10;
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockRemoveArchiveUriAction).getParentNode(); will(returnValue(mockParentNode));
            oneOf(mockRemoveArchiveUriAction).getAffectedNode(); will(returnValue(mockChildNode));
            
            oneOf(mockWorkspaceNodeLinkManager).removeArchiveUriFromChildNode(mockParentNode, mockChildNode);
                will(throwException(expectedException));
        }});
        
        try {
            replaceActionExecutor.execute(mockRemoveArchiveUriAction);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
}