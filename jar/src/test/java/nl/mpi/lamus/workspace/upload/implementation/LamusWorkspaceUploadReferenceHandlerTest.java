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
package nl.mpi.lamus.workspace.upload.implementation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.handle.util.implementation.HandleManagerImpl;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadNodeMatcher;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadReferenceHandler;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.util.HandleUtil;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
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
public class LamusWorkspaceUploadReferenceHandlerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private WorkspaceUploadReferenceHandler workspaceUploadReferenceHandler;
    
    @Mock HandleUtil mockMetadataApiHandleUtil;
    @Mock WorkspaceUploadNodeMatcher mockWorkspaceUploadNodeMatcher;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock WorkspaceNodeLinkManager mockWorkspaceNodeLinkManager;
    @Mock HandleManagerImpl mockHandleMatcher;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    
    @Mock ReferencingMetadataDocument mockMetadataDocument;
    @Mock WorkspaceNode mockFirstNode;
    @Mock WorkspaceNode mockSecondNode;
    @Mock WorkspaceNode mockThirdNode;
    @Mock WorkspaceNode mockExternalNode;
    @Mock Reference mockFirstReference;
    @Mock Reference mockSecondReference;
    @Mock WorkspaceNodeLink mockNodeLink;
    @Mock StreamResult mockStreamResult;
    
    public LamusWorkspaceUploadReferenceHandlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        workspaceUploadReferenceHandler = new LamusWorkspaceUploadReferenceHandler(
                mockMetadataApiHandleUtil, mockWorkspaceUploadNodeMatcher,
                mockWorkspaceDao, mockWorkspaceNodeLinkManager, mockHandleMatcher,
                mockMetadataAPI, mockWorkspaceFileHandler);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void matchOneReference_WithLocalUrl_WithEmptyUri() throws MalformedURLException, WorkspaceException, URISyntaxException, IOException, TransformerException, MetadataException {
        
        final int workspaceID = 10;
        final int firstNodeID = 101;
        final int secondNodeID = 102;
        
        final URI firstRefURI = new URI("");
        final URL firstRefLocalUrl = new URL("file://absolute/path/to/resource.txt");
        
        final URI firstDocumentLocation = new URI("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<WorkspaceNode>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<Reference>();
        references.add(mockFirstReference);
        
        Collection<Reference> failedLinks = new ArrayList<Reference>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getDocumentReferences(); will(returnValue(references));
            
            //loop over references
            //first reference contains a localURI
            oneOf(mockFirstReference).getLocation(); will(returnValue(firstRefLocalUrl));
            oneOf(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            
            //matches second node
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefLocalUrl.toString());
                will(returnValue(mockSecondNode));

            //empty URI, so it will be set with the value of the location URL
//            exactly(2).of(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            oneOf(mockFirstReference).setURI(new URI(""));
            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(firstDocumentLocation));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(firstDocumentLocationFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
                
            //link parent node with node that matches the reference, ONLY in the DB
            oneOf(mockWorkspaceNodeLinkManager).linkNodesOnlyInDb(mockFirstNode, mockSecondNode);
        }});
        
        workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, failedLinks);
        
        assertTrue("Map of failed links should be empty", failedLinks.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithLocalUrl_WithHandle() throws MalformedURLException, WorkspaceException, URISyntaxException, IOException, TransformerException, MetadataException {
        
        final int workspaceID = 10;
        final int firstNodeID = 101;
        final int secondNodeID = 102;
        
        final URI firstRefURI = new URI("hdl:" + UUID.randomUUID().toString());
        final URL firstRefLocalUrl = new URL("file://absolute/path/to/resource.txt");
        
        final URI firstDocumentLocation = new URI("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<WorkspaceNode>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<Reference>();
        references.add(mockFirstReference);
        
        Collection<Reference> failedLinks = new ArrayList<Reference>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getDocumentReferences(); will(returnValue(references));
            
            //loop over references
            //first reference contains a localURI
            oneOf(mockFirstReference).getLocation(); will(returnValue(firstRefLocalUrl));
            oneOf(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            
            //matches second node
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefLocalUrl.toString());
                will(returnValue(mockSecondNode));

            //URI is a handle, so URI in DB should be updated
            oneOf(mockMetadataApiHandleUtil).isHandleUri(firstRefURI); will(returnValue(Boolean.TRUE));
            oneOf(mockSecondNode).setArchiveURI(firstRefURI);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockSecondNode);
                
            //link parent node with node that matches the reference, ONLY in the DB
            oneOf(mockWorkspaceNodeLinkManager).linkNodesOnlyInDb(mockFirstNode, mockSecondNode);
        }});
        
        workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, failedLinks);
        
        assertTrue("Map of failed links should be empty", failedLinks.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithLocalUrl_WithUri() throws MalformedURLException, WorkspaceException, URISyntaxException, IOException, TransformerException, MetadataException {
        
        final int workspaceID = 10;
        final int firstNodeID = 101;
        final int secondNodeID = 102;
        
        final URL firstRefLocalUrl = new URL("file://absolute/path/to/resource.txt");
        final URI firstRefURI = firstRefLocalUrl.toURI();
        
        final URI firstDocumentLocation = new URI("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<WorkspaceNode>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<Reference>();
        references.add(mockFirstReference);
        
        Collection<Reference> failedLinks = new ArrayList<Reference>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getDocumentReferences(); will(returnValue(references));
            
            //loop over references
            //first reference contains a localURI
            oneOf(mockFirstReference).getLocation(); will(returnValue(firstRefLocalUrl));
            oneOf(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            
            //matches second node
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefLocalUrl.toString());
                will(returnValue(mockSecondNode));

            //URI is not a handle, so it should be cleared (since the local URL is already present in the localURI attribute)
            oneOf(mockMetadataApiHandleUtil).isHandleUri(firstRefURI); will(returnValue(Boolean.FALSE));
            
//            exactly(2).of(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            oneOf(mockFirstReference).setURI(new URI(""));
            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(firstDocumentLocation));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(firstDocumentLocationFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
                
                
            //link parent node with node that matches the reference, ONLY in the DB
            oneOf(mockWorkspaceNodeLinkManager).linkNodesOnlyInDb(mockFirstNode, mockSecondNode);
        }});
        
        workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, failedLinks);
        
        assertTrue("Map of failed links should be empty", failedLinks.isEmpty());
    }

    @Test
    public void matchOneReference_WithoutLocalUrl_WithHandle() throws URISyntaxException, WorkspaceException, MalformedURLException, IOException, TransformerException, MetadataException {
        
        final int workspaceID = 10;
        final int firstNodeID = 101;
        final int secondNodeID = 102;
        
        final URI firstRefURI = new URI("hdl:" + UUID.randomUUID().toString());
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        
        final URI firstDocumentLocation = new URI("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<WorkspaceNode>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<Reference>();
        references.add(mockFirstReference);
        
        Collection<Reference> failedLinks = new ArrayList<Reference>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getDocumentReferences(); will(returnValue(references));
            
            //loop over references
            //first reference contains a handle
            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            oneOf(mockMetadataApiHandleUtil).isHandleUri(firstRefURI); will(returnValue(Boolean.TRUE));
            
            //matches second node
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForHandle(workspaceID, nodesToCheck, firstRefURI);
                will(returnValue(mockSecondNode));

            oneOf(mockSecondNode).getWorkspaceURL(); will(returnValue(secondNodeURL));
            
            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).setLocation(secondNodeURL);

            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(firstDocumentLocation));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(firstDocumentLocationFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
            
            //not necessary to change the archive URI in the workspace DB, since it's already the correct one
            oneOf(mockSecondNode).getArchiveURI(); will(returnValue(firstRefURI));
            oneOf(mockHandleMatcher).areHandlesEquivalent(firstRefURI, firstRefURI); will(returnValue(Boolean.TRUE));
                
            //link parent node with node that matches the reference, ONLY in the DB
            oneOf(mockWorkspaceNodeLinkManager).linkNodesOnlyInDb(mockFirstNode, mockSecondNode);
        }});
        
        workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, failedLinks);
        
        assertTrue("Map of failed links should be empty", failedLinks.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithoutLocalUrl_WithHandle_MissingDbHandle() throws URISyntaxException, WorkspaceException, MalformedURLException, IOException, TransformerException, MetadataException {
        
        final int workspaceID = 10;
        final int firstNodeID = 101;
        final int secondNodeID = 102;
        
        //URI is a handle
        final URI firstRefURI = new URI("hdl:" + UUID.randomUUID().toString());
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        
        final URI firstDocumentLocation = new URI("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<WorkspaceNode>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<Reference>();
        references.add(mockFirstReference);
        
        Collection<Reference> failedLinks = new ArrayList<Reference>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getDocumentReferences(); will(returnValue(references));
            
            //loop over references
            //first reference contains a handle
            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            oneOf(mockMetadataApiHandleUtil).isHandleUri(firstRefURI); will(returnValue(Boolean.TRUE));
            
            //matches second node
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForHandle(workspaceID, nodesToCheck, firstRefURI);
                will(returnValue(mockSecondNode));
                
            oneOf(mockSecondNode).getWorkspaceURL(); will(returnValue(secondNodeURL));
            
            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).setLocation(secondNodeURL);

            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(firstDocumentLocation));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(firstDocumentLocationFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
            
            //necessary to change the archive URI in the workspace DB, since it's not the correct one
            oneOf(mockSecondNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockHandleMatcher).areHandlesEquivalent(firstRefURI, null); will(returnValue(Boolean.FALSE));
            
            oneOf(mockSecondNode).setArchiveURI(firstRefURI);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockSecondNode);
                
            //link parent node with node that matches the reference, ONLY in DB
            oneOf(mockWorkspaceNodeLinkManager).linkNodesOnlyInDb(mockFirstNode, mockSecondNode);
        }});
        
        workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, failedLinks);
        
        assertTrue("Map of failed links should be empty", failedLinks.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithoutLocalUrl_WithUri() throws URISyntaxException, WorkspaceException, MalformedURLException, IOException, TransformerException, MetadataException {
        
        final int workspaceID = 10;
        
        //URI is not a handle
        final URI firstRefURI = new URI("parent/child.txt");
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        
        final URI firstDocumentLocation = new URI("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<WorkspaceNode>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<Reference>();
        references.add(mockFirstReference);
        
        Collection<Reference> failedLinks = new ArrayList<Reference>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getDocumentReferences(); will(returnValue(references));
            
            //loop over references
            //first reference contains a URI
            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            oneOf(mockMetadataApiHandleUtil).isHandleUri(firstRefURI); will(returnValue(Boolean.FALSE));
            
            //matches second node
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefURI.toString());
                will(returnValue(mockSecondNode));
            
            //change the reference URI to the workspace URL and save the document in the same location
            oneOf(mockSecondNode).getWorkspaceURL(); will(returnValue(secondNodeURL));

            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).setLocation(secondNodeURL);
            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(firstDocumentLocation));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(firstDocumentLocationFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
                
            //link parent node with node that matches the reference, ONLY in DB
            oneOf(mockWorkspaceNodeLinkManager).linkNodesOnlyInDb(mockFirstNode, mockSecondNode);
        }});
        
        workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, failedLinks);
        
        assertTrue("Map of failed links should be empty", failedLinks.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithoutLocalUrl_WithExternalUri() throws URISyntaxException, WorkspaceException {
        
        final int workspaceID = 10;
        
        //URI is not a handle
        final URI firstRefURI = new URI("http://some/external/folder/file.txt");
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<WorkspaceNode>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<Reference>();
        references.add(mockFirstReference);
        
        Collection<Reference> failedLinks = new ArrayList<Reference>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getDocumentReferences(); will(returnValue(references));
            
            //loop over references
            //first reference contains a URI
            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            oneOf(mockMetadataApiHandleUtil).isHandleUri(firstRefURI); will(returnValue(Boolean.FALSE));
            
            //matches second node
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefURI.toString());
                will(returnValue(null));
            //since a match was not found, perhaps it's an external node
            oneOf(mockWorkspaceUploadNodeMatcher).findExternalNodeForUri(workspaceID, firstRefURI);
                will(returnValue(mockExternalNode));
                
            //link parent node with node that matches the reference, ONLY in DB
            oneOf(mockWorkspaceNodeLinkManager).linkNodesOnlyInDb(mockFirstNode, mockExternalNode);
        }});
        
        workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, failedLinks);
        
        assertTrue("Map of failed links should be empty", failedLinks.isEmpty());
    }
    
    @Test
    public void matchOneReference_LinkingNodesThrowsException() throws URISyntaxException, WorkspaceException, MalformedURLException, IOException, TransformerException, MetadataException {
        
        final int workspaceID = 10;
        final int firstNodeID = 101;
        final int secondNodeID = 102;
        
        final URI firstRefURI = new URI("hdl:" + UUID.randomUUID().toString());
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        
        final URI firstDocumentLocation = new URI("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<WorkspaceNode>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<Reference>();
        references.add(mockFirstReference);
        
        Collection<Reference> failedLinks = new ArrayList<Reference>();
        
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getDocumentReferences(); will(returnValue(references));
            
            //loop over references
            //first reference contains a handle
            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            oneOf(mockMetadataApiHandleUtil).isHandleUri(firstRefURI); will(returnValue(Boolean.TRUE));
            
            //matches second node
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForHandle(workspaceID, nodesToCheck, firstRefURI);
                will(returnValue(mockSecondNode));
                
            oneOf(mockSecondNode).getWorkspaceURL(); will(returnValue(secondNodeURL));
            
            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).setLocation(secondNodeURL);

            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(firstDocumentLocation));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(firstDocumentLocationFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
            
            //not necessary to change the archive URI in the workspace DB, since it's already the correct one
            oneOf(mockSecondNode).getArchiveURI(); will(returnValue(firstRefURI));
            oneOf(mockHandleMatcher).areHandlesEquivalent(firstRefURI, firstRefURI); will(returnValue(Boolean.TRUE));
                
            //link parent node with node that matches the reference, ONLY in DB
            oneOf(mockWorkspaceNodeLinkManager).linkNodesOnlyInDb(mockFirstNode, mockSecondNode); will(throwException(expectedException));
            //for logging
            oneOf(mockFirstNode).getWorkspaceNodeID(); will(returnValue(firstNodeID));
            oneOf(mockSecondNode).getWorkspaceNodeID(); will(returnValue(secondNodeID));
        }});
        
        workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, failedLinks);
        
        assertFalse("Map of failed links should not be empty", failedLinks.isEmpty());
        assertTrue("Map of failed links should contain entry containing firstNode (parent) and firstReference (child)",
                failedLinks.contains(mockFirstReference));
    }
    
    @Test
    public void matchTwoReferences_WithoutLocalUrl() throws URISyntaxException, WorkspaceException, MalformedURLException, IOException, TransformerException, MetadataException {

        final int workspaceID = 10;
        
        //first URI is a handle
        final URI firstRefURI = new URI("hdl:" + UUID.randomUUID().toString());
        //second URI is not a handle
        final URI secondRefURI = new URI("parent/child.txt");
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        
        final URL thirdNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/anotherChild.txt");
        final URI thirdNodeURI = thirdNodeURL.toURI();
        
        final URI firstDocumentLocation = new URI("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<WorkspaceNode>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        nodesToCheck.add(mockThirdNode);
        
        final List<Reference> references = new ArrayList<Reference>();
        references.add(mockFirstReference);
        references.add(mockSecondReference);
        
        Collection<Reference> failedLinks = new ArrayList<Reference>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getDocumentReferences(); will(returnValue(references));
            
            //loop over references
            //first reference contains a handle
            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            oneOf(mockMetadataApiHandleUtil).isHandleUri(firstRefURI); will(returnValue(Boolean.TRUE));
            
            //matches second node
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForHandle(workspaceID, nodesToCheck, firstRefURI);
                will(returnValue(mockSecondNode));
                
            oneOf(mockSecondNode).getWorkspaceURL(); will(returnValue(secondNodeURL));
            
            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).setLocation(secondNodeURL);

            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(firstDocumentLocation));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(firstDocumentLocationFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
            
            //not necessary to change the archive URI in the workspace DB, since it's already the correct one
            oneOf(mockSecondNode).getArchiveURI(); will(returnValue(firstRefURI));
            oneOf(mockHandleMatcher).areHandlesEquivalent(firstRefURI, firstRefURI); will(returnValue(Boolean.TRUE));
                
            //link parent node with node that matches the reference, ONLY in DB
            oneOf(mockWorkspaceNodeLinkManager).linkNodesOnlyInDb(mockFirstNode, mockSecondNode);
            
            //second iteration
            //first reference contains a URI
            oneOf(mockSecondReference).getLocation(); will(returnValue(null));
            oneOf(mockSecondReference).getURI(); will(returnValue(secondRefURI));
            oneOf(mockMetadataApiHandleUtil).isHandleUri(secondRefURI); will(returnValue(Boolean.FALSE));
            
            //matches third node
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, secondRefURI.toString());
                will(returnValue(mockThirdNode));
            
            //change the reference URI to the workspace URL and save the document in the same location
            oneOf(mockThirdNode).getWorkspaceURL(); will(returnValue(thirdNodeURL));

