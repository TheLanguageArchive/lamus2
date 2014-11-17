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
package nl.mpi.lamus.workspace.management.implementation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.MetadataReference;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.model.ResourceReference;
import nl.mpi.metadata.cmdi.api.model.CMDIDocument;
import nl.mpi.metadata.cmdi.api.model.DataResourceProxy;
import nl.mpi.metadata.cmdi.api.model.MetadataResourceProxy;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({FileUtils.class})
public class LamusWorkspaceNodeLinkManagerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private WorkspaceNodeLinkManager nodeLinkManager;
    
    @Mock WorkspaceNodeLinkFactory mockWorkspaceNodeLinkFactory;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock MetadataApiBridge mockMetadataApiBridge;
    
    @Mock WorkspaceNodeLink mockWorkspaceNodeLink;
    
    @Mock Workspace mockWorkspace;
    @Mock WorkspaceNode mockParentNode;
    @Mock WorkspaceNode mockOtherParentNode;
    @Mock WorkspaceNode mockChildNode;
    @Mock WorkspaceNode mockOldNode;
    @Mock WorkspaceNode mockNewNode;
    @Mock Reference mockChildReference;
    @Mock ResourceProxy mockChildReferenceWithHandle;

    @Mock ReferencingMetadataDocument mockParentDocument;
    @Mock ReferencingMetadataDocument mockOtherParentDocument;
    @Mock MetadataReference mockChildMetadataReference;
    @Mock ResourceReference mockChildResourceReference;
    
    @Mock MetadataDocument mockNotReferencingDocument;
    
    @Mock CMDIDocument mockParentCmdiDocument;
    @Mock MetadataResourceProxy mockChildMetadataResourceProxy;
    @Mock DataResourceProxy mockChildDataResourceProxy;
    @Mock CMDIDocument mockChildCmdiDocument;
    
    @Mock File mockParentFile;
    @Mock StreamResult mockParentStreamResult;
    @Mock File mockChildFile;
    @Mock StreamResult mockChildStreamResult;
    
    
    public LamusWorkspaceNodeLinkManagerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        nodeLinkManager = new LamusWorkspaceNodeLinkManager(
                mockWorkspaceNodeLinkFactory, mockWorkspaceDao,
                mockMetadataAPI, mockWorkspaceFileHandler,
                mockMetadataApiBridge);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void linkNodesWithReference() throws URISyntaxException {
        
        final int workspaceID = 1;
        final int parentNodeID = 1;
        final int childNodeID = 2;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
                
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        nodeLinkManager.linkNodesWithReference(mockWorkspace, mockParentNode, mockChildNode, mockChildReference);
    }

    @Test
    public void linkNodesWithReferenceWithNullParentNodeAndChildLink() throws URISyntaxException, MalformedURLException {
        
        final int childNodeID = 2;
        final URI childNodeURI = new URI(UUID.randomUUID().toString());
        final URL childNodeURL = new URL("file:/archive/somewhere/node.cmdi");
        final int workspaceID = 1;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
                
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspace).setTopNodeID(childNodeID);
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(childNodeURI));
            oneOf(mockWorkspace).setTopNodeArchiveURI(childNodeURI);
            oneOf(mockChildNode).getArchiveURL(); will(returnValue(childNodeURL));
            oneOf(mockWorkspace).setTopNodeArchiveURL(childNodeURL);
            
