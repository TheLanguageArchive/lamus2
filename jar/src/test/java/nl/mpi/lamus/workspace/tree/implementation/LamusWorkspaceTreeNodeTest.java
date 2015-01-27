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
package nl.mpi.lamus.workspace.tree.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceTreeNodeTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    private WorkspaceNode child1;
    private WorkspaceNode child2;
    private Collection<WorkspaceNode> nodeChildren;
    
    private WorkspaceTreeNode parentTreeNode;
    private WorkspaceTreeNode treeNode;
    private WorkspaceTreeNode childTreeNode1;
    private WorkspaceTreeNode childTreeNode2;
    private List<WorkspaceTreeNode> treeNodeChildren;
    
    @Mock private WorkspaceDao mockWorkspaceDao;
    

    public LamusWorkspaceTreeNodeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws MalformedURLException {
        
        URI parentNodeURI = URI.create(UUID.randomUUID().toString());
        parentTreeNode = new LamusWorkspaceTreeNode(1, 1, URI.create("file:/parent.uri"),
                "parentName", "parent title", WorkspaceNodeType.UNKNOWN, 
                new URL("file:/parent/fake1.url"), parentNodeURI, new URL("file:/some/fake3.url"), URI.create("file:/parent/fake3.url"),
                WorkspaceNodeStatus.NODE_CREATED, Boolean.FALSE, "unknown",
                null, mockWorkspaceDao);
        
        URI nodeURI = URI.create(UUID.randomUUID().toString());
        treeNode = new LamusWorkspaceTreeNode(10, 1, URI.create("file:/some.uri"),
                "nodeName", "node title", WorkspaceNodeType.UNKNOWN, 
                new URL("file:/some/fake1.url"), nodeURI, new URL("file:/some/fake3.url"), URI.create("file:/some/fake3.url"),
                WorkspaceNodeStatus.NODE_CREATED, Boolean.FALSE, "unknown",
                parentTreeNode, mockWorkspaceDao);
        
        nodeChildren = new ArrayList<>();
        treeNodeChildren = new ArrayList<>();
        
        URI child1URI = URI.create(UUID.randomUUID().toString());
        child1 = new LamusWorkspaceNode(20, 1, URI.create("file:/child1.uri"),
                "child1Name", "child1 title", WorkspaceNodeType.UNKNOWN, 
                new URL("file:/child1/fake1.url"), child1URI, new URL("file:/some/fake3.url"), URI.create("file:/child1/fake3.url"),
                WorkspaceNodeStatus.NODE_CREATED, Boolean.FALSE, "unknown");
        nodeChildren.add(child1);
        childTreeNode1 = new LamusWorkspaceTreeNode(20, 1, URI.create("file:/child1.uri"),
                "child1Name", "child1 title", WorkspaceNodeType.UNKNOWN, 
                new URL("file:/child1/fake1.url"), child1URI, new URL("file:/some/fake3.url"), URI.create("file:/child1/fake3.url"),
                WorkspaceNodeStatus.NODE_CREATED, Boolean.FALSE, "unknown",
                treeNode, mockWorkspaceDao);
        treeNodeChildren.add(childTreeNode1);
        
        URI child2URI = URI.create(UUID.randomUUID().toString());
        child2 = new LamusWorkspaceNode(21, 1, URI.create("file:/child2.uri"),
                "child2Name", "child2 title", WorkspaceNodeType.UNKNOWN, 
                new URL("file:/child2/fake1.url"), child2URI, new URL("file:/some/fake3.url"), URI.create("file:/child2/fake3.url"),
                WorkspaceNodeStatus.NODE_CREATED, Boolean.FALSE, "unknown");
        nodeChildren.add(child2);
        childTreeNode2 = new LamusWorkspaceTreeNode(21, 1, URI.create("file:/child2.uri"),
                "child2Name", "child2 title", WorkspaceNodeType.UNKNOWN, 
                new URL("file:/child2/fake1.url"), child2URI, new URL("file:/some/fake3.url"), URI.create("file:/child2/fake3.url"),
                WorkspaceNodeStatus.NODE_CREATED, Boolean.FALSE, "unknown",
                treeNode, mockWorkspaceDao);
        treeNodeChildren.add(childTreeNode2);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getChild() {
        
        int index = 0;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(treeNode.getWorkspaceNodeID()); will(returnValue(nodeChildren));
        }});
        
        WorkspaceTreeNode retrievedChild = treeNode.getChild(index);
        
        assertEquals("Retrieved child tree node different from expected", treeNodeChildren.get(index), retrievedChild);
    }

    @Test
    public void getChildCount() {
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(treeNode.getWorkspaceNodeID()); will(returnValue(nodeChildren));
        }});
        
        int childCount = treeNode.getChildCount();
        
        assertEquals("Child count different from expected", treeNodeChildren.size(), childCount);
    }

    @Test
    public void getIndexOfChild() {
        
        int index = 1;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(treeNode.getWorkspaceNodeID()); will(returnValue(nodeChildren));
        }});
        
        int retrievedIndex = treeNode.getIndexOfChild(childTreeNode2);
        
        assertEquals("Retrieved index different from expected", index, retrievedIndex);
    }

    @Test
    public void getParent() {
        
        WorkspaceTreeNode retrievedTreeParent = treeNode.getParent();
        
        assertEquals("Retrieved parent tree nodes different from expected", parentTreeNode, retrievedTreeParent);
    }
    
    @Test
    public void getChildren() {
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(treeNode.getWorkspaceNodeID()); will(returnValue(nodeChildren));
        }});
        
        List<WorkspaceTreeNode> retrievedChildren = treeNode.getChildren();
        
        assertEquals("Retrieved list of children different from expected", treeNodeChildren, retrievedChildren);
    }
    
    @Test
    public void isTopNodeOfWorkspace() {
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNodeID(parentTreeNode.getWorkspaceID()); will(returnValue(parentTreeNode.getWorkspaceNodeID()));
        }});
        
        boolean result = parentTreeNode.isTopNodeOfWorkspace();
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void isNotTopNodeOfWorkspace() {
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNodeID(treeNode.getWorkspaceID()); will(returnValue(parentTreeNode.getWorkspaceNodeID()));
        }});
        
        boolean result = treeNode.isTopNodeOfWorkspace();
        
        assertFalse("Result should be false", result);
    }
}
