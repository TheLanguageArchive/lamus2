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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;
import javax.xml.transform.TransformerException;
import net.handle.hdllib.HandleException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.lamus.archive.ArchiveHandleHelper;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.VersioningHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.metadata.api.MetadataException;
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
public class UnlinkedNodeExporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    private NodeExporter unlinkedNodeExporter;
    private Workspace testWorkspace;
    
    @Mock VersioningHandler mockVersioningHandler;
    @Mock ArchiveHandleHelper mockArchiveHandleHelper;
    
    @Mock Workspace mockWorkspace;
    @Mock WorkspaceNode mockWorkspaceNode;
    @Mock CorpusNode mockCorpusNode;
    
    public UnlinkedNodeExporterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        unlinkedNodeExporter = new UnlinkedNodeExporter();
        ReflectionTestUtils.setField(unlinkedNodeExporter, "versioningHandler", mockVersioningHandler);
        ReflectionTestUtils.setField(unlinkedNodeExporter, "archiveHandleHelper", mockArchiveHandleHelper);
        
        testWorkspace = new LamusWorkspace(1, "someUser",  -1, null, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.SUBMITTED, "Workspace submitted", "");
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void exportNodeWithArchiveURI_DoNotKeepUnlinkedFiles()
            throws MalformedURLException, WorkspaceExportException {
        
        final int wsID = 1;
        final int nodeWsID = 10;
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        
        final String nodeVersionArchivePath = "file:/trash/location/r_node.txt";
        final URI nodeVersionArchivePathURI = URI.create(nodeVersionArchivePath);
        final URL nodeVersionArchiveURL = nodeVersionArchivePathURI.toURL();
        
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(wsID));
            oneOf(mockWorkspaceNode).getWorkspaceNodeID(); will(returnValue(nodeWsID));
            
            oneOf(mockWorkspaceNode).isProtected(); will(returnValue(isNodeProtected));
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            
            oneOf(mockVersioningHandler).moveFileToTrashCanFolder(mockWorkspaceNode); will(returnValue(nodeVersionArchiveURL));
            oneOf(mockWorkspaceNode).setArchiveURL(nodeVersionArchiveURL);
            //logger
            oneOf(mockWorkspaceNode).getWorkspaceNodeID(); will(returnValue(nodeWsID));
        }});

        unlinkedNodeExporter.exportNode(mockWorkspace, null, mockWorkspaceNode, keepUnlinkedFiles);
    }
    
    @Test
    public void exportNodeWithArchiveURI_KeepUnlinkedFiles()
            throws MalformedURLException, WorkspaceExportException,
            HandleException, IOException, TransformerException, MetadataException {
        
        final int wsID = 1;
        final int nodeWsID = 10;
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        
        final String nodeFilename = "node.cmdi";
        final URL newNodeLocation = new URL("file:/archive/some/location/sessions/" + nodeFilename);
        
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(wsID));
            oneOf(mockWorkspaceNode).getWorkspaceNodeID(); will(returnValue(nodeWsID));
            
            oneOf(mockWorkspaceNode).isProtected(); will(returnValue(isNodeProtected));
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            
            oneOf(mockVersioningHandler).moveFileToOrphansFolder(mockWorkspace, mockWorkspaceNode); will(returnValue(newNodeLocation));
            
            oneOf(mockArchiveHandleHelper).deleteArchiveHandle(mockWorkspaceNode, Boolean.FALSE);
        }});

        unlinkedNodeExporter.exportNode(mockWorkspace, null, mockWorkspaceNode, keepUnlinkedFiles);
    }
    
    @Test
    public void exportProtectedNode() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final int nodeWsID = 10;
        final boolean isNodeProtected = Boolean.TRUE;
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockWorkspaceNode).getWorkspaceNodeID(); will(returnValue(nodeWsID));
            
            oneOf(mockWorkspaceNode).isProtected(); will(returnValue(isNodeProtected));
            
            //logger
            oneOf(mockWorkspaceNode).getWorkspaceNodeID(); will(returnValue(nodeWsID));
        }});
        
        
        //TODO DO NOT USE NULL - THAT WOULD MEAN DELETING THE TOP NODE - THAT WOULD INVOLVE MESSING WITH THE PARENT OF THE TOP NODE (OUTSIDE OF THE SCOPE OF THE WORKSPACE)
        unlinkedNodeExporter.exportNode(testWorkspace, null, mockWorkspaceNode, keepUnlinkedFiles);
        
    }
    
    @Test
    public void exportNodeWithoutArchiveURI_DoNotKeepUnlinkedFiles() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final int wsID = 1;
        final int nodeWsID = 10;
        
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;

        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(wsID));
            oneOf(mockWorkspaceNode).getWorkspaceNodeID(); will(returnValue(nodeWsID));
            
            oneOf(mockWorkspaceNode).isProtected(); will(returnValue(isNodeProtected));
            //node without archiveURL - was never in the archive, so it can just be skipped and will eventually be deleted together with the whole workspace folder
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(null));
            
            //logger
            oneOf(mockWorkspaceNode).getWorkspaceNodeID(); will(returnValue(nodeWsID));
        }});
        
        
        
        //TODO DO NOT USE NULL - THAT WOULD MEAN DELETING THE TOP NODE - THAT WOULD INVOLVE MESSING WITH THE PARENT OF THE TOP NODE (OUTSIDE OF THE SCOPE OF THE WORKSPACE)
        unlinkedNodeExporter.exportNode(mockWorkspace, null, mockWorkspaceNode, keepUnlinkedFiles);
        
    }
    
    @Test
    public void exportNodeWithoutArchiveURI_KeepUnlinkedFiles() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final int wsID = 1;
        final int nodeWsID = 10;
        
        final String nodeFilename = "node.cmdi";
        final URL newNodeLocation = new URL("file:/archive/some/location/sessions/" + nodeFilename);
        
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;

        context.checking(new Expectations() {{
            
           //logger
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(wsID));
            oneOf(mockWorkspaceNode).getWorkspaceNodeID(); will(returnValue(nodeWsID));
            
            oneOf(mockWorkspaceNode).isProtected(); will(returnValue(isNodeProtected));
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(null));
            
            oneOf(mockVersioningHandler).moveFileToOrphansFolder(mockWorkspace, mockWorkspaceNode); will(returnValue(newNodeLocation));
        }});
        
        
        
        //TODO DO NOT USE NULL - THAT WOULD MEAN DELETING THE TOP NODE - THAT WOULD INVOLVE MESSING WITH THE PARENT OF THE TOP NODE (OUTSIDE OF THE SCOPE OF THE WORKSPACE)
        unlinkedNodeExporter.exportNode(mockWorkspace, null, mockWorkspaceNode, keepUnlinkedFiles);
        
    }
    
