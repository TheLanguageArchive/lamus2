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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.net.MalformedURLException;
import java.util.Collection;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceExportPhase;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.metadata.api.MetadataAPI;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
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
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusNodeExporterFactoryTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock ArchiveFileLocationProvider mockArchiveFileLocationProvider;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock WorkspaceTreeExporter mockWorkspaceTreeExporter;
    @Mock WorkspaceDao mockWorkspaceDao;
    
    @Mock AddedNodeExporter mockAddedNodeExporter;
    @Mock ReplacedOrDeletedNodeExporter mockReplacedOrDeletedNodeExporter;
    @Mock GeneralNodeExporter mockGeneralNodeExporter;
    @Mock UnlinkedNodeExporter mockUnlinkedNodeExporter;
    
    @Mock Workspace mockWorkspace;
    @Mock WorkspaceNode mockNode;
    @Mock Collection<WorkspaceNode> mockParentNodes;
    
    private NodeExporterFactory exporterFactory;
    
    
    public LamusNodeExporterFactoryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        exporterFactory = new LamusNodeExporterFactory();
        ReflectionTestUtils.setField(exporterFactory, "workspaceDao", mockWorkspaceDao);
        ReflectionTestUtils.setField(exporterFactory, "addedNodeExporter", mockAddedNodeExporter);
        ReflectionTestUtils.setField(exporterFactory, "replacedOrDeletedNodeExporter", mockReplacedOrDeletedNodeExporter);
        ReflectionTestUtils.setField(exporterFactory, "generalNodeExporter", mockGeneralNodeExporter);
        ReflectionTestUtils.setField(exporterFactory, "unlinkedNodeExporter", mockUnlinkedNodeExporter);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void getNodeExporterForTopNode() throws MalformedURLException {
        
        final int topNodeID = 1;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(topNodeID));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, mockNode, WorkspaceExportPhase.TREE_EXPORT);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof GeneralNodeExporter);
        assertEquals("Retrieved node exporter different from expected", mockGeneralNodeExporter, retrievedExporter);
    }
    
    @Test
    public void getNodeExporterForUnlinkedNode() throws MalformedURLException {
        
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_UPLOADED;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            allowing(mockNode).getWorkspaceNodeID(); will(returnValue(workspaceNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.TRUE));
            allowing(mockNode).getStatus(); will(returnValue(nodeStatus));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, mockNode, WorkspaceExportPhase.UNLINKED_NODES_EXPORT);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof UnlinkedNodeExporter);
        assertEquals("Retrieved node exporter different from expected", mockUnlinkedNodeExporter, retrievedExporter);
    }
    
    @Test
    public void getNodeExporterForDescendantOfUnlinkedNode() throws MalformedURLException {
        
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_UPLOADED;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            allowing(mockNode).getWorkspaceNodeID(); will(returnValue(workspaceNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.FALSE));
            allowing(mockNode).getStatus(); will(returnValue(nodeStatus));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, mockNode, WorkspaceExportPhase.UNLINKED_NODES_EXPORT);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof UnlinkedNodeExporter);
        assertEquals("Retrieved node exporter different from expected", mockUnlinkedNodeExporter, retrievedExporter);
    }

    @Test
    public void getNodeExporterForUploadedNode() throws MalformedURLException {
        
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_UPLOADED;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            allowing(mockNode).getWorkspaceNodeID(); will(returnValue(workspaceNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.FALSE));
            allowing(mockNode).getStatus(); will(returnValue(nodeStatus));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, mockNode, WorkspaceExportPhase.TREE_EXPORT);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof AddedNodeExporter);
        assertEquals("Retrieved node exporter different from expected", mockAddedNodeExporter, retrievedExporter);
    }
    
    @Test
    public void getNodeExporterForCreatedNode() throws MalformedURLException {
        
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_CREATED;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            allowing(mockNode).getWorkspaceNodeID(); will(returnValue(workspaceNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.FALSE));
            allowing(mockNode).getStatus(); will(returnValue(nodeStatus));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, mockNode, WorkspaceExportPhase.TREE_EXPORT);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof AddedNodeExporter);
        assertEquals("Retrieved node exporter different from expected", mockAddedNodeExporter, retrievedExporter);
    }

    @Test
    public void getNodeExporterForDeletedNode() throws MalformedURLException {
        
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_DELETED;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            allowing(mockNode).getWorkspaceNodeID(); will(returnValue(workspaceNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.TRUE));
            allowing(mockNode).getStatus(); will(returnValue(nodeStatus));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, mockNode, WorkspaceExportPhase.UNLINKED_NODES_EXPORT);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof ReplacedOrDeletedNodeExporter);
        assertEquals("Retrieved node exporter different from expected", mockReplacedOrDeletedNodeExporter, retrievedExporter);
    }
    
    @Test
    public void getNodeExporterForExternalDeletedNode() throws MalformedURLException {
        
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_EXTERNAL_DELETED;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            allowing(mockNode).getWorkspaceNodeID(); will(returnValue(workspaceNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.TRUE));
            allowing(mockNode).getStatus(); will(returnValue(nodeStatus));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, mockNode, WorkspaceExportPhase.UNLINKED_NODES_EXPORT);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof ReplacedOrDeletedNodeExporter);
        assertEquals("Retrieved node exporter different from expected", mockReplacedOrDeletedNodeExporter, retrievedExporter);
    }
    
    @Test
    public void getNodeExporterForReplacedNode() throws MalformedURLException {
        
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_REPLACED;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            allowing(mockNode).getWorkspaceNodeID(); will(returnValue(workspaceNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.TRUE));
            allowing(mockNode).getStatus(); will(returnValue(nodeStatus));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, mockNode, WorkspaceExportPhase.TREE_EXPORT);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof ReplacedOrDeletedNodeExporter);
        assertEquals("Retrieved node exporter different from expected", mockReplacedOrDeletedNodeExporter, retrievedExporter);
    }
    
    @Test
    public void getNodeExporterForChangedNode() throws MalformedURLException {
        
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            allowing(mockNode).getWorkspaceNodeID(); will(returnValue(workspaceNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.FALSE));
            allowing(mockNode).getStatus(); will(returnValue(nodeStatus));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, mockNode, WorkspaceExportPhase.TREE_EXPORT);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof GeneralNodeExporter);
        assertEquals("Retrieved node exporter different from expected", mockGeneralNodeExporter, retrievedExporter);
    }
}