//            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
//            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            oneOf(mockWorkspaceDao).updateWorkspaceTopNode(mockWorkspace);
        }});
        
        nodeLinkManager.linkNodesWithReference(mockWorkspace, null, mockChildNode, null);
    }
  
    @Test
    public void linkNodesWithReferenceWithNullParentNode() throws URISyntaxException, MalformedURLException {
        
        final int childNodeID = 2;
        final URI childNodeURI = new URI(UUID.randomUUID().toString());
        final URL childNodeURL = new URL("file:/archive/somewhere/node.cmdi");
        final int workspaceID = 1;
        final String expectedExceptionMessage = "Unable to create link (parent node: " + null + "; child link: " + mockChildReference;
        
        try {
            nodeLinkManager.linkNodesWithReference(mockWorkspace, null, mockChildNode, mockChildReference);
            fail("an exception should have been thrown");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    //TODO top node -> UnknownNodeException
    

    @Test
    public void linkNodesMetadataLocal()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/x-cmdi+xml";
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentMetadataReference(null, childURL, childMimetype);
                will(returnValue(mockChildMetadataReference));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinkManager.linkNodes(mockParentNode, mockChildNode);
    }
    
    @Test
    public void linkNodesResourceLocal()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/plain";
        final WorkspaceNodeType childWsType = WorkspaceNodeType.RESOURCE;
        final String childStringType = childWsType.toString();
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.FALSE));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockChildNode).getType(); will(returnValue(childWsType));
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentResourceReference(null, childURL, childStringType, childMimetype);
                will(returnValue(mockChildResourceReference));
                
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));

            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinkManager.linkNodes(mockParentNode, mockChildNode);
    }
    
    @Test
    public void linkNodesResourceFromArchive()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/archive/path/child.txt");
        final URI childURI = new URI(UUID.randomUUID().toString());
        final String childMimetype = "text/plain";
        final WorkspaceNodeType childWsType = WorkspaceNodeType.RESOURCE;
        final String childStringType = childWsType.toString();
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.FALSE));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(null));
            exactly(2).of(mockChildNode).getArchiveURI(); will(returnValue(childURI));
            oneOf(mockChildNode).getType(); will(returnValue(childWsType));
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentResourceReference(childURI, null, childStringType, childMimetype);
                will(returnValue(mockChildResourceReference));
                
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinkManager.linkNodes(mockParentNode, mockChildNode);
    }
    
    @Test
    public void linkNodesExternal()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 3;
        final URL childURL = new URL("http:/remote/folder/child.txt");
        final URI childURI = childURL.toURI();
        final WorkspaceNodeType childWsType = WorkspaceNodeType.RESOURCE;
        final String childStringType = childWsType.toString();
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.FALSE));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(null));
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(null));
            oneOf(mockChildNode).getOriginURL(); will(returnValue(childURL));
            oneOf(mockChildNode).getType(); will(returnValue(childWsType));
            oneOf(mockChildNode).getFormat(); will(returnValue(null));
            oneOf(mockParentDocument).createDocumentResourceReference(childURI, null, childStringType, null);
                will(returnValue(mockChildResourceReference));
                
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinkManager.linkNodes(mockParentNode, mockChildNode);
    }
    
    @Test
    public void linkNodes_DoNotCreateMultipleParents() throws WorkspaceException, ProtectedNodeException {
        
        // if a child node already has parents, do not allow linking it to another parent (avoid creation of paralel structures)
        
        final int workspaceID = 1;
        final int childNodeID = 3;
        
        final Collection<WorkspaceNode> existingParentNodes = new ArrayList<>();
        existingParentNodes.add(mockOtherParentNode);
        
        final String expectedExceptionMessage = "Child node (ID = " + childNodeID + ") already has a parent. Cannot be linked again.";
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(existingParentNodes));
            
            //log
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Exception workspace ID different from expected", workspaceID, ex.getWorkspaceID());
        }
    }
    
    @Test
    public void linkNodes_DoNotLinkProtectedParent() throws WorkspaceException, URISyntaxException {
        
        // if the parent is protected, it should not be allowed to have more children linked to it
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URI parentNodeURI = new URI(UUID.randomUUID().toString());
        
        final String expectedExceptionMessage = "Cannot proceed with linking because parent node (ID = " + parentNodeID + ") is protected (WS ID = " + workspaceID + ").";
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.TRUE));
            //log
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(parentNodeURI));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode);
        } catch(ProtectedNodeException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Exception node URI different from expected", parentNodeURI, ex.getNodeURI());
            assertEquals("Exception workspace ID different from expected", workspaceID, ex.getWorkspaceID());
        }
    }
    
    @Test
    public void linkNodesGetMetadataThrowsIOException()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Error retrieving metadata document for node " + parentNodeID;
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(throwException(expectedException));
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void linkNodesGetMetadataThrowsMetadataException()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Error retrieving metadata document for node " + parentNodeID;
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(throwException(expectedException));
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));

        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void linkNodesParentNotReferencingDocument()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Error retrieving referencing document for node " + parentNodeID;
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockNotReferencingDocument));
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void linkNodesMetadataException()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/x-cmdi+xml";
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Error creating reference in document with node ID " + parentNodeID;
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentMetadataReference(null, childURL, childMimetype);
                will(throwException(expectedException));
                
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void linkNodesIOException()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/x-cmdi+xml";
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Error creating reference in document with node ID " + parentNodeID;
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentMetadataReference(null, childURL, childMimetype);
                will(returnValue(mockChildMetadataReference));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
                will(throwException(expectedException));
                
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void linkNodesTransformerException()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/x-cmdi+xml";
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Error creating reference in document with node ID " + parentNodeID;
        final TransformerException expectedException = new TransformerException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentMetadataReference(null, childURL, childMimetype);
                will(returnValue(mockChildMetadataReference));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
                will(throwException(expectedException));
                
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    //TODO URISyntaxException
    
    @Test
    public void linkNodesOnlyInDb() throws MalformedURLException, URISyntaxException, WorkspaceException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
                
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        nodeLinkManager.linkNodesOnlyInDb(mockParentNode, mockChildNode);
    }
    
    
    
    //TODO URISyntaxException
    
    
    
    @Test
    public void unlinkNodesWithLocationWithHandle() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURL); will (returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
//        checkUnlinkNodeExpectations(workspaceID, childURL, parentURL, childURI, parentNodeID, childNodeID);
        
        
        nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
    }
    
    @Test
    public void unlinkNodesWithoutLocationWithHandle() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childArchiveURI = new URI(UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURL); will (returnValue(null));
            exactly(2).of(mockChildNode).getArchiveURI(); will(returnValue(childArchiveURI));
            oneOf(mockParentDocument).getDocumentReferenceByURI(childArchiveURI); will(returnValue(mockChildReferenceWithHandle));
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
//        checkUnlinkNodeExpectations(workspaceID, childURL, parentURL, childURI, parentNodeID, childNodeID);
        
        
        nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
    }
    
    @Test
    public void unlinkNodesWithoutLocationWithUri() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));

            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURL); will (returnValue(null));
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(null));
            exactly(2).of(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockParentDocument).getDocumentReferenceByURI(childURI); will(returnValue(mockChildReference));
            oneOf(mockParentDocument).removeDocumentReference(mockChildReference); will(returnValue(mockChildReference));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
    }
    
    @Test
    public void unlinkExternalNode() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 3;
        final URL childURL = new URL("http://external/path/child.cmdi");
        final URI childURI = childURL.toURI();
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURL); will (returnValue(null));
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(null));
            oneOf(mockChildNode).getOriginURL(); will(returnValue(childURL));
            oneOf(mockParentDocument).getDocumentReferenceByURI(childURI); will(returnValue(mockChildReference));
            oneOf(mockParentDocument).removeDocumentReference(mockChildReference); will(returnValue(mockChildReference));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
    }
    
    @Test
    public void unlinkNodes_DoNotAllowUnlinkingFromProtectedParent() throws WorkspaceException, URISyntaxException {
        
        // if the parent node is protected, unlinking its children should not be allowed
        // if the child (or any other descendant) is protected, unlinking will proceed (this won't be checked)
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URI parentNodeURI = new URI(UUID.randomUUID().toString());
        final String expectedExceptionMessage = "Cannot proceed with unlinking because parent node (ID = " + parentNodeID + ") is protected (WS ID = " + workspaceID + ").";
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.TRUE));
            //log
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(parentNodeURI));
        }});
        
        try {
            nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
            fail("should have thrown an exception");
        } catch(ProtectedNodeException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Exception node URI different from expected", parentNodeURI, ex.getNodeURI());
            assertEquals("Exception workspace ID different from expected", workspaceID, ex.getWorkspaceID());
        }
    }
    
    @Test
    public void unlinkNodesGetMetadataThrowsIOException() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        
        final String expectedErrorMessage = "Error retrieving metadata document for node " + parentNodeID;
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(throwException(expectedException));
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
        }});

        try {
            nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void unlinkNodesGetMetadataThrowsMetadataException() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        
        final String expectedErrorMessage = "Error retrieving metadata document for node " + parentNodeID;
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(throwException(expectedException));
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
        }});

        try {
            nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void unlinkNodesParentNotReferencingDocument() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        
        final String expectedErrorMessage = "Error retrieving referencing document for node " + parentNodeID;
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockNotReferencingDocument));
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
        }});

        try {
            nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void unlinkNodesMetadataException() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final int childNodeID = 3;
        
        final String expectedErrorMessage = "Error removing reference in document with node ID " + childNodeID;
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURL); will(returnValue(mockChildReferenceWithHandle));
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(throwException(expectedException));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
        }});
        
        try {
            nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expeted", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void unlinkNodesIOException() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final int childNodeID = 3;
        
        final String expectedErrorMessage = "Error removing reference in document with node ID " + childNodeID;
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURL); will(returnValue(mockChildReferenceWithHandle));
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle);
                will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
                will(throwException(expectedException));
                
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        try {
            nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expeted", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void unlinkNodesTransformerException() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final int childNodeID = 3;
        
        final String expectedErrorMessage = "Error removing reference in document with node ID " + childNodeID;
        final TransformerException expectedException = new TransformerException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURL); will(returnValue(mockChildReferenceWithHandle));
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle);
                will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
                will(throwException(expectedException));
                
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        try {
            nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expeted", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void unlinkNodeWithLocationWithOneParent() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 4;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        
        final Collection<WorkspaceNode> parentNodes = new ArrayList<>();
        parentNodes.add(mockParentNode);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(parentNodes));
        }});
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURL); will(returnValue(mockChildReferenceWithHandle));
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinkManager.unlinkNodeFromAllParents(mockChildNode);
    }
    
    @Test
    public void unlinkNodeWithLocationWithSeveralParents() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int otherParentNodeID = 3;
        final int[] parentIDs = { parentNodeID, otherParentNodeID };
        
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URL otherParentURL = new URL("file:/lamus/workspace/" + workspaceID + "/otherparent.cmdi");
        final URL[] parentURLs = { parentURL, otherParentURL };
        
        final int childNodeID = 4;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        
        final Collection<WorkspaceNode> parentNodes = new ArrayList<>();
        parentNodes.add(mockParentNode);
        parentNodes.add(mockOtherParentNode);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(parentNodes));
        }});
        
        // first iteration of the loop
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURL); will(returnValue(mockChildReferenceWithHandle));
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
        }});
        
        // second iteration of the loop
        context.checking(new Expectations() {{
            
            oneOf(mockOtherParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            //logger
            oneOf(mockOtherParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockOtherParentNode).getWorkspaceURL(); will(returnValue(otherParentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(otherParentURL); will(returnValue(mockOtherParentDocument));
            
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockOtherParentDocument).getDocumentReferenceByLocation(childURL); will(returnValue(mockChildReferenceWithHandle));
            oneOf(mockOtherParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockOtherParentNode).getWorkspaceURL(); will(returnValue(otherParentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockOtherParentDocument, mockParentStreamResult);
            
            oneOf(mockOtherParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockOtherParentNode).getWorkspaceNodeID(); will(returnValue(otherParentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, otherParentNodeID, childNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinkManager.unlinkNodeFromAllParents(mockChildNode);
    }
    
    @Test
    public void replaceResourceNode() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
     
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        
        final int oldChildNodeID = 3;
        final URL oldChildURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI oldChildURI = oldChildURL.toURI();
        
        final int newChildNodeID = 20;
        final URL newChildURL = new URL("file:/lamus/workspace/" + workspaceID + "/another_child.txt");
        final URI newChildURI = newChildURL.toURI();
        
        final String childMimetype = "text/plain";
        final WorkspaceNodeType childWsType = WorkspaceNodeType.RESOURCE;
        final String childStringType = childWsType.toString();
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        
//        checkUnlinkNodeExpectations(workspaceID, childURL, parentURL, childURI, parentNodeID, childNodeID);
        
        // unlink old node
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldChildNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newChildNodeID));
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newChildNodeID));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(newChildNodeID); will(returnValue(emptyParentNodes));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldChildNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockOldNode).getWorkspaceURL(); will(returnValue(oldChildURL));
            oneOf(mockParentDocument).getDocumentReferenceByLocation(oldChildURL); will(returnValue(mockChildReferenceWithHandle));
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldChildNodeID));
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, oldChildNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        
        // link new node
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newChildNodeID));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockNewNode).isMetadata(); will(returnValue(Boolean.FALSE));
            oneOf(mockNewNode).getWorkspaceURL(); will(returnValue(newChildURL));
            oneOf(mockNewNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockNewNode).getType(); will(returnValue(childWsType));
            oneOf(mockNewNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentResourceReference(null, newChildURL, childStringType, childMimetype);
                will(returnValue(mockChildResourceReference));
                
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newChildNodeID));
            
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, newChildNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        
        //replace node in DB (create new version and set old node as replaced)
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).replaceNode(mockOldNode, mockNewNode);
        }});
        
        
        nodeLinkManager.replaceNode(mockParentNode, mockOldNode, mockNewNode, Boolean.FALSE);
    }
    
    @Test
    public void replaceResourceNode_NewNodeAlreadyLinked() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
     
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        
        final int oldChildNodeID = 3;
        final URL oldChildURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI oldChildURI = oldChildURL.toURI();
        
        final int newChildNodeID = 20;
        final URL newChildURL = new URL("file:/lamus/workspace/" + workspaceID + "/another_child.txt");
        final URI newChildURI = newChildURL.toURI();
        
        final String childMimetype = "text/plain";
        final WorkspaceNodeType childWsType = WorkspaceNodeType.RESOURCE;
        final String childStringType = childWsType.toString();
        
        
        //replace node in DB (create new version and set old node as replaced)
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldChildNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newChildNodeID));
            
            oneOf(mockWorkspaceDao).replaceNode(mockOldNode, mockNewNode);
        }});
        
        
        nodeLinkManager.replaceNode(mockParentNode, mockOldNode, mockNewNode, Boolean.TRUE);
    }
    
    @Test
    public void removeArchiveUriFromResourceChildNode() throws WorkspaceException, MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException {
        
        final int workspaceID = 1;
        final int parentNodeID = 1;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 2;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final URI childArchiveURI = new URI(UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(childArchiveURI));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            // NOT SURE YET IF THE URI WILL CONTAIN THE HANDLE IN THIS CASE...
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(childArchiveURI));
            oneOf(mockParentDocument).getDocumentReferenceByURI(childArchiveURI); will(returnValue(mockChildDataResourceProxy));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildDataResourceProxy).setURI(childURI);

            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildNode).setArchiveURI(null);
            oneOf(mockChildNode).setArchiveURL(null);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockChildNode);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockChildNode);
        }});
        
        nodeLinkManager.removeArchiveUriFromChildNode(mockParentNode, mockChildNode);
    }
    
    @Test
    public void removeArchiveUriFromMetadataChildNode() throws WorkspaceException, MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException {
        
        final int workspaceID = 1;
        final int parentNodeID = 1;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 2;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final URI childArchiveURI = new URI(UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(childArchiveURI));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            // NOT SURE YET IF THE URI WILL CONTAIN THE HANDLE IN THIS CASE...
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(childArchiveURI));
            oneOf(mockParentDocument).getDocumentReferenceByURI(childArchiveURI); will(returnValue(mockChildMetadataResourceProxy));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildMetadataResourceProxy).setURI(childURI);

            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.TRUE));
            
            //remove self handle
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockMetadataAPI).getMetadataDocument(childURL); will(returnValue(mockChildCmdiDocument));
            oneOf(mockChildCmdiDocument).setHandle(null);
            
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockChildCmdiDocument, childURL);

            //remove archive URI and URL
            oneOf(mockChildNode).setArchiveURI(null);
            oneOf(mockChildNode).setArchiveURL(null);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockChildNode);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockChildNode);
        }});
        
        nodeLinkManager.removeArchiveUriFromChildNode(mockParentNode, mockChildNode);
    }
    
    @Test
    public void removeArchiveUri_MetadataException_GetParentDocument() throws MalformedURLException, IOException, MetadataException, WorkspaceException, URISyntaxException {
        
        final int workspaceID = 1;
        
        final int parentID = 100;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        
        final int childID = 200;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childArchiveURI = new URI(UUID.randomUUID().toString());
        
        final MetadataException expectedCause = new MetadataException("some exception message");
        String expectedMessage = "Error when trying to remove URI of node " + childID + ", referenced in node " + parentID;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childID));
            
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(childArchiveURI));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(throwException(expectedCause));
            
            //exception
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentID));
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
        }});

        try {
            nodeLinkManager.removeArchiveUriFromChildNode(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedCause, ex.getCause());
        }
    }
    
    @Test
    public void removeArchiveUri_IOException_SaveParentDocument() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException {
        
        final int workspaceID = 1;
        
        final int parentID = 100;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        
        final int childID = 200;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final URI childArchiveURI = new URI(UUID.randomUUID().toString());
        
        final IOException expectedCause = new IOException("some exception message");
        String expectedMessage = "Error when trying to remove URI of node " + childID + ", referenced in node " + parentID;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childID));
            
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(childArchiveURI));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            // NOT SURE YET IF THE URI WILL CONTAIN THE HANDLE IN THIS CASE...
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(childArchiveURI));
            oneOf(mockParentDocument).getDocumentReferenceByURI(childArchiveURI); will(returnValue(mockChildDataResourceProxy));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildDataResourceProxy).setURI(childURI);

            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL); will(throwException(expectedCause));
            
            //exception
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentID));
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
        }});
        
        try {
            nodeLinkManager.removeArchiveUriFromChildNode(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedCause, ex.getCause());
        }
    }
    
    @Test
    public void removeArchiveUri_TransformerException_SaveChildDocument() throws WorkspaceException, MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException {
        
        final int workspaceID = 1;
        
        final int parentID = 100;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        
        final int childID = 200;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final URI childArchiveURI = new URI(UUID.randomUUID().toString());
        
        final TransformerException expectedCause = new TransformerException("some exception message");
        String expectedMessage = "Error when trying to remove URI of node " + childID + ", referenced in node " + parentID;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childID));
            
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(childArchiveURI));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            // NOT SURE YET IF THE URI WILL CONTAIN THE HANDLE IN THIS CASE...
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(childArchiveURI));
            oneOf(mockParentDocument).getDocumentReferenceByURI(childArchiveURI); will(returnValue(mockChildMetadataResourceProxy));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildMetadataResourceProxy).setURI(childURI);

            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.TRUE));
            
            //remove self handle
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockMetadataAPI).getMetadataDocument(childURL); will(returnValue(mockChildCmdiDocument));
            oneOf(mockChildCmdiDocument).setHandle(null);
            
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockChildCmdiDocument, childURL); will(throwException(expectedCause));
            
            //exception
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentID));
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
        }});
        
        try {
            nodeLinkManager.removeArchiveUriFromChildNode(mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedCause, ex.getCause());
        }
    }
    
    @Test
    public void removeArchiveUri_nullArchiveUri() throws MalformedURLException, URISyntaxException, WorkspaceException {
        
        final int workspaceID = 1;
        final int parentNodeID = 1;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 2;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(null));
            
        }});
        
        nodeLinkManager.removeArchiveUriFromChildNode(mockParentNode, mockChildNode);
    }

    
    private void checkUnlinkNodeExpectations(
            final int workspaceID, final URL childURL,
            final URL parentURL, final URI childURI,
            final int parentNodeID, final int childNodeID)
                throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException {
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            exactly(2).of(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(childURI));
            
            oneOf(mockParentDocument).getDocumentReferenceByURI(childURI); will(returnValue(mockChildReferenceWithHandle));
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
    }
}