//            exactly(2).of(mockSecondReference).getURI(); will(returnValue(null));
//            oneOf(mockSecondReference).setURI(new URI(""));
            oneOf(mockSecondReference).getLocation(); will(returnValue(null));
            oneOf(mockSecondReference).setLocation(thirdNodeURL);
            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(firstDocumentLocation));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(firstDocumentLocationFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
            
            //link parent node with node that matches the reference, ONLY in DB
            oneOf(mockWorkspaceNodeLinkManager).linkNodesOnlyInDb(mockFirstNode, mockThirdNode);
        }});
        
        workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, failedLinks);
        
        assertTrue("Map of failed links should be empty", failedLinks.isEmpty());
    }
    
    @Test
    public void noMatchForReference_WithoutLocalUrl() throws URISyntaxException, MetadataException, IOException, TransformerException {
        
        //TODO if a match is never found, what should happen?
            // reference should be removed from parent file??
                // what else?
        
        final int workspaceID = 10;
        
        //URI is not a handle
        final URI firstRefURI = new URI("parent/child.txt");
        
        final URI firstDocumentLocation = new URI("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<WorkspaceNode>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<Reference>();
        references.add(mockFirstReference);
        
        Collection<Reference> failedLinks = new ArrayList<Reference>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getDocumentReferences(); will(returnValue(references));
            
            //loop over references
            //first reference contains a URI
            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            oneOf(mockMetadataApiHandleUtil).isHandleUri(firstRefURI); will(returnValue(Boolean.FALSE));
            
            //no matches
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefURI.toString());
                will(returnValue(null));
            //an attempt is made to check if the reference corresponds to an external node
            oneOf(mockWorkspaceUploadNodeMatcher).findExternalNodeForUri(workspaceID, firstRefURI);
                will(returnValue(null));
                
            //reference without matches is removed from metadata file
            oneOf(mockMetadataDocument).removeDocumentReference(mockFirstReference);
            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(firstDocumentLocation));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(firstDocumentLocationFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
        }});
        
        workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, failedLinks);
        
        assertTrue("Map of failed links should be empty", failedLinks.isEmpty());
    }
    
    @Test
    public void noMatchForReference_WithoutLocalUrl_WithHandle() throws URISyntaxException, MetadataException, IOException, TransformerException {
        
        //TODO if a match is never found, what should happen?
            // reference should be removed from parent file??
                // what else?
        
        final int workspaceID = 10;
        final int firstNodeID = 101;
        final int secondNodeID = 102;
        
        //URI is a handle
        final URI firstRefURI = new URI("hdl:" + UUID.randomUUID().toString());
        
        final URI firstDocumentLocation = new URI("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<WorkspaceNode>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<Reference>();
        references.add(mockFirstReference);
        
        Collection<Reference> failedLinks = new ArrayList<Reference>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getDocumentReferences(); will(returnValue(references));
            
            //loop over references
            //first reference contains a handle
            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            oneOf(mockMetadataApiHandleUtil).isHandleUri(firstRefURI); will(returnValue(Boolean.TRUE));
            
            //matches second node
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForHandle(workspaceID, nodesToCheck, firstRefURI);
                will(returnValue(null));
            
            //reference without matches is removed from metadata file
            oneOf(mockMetadataDocument).removeDocumentReference(mockFirstReference);
            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(firstDocumentLocation));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(firstDocumentLocationFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
        }});
        
        workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, failedLinks);
        
        assertTrue("Map of failed links should be empty", failedLinks.isEmpty());
    }
    
    @Test
    public void noMatchForReference_RemovingReferenceThrowsException() throws URISyntaxException, MetadataException {
        
        //TODO if a match is never found, what should happen?
            // reference should be removed from parent file??
                // what else?
        
        final int workspaceID = 10;
        final int firstNodeID = 101;
        
        //URI is not a handle
        final URI firstRefURI = new URI("parent/child.txt");
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<WorkspaceNode>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<Reference>();
        references.add(mockFirstReference);
        
        Collection<Reference> failedLinks = new ArrayList<Reference>();
        
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getDocumentReferences(); will(returnValue(references));
            
            //loop over references
            //first reference contains a URI
            oneOf(mockFirstReference).getLocation(); will(returnValue(null));
            oneOf(mockFirstReference).getURI(); will(returnValue(firstRefURI));
            oneOf(mockMetadataApiHandleUtil).isHandleUri(firstRefURI); will(returnValue(Boolean.FALSE));
            
            //no matches
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefURI.toString());
                will(returnValue(null));
            //an attempt is made to check if the reference corresponds to an external node
            oneOf(mockWorkspaceUploadNodeMatcher).findExternalNodeForUri(workspaceID, firstRefURI);
                will(returnValue(null));
                
            //reference without matches is not removed because an exception is thrown
            oneOf(mockMetadataDocument).removeDocumentReference(mockFirstReference);
                will(throwException(expectedException));
            //for logging
            oneOf(mockFirstNode).getWorkspaceNodeID(); will(returnValue(firstNodeID));
        }});
        
        workspaceUploadReferenceHandler.matchReferencesWithNodes(workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, failedLinks);
        
        assertTrue("Map of failed links should be empty", failedLinks.isEmpty());
    }
}