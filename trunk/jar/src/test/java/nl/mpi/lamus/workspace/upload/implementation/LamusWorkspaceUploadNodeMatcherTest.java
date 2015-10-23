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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.model.Reference;
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
public class LamusWorkspaceUploadNodeMatcherTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private LamusWorkspaceUploadNodeMatcher workspaceUploadNodeMatcher;
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock HandleParser mockHandleParser;
    @Mock WorkspaceNodeFactory mockWorkspaceNodeFactory;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock NodeUtil mockNodeUtil;
    
    @Mock WorkspaceNode mockFirstNode;
    @Mock WorkspaceNode mockSecondNode;
    @Mock WorkspaceNode mockSomeOtherNode;
    @Mock WorkspaceNode mockYetAnotherNode;
    @Mock WorkspaceNode mockExternalNode;
    @Mock CorpusNode mockCorpusNode;
    @Mock Reference mockReference;
    
    
    
    public LamusWorkspaceUploadNodeMatcherTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        workspaceUploadNodeMatcher = new LamusWorkspaceUploadNodeMatcher(
                mockCorpusStructureProvider, mockNodeResolver,
                mockHandleParser, mockWorkspaceNodeFactory,
                mockWorkspaceDao, mockNodeUtil);
    }
    
    @After
    public void tearDown() {
    }

    
    
    @Test
    public void findNodeForMetadataHandle() throws MalformedURLException {
        
        final int workspaceID = 10;
        final URI handleToMatch = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        //handle will match the URI of the first node
        final URI firstNodeURI = handleToMatch;
        
        context.checking(new Expectations() {{
            
            //loop
            //first node is metadata and matches the given handle, so it won't continue the loop and return the node
            oneOf(mockNodeUtil).isNodeMetadata(mockFirstNode); will(returnValue(Boolean.TRUE));
            oneOf(mockFirstNode).getArchiveURI(); will(returnValue(firstNodeURI));
            oneOf(mockHandleParser).areHandlesEquivalent(handleToMatch, firstNodeURI); will(returnValue(Boolean.TRUE));
        }});
        
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findNodeForHandle(workspaceID, nodesToCheck, handleToMatch);
        
        assertNotNull("Matching node should not be null", retrievedNode);
        assertEquals("Matching node different from expected", mockFirstNode, retrievedNode);
    }
    
    @Test
    public void findNodeForResourceHandleWithMatchInWorkspace() throws MalformedURLException {
        
        final int workspaceID = 10;
        final URI handleToMatch = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        //handle will not match the URI of the first node
        final URI firstNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final Collection<WorkspaceNode> matchesInWorkspace = new ArrayList<>();
        matchesInWorkspace.add(mockSomeOtherNode);
        
        context.checking(new Expectations() {{
            
            //loop
            //first node is metadata and doesn't match the given handle, so it will continue the loop
            oneOf(mockNodeUtil).isNodeMetadata(mockFirstNode); will(returnValue(Boolean.TRUE));
            oneOf(mockFirstNode).getArchiveURI(); will(returnValue(firstNodeURI));
            oneOf(mockHandleParser).areHandlesEquivalent(handleToMatch, firstNodeURI); will(returnValue(Boolean.FALSE));
            
            //next iteration
            //second node is resource, so a match will be searched in the corpusstructure DB
            // and its URL will not match the one of the current node, so it will exit the loop
            //  and create an external node pointing to the matched corpus node
            oneOf(mockNodeUtil).isNodeMetadata(mockSecondNode); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceDao).getWorkspaceNodeByArchiveURI(handleToMatch); will(returnValue(matchesInWorkspace));
        }});
        
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findNodeForHandle(workspaceID, nodesToCheck, handleToMatch);
        
        assertNotNull("Matching node should not be null", retrievedNode);
        assertEquals("Matching node different from expected", mockSomeOtherNode, retrievedNode);
    }
    
    @Test
    public void findNodeForResourceHandleWithSeveralMatchesInWorkspace() throws MalformedURLException {
        
        final int workspaceID = 10;
        final URI handleToMatch = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        //handle will not match the URI of the first node
        final URI firstNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final Collection<WorkspaceNode> matchesInWorkspace = new ArrayList<>();
        matchesInWorkspace.add(mockSomeOtherNode);
        matchesInWorkspace.add(mockYetAnotherNode);
        
        final String expectedExceptionMessage = "Several matches found in workspace for URI " + handleToMatch;
        
        context.checking(new Expectations() {{
            
            //loop
            //first node is metadata and doesn't match the given handle, so it will continue the loop
            oneOf(mockNodeUtil).isNodeMetadata(mockFirstNode); will(returnValue(Boolean.TRUE));
            oneOf(mockFirstNode).getArchiveURI(); will(returnValue(firstNodeURI));
            oneOf(mockHandleParser).areHandlesEquivalent(handleToMatch, firstNodeURI); will(returnValue(Boolean.FALSE));
            
            //next iteration
            //second node is resource, so a match will be searched in the corpusstructure DB
            // and its URL will not match the one of the current node, so it will exit the loop
            //  and create an external node pointing to the matched corpus node
            oneOf(mockNodeUtil).isNodeMetadata(mockSecondNode); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceDao).getWorkspaceNodeByArchiveURI(handleToMatch); will(returnValue(matchesInWorkspace));
        }});
        
        try {
            workspaceUploadNodeMatcher.findNodeForHandle(workspaceID, nodesToCheck, handleToMatch);
            fail("should have thrown exception");
        } catch(IllegalStateException ex) {
            assertEquals(expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void findNodeForResourceHandlePointingToArchive() throws MalformedURLException {
        
        final int workspaceID = 10;
        final URI handleToMatch = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        //handle will not match the URI of the first node
        final URI firstNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String commonPath = "parent";
        final URL archiveUrlInDb = new URL("file:/archive/path/" + commonPath + "/child.txt");
        
        final Collection<WorkspaceNode> emptyMatchesInWorkspace = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            //loop
            //first node is metadata and doesn't match the given handle, so it will continue the loop
            oneOf(mockNodeUtil).isNodeMetadata(mockFirstNode); will(returnValue(Boolean.TRUE));
            oneOf(mockFirstNode).getArchiveURI(); will(returnValue(firstNodeURI));
            oneOf(mockHandleParser).areHandlesEquivalent(handleToMatch, firstNodeURI); will(returnValue(Boolean.FALSE));
            
            //next iteration
            //second node is resource, so a match will be searched in the corpusstructure DB
            // and its URL will not match the one of the current node, so it will exit the loop
            //  and create an external node pointing to the matched corpus node
            oneOf(mockNodeUtil).isNodeMetadata(mockSecondNode); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceDao).getWorkspaceNodeByArchiveURI(handleToMatch); will(returnValue(emptyMatchesInWorkspace));
            
            oneOf(mockCorpusStructureProvider).getNode(handleToMatch); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(archiveUrlInDb));

            oneOf(mockWorkspaceNodeFactory).getNewExternalNodeFromArchive(workspaceID, mockCorpusNode, handleToMatch, archiveUrlInDb);
                will(returnValue(mockSomeOtherNode));
            oneOf(mockWorkspaceDao).addWorkspaceNode(mockSomeOtherNode);
        }});
        
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findNodeForHandle(workspaceID, nodesToCheck, handleToMatch);
        
        assertNotNull("Matching node should not be null", retrievedNode);
        assertEquals("Matching node different from expected", mockSomeOtherNode, retrievedNode);
    }
    
    @Test
    public void findNodeForResourceHandleWithoutMatchInTheArchive() throws MalformedURLException {
        
        final int workspaceID = 10;
        final URI handleToMatch = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        //handle will not match the URI of the first node
        final URI firstNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final Collection<WorkspaceNode> emptyMatchesInWorkspace = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            //loop
            //first node is metadata and doesn't match the given handle, so it will continue the loop
            oneOf(mockNodeUtil).isNodeMetadata(mockFirstNode); will(returnValue(Boolean.TRUE));
            oneOf(mockFirstNode).getArchiveURI(); will(returnValue(firstNodeURI));
            oneOf(mockHandleParser).areHandlesEquivalent(handleToMatch, firstNodeURI); will(returnValue(Boolean.FALSE));
            
            //next iteration
            //second node is resource, so a match will be searched in the corpusstructure DB
            // and since the retrieved archive URL is null, it will continue (in this case exit) the loop
            oneOf(mockNodeUtil).isNodeMetadata(mockSecondNode); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceDao).getWorkspaceNodeByArchiveURI(handleToMatch); will(returnValue(emptyMatchesInWorkspace));

            //since the node could not be found in the archive, an external archive node won't be created, and null will be returned instead
            oneOf(mockCorpusStructureProvider).getNode(handleToMatch); will(returnValue(null));
            
        }});
        
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findNodeForHandle(workspaceID, nodesToCheck, handleToMatch);
        
        assertNull("Matching node should be null", retrievedNode);
    }
    
    @Test
    public void findNodeForResourceHandleWithNullArchiveURIRetrieved() throws MalformedURLException {
        
        final int workspaceID = 10;
        final URI handleToMatch = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final IllegalArgumentException secondExpectedException = new IllegalArgumentException("some error message");
        
        final Collection<WorkspaceNode> emptyMatchesInWorkspace = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            //loop
            //first node is metadata and retrieved archive URI is null, so it will continue the loop
            oneOf(mockNodeUtil).isNodeMetadata(mockFirstNode); will(returnValue(Boolean.TRUE));
            oneOf(mockFirstNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockHandleParser).areHandlesEquivalent(handleToMatch, null); will(throwException(secondExpectedException));
            //extra call from the logger
            oneOf(mockFirstNode).getArchiveURI(); will(returnValue(null));
            
            //next iteration
            //second node is resource, so a match will be searched in the corpusstructure DB
            // and since the retrieved archive URL is null, it will continue (in this case exit) the loop
            oneOf(mockNodeUtil).isNodeMetadata(mockSecondNode); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceDao).getWorkspaceNodeByArchiveURI(handleToMatch); will(returnValue(emptyMatchesInWorkspace));

            //since the node could not be found in the archive, an external archive node won't be created, and null will be returned instead
            oneOf(mockCorpusStructureProvider).getNode(handleToMatch); will(returnValue(null));
            
        }});
        
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findNodeForHandle(workspaceID, nodesToCheck, handleToMatch);
        
        assertNull("Matching node should be null", retrievedNode);
    }

    @Test
    public void findNodeForPathMatches() throws MalformedURLException {
        
        final int workspaceID = 10;
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final String commonPath = "parent";
        
        final File wsUploadDirectory = new File("file:/workspaces/upload/" + workspaceID);
        final URL firstNodeWorkspaceURL = new URL(wsUploadDirectory.getPath() + File.separator + commonPath + ".cmdi");
        final URL secondNodeWorkspaceURL = new URL(wsUploadDirectory.getPath() + File.separator + commonPath + "/child.txt");
        
        //reference will match the second node
        final String referencePath = "parent/child.txt";
        
        context.checking(new Expectations() {{
            
            //loop
            //first node doesn't match the given reference URI, so it will continue the loop
            oneOf(mockFirstNode).getWorkspaceURL(); will(returnValue(firstNodeWorkspaceURL));
            
            //second iteration
            //second node matches the given reference URI, so it will return this node
            oneOf(mockSecondNode).getWorkspaceURL(); will(returnValue(secondNodeWorkspaceURL));
        }});
        
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, referencePath);
        
        assertNotNull("Matching node should not be null", retrievedNode);
        assertEquals("Matching node different from expected", mockSecondNode, retrievedNode);
    }
    
    @Test
    public void findNodeForRelativePathMatches() throws MalformedURLException {
        
        final int workspaceID = 10;
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final File wsUploadDirectory = new File("file:/workspaces/upload/" + workspaceID);
        final URL firstNodeWorkspaceURL = new URL(wsUploadDirectory.getPath() + File.separator + "Metadata" + File.separator + "parent.cmdi");
        final URL secondNodeWorkspaceURL = new URL(wsUploadDirectory.getPath() + File.separator + "Media" + File.separator + "child.jpg");
        
        //reference will match the second node
        final String referencePath = ".." + File.separator + "Media" + File.separator + "child.jpg";
        
        context.checking(new Expectations() {{
            
            //loop
            //first node doesn't match the given reference URI, so it will continue the loop
            oneOf(mockFirstNode).getWorkspaceURL(); will(returnValue(firstNodeWorkspaceURL));
            
            //second iteration
            //second node matches the given reference URI, so it will return this node
            oneOf(mockSecondNode).getWorkspaceURL(); will(returnValue(secondNodeWorkspaceURL));
        }});
        
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, referencePath);
        
        assertNotNull("Matching node should not be null", retrievedNode);
        assertEquals("Matching node different from expected", mockSecondNode, retrievedNode);
    }
    
    @Test
    public void findNodeForPathDoesNotMatch() throws MalformedURLException {
        
        final int workspaceID = 10;
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        final String commonPath = "parent";
        
        final File wsUploadDirectory = new File("file:/workspaces/upload/" + workspaceID);
        final URL firstNodeWorkspaceURL = new URL(wsUploadDirectory.getPath() + File.separator + commonPath + ".cmdi");
        final URL secondNodeWorkspaceURL = new URL(wsUploadDirectory.getPath() + File.separator + commonPath + "/different_child.txt");
        
        //reference will match the second node
        final String referencepath = "parent/child.txt";
        
        context.checking(new Expectations() {{
            
            //loop
            //first node doesn't match the given reference URI, so it will continue the loop
            oneOf(mockFirstNode).getWorkspaceURL(); will(returnValue(firstNodeWorkspaceURL));
            
            //second iteration
            //second node doesn't match the given reference URI, so it will continue the loop (in this case it will finish)
            oneOf(mockSecondNode).getWorkspaceURL(); will(returnValue(secondNodeWorkspaceURL));
        }});
        
        //no match was found, so a null value will be returned
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, referencepath);
        
        assertNull("Matching node should be null", retrievedNode);
    }
    
    @Test
    public void findNodeForEmptyPath() {
        
        final Collection<WorkspaceNode> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(mockFirstNode);
        nodesToCheck.add(mockSecondNode);
        
        //no match was found, so a null value will be returned
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findNodeForPath(nodesToCheck, "");
        
        assertNull("Matching node should be null", retrievedNode);
    }

    @Test
    public void findExternalNodeForKnownHandle() throws MalformedURLException {
        
        final int workspaceID = 10;
        
        //reference to a local file (at this point should be external)
        final URI uriToMatch = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).isHandleUriWithKnownPrefix(uriToMatch); will(returnValue(Boolean.TRUE));
        }});
                
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findExternalNodeForUri(workspaceID, uriToMatch);
        
        assertNull("Matching node should be null", retrievedNode);
    }
    
    @Test
    public void findExternalNodeForUnknownHandle() throws MalformedURLException {
        
        final int workspaceID = 10;
        
        //reference to a local file (at this point should be external)
        final URI uriToMatch = URI.create("hdl:55555/" + UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).isHandleUriWithKnownPrefix(uriToMatch); will(returnValue(Boolean.FALSE));
        }});
        
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findExternalNodeForUri(workspaceID, uriToMatch);
        
        assertNull("Retrieved node should be null", retrievedNode);
    }
    
    @Test
    public void findExternalNodeForUriWithFileProtocol() throws MalformedURLException {
        
        final int workspaceID = 10;
        
        //reference to a local file (at this point should be external)
        final URI uriToMatch = URI.create("file:/some/local/folder/parent/child.txt");
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).isHandleUriWithKnownPrefix(uriToMatch); will(returnValue(Boolean.FALSE));
        }});
        
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findExternalNodeForUri(workspaceID, uriToMatch);
        
        assertNull("Matching node should be null", retrievedNode);
    }
    
    @Test
    public void findExternalNodeForUriWithExternalProtocol() throws MalformedURLException {
        
        final int workspaceID = 10;
        
        //reference to an external file
        final URI uriToMatch = URI.create("http:/some/remote/folder/parent/child.txt");
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).isHandleUriWithKnownPrefix(uriToMatch); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceNodeFactory).getNewExternalNode(workspaceID, uriToMatch);
                will(returnValue(mockExternalNode));
            oneOf(mockWorkspaceDao).addWorkspaceNode(mockExternalNode);
        }});
        
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findExternalNodeForUri(workspaceID, uriToMatch);
        
        assertNotNull("Retrieved node should not be null", retrievedNode);
        assertEquals("Retrieved node different from expected", mockExternalNode, retrievedNode);
    }
    
    @Test
    public void findExternalNodeForUriWithInvalidUrl() throws MalformedURLException {
        
        final int workspaceID = 10;
        
        //reference with a URI which is not a URL
        final URI uriToMatch = URI.create("invalidprotocol:/something");
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).isHandleUriWithKnownPrefix(uriToMatch); will(returnValue(Boolean.FALSE));
        }});
                
        //the invalid URL will cause an exception to be thrown, and therefore a null value to be returned
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findExternalNodeForUri(workspaceID, uriToMatch);
        
        assertNull("Retrieved node should be null", retrievedNode);
    }
    
    @Test
    public void findExternalNodeForUriWithNonAbsoluteUrl() throws MalformedURLException {
        
        final int workspaceID = 10;
        
        //reference with a URI which is not a URL
        final URI uriToMatch = URI.create("some/relative/path.cmdi");
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).isHandleUriWithKnownPrefix(uriToMatch); will(returnValue(Boolean.FALSE));
        }});
                
        //the invalid URL will cause an exception to be thrown, and therefore a null value to be returned
        WorkspaceNode retrievedNode = workspaceUploadNodeMatcher.findExternalNodeForUri(workspaceID, uriToMatch);
        
        assertNull("Retrieved node should be null", retrievedNode);
    }
}