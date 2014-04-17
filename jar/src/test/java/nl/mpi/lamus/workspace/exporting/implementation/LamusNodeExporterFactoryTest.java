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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
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
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusNodeExporterFactoryTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock SearchClientBridge mockSearchClientBridge;
    @Mock ArchiveFileLocationProvider mockArchiveFileLocationProvider;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock WorkspaceTreeExporter mockWorkspaceTreeExporter;
    @Mock WorkspaceDao mockWorkspaceDao;
    
    @Mock Workspace mockWorkspace;
    @Mock Collection<WorkspaceNode> mockParentNodes;
    
    private NodeExporterFactory exporterFactory;
    
    private Workspace workspace;
    
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
        
        workspace = new LamusWorkspace(1, "someUser", -1, null, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.SUBMITTED, "Workspace submitted", "archiveInfo/something");
        
        exporterFactory = new LamusNodeExporterFactory();
        ReflectionTestUtils.setField(exporterFactory, "workspaceDao", mockWorkspaceDao);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void getNodeExporterForTopNode() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 1;
        final int topNodeID = 1;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URL nodeOriginURL = new URL("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURL;
        final URI nodeURI = new URI(UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "";
        final URI nodeSchemaLocation = new URI("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(topNodeID, workspaceID, nodeSchemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURL, WorkspaceNodeStatus.NODE_UPLOADED, nodeFormat);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, node);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof GeneralNodeExporter);
        assertEquals("Workspace set in exporter is different from expected", mockWorkspace, retrievedExporter.getWorkspace());
    }
    
    @Test
    public void getNodeExporterForUnlinkedNode() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 1;
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URL nodeOriginURL = new URL("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURL;
        final URI nodeURI = new URI(UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "";
        final URI nodeSchemaLocation = new URI("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(workspaceNodeID, workspaceID, nodeSchemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURL, WorkspaceNodeStatus.NODE_UPLOADED, nodeFormat);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.TRUE));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, node);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof UnlinkedNodeExporter);
        assertEquals("Workspace set in exporter is different from expected", mockWorkspace, retrievedExporter.getWorkspace());
    }

    @Test
    public void getNodeExporterForUploadedNode() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 1;
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URL nodeOriginURL = new URL("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURL;
        final URI nodeURI = new URI(UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "";
        final URI nodeSchemaLocation = new URI("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(workspaceNodeID, workspaceID, nodeSchemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURL, WorkspaceNodeStatus.NODE_UPLOADED, nodeFormat);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.FALSE));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, node);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof AddedNodeExporter);
        assertEquals("Workspace set in exporter is different from expected", mockWorkspace, retrievedExporter.getWorkspace());
    }
    
    @Test
    public void getNodeExporterForCreatedNode() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 1;
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URL nodeOriginURL = new URL("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURL;
        final URI nodeURI = new URI(UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "";
        final URI nodeSchemaLocation = new URI("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(workspaceNodeID, workspaceID, nodeSchemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURL, WorkspaceNodeStatus.NODE_CREATED, nodeFormat);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.FALSE));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, node);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof AddedNodeExporter);
        assertEquals("Workspace set in exporter is different from expected", mockWorkspace, retrievedExporter.getWorkspace());
    }

    @Test
    public void getNodeExporterForDeletedNode() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 1;
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URL nodeOriginURL = new URL("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURL;
        final URI nodeURI = new URI(UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "";
        final URI nodeSchemaLocation = new URI("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(workspaceNodeID, workspaceID, nodeSchemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURL, WorkspaceNodeStatus.NODE_DELETED, nodeFormat);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.TRUE));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, node);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof ReplacedOrDeletedNodeExporter);
        assertEquals("Workspace set in exporter is different from expected", mockWorkspace, retrievedExporter.getWorkspace());
    }
    
    @Test
    public void getNodeExporterForReplacedNode() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 1;
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URL nodeOriginURL = new URL("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURL;
        final URI nodeURI = new URI(UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "";
        final URI nodeSchemaLocation = new URI("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(workspaceNodeID, workspaceID, nodeSchemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURL, WorkspaceNodeStatus.NODE_REPLACED, nodeFormat);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.TRUE));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, node);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof ReplacedOrDeletedNodeExporter);
        assertEquals("Workspace set in exporter is different from expected", mockWorkspace, retrievedExporter.getWorkspace());
    }
    
    @Test
    public void getNodeExporterForChangedNode() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 1;
        final int topNodeID = 1;
        final int workspaceNodeID = 10;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URL nodeOriginURL = new URL("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURL;
        final URI nodeURI = new URI(UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "";
        final URI nodeSchemaLocation = new URI("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(workspaceNodeID, workspaceID, nodeSchemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURL, WorkspaceNodeStatus.NODE_ISCOPY, nodeFormat);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getTopNodeID(); will(returnValue(topNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(workspaceNodeID); will(returnValue(mockParentNodes));
            oneOf(mockParentNodes).isEmpty(); will(returnValue(Boolean.FALSE));
        }});
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(mockWorkspace, node);
        
        assertNotNull(retrievedExporter);
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof GeneralNodeExporter);
        assertEquals("Workspace set in exporter is different from expected", mockWorkspace, retrievedExporter.getWorkspace());
    }
}