//    @Test
//    public void exportUnknownNode() throws MalformedURLException, URISyntaxException, UnknownNodeException, WorkspaceExportException {
//        
//        final int testWorkspaceNodeID = 10;
//        final String testBaseName = "node.txt";
//        final URL testNodeWsURL = new URL("file:/workspace/" + testBaseName);
//        final URI testNodeArchiveURI = new URI(UUID.randomUUID().toString());
//        final URL testNodeOriginURL = new URL("file:/lat/corpora/archive/folder/" + testBaseName);
//        final URL testNodeArchiveURL = testNodeOriginURL;
//        
//        final String testNodeDisplayValue = "node";
//        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
//        final String testNodeFormat = "text/plain";
//        final URI testNodeSchemaLocation = new URI("http://some.location");
//
//        final WorkspaceNode testNode = new LamusWorkspaceNode(testWorkspaceNodeID, testWorkspace.getWorkspaceID(), testNodeSchemaLocation,
//                testNodeDisplayValue, "", testNodeType, testNodeWsURL, testNodeArchiveURI, testNodeArchiveURL, testNodeOriginURL, WorkspaceNodeStatus.NODE_DELETED, testNodeFormat);
//        
//        final URL testNodeVersionArchiveURL = new URL("file:/trash/location/r_node.txt");
//        
//        final String expectedErrorMessage = "Node not found in archive database for URI " + testNode.getArchiveURI();
//        final UnknownNodeException expectedException = new UnknownNodeException("some exception message");
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(testNodeArchiveURL));
//            
//            oneOf(mockVersioningHandler).moveFileToTrashCanFolder(mockWorkspaceNode); will(returnValue(testNodeVersionArchiveURL));
//            oneOf(mockWorkspaceNode).setArchiveURL(testNodeVersionArchiveURL);
//            
//            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
//            oneOf(mockCorpusStructureProvider).getNode(testNodeArchiveURI); will(throwException(expectedException));
//
//            //exception caught
//            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
//        }});
//        
//        try {
//            deletedNodeExporter.exportNode(null, mockWorkspaceNode);
//            fail("should have thrown exception");
//        } catch(WorkspaceExportException ex) {
//            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
//            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
//            assertEquals("Cause different from expected", expectedException, ex.getCause());
//        }
//    }
    
    @Test
    public void exportNodeNullWorkspace() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        
        try {
            unlinkedNodeExporter.exportNode(null, null, mockWorkspaceNode, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "Workspace not set";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
}