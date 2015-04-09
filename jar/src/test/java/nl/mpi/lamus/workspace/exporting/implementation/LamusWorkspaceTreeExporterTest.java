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
import nl.mpi.lamus.workspace.model.WorkspaceExportPhase;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;
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
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        final int workspaceNodeID = 10;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URI nodeOriginURI = URI.create("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURI.toURL();
        final URI nodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final String nodeFormat = "";
        final URI schemaLocation = URI.create("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(workspaceNodeID, workspaceID, schemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURI, WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, nodeFormat);
        
        final int childWorkspaceNodeID = 20;
        final URL childWsURL = new URL("file://workspace/folder/someOtherName.pdf");
        final URI childOriginURI = URI.create("file://some/different/local/folder/someOtherName.pdf");
        final String childNodeName = "someOtherName";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childNodeFormat = "";
        final WorkspaceNode childNode = new LamusWorkspaceNode(childWorkspaceNodeID, workspaceID, schemaLocation,
                childNodeName, "", childNodeType, childWsURL, null, null, childOriginURI, WorkspaceNodeStatus.NODE_UPLOADED, Boolean.FALSE, childNodeFormat);
        
        final Collection<WorkspaceNode> children = new ArrayList<>();
        children.add(childNode);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(node.getWorkspaceNodeID()); will(returnValue(children));
            
            oneOf(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, childNode, exportPhase); will(returnValue(mockNodeExporter));
            oneOf(mockNodeExporter).exportNode(mockWorkspace, node, childNode, keepUnlinkedFiles, submissionType, exportPhase);
            
        }});
        
        workspaceTreeExporter.explore(mockWorkspace, node, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exploreThrowsException() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final int workspaceID = 1;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        final int workspaceNodeID = 10;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URI nodeOriginURI = URI.create("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURI.toURL();
        final URI nodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final String nodeFormat = "";
        final URI schemaLocation = URI.create("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(workspaceNodeID, workspaceID, schemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURI, WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, nodeFormat);
        
        final int childWorkspaceNodeID = 20;
        final URL childWsURL = new URL("file://workspace/folder/someOtherName.pdf");
        final URI childOriginURI = URI.create("file://some/different/local/folder/someOtherName.pdf");
        final String childNodeName = "someOtherName";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childNodeFormat = "";
        final WorkspaceNode childNode = new LamusWorkspaceNode(childWorkspaceNodeID, workspaceID, schemaLocation,
                childNodeName, "", childNodeType, childWsURL, null, null, childOriginURI, WorkspaceNodeStatus.NODE_UPLOADED, Boolean.FALSE, childNodeFormat);
        
        final Collection<WorkspaceNode> children = new ArrayList<>();
        children.add(childNode);
        
        final WorkspaceExportException expectedException = new WorkspaceExportException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(node.getWorkspaceNodeID()); will(returnValue(children));
            
            oneOf(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, childNode, exportPhase); will(returnValue(mockNodeExporter));
            oneOf(mockNodeExporter).exportNode(mockWorkspace, node, childNode, keepUnlinkedFiles, submissionType, exportPhase);
                will(throwException(expectedException));
            
        }});
        
        try {
            workspaceTreeExporter.explore(mockWorkspace, node, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    public void exportWithExternalChild() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final int workspaceID = 1;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        final int workspaceNodeID = 10;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URI nodeOriginURI = URI.create("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURI.toURL();
        final URI nodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final String nodeFormat = "";
        final URI schemaLocation = URI.create("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(workspaceNodeID, workspaceID, schemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURI, WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, nodeFormat);
        
        final int childWorkspaceNodeID = 20;
        final URL childWsURL = new URL("file://workspace/folder/someOtherName.pdf");
        final URI childOriginURI = URI.create("file://some/different/local/folder/someOtherName.pdf");
        final String childNodeName = "someOtherName";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childNodeFormat = "application/pdf";
        final WorkspaceNode childNode = new LamusWorkspaceNode(childWorkspaceNodeID, workspaceID, schemaLocation,
                childNodeName, "", childNodeType, childWsURL, null, null, childOriginURI, WorkspaceNodeStatus.NODE_UPLOADED, Boolean.FALSE, childNodeFormat);
        
        final int externalWorkspaceNodeID = 30;
        final String externalNodeName = "externalNode";
        final WorkspaceNodeType externalNodeType = WorkspaceNodeType.RESOURCE_IMAGE;
        final String externalNodeFormat = "image/jpeg";
        final WorkspaceNode externalNode = new LamusWorkspaceNode(externalWorkspaceNodeID, workspaceID, schemaLocation,
                externalNodeName, "", externalNodeType, null, null, null, childOriginURI, WorkspaceNodeStatus.NODE_EXTERNAL, Boolean.FALSE, externalNodeFormat);
        
        final Collection<WorkspaceNode> children = new ArrayList<>();
        children.add(childNode);
        children.add(externalNode);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(node.getWorkspaceNodeID()); will(returnValue(children));
            
            oneOf(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, childNode, exportPhase); will(returnValue(mockNodeExporter));
            oneOf(mockNodeExporter).exportNode(mockWorkspace, node, childNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            // should leave the loop for the second node because it's external and therefore doesn't require exporting
            never(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, childNode, exportPhase); will(returnValue(mockNodeExporter));
            never(mockNodeExporter).exportNode(mockWorkspace, node, childNode, keepUnlinkedFiles, submissionType, exportPhase);
        }});
        
        workspaceTreeExporter.explore(mockWorkspace, node, keepUnlinkedFiles, submissionType, exportPhase);
    }
}