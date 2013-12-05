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
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.exporting.TrashCanHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class DeletedNodeExporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    private NodeExporter deletedNodeExporter;
    private Workspace testWorkspace;
    
    @Mock TrashCanHandler mockTrashCanHandler;
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock SearchClientBridge mockSearchClientBridge;
    
    @Mock WorkspaceNode mockWorkspaceNode;
    @Mock CorpusNode mockCorpusNode;
    
    public DeletedNodeExporterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        deletedNodeExporter = new DeletedNodeExporter(mockTrashCanHandler,
                mockCorpusStructureProvider, mockSearchClientBridge);
        
        testWorkspace = new LamusWorkspace(1, "someUser",  -1, null, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.SUBMITTED, "Workspace submitted", "archiveInfo/something");
        deletedNodeExporter.setWorkspace(testWorkspace);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void exportNode() throws MalformedURLException, URISyntaxException, UnknownNodeException, WorkspaceExportException {
        
        final int testWorkspaceNodeID = 10;
        final String testBaseName = "node.txt";
        final URL testNodeWsURL = new URL("file:/workspace/" + testBaseName);
        final URI testNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL testNodeOriginURL = new URL("file:/lat/corpora/archive/folder/" + testBaseName);
        final URL testNodeArchiveURL = testNodeOriginURL;
        
        final String testNodeDisplayValue = "node";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "text/plain";
        final URI testNodeSchemaLocation = new URI("http://some.location");

        final WorkspaceNode testNode = new LamusWorkspaceNode(testWorkspaceNodeID, testWorkspace.getWorkspaceID(), testNodeSchemaLocation,
                testNodeDisplayValue, "", testNodeType, testNodeWsURL, testNodeArchiveURI, testNodeArchiveURL, testNodeOriginURL, WorkspaceNodeStatus.NODE_DELETED, testNodeFormat);
        
//        final StringBuilder testNodeVersionFileNameBuilder = new StringBuilder().append("v").append(testArchiveNodeID).append("__.").append(testBaseName);
        
        final URL testNodeVersionArchiveURL = new URL("file:/trash/location/r_node.txt");
        
        context.checking(new Expectations() {{
            
            oneOf(mockTrashCanHandler).moveFileToTrashCan(mockWorkspaceNode); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockWorkspaceNode).setArchiveURL(testNodeVersionArchiveURL);
            
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(testNodeArchiveURI); will(returnValue(mockCorpusNode));
            
//            oneOf(mockCorpusstructureWriter).deleteNode(mockCorpusNode);
            
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            oneOf(mockSearchClientBridge).removeNode(testNodeArchiveURI);
            
        }});
        
        //TODO Handle external nodes (those can't be deleted, just unlinked)
        
        
        //retire version
        //move to trash
        //update csdb to point to the trash location
        
        //remove node from searchDB????
        
        
        //TODO DO NOT USE NULL - THAT WOULD MEAN DELETING THE TOP NODE - THAT WOULD INVOLVE MESSING WITH THE PARENT OF THE TOP NODE (OUTSIDE OF THE SCOPE OF THE WORKSPACE)
        deletedNodeExporter.exportNode(null, mockWorkspaceNode);
        
    }
    
    @Test
    public void exportUnknownNode() throws MalformedURLException, URISyntaxException, UnknownNodeException, WorkspaceExportException {
        
        final int testWorkspaceNodeID = 10;
        final String testBaseName = "node.txt";
        final URL testNodeWsURL = new URL("file:/workspace/" + testBaseName);
        final URI testNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL testNodeOriginURL = new URL("file:/lat/corpora/archive/folder/" + testBaseName);
        final URL testNodeArchiveURL = testNodeOriginURL;
        
        final String testNodeDisplayValue = "node";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "text/plain";
        final URI testNodeSchemaLocation = new URI("http://some.location");

        final WorkspaceNode testNode = new LamusWorkspaceNode(testWorkspaceNodeID, testWorkspace.getWorkspaceID(), testNodeSchemaLocation,
                testNodeDisplayValue, "", testNodeType, testNodeWsURL, testNodeArchiveURI, testNodeArchiveURL, testNodeOriginURL, WorkspaceNodeStatus.NODE_DELETED, testNodeFormat);
        
        final URL testNodeVersionArchiveURL = new URL("file:/trash/location/r_node.txt");
        
        final String expectedErrorMessage = "Node not found in archive database for URI " + testNode.getArchiveURI();
        final UnknownNodeException expectedException = new UnknownNodeException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockTrashCanHandler).moveFileToTrashCan(mockWorkspaceNode); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockWorkspaceNode).setArchiveURL(testNodeVersionArchiveURL);
            
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(testNodeArchiveURI); will(throwException(expectedException));

            //exception caught
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
        }});
        
        try {
            deletedNodeExporter.exportNode(null, mockWorkspaceNode);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void exportNodeNullWorkspace() throws MalformedURLException, URISyntaxException, UnknownNodeException, WorkspaceExportException {
        
        deletedNodeExporter.setWorkspace(null);
        
        try {
            deletedNodeExporter.exportNode(null, mockWorkspaceNode);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "Workspace not set";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
}