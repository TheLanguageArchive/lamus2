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
package nl.mpi.lamus.workspace.importing.implementation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.xml.transform.TransformerException;
import nl.mpi.lamus.exception.UnusableReferenceTypeException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.importing.NodeImporterAssigner;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeExplorer;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.cmdi.api.model.DataResourceProxy;
import nl.mpi.metadata.cmdi.api.model.MetadataResourceProxy;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;
import static org.junit.Assert.*;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceNodeExplorerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private WorkspaceNodeExplorer nodeExplorer;
    @Mock private NodeImporterAssigner mockNodeImporterAssigner;
    @Mock private MetadataApiBridge mockMetadataApiBridge;
    @Mock private NodeImporter mockNodeImporter;
    @Mock private WorkspaceNode mockNodeToExplore;
    @Mock private ReferencingMetadataDocument mockNodeDocument;
    
    @Mock private Workspace mockWorkspace;
    
    private final int workspaceID = 1;
    
    public LamusWorkspaceNodeExplorerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        nodeExplorer = new LamusWorkspaceNodeExplorer();
        ReflectionTestUtils.setField(nodeExplorer, "nodeImporterAssigner", mockNodeImporterAssigner);
        ReflectionTestUtils.setField(nodeExplorer, "metadataApiBridge", mockMetadataApiBridge);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void exploreSuccessfullyLinkWithHandle() throws MalformedURLException,
            UnusableReferenceTypeException, WorkspaceImportException, IOException,
            TransformerException, MetadataException {

        final int nodeID = 10;
        
        final URL nodeURL = new URL("file:/workspace/location/parentTest.cmdi");
        
        final URI metadataURI = URI.create("https://testURL.mpi.nl/test.cmdi");
        final URI resourceURI = URI.create("https://testURL.mpi.nl/test.jpg");
        final ResourceProxy metadataLink = new MetadataResourceProxy("1", metadataURI, "cmdi");
        final ResourceProxy resourceLink = new DataResourceProxy("2", resourceURI, "jpg");
        
        final URI metadataLinkHandle = URI.create("hdl:3492/2932");
        metadataLink.setHandle(metadataLinkHandle);
        final URI resourceLinkHandle = URI.create("hdl:3492/2933");
        resourceLink.setHandle(resourceLinkHandle);
        
        final Collection<Reference> testLinks = new ArrayList<>();
        testLinks.add(metadataLink);
        testLinks.add(resourceLink);
        
        context.checking(new Expectations() {{
            
            //logger
            allowing(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockNodeToExplore).getWorkspaceNodeID(); will(returnValue(nodeID));
            allowing(mockNodeToExplore).getWorkspaceURL(); will(returnValue(nodeURL));
            
            for(Reference currentLink : testLinks) { //instances of HandleCarrier
                
                oneOf(mockNodeImporterAssigner).getImporterForReference(currentLink); will(returnValue(mockNodeImporter));
                oneOf(mockNodeImporter).importNode(mockWorkspace, mockNodeToExplore, mockNodeDocument, currentLink);
            }
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockNodeDocument, nodeURL);
        }});
        
        nodeExplorer.explore(mockWorkspace, mockNodeToExplore, mockNodeDocument, testLinks);
    }

    @Test
    public void explore_UnusableReferenceType() throws MalformedURLException, UnusableReferenceTypeException, WorkspaceImportException, IOException, TransformerException, MetadataException {
        
        final int nodeID = 10;
        
        final URL nodeURL = new URL("file:/workspace/location/parentTest.cmdi");
        
        final URI usableURI = URI.create("https://testURL.mpi.nl/test.jpg");
        final URI unusableURI = URI.create("https://testURL.mpi.nl/search.html");
        final ResourceProxy usableLink = new DataResourceProxy("1", usableURI, "SearchPage", "text/html");
        final ResourceProxy unusableLink = new DataResourceProxy("2", unusableURI, "Resource", "jpg");
        
        final URI usableLinkHandle = URI.create("hdl:3492/2932");
        usableLink.setHandle(usableLinkHandle);
        final URI resourceLinkHandle = URI.create("hdl:3492/2933");
        unusableLink.setHandle(resourceLinkHandle);
        
        final Collection<Reference> testLinks = new ArrayList<>();
        testLinks.add(unusableLink);
        testLinks.add(usableLink);
        
        final Iterator<Reference> linkIterator = testLinks.iterator();
        
        final UnusableReferenceTypeException expectedException = new UnusableReferenceTypeException("some message", "SomeOtherType", null);
        
        context.checking(new Expectations() {{
            
            //logger
            allowing(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockNodeToExplore).getWorkspaceNodeID(); will(returnValue(nodeID));
            allowing(mockNodeToExplore).getWorkspaceURL(); will(returnValue(nodeURL));
            
            Reference currentLink = linkIterator.next();
            oneOf(mockNodeImporterAssigner).getImporterForReference(currentLink); will(throwException(expectedException));
                
            // even with the exception, the loop continues
            currentLink = linkIterator.next();
            oneOf(mockNodeImporterAssigner).getImporterForReference(currentLink); will(returnValue(mockNodeImporter));
            oneOf(mockNodeImporter).importNode(mockWorkspace, mockNodeToExplore, mockNodeDocument, currentLink);
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockNodeDocument, nodeURL);
        }});

        nodeExplorer.explore(mockWorkspace, mockNodeToExplore, mockNodeDocument, testLinks);
    }
    
    @Test
    public void exploreThrowsException() throws Exception {

        final int nodeID = 10;
        
        final URI metadataURI = new URI("https://testURL.mpi.nl/test.cmdi");
        final URI resourceURI = new URI("https://testURL.mpi.nl/test.jpg");
        final ResourceProxy metadataLink = new MetadataResourceProxy("1", metadataURI, "cmdi");
        final ResourceProxy resourceLink = new DataResourceProxy("2", resourceURI, "jpg");
        
        final URI metadataLinkHandle = new URI("hdl:3492/2932");
        metadataLink.setHandle(metadataLinkHandle);
        final URI resourceLinkHandle = new URI("hdl:3492/2933");
        resourceLink.setHandle(resourceLinkHandle);
        
        final Collection<Reference> testLinks = new ArrayList<>();
        testLinks.add(metadataLink);
        testLinks.add(resourceLink);
        
        final IllegalArgumentException expectedException = new IllegalArgumentException("some exception message");
        
        context.checking(new Expectations() {{
            
            //logger
            allowing(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockNodeToExplore).getWorkspaceNodeID(); will(returnValue(nodeID));
            
            for(Reference currentLink : testLinks) { //instances of HandleCarrier

                oneOf(mockNodeImporterAssigner).getImporterForReference(currentLink); will(throwException(expectedException));
                
                break; // due to the exception, the loop doesn't continue
            }
            
        }});

        try {
            nodeExplorer.explore(mockWorkspace, mockNodeToExplore, mockNodeDocument, testLinks);
            fail("should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String errorMessage = "Error getting file importer";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void saveMetadataThrowsException() throws Exception {

        final int nodeID = 10;
        
        final URL nodeURL = new URL("file:/workspace/location/parentTest.cmdi");
        
        final URI metadataURI = URI.create("https://testURL.mpi.nl/test.cmdi");
        final URI resourceURI = URI.create("https://testURL.mpi.nl/test.jpg");
        final ResourceProxy metadataLink = new MetadataResourceProxy("1", metadataURI, "cmdi");
        final ResourceProxy resourceLink = new DataResourceProxy("2", resourceURI, "jpg");
        
        final URI metadataLinkHandle = URI.create("hdl:3492/2932");
        metadataLink.setHandle(metadataLinkHandle);
        final URI resourceLinkHandle = URI.create("hdl:3492/2933");
        resourceLink.setHandle(resourceLinkHandle);
        
        final Collection<Reference> testLinks = new ArrayList<>();
        testLinks.add(metadataLink);
        testLinks.add(resourceLink);
        
        final MetadataException expectedException = new MetadataException("some error saving the metadata document");
        
        context.checking(new Expectations() {{
            
            //logger
            allowing(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockNodeToExplore).getWorkspaceNodeID(); will(returnValue(nodeID));
            allowing(mockNodeToExplore).getWorkspaceURL(); will(returnValue(nodeURL));
            
            int current = 0;
            for(Reference currentLink : testLinks) { //instances of HandleCarrier
                
                oneOf(mockNodeImporterAssigner).getImporterForReference(currentLink); will(returnValue(mockNodeImporter));
                oneOf(mockNodeImporter).importNode(mockWorkspace, mockNodeToExplore, mockNodeDocument, currentLink);
                
                current++;
            }
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockNodeDocument, nodeURL); will(throwException(expectedException));
        }});
        
        try {
            nodeExplorer.explore(mockWorkspace, mockNodeToExplore, mockNodeDocument, testLinks);
            fail("should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String errorMessage = "Failed to save file " + nodeURL
		    + " in workspace " + workspaceID;
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
}
