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

import java.net.URI;
import java.net.URL;
import nl.mpi.lamus.archive.ArchiveHandleHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.workspace.exporting.WorkspaceCorpusStructureExporter;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.tree.implementation.LamusWorkspaceTreeNode;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.management.WorkspaceNodeManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.replace.implementation.LamusNodeReplaceManager;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceTreeServiceTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private WorkspaceTreeService service;
    @Mock private WorkspaceAccessChecker mockNodeAccessChecker;
    @Mock private ArchiveHandleHelper mockArchivePidHelper;
    @Mock private WorkspaceManager mockWorkspaceManager;
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private WorkspaceUploader mockWorkspaceUploader;
    @Mock private WorkspaceNodeLinkManager mockWorkspaceNodeLinkManager;
    @Mock private WorkspaceNodeManager mockWorkspaceNodeManager;
    @Mock private LamusNodeReplaceManager mockTopNodeReplaceManager;
    @Mock private WorkspaceCorpusStructureExporter mockWorkspaceCorpusStructureExporter;
    
    public LamusWorkspaceTreeServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        service = new LamusWorkspaceTreeService(
                mockNodeAccessChecker, mockArchivePidHelper, mockWorkspaceManager, mockWorkspaceDao,
                mockWorkspaceUploader, mockWorkspaceNodeLinkManager,
                mockWorkspaceNodeManager, mockTopNodeReplaceManager,
                mockWorkspaceCorpusStructureExporter);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getTreeNode method, of class LamusWorkspaceTreeService.
     */
    @Test
    public void testGetTreeNodeWithoutParent() throws WorkspaceNodeNotFoundException {

        final int nodeID = 1;
        final int workspaceID = 1;
        URI profileSchemaURI = null;
        String name = "node_name";
        String title = "node_title";
        WorkspaceNodeType type = WorkspaceNodeType.METADATA;
        URL wsURL = null;
        URI archiveURI = null;
        URL archiveURL = null;
        URI originURI = null;
        WorkspaceNodeStatus status = WorkspaceNodeStatus.ARCHIVE_COPY;
        boolean isProtected = Boolean.FALSE;
        String format = "cmdi";
        
        final WorkspaceNode node = new LamusWorkspaceNode(
                nodeID, workspaceID, profileSchemaURI,
                name, title, type, wsURL, archiveURI,
                archiveURL, originURI, status, isProtected, format);
        
        final WorkspaceTreeNode treeNodeToRetrieve = new LamusWorkspaceTreeNode(
                node, null, mockWorkspaceDao);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceNode(nodeID); will(returnValue(node));
        }});
        
        WorkspaceTreeNode result = service.getTreeNode(nodeID, null);
        assertNotNull("Returned tree node should not be null", result);
        assertEquals("Returned tree node is different from expected", result, treeNodeToRetrieve);
        assertNull("Returned tree node should have a null parent tree node.", result.getParent());
    }
    
    /**
     * Test of getTreeNode method, of class LamusWorkspaceTreeService.
     */
    @Test
    public void testGetTreeNodeWithParent() throws WorkspaceNodeNotFoundException {

        final int parentNodeID = 0;
        final int workspaceID = 1;
        String parentName = "parent_name";
        String parentTitle = "parent_title";
                
        final int nodeID = 1;
        URI profileSchemaURI = null;
        String name = "node_name";
        String title = "node_title";
        WorkspaceNodeType type = WorkspaceNodeType.METADATA;
        URL wsURL = null;
        URI archiveURI = null;
        URL archiveURL = null;
        URI originURI = null;
        WorkspaceNodeStatus status = WorkspaceNodeStatus.ARCHIVE_COPY;
        boolean isProtected = Boolean.FALSE;
        String format = "cmdi";

        final WorkspaceTreeNode parentTreeNode = new LamusWorkspaceTreeNode(
                parentNodeID, workspaceID, profileSchemaURI,
                parentName, parentTitle, type, wsURL, archiveURI,
                archiveURL, originURI, status, isProtected, format, null, mockWorkspaceDao);
        
        final WorkspaceNode node = new LamusWorkspaceNode(
                nodeID, workspaceID, profileSchemaURI,
                name, title, type, wsURL, archiveURI,
                archiveURL, originURI, status, isProtected, format);
        
        final WorkspaceTreeNode treeNodeToRetrieve = new LamusWorkspaceTreeNode(
                node, parentTreeNode, mockWorkspaceDao);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceNode(nodeID); will(returnValue(node));
        }});
        
        WorkspaceTreeNode result = service.getTreeNode(nodeID, parentTreeNode);
        assertNotNull("Returned tree node should not be null", result);
        assertEquals("Returned tree node is different from expected", treeNodeToRetrieve, result);
        assertNotNull("Returned tree node should have a null parent tree node.", result.getParent());
        assertEquals("The parent tree node of the returned tree node is different from expected", parentTreeNode, result.getParent());
    }
    
    //TODO test exception possibilities and other methods
}
