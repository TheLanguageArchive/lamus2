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

import nl.mpi.lamus.workspace.importing.implementation.MatchImportProblem;
import nl.mpi.lamus.workspace.importing.implementation.LinkImportProblem;
import nl.mpi.lamus.workspace.importing.implementation.ImportProblem;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadNodeMatcher;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadReferenceHandler;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.MetadataElement;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.type.MetadataElementType;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
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
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private WorkspaceUploadReferenceHandler workspaceUploadReferenceHandler;
    
    @Mock WorkspaceUploadNodeMatcher mockWorkspaceUploadNodeMatcher;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock WorkspaceNodeLinkManager mockWorkspaceNodeLinkManager;
    @Mock HandleParser mockHandleParser;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock MetadataApiBridge mockMetadataApiBridge;
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
    @Mock MetadataElement mockMetadataElement;
    @Mock MetadataElementType mockMetadataElementType;
    
    
    private final int workspaceID = 10;
    private final int firstNodeID = 101;
    private final int secondNodeID = 102;
    private final int thirdNodeID = 103;
    private final int externalNodeID = 104;
    
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
                mockWorkspaceUploadNodeMatcher,
                mockWorkspaceDao, mockWorkspaceNodeLinkManager, mockHandleParser,
                mockMetadataAPI, mockMetadataApiBridge, mockWorkspaceFileHandler);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void nodeWithoutReferences() throws MalformedURLException, WorkspaceException, IOException, TransformerException, MetadataException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        final List<Reference> references = new ArrayList<>();
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //no references, no loop
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                    workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection of failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }
    
    
    @Test
    public void matchOneReference_WithLocalUri_WithEmptyUri() throws MalformedURLException, WorkspaceException, IOException, TransformerException, MetadataException, URISyntaxException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final URI firstRefURI = URI.create("");
        final URI firstRefLocalUri = URI.create("file://absolute/path/to/resource.txt");
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        final URI secondNodeURI = secondNodeURL.toURI();
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithLocalUri_MatchesNode(mockFirstReference, mockSecondNode, firstRefLocalUri, firstRefURI, nodesToCheck);
        //empty URI, so it will be set with the value of the location URL
        clearReferenceUri(mockMetadataDocument, firstDocumentLocation, firstDocumentLocationFile, mockFirstReference, firstRefURI, Boolean.FALSE);
        
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockFirstReference, firstRefURI, Boolean.FALSE, Boolean.TRUE, mockSecondNode,
                secondNodeURL, null, firstRefLocalUri, secondNodeURI, null, firstDocumentLocation, firstDocumentLocationFile);
        
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                    workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection of failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithLocalUri_WithEmptyUri_OneReferenceIsAPage() throws MalformedURLException, WorkspaceException, IOException, TransformerException, MetadataException, URISyntaxException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final URI secondRefURI = URI.create("");
        final URI secondRefLocalUri = URI.create("file://absolute/path/to/resource.txt");
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        final URI secondNodeURI = secondNodeURL.toURI();
        
        final URI secondDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File secondDocumentLocationFile = new File(secondDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        references.add(mockSecondReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsAPage(mockFirstReference);
        
        reference_IsNotAPage(mockSecondReference);
        reference_WithLocalUri_MatchesNode(mockSecondReference, mockSecondNode, secondRefLocalUri, secondRefURI, nodesToCheck);
        //empty URI, so it will be set with the value of the location URL
        clearReferenceUri(mockMetadataDocument, secondDocumentLocation, secondDocumentLocationFile, mockSecondReference, secondRefURI, Boolean.FALSE);
        
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockSecondReference, secondRefURI, Boolean.FALSE, Boolean.TRUE, mockSecondNode,
                secondNodeURL, null, secondRefLocalUri, secondNodeURI, null, secondDocumentLocation, secondDocumentLocationFile);
        
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockSecondReference, Boolean.FALSE, mockSecondNode);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                    workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection of failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithLocalUri_WithEmptyUri_parentHasNoSelfHandle() throws MalformedURLException, WorkspaceException, IOException, TransformerException, MetadataException, URISyntaxException {
        
        final URI firstRefURI = URI.create("");
        final URI firstRefLocalUri = URI.create("file://absolute/path/to/resource.txt");
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        final URI secondNodeURI = secondNodeURL.toURI();
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        initialChecks(mockMetadataDocument, null, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithLocalUri_MatchesNode(mockFirstReference, mockSecondNode, firstRefLocalUri, firstRefURI, nodesToCheck);
        //empty URI, so it will be set with the value of the location URL
        clearReferenceUri(mockMetadataDocument, firstDocumentLocation, firstDocumentLocationFile, mockFirstReference, firstRefURI, Boolean.FALSE);
        
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockFirstReference, firstRefURI, Boolean.FALSE, Boolean.TRUE, mockSecondNode,
                secondNodeURL, null, firstRefLocalUri, secondNodeURI, null, firstDocumentLocation, firstDocumentLocationFile);
        
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                    workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection of failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithLocalUri_WithEmptyUri_ParentWithExternalSelfHandle() throws MalformedURLException, WorkspaceException, IOException, TransformerException, MetadataException, URISyntaxException {
        
        final URI parentDocumentHandle = URI.create("hdl:34444/" + UUID.randomUUID().toString());
        
        final URI firstRefURI = URI.create("");
        final URI firstRefLocalUri = URI.create("file://absolute/path/to/resource.txt");
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        final URI secondNodeURI = secondNodeURL.toURI();
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        documentsWithExternalSelfHandles.put(mockMetadataDocument, mockFirstNode);
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.FALSE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithLocalUri_MatchesNode(mockFirstReference, mockSecondNode, firstRefLocalUri, firstRefURI, nodesToCheck);
        //empty URI, so it will be set with the value of the location URL
        clearReferenceUri(mockMetadataDocument, firstDocumentLocation, firstDocumentLocationFile, mockFirstReference, firstRefURI, Boolean.FALSE);
        
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockFirstReference, firstRefURI, Boolean.FALSE, Boolean.TRUE, mockSecondNode,
                secondNodeURL, null, firstRefLocalUri, secondNodeURI, null, firstDocumentLocation, firstDocumentLocationFile);
        
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                    workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection of failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should have one entry", documentsWithExternalSelfHandles.size() == 1);
    }
    
    @Test
    public void matchOneReference_WithLocalUri_WithHandle() throws MalformedURLException, WorkspaceException, IOException, TransformerException, MetadataException, URISyntaxException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final URI firstRefURI = URI.create("hdl:" + UUID.randomUUID().toString());
        final URI firstRefLocalUri = URI.create("file://absolute/path/to/resource.txt");
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        final URI secondNodeURI = secondNodeURL.toURI();
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithLocalUri_MatchesNode(mockFirstReference, mockSecondNode, firstRefLocalUri, firstRefURI, nodesToCheck);
        //URI is a handle, so URI in DB should be updated
        updateReferenceDbUri_refHasLocalUri(firstRefURI, mockSecondNode, Boolean.TRUE);
        
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockFirstReference, firstRefURI, Boolean.FALSE, Boolean.TRUE, mockSecondNode,
                secondNodeURL, null, firstRefLocalUri, secondNodeURI, null, firstDocumentLocation, firstDocumentLocationFile);
        
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        

        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection of failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithLocalUri_WithHandle_InfoLink() throws MalformedURLException, WorkspaceException, IOException, TransformerException, MetadataException, URISyntaxException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final URI firstRefURI = URI.create("hdl:" + UUID.randomUUID().toString());
        final URI firstRefLocalUri = URI.create("file://absolute/path/to/resource.txt");
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        final URI secondNodeURI = secondNodeURL.toURI();
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithLocalUri_MatchesNode(mockFirstReference, mockSecondNode, firstRefLocalUri, firstRefURI, nodesToCheck);
        //URI is a handle, so URI in DB should be updated
        updateReferenceDbUri_refHasLocalUri(firstRefURI, mockSecondNode, Boolean.TRUE);
        
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockFirstReference, firstRefURI, Boolean.FALSE, Boolean.TRUE, mockSecondNode,
                secondNodeURL, null, firstRefLocalUri, secondNodeURI, null, firstDocumentLocation, firstDocumentLocationFile);
        
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.TRUE, mockSecondNode);
        

        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection of failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithLocalUriButNoMatchThere_WithHandle_WithParentWhichIsTheSame() throws MalformedURLException, WorkspaceException, IOException, TransformerException, MetadataException, URISyntaxException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final URL secondNodeWsURL = null;
        final URL secondNodeArURL = new URL("file:/archive/parent/child.txt");
        final URI secondNodeArURI = secondNodeArURL.toURI();
        
        final URI firstRefURI = URI.create("hdl:" + UUID.randomUUID().toString());
        final URI firstRefLocalUri = URI.create("file://absolute/path/to/resource.txt");
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        existingParents.add(mockThirdNode);
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithLocalUriButNoMatchThere_WithHandle_MatchesNode(mockFirstReference, mockSecondNode, firstRefLocalUri, Boolean.TRUE, Boolean.FALSE, firstRefURI, nodesToCheck, workspaceID);
        
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockFirstReference, firstRefURI, Boolean.TRUE, Boolean.TRUE, mockSecondNode,
                secondNodeWsURL, secondNodeArURL, firstRefLocalUri, secondNodeArURI, firstRefURI, firstDocumentLocation, firstDocumentLocationFile);
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        

        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection of failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithLocalUri_WithHandle_MultipleParents() throws MalformedURLException, WorkspaceException, IOException, TransformerException, MetadataException, URISyntaxException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final String firstRefRawHandle = UUID.randomUUID().toString();
        final URI firstRefURI = URI.create("11142" + firstRefRawHandle);
        final URI completeFirstRefURI = URI.create("hdl:11142/" + firstRefRawHandle);
        
        final URI firstRefLocalUri = URI.create("file://absolute/path/to/resource.txt");
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        final URI secondNodeURI = secondNodeURL.toURI();
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        existingParents.add(mockThirdNode);
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithLocalUri_MatchesNode(mockFirstReference, mockSecondNode, firstRefLocalUri, firstRefURI, nodesToCheck);
        //URI is a handle, so URI in DB should be updated
        updateReferenceDbUri_refHasLocalUri(firstRefURI, mockSecondNode, Boolean.TRUE);
        
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockFirstReference, firstRefURI, Boolean.FALSE, Boolean.TRUE, mockSecondNode,
                secondNodeURL, null, firstRefLocalUri, secondNodeURI, completeFirstRefURI, firstDocumentLocation, firstDocumentLocationFile);
        
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingParents, mockFirstNode, Boolean.TRUE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertFalse("Collection with failed links should not be empty", failedLinks.isEmpty());
        assertTrue("Collection with failed links should have one entry", failedLinks.size() == 1);
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
        
        ImportProblem problem = failedLinks.iterator().next();
        assertTrue("Upload problem different from expected", problem instanceof LinkImportProblem);
        assertEquals("Upload problem has different parent node from expected", mockFirstNode, ((LinkImportProblem) problem).getParentNode());
        assertEquals("Upload problem has different child node from expected", mockSecondNode, ((LinkImportProblem) problem).getChildNode());
    }
    
    @Test
    public void matchOneReference_WithLocalUri_WithUri() throws MalformedURLException, WorkspaceException, IOException, TransformerException, MetadataException, URISyntaxException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final String firstRefRawHandle = UUID.randomUUID().toString();
        final URI firstRefURI = URI.create("11142" + firstRefRawHandle);
        final URI completeFirstRefURI = URI.create("hdl:11142/" + firstRefRawHandle);
        
        final URI firstRefLocalUri = URI.create("file://absolute/path/to/resource.txt");
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        final URI secondNodeURI = secondNodeURL.toURI();
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();

        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithLocalUri_MatchesNode(mockFirstReference, mockSecondNode, firstRefLocalUri, firstRefLocalUri, nodesToCheck);
        //URI is not a handle, so it should be cleared (since the local URL is already present in the localURI attribute)
        clearReferenceUri(mockMetadataDocument, firstDocumentLocation, firstDocumentLocationFile, mockFirstReference, firstRefLocalUri, Boolean.TRUE);
        
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockFirstReference, firstRefURI, Boolean.FALSE, Boolean.TRUE, mockSecondNode,
                secondNodeURL, null, firstRefLocalUri, secondNodeURI, completeFirstRefURI, firstDocumentLocation, firstDocumentLocationFile);
        
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection with failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }

    @Test
    public void matchOneReference_WithoutLocalUri_WithHandle() throws WorkspaceException, MalformedURLException, IOException, TransformerException, MetadataException, URISyntaxException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final String firstRefRawHandle = UUID.randomUUID().toString();
        final URI firstRefURI = URI.create("11142" + firstRefRawHandle);
        final URI completeFirstRefURI = URI.create("hdl:11142/" + firstRefRawHandle);
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        final URI secondNodeURI = secondNodeURL.toURI();
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithoutLocalUri_MatchesNode(mockFirstReference, mockSecondNode, Boolean.TRUE, Boolean.FALSE, firstRefURI, completeFirstRefURI, nodesToCheck, workspaceID);
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockFirstReference, completeFirstRefURI, Boolean.TRUE, Boolean.TRUE, mockSecondNode,
                secondNodeURL, null, null, secondNodeURI, completeFirstRefURI, firstDocumentLocation, firstDocumentLocationFile);
        
        updateHandle(mockMetadataDocument, mockFirstReference, mockSecondNode, firstRefURI, completeFirstRefURI, firstDocumentLocation, firstDocumentLocationFile);
        
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection with failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithoutLocalUrl_WithHandle_MissingDbHandle() throws WorkspaceException, MalformedURLException, IOException, TransformerException, MetadataException, URISyntaxException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        //URI is a handle
        final String firstRefRawHandle = UUID.randomUUID().toString();
        final URI firstRefURI = URI.create("11142" + firstRefRawHandle);
        final URI completeFirstRefURI = URI.create("hdl:11142/" + firstRefRawHandle);
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        final URI secondNodeURI = secondNodeURL.toURI();
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithoutLocalUri_MatchesNode(mockFirstReference, mockSecondNode, Boolean.TRUE, Boolean.FALSE, firstRefURI, completeFirstRefURI, nodesToCheck, workspaceID);
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockFirstReference, completeFirstRefURI, Boolean.TRUE, Boolean.FALSE, mockSecondNode,
                secondNodeURL, null, null, secondNodeURI, null, firstDocumentLocation, firstDocumentLocationFile);
        
        updateHandle(mockMetadataDocument, mockFirstReference, mockSecondNode, firstRefURI, completeFirstRefURI, firstDocumentLocation, firstDocumentLocationFile);
        
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection with failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithoutLocalUrl_WithExternalHandle() throws MetadataException, IOException, TransformerException, WorkspaceException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        //URI is a handle
        final String firstRefRawHandle = UUID.randomUUID().toString();
        final URI firstRefURI = URI.create("11142" + firstRefRawHandle);
        final URI completeFirstRefURI = URI.create("hdl:11142/" + firstRefRawHandle);
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithoutLocalUri_MatchesNode(mockFirstReference, mockExternalNode, Boolean.TRUE, Boolean.TRUE, firstRefURI, completeFirstRefURI, nodesToCheck, workspaceID);
        dealWithMatchedNode(mockExternalNode, externalNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection with failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithoutLocalUrl_WithUri() throws WorkspaceException, MalformedURLException, IOException, TransformerException, MetadataException, URISyntaxException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        //URI is not a handle
        final URI firstRefURI = URI.create("parent/child.txt");
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        final URI secondNodeURI = secondNodeURL.toURI();
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();

        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithoutLocalUri_MatchesNode(mockFirstReference, mockSecondNode, Boolean.FALSE, Boolean.FALSE, firstRefURI, null, nodesToCheck, workspaceID);
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockFirstReference, firstRefURI, Boolean.FALSE, Boolean.FALSE, mockSecondNode,
                secondNodeURL, null, null, secondNodeURI, null, firstDocumentLocation, firstDocumentLocationFile);
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection with failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }
    
    @Test
    public void matchOneReference_WithoutLocalUrl_WithExternalUri() throws WorkspaceException, IOException, TransformerException, MetadataException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        //URI is not a handle
        final URI firstRefURI = URI.create("http://some/external/folder/file.txt");
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithoutLocalUri_MatchesNode(mockFirstReference, mockExternalNode, Boolean.FALSE, Boolean.TRUE, firstRefURI, null, nodesToCheck, workspaceID);
        dealWithMatchedNode(mockExternalNode, externalNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection with failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }
    
    @Test
    public void matchOneReference_LinkingNodesThrowsException() throws WorkspaceException, MalformedURLException, IOException, TransformerException, MetadataException, URISyntaxException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final String firstRefRawHandle = UUID.randomUUID().toString();
        final URI firstRefURI = URI.create("11142" + firstRefRawHandle);
        final URI completeFirstRefURI = URI.create("hdl:11142/" + firstRefRawHandle);
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        final URI secondNodeURI = secondNodeURL.toURI();
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Collection<WorkspaceNode> existingParents = new ArrayList<>();
        
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithoutLocalUri_MatchesNode(mockFirstReference, mockSecondNode, Boolean.TRUE, Boolean.FALSE, firstRefURI, completeFirstRefURI, nodesToCheck, workspaceID);
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockFirstReference, completeFirstRefURI, Boolean.TRUE, Boolean.TRUE, mockSecondNode, secondNodeURL, null, null, secondNodeURI, completeFirstRefURI, firstDocumentLocation, firstDocumentLocationFile);
        
        updateHandle(mockMetadataDocument, mockFirstReference, mockSecondNode, firstRefURI, completeFirstRefURI, firstDocumentLocation, firstDocumentLocationFile);
        
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingParents, mockFirstNode, Boolean.FALSE, firstNodeID, expectedException);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertFalse("Collection with failed links should not be empty", failedLinks.isEmpty());
        assertTrue("Collection with failed links should have one entry", failedLinks.size() == 1);
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
        
        ImportProblem problem = failedLinks.iterator().next();
        assertTrue("Upload problem different from expected", problem instanceof LinkImportProblem);
        assertEquals("Upload problem has different parent node from expected", mockFirstNode, ((LinkImportProblem) problem).getParentNode());
        assertEquals("Upload problem has different child node from expected", mockSecondNode, ((LinkImportProblem) problem).getChildNode());
    }
    
    @Test
    public void matchTwoReferences_WithoutLocalUrl() throws WorkspaceException, MalformedURLException, IOException, TransformerException, MetadataException, URISyntaxException {

        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        //first URI is a handle
        final String firstRefRawHandle = UUID.randomUUID().toString();
        final URI firstRefURI = URI.create("11142" + firstRefRawHandle);
        final URI completeFirstRefURI = URI.create("hdl:11142/" + firstRefRawHandle);
        //second URI is not a handle
        final URI secondRefURI = URI.create("parent/child.txt");
        
        final URL secondNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/child.txt");
        final URI secondNodeURI = secondNodeURL.toURI();
        
        final URL thirdNodeURL = new URL("file:/workspaces/" + workspaceID + "/upload/parent/anotherChild.txt");
        final URI thirdNodeURI = thirdNodeURL.toURI();
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        nodesToCheck.add(mockThirdNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        references.add(mockSecondReference);
        
        final Collection<WorkspaceNode> existingSecondNodeParents = new ArrayList<>();
        final Collection<WorkspaceNode> existingThirdNodeParents = new ArrayList<>();
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithoutLocalUri_MatchesNode(mockFirstReference, mockSecondNode, Boolean.TRUE, Boolean.FALSE, firstRefURI, completeFirstRefURI, nodesToCheck, workspaceID);
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockFirstReference, completeFirstRefURI, Boolean.TRUE, Boolean.TRUE, mockSecondNode,
                secondNodeURL, null, null, secondNodeURI, completeFirstRefURI, firstDocumentLocation, firstDocumentLocationFile);
        
        updateHandle(mockMetadataDocument, mockFirstReference, mockSecondNode, firstRefURI, completeFirstRefURI, firstDocumentLocation, firstDocumentLocationFile);
        
        dealWithMatchedNode(mockSecondNode, secondNodeID, existingSecondNodeParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockFirstReference, Boolean.FALSE, mockSecondNode);
        
        reference_IsNotAPage(mockSecondReference);
        reference_WithoutLocalUri_MatchesNode(mockSecondReference, mockThirdNode, Boolean.FALSE, Boolean.FALSE, secondRefURI, null, nodesToCheck, workspaceID);
        //change the reference URI to the workspace URL and save the document in the same location
        updateLocalUri(mockMetadataDocument, mockSecondReference, secondRefURI, Boolean.FALSE, Boolean.FALSE, mockThirdNode,
                thirdNodeURL, null, null, thirdNodeURI, null, firstDocumentLocation, firstDocumentLocationFile);
        dealWithMatchedNode(mockThirdNode, thirdNodeID, existingThirdNodeParents, mockFirstNode, Boolean.FALSE, firstNodeID, null);
        isRefInfoLink(mockMetadataDocument, mockSecondReference, Boolean.FALSE, mockThirdNode);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertTrue("Collection with failed links should be empty", failedLinks.isEmpty());
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
    }
    
    @Test
    public void noMatchForReference_WithoutLocalUrl() throws MetadataException, IOException, TransformerException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        //URI is not a handle
        final URI firstRefURI = URI.create("parent/child.txt");
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithoutMatch(mockFirstReference, Boolean.FALSE, firstRefURI, nodesToCheck, workspaceID);
        
        removeReference(mockMetadataDocument, mockFirstNode, firstNodeID, firstDocumentLocation,
                firstDocumentLocationFile, mockFirstReference, firstRefURI, null);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertFalse("Collection with failed links should not be empty", failedLinks.isEmpty());
        assertTrue("Collection with failed links should have one entry", failedLinks.size() == 1);
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
        
        ImportProblem problem = failedLinks.iterator().next();
        assertTrue("Upload problem different from expected", problem instanceof MatchImportProblem);
        assertEquals("Upload problem has different parent node from expected", mockFirstNode, ((MatchImportProblem) problem).getParentNode());
        assertEquals("Upload problem has different child node from expected", mockFirstReference, ((MatchImportProblem) problem).getChildReference());
    }
    
    @Test
    public void noMatchForReference_WithoutLocalUrl_WithHandle() throws MetadataException, IOException, TransformerException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        //URI is a handle
        final URI firstRefURI = URI.create("hdl:" + UUID.randomUUID().toString());
        
        final URI firstDocumentLocation = URI.create("file:/workspaces/" + workspaceID + "/upload/parent.cmdi");
        final File firstDocumentLocationFile = new File(firstDocumentLocation.getPath());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithoutMatch(mockFirstReference, Boolean.TRUE, firstRefURI, nodesToCheck, workspaceID);
        
        removeReference(mockMetadataDocument, mockFirstNode, firstNodeID, firstDocumentLocation, firstDocumentLocationFile, mockFirstReference, firstRefURI, null);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertFalse("Collection with failed links should not be empty", failedLinks.isEmpty());
        assertTrue("Collection with failed links should have one entry", failedLinks.size() == 1);
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
        
        ImportProblem problem = failedLinks.iterator().next();
        assertTrue("Upload problem different from expected", problem instanceof MatchImportProblem);
        assertEquals("Upload problem has different parent node from expected", mockFirstNode, ((MatchImportProblem) problem).getParentNode());
        assertEquals("Upload problem has different child node from expected", mockFirstReference, ((MatchImportProblem) problem).getChildReference());
    }
    
    @Test
    public void noMatchForReference_RemovingReferenceThrowsException() throws MetadataException, IOException, TransformerException {
        
        final URI parentDocumentHandle = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        //URI is not a handle
        final URI firstRefURI = URI.create("parent/child.txt");
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final List<Reference> references = new ArrayList<>();
        references.add(mockFirstReference);
        
        final MetadataException expectedException = new MetadataException("some exception message");
        
        final Map<MetadataDocument, WorkspaceNode> documentsWithExternalSelfHandles = new HashMap<>();
        
        
        initialChecks(mockMetadataDocument, parentDocumentHandle, Boolean.TRUE, references);
        //loop over references
        reference_IsNotAPage(mockFirstReference);
        reference_WithoutMatch(mockFirstReference, Boolean.FALSE, firstRefURI, nodesToCheck, workspaceID);
        
        removeReference(mockMetadataDocument, mockFirstNode, firstNodeID, null, null, mockFirstReference, firstRefURI, expectedException);
        
        
        Collection<ImportProblem> failedLinks =
                workspaceUploadReferenceHandler.matchReferencesWithNodes(
                workspaceID, nodesToCheck, mockFirstNode, mockMetadataDocument, documentsWithExternalSelfHandles);
        
        assertFalse("Collection with failed links should not be empty", failedLinks.isEmpty());
        assertTrue("Collection with failed links should have one entry", failedLinks.size() == 1);
        assertTrue("Map of documents with external self-handle should be empty", documentsWithExternalSelfHandles.isEmpty());
        
        ImportProblem problem = failedLinks.iterator().next();
        assertTrue("Upload problem different from expected", problem instanceof MatchImportProblem);
        assertEquals("Upload problem has different parent node from expected", mockFirstNode, ((MatchImportProblem) problem).getParentNode());
        assertEquals("Upload problem has different child node from expected", mockFirstReference, ((MatchImportProblem) problem).getChildReference());
    }
    
    
    private void initialChecks(final ReferencingMetadataDocument mockDocument, final URI parentDocumentHandle, final boolean parentHandlePrefixKnown, final List<Reference> references) {
        
        context.checking(new Expectations() {{
            oneOf(mockMetadataApiBridge).getSelfHandleFromDocument(mockDocument); will(returnValue(parentDocumentHandle));
            oneOf(mockHandleParser).isHandleUriWithKnownPrefix(parentDocumentHandle); will(returnValue(parentHandlePrefixKnown));
            
            oneOf(mockDocument).getDocumentReferences(); will(returnValue(references));
        }});
    }
    
    private void reference_IsAPage(final Reference mockReference) {
        
        context.checking(new Expectations() {{
            oneOf(mockMetadataApiBridge).isReferenceTypeAPage(mockReference); will(returnValue(Boolean.TRUE));
        }});
    }
    
    private void reference_IsNotAPage(final Reference mockReference) {
        
        context.checking(new Expectations() {{
            oneOf(mockMetadataApiBridge).isReferenceTypeAPage(mockReference); will(returnValue(Boolean.FALSE));
        }});
    }
    
    private void reference_WithLocalUri_MatchesNode(final Reference mockReference, final WorkspaceNode mockNode,
            final URI firstRefLocalUri, final URI firstRefURI, final Collection<WorkspaceNode> nodesToCheck) {
        
        context.checking(new Expectations() {{
            //first reference contains a localURI
            oneOf(mockReference).getLocation(); will(returnValue(firstRefLocalUri));
            oneOf(mockReference).getURI(); will(returnValue(firstRefURI));
            //matches second node
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefLocalUri.toString());
                will(returnValue(mockNode));
        }});
    }
    
    private void reference_WithoutLocalUri_MatchesNode(final Reference mockReference, final WorkspaceNode mockNode,
            final boolean hasHandle, final boolean isExternal, final URI firstRefURI, final URI completeFirstRefURI,
            final Collection<WorkspaceNode> nodesToCheck, final int workspaceID) {
        
        context.checking(new Expectations() {{
            //first reference contains a handle
            oneOf(mockReference).getLocation(); will(returnValue(null));
            oneOf(mockReference).getURI(); will(returnValue(firstRefURI));
            oneOf(mockHandleParser).isHandleUriWithKnownPrefix(firstRefURI); will(returnValue(hasHandle));
        }});
        
        if(hasHandle) {
            context.checking(new Expectations() {{
                oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(firstRefURI); will(returnValue(completeFirstRefURI));
                if(!isExternal) {
                    //matches second node
                    oneOf(mockWorkspaceUploadNodeMatcher).findNodeForHandle(workspaceID, nodesToCheck, completeFirstRefURI);
                        will(returnValue(mockNode));
                } else {
                    //doesn't match any node
                    oneOf(mockWorkspaceUploadNodeMatcher).findNodeForHandle(workspaceID, nodesToCheck, completeFirstRefURI);
                        will(returnValue(null));
                }
            }});
        } else {
            context.checking(new Expectations() {{
                if(!isExternal) {
                    //matches second node
                    oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefURI.toString());
                        will(returnValue(mockNode));
                } else {
                    //doesn't match any node
                    oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefURI.toString());
                        will(returnValue(null));
                }
            }});
        }
        
        if(isExternal) {
            context.checking(new Expectations() {{
                 //since a match was not found, perhaps it's an external node
                oneOf(mockWorkspaceUploadNodeMatcher).findExternalNodeForUri(workspaceID, firstRefURI);
                    will(returnValue(mockNode));
            }});
        }
    }
    
    private void reference_WithLocalUriButNoMatchThere_WithHandle_MatchesNode(final Reference mockReference, final WorkspaceNode mockNode, final URI firstRefLocalUri,
            final boolean hasHandle, final boolean isExternal, final URI firstRefURI, final Collection<WorkspaceNode> nodesToCheck, final int workspaceID) {
        
        context.checking(new Expectations() {{
            //first reference contains a localURI
            oneOf(mockReference).getLocation(); will(returnValue(firstRefLocalUri));
            oneOf(mockReference).getURI(); will(returnValue(firstRefURI));
            //localURI doesn't match any node
            oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefLocalUri.toString());
                will(returnValue(null));

            oneOf(mockHandleParser).isHandleUriWithKnownPrefix(firstRefURI); will(returnValue(hasHandle));
        }});
        
        if(hasHandle) {
            context.checking(new Expectations() {{
                oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(firstRefURI); will(returnValue(firstRefURI));
                if(!isExternal) {
                    //matches second node
                    oneOf(mockWorkspaceUploadNodeMatcher).findNodeForHandle(workspaceID, nodesToCheck, firstRefURI);
                        will(returnValue(mockNode));
                } else {
                    //doesn't match any node
                    oneOf(mockWorkspaceUploadNodeMatcher).findNodeForHandle(workspaceID, nodesToCheck, firstRefURI);
                        will(returnValue(null));
                }
            }});
        } else {
            context.checking(new Expectations() {{
                if(!isExternal) {
                    //matches second node
                    oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefURI.toString());
                        will(returnValue(mockNode));
                } else {
                    //doesn't match any node
                    oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefURI.toString());
                        will(returnValue(null));
                }
            }});
        }
        
        if(isExternal) {
            context.checking(new Expectations() {{
                 //since a match was not found, perhaps it's an external node
                oneOf(mockWorkspaceUploadNodeMatcher).findExternalNodeForUri(workspaceID, firstRefURI);
                    will(returnValue(mockNode));
            }});
        }
    }
    
    private void reference_WithoutMatch(final Reference mockReference, final boolean hasHandle,
            final URI firstRefURI, final Collection<WorkspaceNode> nodesToCheck, final int workspaceID) {
        
        context.checking(new Expectations() {{
            //first reference contains a URI
            oneOf(mockReference).getLocation(); will(returnValue(null));
            oneOf(mockReference).getURI(); will(returnValue(firstRefURI));
            oneOf(mockHandleParser).isHandleUriWithKnownPrefix(firstRefURI); will(returnValue(hasHandle));
        }});
        
        if(hasHandle) {
            context.checking(new Expectations() {{
                oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(firstRefURI); will(returnValue(firstRefURI));
                //no matches
                oneOf(mockWorkspaceUploadNodeMatcher).findNodeForHandle(workspaceID, nodesToCheck, firstRefURI);
                    will(returnValue(null));
            }});
        } else {
            context.checking(new Expectations() {{
                //no matches
                oneOf(mockWorkspaceUploadNodeMatcher).findNodeForPath(nodesToCheck, firstRefURI.toString());
                    will(returnValue(null));
            }});
        }
        
        context.checking(new Expectations() {{
            //an attempt is made to check if the reference corresponds to an external node
            oneOf(mockWorkspaceUploadNodeMatcher).findExternalNodeForUri(workspaceID, firstRefURI);
                will(returnValue(null));
        }});
    }
    
    private void clearReferenceUri(final ReferencingMetadataDocument mockDocument, final URI documentLocation, final File documentLocationFile,
            final Reference mockReference, final URI referenceUri, final boolean checkIsHandle)
            throws IOException, TransformerException, MetadataException {
        
        if(checkIsHandle) {
            context.checking(new Expectations() {{
                oneOf(mockHandleParser).isHandleUriWithKnownPrefix(referenceUri); will(returnValue(Boolean.FALSE));
            }});
        }
        
        context.checking(new Expectations() {{
            oneOf(mockReference).setURI(URI.create(""));
            oneOf(mockDocument).getFileLocation(); will(returnValue(documentLocation));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(documentLocationFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockDocument, mockStreamResult);
        }});
    }
    
    private void updateReferenceDbUri_refHasLocalUri(final URI referenceUri, final WorkspaceNode mockNode, final boolean isHandle) {
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).isHandleUriWithKnownPrefix(referenceUri); will(returnValue(isHandle));
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(referenceUri); will(returnValue(referenceUri));
            oneOf(mockNode).setArchiveURI(referenceUri);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockNode);
        }});
    }
    
    private void updateReferenceDbUri_refHasNoLocalUri(final WorkspaceNode mockNode, final URI mockNodeArchiveUri, final URI referenceUri, final boolean handlesEquivalent) {
        
        context.checking(new Expectations() {{
            //not necessary to change the archive URI in the workspace DB, since it's already the correct one
            oneOf(mockNode).getArchiveURI(); will(returnValue(mockNodeArchiveUri));
            oneOf(mockHandleParser).areHandlesEquivalent(referenceUri, mockNodeArchiveUri); will(returnValue(handlesEquivalent));
                
            if(!handlesEquivalent) {
                oneOf(mockNode).setArchiveURI(referenceUri);
                oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockNode);
            }
        }});
    }
    
    
    private void updateHandle(final ReferencingMetadataDocument mockDocument, final Reference mockReference,
            final WorkspaceNode mockNode, final URI oldHandle, final URI newHandle,
            final URI documentLocation, final File documentLocationFile) throws IOException, TransformerException, MetadataException {
        
        context.checking(new Expectations() {{
            
            oneOf(mockReference).getURI(); will(returnValue(oldHandle));
            if(!oldHandle.equals(newHandle)) {
                oneOf(mockReference).setURI(newHandle);
            }

            oneOf(mockDocument).getFileLocation(); will(returnValue(documentLocation));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(documentLocationFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockDocument, mockStreamResult);
        }});
    }
    
    private void updateLocalUri(final ReferencingMetadataDocument mockDocument, final Reference mockReference, final URI referenceURI,
            final boolean hasHandleAndNoLocalURI, final boolean handlesEquivalent,
            final WorkspaceNode mockNode, final URL nodeWsURL, final URL nodeArURL, final URI oldLocalUri, final URI newLocalUri, final URI nodeArchiveURI,
            final URI documentLocation, final File documentLocationFile) throws IOException, TransformerException, MetadataException {
        
        context.checking(new Expectations() {{
            oneOf(mockNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            if(nodeWsURL == null) {
                oneOf(mockNode).getArchiveURL(); will(returnValue(nodeArURL));
            }
            
            allowing(mockReference).getLocation(); will(returnValue(oldLocalUri));
            oneOf(mockReference).setLocation(newLocalUri);

            oneOf(mockDocument).getFileLocation(); will(returnValue(documentLocation));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(documentLocationFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockDocument, mockStreamResult);
        }});
        
        if(hasHandleAndNoLocalURI) {
            updateReferenceDbUri_refHasNoLocalUri(mockNode, nodeArchiveURI, referenceURI, handlesEquivalent);
        }
    }
    
    private void dealWithMatchedNode(final WorkspaceNode mockMatchedNode, final int mockMatchedNodeID,
            final Collection<WorkspaceNode> existingParents, final WorkspaceNode mockParentNode, final boolean multipleParents,
            final int mockParentNodeID, final Exception exceptionToThrow) throws WorkspaceException {
        
        context.checking(new Expectations() {{

            //check if the matched node already has parents
            oneOf(mockMatchedNode).getWorkspaceNodeID(); will(returnValue(mockMatchedNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(mockMatchedNodeID); will(returnValue(existingParents));
        }});
        
        if(existingParents.isEmpty()) {
            if(exceptionToThrow == null) {
                context.checking(new Expectations() {{
                    //link parent node with node that matches the reference, ONLY in the DB
                    oneOf(mockWorkspaceNodeLinkManager).linkNodesOnlyInDb(mockParentNode, mockMatchedNode);
                }});
            } else {
                context.checking(new Expectations(){{
                    //link parent node with node that matches the reference, ONLY in DB
                    oneOf(mockWorkspaceNodeLinkManager).linkNodesOnlyInDb(mockParentNode, mockMatchedNode); will(throwException(exceptionToThrow));
                }});
                loggerCalls(mockMatchedNode, mockMatchedNodeID, mockParentNode, mockParentNodeID);
            }
        } else {
            for(final WorkspaceNode mockExistingParent : existingParents) {
                context.checking(new Expectations() {{
                    //doesn't really matter what this is returning... I can manipulate the result of the other method
                    oneOf(mockExistingParent).getArchiveURI(); will(returnValue(URI.create("node:123")));
                    oneOf(mockParentNode).getArchiveURI(); will(returnValue(URI.create("node:123")));
                    oneOf(mockHandleParser).areHandlesEquivalent(with(any(URI.class)), with(any(URI.class))); will(returnValue(!multipleParents));
                }});
            }
            if(multipleParents) {
                loggerCalls(mockMatchedNode, mockMatchedNodeID, mockParentNode, mockParentNodeID);
            } else {
                context.checking(new Expectations() {{
                    oneOf(mockWorkspaceNodeLinkManager).linkNodesOnlyInDb(mockParentNode, mockMatchedNode);
                }});
            }
        }
    }
    
    private void removeReference(final ReferencingMetadataDocument mockDocument, final WorkspaceNode mockNode, final int mockNodeID,
            final URI documentLocation, final File documentLocationFile,
            final Reference mockReference, final URI referenceUri, final Exception exceptionToThrow) throws MetadataException, IOException, TransformerException {
        
        if(exceptionToThrow == null) {
            context.checking(new Expectations() {{
                //reference without matches is removed from metadata file
                oneOf(mockDocument).removeDocumentReference(mockReference);
                oneOf(mockDocument).getFileLocation(); will(returnValue(documentLocation));
                oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(documentLocationFile); will(returnValue(mockStreamResult));
                oneOf(mockMetadataAPI).writeMetadataDocument(mockDocument, mockStreamResult);

                //logger / message
                oneOf(mockReference).getURI(); will(returnValue(referenceUri));
                oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(mockNodeID));
            }});
        } else {
            context.checking(new Expectations() {{
                //reference without matches is not removed because an exception is thrown
                oneOf(mockDocument).removeDocumentReference(mockReference);
                    will(throwException(exceptionToThrow));
                //for logging
                exactly(2).of(mockReference).getURI(); will(returnValue(referenceUri));
                exactly(2).of(mockNode).getWorkspaceNodeID(); will(returnValue(mockNodeID));
            }});
        }
    }
    
    private void isRefInfoLink(final ReferencingMetadataDocument mockDocument,
            final Reference mockReference, final boolean isInfoLink,
            final WorkspaceNode mockMatchedNode) {
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataApiBridge).isReferenceAnInfoLink(mockDocument, mockReference); will(returnValue(isInfoLink));
            
            if(isInfoLink) {
                oneOf(mockMatchedNode).setType(WorkspaceNodeType.RESOURCE_INFO);
                oneOf(mockWorkspaceDao).updateNodeType(mockMatchedNode);
            }
        }});
    }
    
    private void loggerCalls(final WorkspaceNode mockMatchedNode, final int mockMatchedNodeID, final WorkspaceNode mockParentNode, final int mockParentNodeID) {
        
        context.checking(new Expectations() {{
            //logger
            oneOf(mockMatchedNode).getWorkspaceNodeID(); will(returnValue(mockMatchedNodeID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(mockParentNodeID));
        }});
    }
}