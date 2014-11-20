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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
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
public class LamusWorkspaceTreeExporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    private WorkspaceTreeExporter workspaceTreeExporter;
    
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock NodeExporterFactory mockNodeExporterFactory;
    
    @Mock NodeExporter mockNodeExporter;
    @Mock Workspace mockWorkspace;
    
    public LamusWorkspaceTreeExporterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        workspaceTreeExporter = new LamusWorkspaceTreeExporter();
        ReflectionTestUtils.setField(workspaceTreeExporter, "workspaceDao", mockWorkspaceDao);
        ReflectionTestUtils.setField(workspaceTreeExporter, "nodeExporterFactory", mockNodeExporterFactory);
    }
    
    @After
    public void tearDown() {
    }


    
    @Test
    public void explore() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final int workspaceID = 1;
        final int workspaceNodeID = 10;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URL nodeOriginURL = new URL("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURL;
        final URI nodeURI = new URI(UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "";
        final URI schemaLocation = new URI("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(workspaceNodeID, workspaceID, schemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURL, WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, nodeFormat);
        
        final int childWorkspaceNodeID = 20;
        final URL childWsURL = new URL("file://workspace/folder/someOtherName.pdf");
        final URL childOriginURL = new URL("file://some/different/local/folder/someOtherName.pdf");
        final String childNodeName = "someOtherName";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE;
        final String childNodeFormat = "";
        final WorkspaceNode childNode = new LamusWorkspaceNode(childWorkspaceNodeID, workspaceID, schemaLocation,
                childNodeName, "", childNodeType, childWsURL, null, null, childOriginURL, WorkspaceNodeStatus.NODE_UPLOADED, Boolean.FALSE, childNodeFormat);
        
        final Collection<WorkspaceNode> children = new ArrayList<>();
        children.add(childNode);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(node.getWorkspaceNodeID()); will(returnValue(children));
            
            //TODO FOR EACH CHILD NODE, GET THE PROPER EXPORTER AND CALL IT
            oneOf(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, childNode); will(returnValue(mockNodeExporter));
            oneOf(mockNodeExporter).exportNode(mockWorkspace, node, childNode);
            
        }});
        
        workspaceTreeExporter.explore(mockWorkspace, node);
    }
    
    @Test
    public void exploreThrowsException() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final int workspaceID = 1;
        final int workspaceNodeID = 10;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URL nodeOriginURL = new URL("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURL;
        final URI nodeURI = new URI(UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "";
        final URI schemaLocation = new URI("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(workspaceNodeID, workspaceID, schemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURL, WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, nodeFormat);
        
        final int childWorkspaceNodeID = 20;
        final URL childWsURL = new URL("file://workspace/folder/someOtherName.pdf");
        final URL childOriginURL = new URL("file://some/different/local/folder/someOtherName.pdf");
        final String childNodeName = "someOtherName";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE;
        final String childNodeFormat = "";
        final WorkspaceNode childNode = new LamusWorkspaceNode(childWorkspaceNodeID, workspaceID, schemaLocation,
                childNodeName, "", childNodeType, childWsURL, null, null, childOriginURL, WorkspaceNodeStatus.NODE_UPLOADED, Boolean.FALSE, childNodeFormat);
        
        final Collection<WorkspaceNode> children = new ArrayList<>();
        children.add(childNode);
        
        final WorkspaceExportException expectedException = new WorkspaceExportException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(node.getWorkspaceNodeID()); will(returnValue(children));
            
            //TODO FOR EACH CHILD NODE, GET THE PROPER EXPORTER AND CALL IT
            oneOf(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, childNode); will(returnValue(mockNodeExporter));
            oneOf(mockNodeExporter).exportNode(mockWorkspace, node, childNode);
                will(throwException(expectedException));
            
        }});
        
        try {
            workspaceTreeExporter.explore(mockWorkspace, node);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    public void exportWithExternalChild() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final int workspaceID = 1;
        final int workspaceNodeID = 10;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URL nodeOriginURL = new URL("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURL;
        final URI nodeURI = new URI(UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "";
        final URI schemaLocation = new URI("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(workspaceNodeID, workspaceID, schemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURL, WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, nodeFormat);
        
        final int childWorkspaceNodeID = 20;
        final URL childWsURL = new URL("file://workspace/folder/someOtherName.pdf");
        final URL childOriginURL = new URL("file://some/different/local/folder/someOtherName.pdf");
        final String childNodeName = "someOtherName";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE;
        final String childNodeFormat = "application/pdf";
        final WorkspaceNode childNode = new LamusWorkspaceNode(childWorkspaceNodeID, workspaceID, schemaLocation,
                childNodeName, "", childNodeType, childWsURL, null, null, childOriginURL, WorkspaceNodeStatus.NODE_UPLOADED, Boolean.FALSE, childNodeFormat);
        
        final int externalWorkspaceNodeID = 30;
        final URL externalOriginURL = new URL("http://some/remote/location/externalNode.jpg");
        final String externalNodeName = "externalNode";
        final WorkspaceNodeType externalNodeType = WorkspaceNodeType.RESOURCE;
        final String externalNodeFormat = "image/jpeg";
        final WorkspaceNode externalNode = new LamusWorkspaceNode(externalWorkspaceNodeID, workspaceID, schemaLocation,
                externalNodeName, "", externalNodeType, null, null, null, childOriginURL, WorkspaceNodeStatus.NODE_EXTERNAL, Boolean.FALSE, externalNodeFormat);
        
        final Collection<WorkspaceNode> children = new ArrayList<>();
        children.add(childNode);
        children.add(externalNode);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(node.getWorkspaceNodeID()); will(returnValue(children));
            
            //TODO FOR EACH CHILD NODE, GET THE PROPER EXPORTER AND CALL IT
            oneOf(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, childNode); will(returnValue(mockNodeExporter));
            oneOf(mockNodeExporter).exportNode(mockWorkspace, node, childNode);
            
            // should leave the loop for the second node because it's external and therefore doesn't require exporting
            never(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, childNode); will(returnValue(mockNodeExporter));
            never(mockNodeExporter).exportNode(mockWorkspace, node, childNode);
        }});
        
        workspaceTreeExporter.explore(mockWorkspace, node);
    }
}