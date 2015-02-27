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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.UnlinkedAndDeletedNodesExportHandler;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
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

/**
 *
 * @author guisil
 */
public class LamusUnlinkedAndDeletedNodesExportHandlerTest {
    
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    private UnlinkedAndDeletedNodesExportHandler unlinkedAndDeletedNodesExportHandler;
    
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock NodeExporterFactory mockNodeExporterFactory;
    
    @Mock NodeExporter mockNodeExporter;
    @Mock Workspace mockWorkspace;
    
    
    public LamusUnlinkedAndDeletedNodesExportHandlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        unlinkedAndDeletedNodesExportHandler = new LamusUnlinkedAndDeletedNodesExportHandler(mockWorkspaceDao, mockNodeExporterFactory);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void explore_ExportPhaseTree() throws WorkspaceExportException {
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
    
        try {
            unlinkedAndDeletedNodesExportHandler.exploreUnlinkedAndDeletedNodes(mockWorkspace, keepUnlinkedFiles, submissionType, exportPhase);
        } catch(IllegalArgumentException ex) {
            String errorMessage = "This stage of the export (handling unlinked and deleted nodes) should not be called during the tree export (current 'exportPhase': " + exportPhase.toString();
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    
    @Test
    public void exploreDeletedTopNodes() throws MalformedURLException, WorkspaceExportException {

        final int workspaceID = 1;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        final Collection<WorkspaceNode> unlinkedAndDeletedTopNodes = new ArrayList<>();
        
        final int firstNodeID = 10;
        final URL firstNodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URI firstNodeOriginURI = URI.create("file:/some.url/someName.cmdi");
        final URL firstNodeArchiveURL = firstNodeOriginURI.toURL();
        final URI firstNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String firstNodeDisplayValue = "someName";
        final WorkspaceNodeType firstNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String firstNodeFormat = "";
        final URI firstNodeSchemaLocation = URI.create("http://some.location");
        final WorkspaceNode firstNode = new LamusWorkspaceNode(firstNodeID, workspaceID, firstNodeSchemaLocation,
                firstNodeDisplayValue, "", firstNodeType, firstNodeWsURL, firstNodeURI, firstNodeArchiveURL, firstNodeOriginURI,
                WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, firstNodeFormat);
        
        final int secondNodeID = 10;
        final URL secondNodeWsURL = new URL("file:/workspace/folder/node.cmdi");
        final URI secondNodeOriginURI = URI.create("file:/some.url/node.cmdi");
        final URL secondNodeArchiveURL = secondNodeOriginURI.toURL();
        final URI secondNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String secondNodeDisplayValue = "someName";
        final WorkspaceNodeType secondNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String secondNodeFormat = "";
        final URI secondNodeSchemaLocation = URI.create("http://some.location");
        final WorkspaceNode secondNode = new LamusWorkspaceNode(secondNodeID, workspaceID, secondNodeSchemaLocation,
                secondNodeDisplayValue, "", secondNodeType, secondNodeWsURL, secondNodeURI, secondNodeArchiveURL, secondNodeOriginURI,
                WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, secondNodeFormat);
        
        unlinkedAndDeletedTopNodes.add(firstNode);
        unlinkedAndDeletedTopNodes.add(secondNode);
        
    
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspaceDao).getUnlinkedAndDeletedTopNodes(workspaceID); will(returnValue(unlinkedAndDeletedTopNodes));
        }});
        
        for(final WorkspaceNode deletedNode : unlinkedAndDeletedTopNodes) {
            
            context.checking(new Expectations() {{
                oneOf(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, deletedNode, exportPhase); will(returnValue(mockNodeExporter));
                oneOf(mockNodeExporter).exportNode(mockWorkspace, null, deletedNode, keepUnlinkedFiles, submissionType, exportPhase);
            }});
        }
    
        unlinkedAndDeletedNodesExportHandler.exploreUnlinkedAndDeletedNodes(mockWorkspace, keepUnlinkedFiles, submissionType, exportPhase);
    }

    @Test
    public void exploreDeletedTopNodesThrowsException() throws MalformedURLException, WorkspaceExportException {

        final int workspaceID = 1;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        final Collection<WorkspaceNode> unlinkedAndDeletedTopNodes = new ArrayList<>();
        
        final int firstNodeID = 10;
        final URL firstNodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URI firstNodeOriginURI = URI.create("file:/some.url/someName.cmdi");
        final URL firstNodeArchiveURL = firstNodeOriginURI.toURL();
        final URI firstNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String firstNodeDisplayValue = "someName";
        final WorkspaceNodeType firstNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String firstNodeFormat = "";
        final URI firstNodeSchemaLocation = URI.create("http://some.location");
        final WorkspaceNode firstNode = new LamusWorkspaceNode(firstNodeID, workspaceID, firstNodeSchemaLocation,
                firstNodeDisplayValue, "", firstNodeType, firstNodeWsURL, firstNodeURI, firstNodeArchiveURL, firstNodeOriginURI,
                WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, firstNodeFormat);
        
        final int secondNodeID = 10;
        final URL secondNodeWsURL = new URL("file:/workspace/folder/node.cmdi");
        final URI secondNodeOriginURI = URI.create("file:/some.url/node.cmdi");
        final URL secondNodeArchiveURL = secondNodeOriginURI.toURL();
        final URI secondNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String secondNodeDisplayValue = "someName";
        final WorkspaceNodeType secondNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String secondNodeFormat = "";
        final URI secondNodeSchemaLocation = URI.create("http://some.location");
        final WorkspaceNode secondNode = new LamusWorkspaceNode(secondNodeID, workspaceID, secondNodeSchemaLocation,
                secondNodeDisplayValue, "", secondNodeType, secondNodeWsURL, secondNodeURI, secondNodeArchiveURL, secondNodeOriginURI,
                WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, secondNodeFormat);
        
        unlinkedAndDeletedTopNodes.add(firstNode);
        unlinkedAndDeletedTopNodes.add(secondNode);
        
        final WorkspaceExportException expectedException = new WorkspaceExportException("some exception message", workspaceID, null);
        
    
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspaceDao).getUnlinkedAndDeletedTopNodes(workspaceID); will(returnValue(unlinkedAndDeletedTopNodes));
        }});
        
        for(final WorkspaceNode deletedNode : unlinkedAndDeletedTopNodes) {
            
            context.checking(new Expectations() {{
                oneOf(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, deletedNode, exportPhase); will(returnValue(mockNodeExporter));
                oneOf(mockNodeExporter).exportNode(mockWorkspace, null, deletedNode, keepUnlinkedFiles, submissionType, exportPhase);
                    will(throwException(expectedException));
            }});
            
            break; // throws exception, so the loop doesn't continue
        }
    
        try {
            unlinkedAndDeletedNodesExportHandler.exploreUnlinkedAndDeletedNodes(mockWorkspace, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
}