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
import nl.mpi.lamus.workspace.exporting.CorpusStructureBridge;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.exporting.TrashCanHandler;
import nl.mpi.lamus.workspace.exporting.TrashVersioningHandler;
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

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class DeletedNodeExporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    private NodeExporter deletedNodeExporter;
    private Workspace testWorkspace;
    
    @Mock TrashVersioningHandler mockTrashVersioningHandler;
    @Mock TrashCanHandler mockTrashCanHandler;
    @Mock CorpusStructureBridge mockCorpusStructureBridge;
    @Mock SearchClientBridge mockSearchClientBridge;
    
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
        deletedNodeExporter = new DeletedNodeExporter(mockTrashVersioningHandler, mockTrashCanHandler, mockCorpusStructureBridge, mockSearchClientBridge);
        
        testWorkspace = new LamusWorkspace(1, "someUser", -1, -1, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.SUBMITTED, "Workspace submitted", "archiveInfo/something");
        deletedNodeExporter.setWorkspace(testWorkspace);
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of exportNode method, of class DeletedNodeExporter.
     */
    @Test
    public void exportNode() throws MalformedURLException, URISyntaxException {
        
        final int testWorkspaceNodeID = 10;
        final int testArchiveNodeID = 100;
        final String testBaseName = "node.something";
        final URL testNodeWorkspaceURL = new URL("file:/workspace/some.url");
        final URL testNodeArchiveURL = new URL("file://archive/some.url/" + testBaseName);
        final String testNodeDisplayValue = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "text/plain";
        final URI testNodeSchemaLocation = new URI("http://some.location");
        final String testNodePid = "somePID";
        final WorkspaceNode testNode = new LamusWorkspaceNode(testWorkspaceNodeID, testWorkspace.getWorkspaceID(), testArchiveNodeID, testNodeSchemaLocation,
                testNodeDisplayValue, "", testNodeType, testNodeWorkspaceURL, testNodeArchiveURL, testNodeArchiveURL, WorkspaceNodeStatus.NODE_DELETED, testNodePid, testNodeFormat);
        
        final StringBuilder testNodeVersionFileNameBuilder = new StringBuilder().append("v").append(testArchiveNodeID).append("__.").append(testBaseName);
        
        final URL testNodeVersionArchiveURL = new URL("file:/some.location");
        
        context.checking(new Expectations() {{
            
            oneOf(mockTrashVersioningHandler).retireNodeVersion(testNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockTrashCanHandler).moveFileToTrashCan(testNode); will(returnValue(testNodeVersionArchiveURL));
            
            oneOf(mockCorpusStructureBridge).updateArchiveObjectsNodeURL(testArchiveNodeID, testNodeArchiveURL, testNodeVersionArchiveURL);
            
            oneOf(mockSearchClientBridge).removeNode(testArchiveNodeID);
            
        }});
        
        //TODO Handle external nodes (those can't be deleted, just unlinked)
        
        
        //retire version
        //move to trash
        //update csdb to point to the trash location
        
        //remove node from searchDB????
        
        
        //TODO DO NOT USE NULL - THAT WOULD MEAN DELETING THE TOP NODE - THAT WOULD INVOLVE MESSING WITH THE PARENT OF THE TOP NODE (OUTSIDE OF THE SCOPE OF THE WORKSPACE)
        deletedNodeExporter.exportNode(null, testNode);
        
    }
    
    //TODO tests for failure situations
}