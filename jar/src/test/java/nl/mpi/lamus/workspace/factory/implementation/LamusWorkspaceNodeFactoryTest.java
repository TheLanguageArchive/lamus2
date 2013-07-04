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
package nl.mpi.lamus.workspace.factory.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.type.MetadataDocumentType;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceNodeFactoryTest {
    
    private WorkspaceNodeFactory factory;
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    
    @Mock TestReferencingMetadataDocumentHandleCarrier mockTestReferencingMetadataDocumentHandleCarrier;
    @Mock MetadataDocumentType mockMetadataDocumentType;
    
    public LamusWorkspaceNodeFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        factory = new LamusWorkspaceNodeFactory(mockArchiveFileHelper);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getNewWorkspaceNode method, of class LamusWorkspaceNodeFactory.
     */
    @Test
    public void workspaceNodeCorrectlyInitialised() throws MalformedURLException {

        int testWorkspaceID = 10;
        int testArchiveNodeID = 100;
        URL testArchiveNodeURL = new URL("http://some.url");
        
        WorkspaceNode testWorkspaceNode = factory.getNewWorkspaceNode(testWorkspaceID, testArchiveNodeID, testArchiveNodeURL);
        
        assertEquals(testWorkspaceID, testWorkspaceNode.getWorkspaceID());
        assertEquals(testArchiveNodeID, testWorkspaceNode.getArchiveNodeID());
        assertEquals(testArchiveNodeURL, testWorkspaceNode.getArchiveURL());
        assertEquals(testArchiveNodeURL, testWorkspaceNode.getOriginURL());
    }
    
    @Test
    public void workspaceMetadataNodeCorrectlyInitialised() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final String userID = "someUser";
        final int topNodeID = 1;
        final int topNodeArchiveID = 2;
        final URL topNodeArchiveURL = new URL("http://top.url");
        final Date startDate = Calendar.getInstance().getTime();
        final Date endDate = null;
        final long usedSpace = 0;
        final long maxSpace = 1000;
        final WorkspaceStatus status = WorkspaceStatus.INITIALISING;
        final String message = "initialising...";
        final String archiveInfo = "info...";
        
        final Workspace testWorkspace = new LamusWorkspace(
                workspaceID, userID, topNodeID, topNodeArchiveID, topNodeArchiveURL, startDate, endDate, startDate, endDate,
                usedSpace, maxSpace, status, message, archiveInfo);
        
        final int nodeArchiveID = 100;
        final URL nodeURL = new URL("http://some.url/node.something");
        final String displayValue = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "";
        final URI schemaLocation = new URI("http://some.location");
        final String pid = UUID.randomUUID().toString();
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_ISCOPY; //TODO change this
        final URI fileLocation = nodeURL.toURI();
        
        final WorkspaceNode expectedNode = new LamusWorkspaceNode(workspaceID, nodeArchiveID, nodeURL, nodeURL);
        expectedNode.setName(displayValue);
        expectedNode.setTitle(displayValue);
        expectedNode.setType(nodeType);
        expectedNode.setFormat(nodeFormat);
        expectedNode.setProfileSchemaURI(schemaLocation);
        expectedNode.setPid(pid);
        expectedNode.setStatus(WorkspaceNodeStatus.NODE_ISCOPY);

        context.checking(new Expectations() {{
        
            exactly(2).of(mockTestReferencingMetadataDocumentHandleCarrier).getFileLocation(); will(returnValue(fileLocation));
            exactly(2).of(mockTestReferencingMetadataDocumentHandleCarrier).getDisplayValue(); will(returnValue(displayValue));
            //TODO get type
            //TODO get format
            oneOf(mockTestReferencingMetadataDocumentHandleCarrier).getDocumentType(); will(returnValue(mockMetadataDocumentType));
            oneOf(mockMetadataDocumentType).getSchemaLocation(); will(returnValue(schemaLocation));
            oneOf(mockTestReferencingMetadataDocumentHandleCarrier).getHandle(); will(returnValue(pid));
        }});
        
        WorkspaceNode retrievedNode = factory.getNewWorkspaceMetadataNode(workspaceID, nodeArchiveID,
                mockTestReferencingMetadataDocumentHandleCarrier);
        
        assertEquals("Retrieved workspace node is different from expected", expectedNode, retrievedNode);
    }
    
    interface TestReferencingMetadataDocumentHandleCarrier extends ReferencingMetadataDocument, HandleCarrier {

    }
}
