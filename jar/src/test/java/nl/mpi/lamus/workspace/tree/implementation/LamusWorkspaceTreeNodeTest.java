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

import nl.mpi.lamus.workspace.tree.implementation.LamusWorkspaceTreeNode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.junit.Assert.assertEquals;
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
    

    public LamusWorkspaceTreeNodeTest() throws MalformedURLException, URISyntaxException {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws URISyntaxException, MalformedURLException {
        
        URI parentNodeURI = new URI(UUID.randomUUID().toString());
        parentTreeNode = new LamusWorkspaceTreeNode(1, 1, new URI("file:/parent.uri"),
                "parentName", "parent title", WorkspaceNodeType.UNKNOWN, 
                new URL("file:/parent/fake1.url"), parentNodeURI, new URL("file:/some/fake3.url"), new URL("file:/parent/fake3.url"),
                WorkspaceNodeStatus.NODE_CREATED, "unknown",
                null, mockWorkspaceDao);
        
        URI nodeURI = new URI(UUID.randomUUID().toString());
        treeNode = new LamusWorkspaceTreeNode(1, 10, new URI("file:/some.uri"),
                "nodeName", "node title", WorkspaceNodeType.UNKNOWN, 
                new URL("file:/some/fake1.url"), nodeURI, new URL("file:/some/fake3.url"), new URL("file:/some/fake3.url"),
                WorkspaceNodeStatus.NODE_CREATED, "unknown",
                parentTreeNode, mockWorkspaceDao);
        
        nodeChildren = new ArrayList<WorkspaceNode>();
        treeNodeChildren = new ArrayList<WorkspaceTreeNode>();
        
        URI child1URI = new URI(UUID.randomUUID().toString());
        child1 = new LamusWorkspaceNode(1, 20, new URI("file:/child1.uri"),
                "child1Name", "child1 title", WorkspaceNodeType.UNKNOWN, 
                new URL("file:/child1/fake1.url"), child1URI, new URL("file:/some/fake3.url"), new URL("file:/child1/fake3.url"),
                WorkspaceNodeStatus.NODE_CREATED, "unknown");
        nodeChildren.add(child1);
        childTreeNode1 = new LamusWorkspaceTreeNode(1, 20, new URI("file:/child1.uri"),
                "child1Name", "child1 title", WorkspaceNodeType.UNKNOWN, 
                new URL("file:/child1/fake1.url"), child1URI, new URL("file:/some/fake3.url"), new URL("file:/child1/fake3.url"),
                WorkspaceNodeStatus.NODE_CREATED, "unknown",
                treeNode, mockWorkspaceDao);
        treeNodeChildren.add(childTreeNode1);
        
        URI child2URI = new URI(UUID.randomUUID().toString());
        child2 = new LamusWorkspaceNode(1, 21, new URI("file:/child2.uri"),
                "child2Name", "child2 title", WorkspaceNodeType.UNKNOWN, 
                new URL("file:/child2/fake1.url"), child2URI, new URL("file:/some/fake3.url"), new URL("file:/child2/fake3.url"),
                WorkspaceNodeStatus.NODE_CREATED, "unknown");
        nodeChildren.add(child2);
        childTreeNode2 = new LamusWorkspaceTreeNode(1, 21, new URI("file:/child2.uri"),
                "child2Name", "child2 title", WorkspaceNodeType.UNKNOWN, 
                new URL("file:/child2/fake1.url"), child2URI, new URL("file:/some/fake3.url"), new URL("file:/child2/fake3.url"),
                WorkspaceNodeStatus.NODE_CREATED, "unknown",
                treeNode, mockWorkspaceDao);
        treeNodeChildren.add(childTreeNode2);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getChild method, of class LamusWorkspaceTreeNode.
     */
    @Test
    public void testGetChild() {
        
        int index = 0;
        
        context.checking(new Expectations() {{
            
//            oneOf(mockWorkspaceDao).getChildWorkspaceTreeNodes(treeNode); will(returnValue(treeNodeChildren));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(treeNode.getWorkspaceNodeID()); will(returnValue(nodeChildren));
        }});
        
        WorkspaceTreeNode retrievedChild = treeNode.getChild(index);
        
        assertEquals("Retrieved child tree node different from expected", treeNodeChildren.get(index), retrievedChild);
        
        context.checking(new Expectations() {{
            
            never(mockWorkspaceDao).getChildWorkspaceNodes(treeNode.getWorkspaceNodeID());
        }});
        
        treeNode.getChild(index);
    }

    /**
     * Test of getChildCount method, of class LamusWorkspaceTreeNode.
     */
    @Test
    public void testGetChildCount() {
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(treeNode.getWorkspaceNodeID()); will(returnValue(nodeChildren));
        }});
        
        int childCount = treeNode.getChildCount();
        
        assertEquals("Child count different from expected", treeNodeChildren.size(), childCount);
        
        context.checking(new Expectations() {{
            
            never(mockWorkspaceDao).getChildWorkspaceNodes(treeNode.getWorkspaceNodeID());
        }});
        
        treeNode.getChildCount();
    }

    /**
     * Test of getIndexOfChild method, of class LamusWorkspaceTreeNode.
     */
    @Test
    public void testGetIndexOfChild() {
        
        int index = 1;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(treeNode.getWorkspaceNodeID()); will(returnValue(nodeChildren));
        }});
        
        int retrievedIndex = treeNode.getIndexOfChild(childTreeNode2);
        
        assertEquals("Retrieved index different from expected", index, retrievedIndex);
        
        context.checking(new Expectations() {{
            
            never(mockWorkspaceDao).getChildWorkspaceNodes(treeNode.getWorkspaceNodeID());
        }});
        
        treeNode.getIndexOfChild(childTreeNode2);
    }

    /**
     * Test of getParent method, of class LamusWorkspaceTreeNode.
     */
    @Test
    public void testGetParent() {
        
        WorkspaceTreeNode retrievedTreeParent = treeNode.getParent();
        
        assertEquals("Retrieved parent tree nodes different from expected", parentTreeNode, retrievedTreeParent);
    }
}
