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
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.metadata.implementation.MetadataReferenceType;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.cmdi.api.model.CMDIContainerMetadataElement;
import nl.mpi.metadata.cmdi.api.model.CMDIDocument;
import nl.mpi.metadata.cmdi.api.model.Component;
import nl.mpi.metadata.cmdi.api.model.DataResourceProxy;
import nl.mpi.metadata.cmdi.api.model.MetadataResourceProxy;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import org.apache.commons.io.FileUtils;
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
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private WorkspaceNodeLinkManager nodeLinkManager;
    
    @Mock WorkspaceNodeLinkFactory mockWorkspaceNodeLinkFactory;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock MetadataApiBridge mockMetadataApiBridge;
    @Mock NodeUtil mockNodeUtil;
    
    @Mock WorkspaceNodeLink mockWorkspaceNodeLink;
    
    @Mock Workspace mockWorkspace;
    @Mock WorkspaceNode mockParentNode;
    @Mock WorkspaceNode mockOtherParentNode;
    @Mock WorkspaceNode mockChildNode;
    @Mock WorkspaceNode mockAnotherChildNode;
    @Mock WorkspaceNode mockOldNode;
    @Mock WorkspaceNode mockNewNode;
    @Mock ResourceProxy mockChildReference;
    @Mock ResourceProxy mockAnotherChildReference;
    @Mock ResourceProxy mockChildReferenceWithHandle;
    
    @Mock WorkspaceNode mockGrandChildNode;
    @Mock ResourceProxy mockGrandChildReference;

    @Mock CMDIDocument mockParentDocument;
    @Mock CMDIDocument mockOtherParentDocument;
    @Mock MetadataResourceProxy mockChildMetadataReference;
    @Mock DataResourceProxy mockChildResourceReference;
    
    @Mock MetadataDocument mockNotReferencingDocument;
    
    @Mock MetadataResourceProxy mockChildMetadataResourceProxy;
    @Mock DataResourceProxy mockChildDataResourceProxy;
    @Mock ResourceProxy mockRetrievedResourceProxy;
    @Mock CMDIDocument mockChildCmdiDocument;
    
    @Mock CMDIContainerMetadataElement mockCmdiContainerMetadataElement;
    @Mock Component mockChildComponent;
    @Mock Component mockAnotherChildComponent;
    
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
                mockMetadataApiBridge, mockNodeUtil);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void linkNodesWithReference() {
        
        final int workspaceID = 1;
        final int parentNodeID = 1;
        final int childNodeID = 2;
        
        context.checking(new Expectations() {{
            
            //logger
            allowing(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
                
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        nodeLinkManager.linkNodesWithReference(mockWorkspace, mockParentNode, mockChildNode, mockChildReference);
    }

    @Test
    public void linkNodesWithReferenceWithNullParentNodeAndChildLink() throws MalformedURLException {
        
        final int childNodeID = 2;
        final URI childNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
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
            oneOf(mockWorkspaceDao).updateWorkspaceTopNode(mockWorkspace);
        }});
        
        nodeLinkManager.linkNodesWithReference(mockWorkspace, null, mockChildNode, null);
    }
  
    @Test
    public void linkNodesWithReferenceWithNullParentNode() {
        
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
    public void linkNodes_ParentNotMetadata() throws WorkspaceException, ProtectedNodeException {
        
        final int workspaceNodeID = 1;
        final String expectedExceptionMessage = "Unable to create link. Parent node (" + workspaceNodeID + ") is not metadata.";
        
        context.checking(new Expectations() {{
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(workspaceNodeID));
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.FALSE));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode, Boolean.FALSE);
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }

    @Test
    public void linkNodesMetadataLocal()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/x-cmdi+xml";
        final String componentPath = "collection";
        final boolean isInfoFile = Boolean.FALSE;
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            
            allowing(mockChildNode).getType(); will(returnValue(WorkspaceNodeType.METADATA));
            
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockMetadataApiBridge).isMetadataReferenceAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.TRUE));
            
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentMetadataReference(null, childURI, childMimetype);
                will(returnValue(mockChildMetadataReference));
            
            allowing(mockChildMetadataReference).getMimetype(); will(returnValue(childMimetype));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, childMimetype, null, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockMetadataApiBridge).createComponentPathWithin(mockParentDocument, componentPath);
                will(returnValue(mockCmdiContainerMetadataElement));
            oneOf(mockMetadataApiBridge).addReferenceInComponent(mockCmdiContainerMetadataElement, mockChildMetadataReference);
                will(returnValue(mockRetrievedResourceProxy));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
    }
    
    @Test
    public void linkNodesMetadataLocal_ReferenceNotAllowed()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final boolean isInfoFile = Boolean.FALSE;
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedExceptionMessage = "A metadata reference is not allowed in the profile of the selected parent node";
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            //logger
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            oneOf(mockMetadataApiBridge).isMetadataReferenceAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.FALSE));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void linkNodesResourceLocal()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/plain";
        final boolean isInfoFile = Boolean.FALSE;
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            
            allowing(mockChildNode).getType(); will(returnValue(WorkspaceNodeType.RESOURCE_WRITTEN));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockMetadataApiBridge).isResourceReferenceAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.TRUE));
            
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentResourceReference(null, childURI, MetadataReferenceType.REFERENCE_TYPE_RESOURCE, childMimetype);
                will(returnValue(mockChildResourceReference));
            
            allowing(mockChildResourceReference).getMimetype(); will(returnValue(childMimetype));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, childMimetype, null, isInfoFile);
                will(returnValue(null));
            // null component retrieved - reference is not mandatory for this profile, so do not create one
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);

            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
    }
    
    @Test
    public void linkNodesResourceLocal_InfoLink()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/plain";
        final boolean isInfoFile = Boolean.TRUE;
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedExceptionMessage = "An info link is not allowed in the profile of the selected parent node";
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockMetadataApiBridge).isResourceReferenceAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.TRUE));
            
            oneOf(mockMetadataApiBridge).isInfoLinkAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.FALSE));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void linkNodesResourceLocal_InfoLinkNotAllowed()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/plain";
        final boolean isInfoFile = Boolean.TRUE;
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            
            allowing(mockChildNode).getType(); will(returnValue(WorkspaceNodeType.RESOURCE_INFO));
            
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockMetadataApiBridge).isResourceReferenceAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.TRUE));
            
            oneOf(mockMetadataApiBridge).isInfoLinkAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.TRUE));
            
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentResourceReference(null, childURI, MetadataReferenceType.REFERENCE_TYPE_RESOURCE, childMimetype);
                will(returnValue(mockChildResourceReference));
            
            allowing(mockChildResourceReference).getMimetype(); will(returnValue(childMimetype));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, childMimetype, null, isInfoFile);
                will(returnValue(null));
            // null component retrieved - reference is not mandatory for this profile, so do not create one
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);

            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
    }
    
    @Test
    public void linkNodesResourceLocal_NoReferenceNeeded()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/plain";
        final boolean isInfoFile = Boolean.FALSE;
        
        final String componentPath = "lat-session/WrittenResource";
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            
            allowing(mockChildNode).getType(); will(returnValue(WorkspaceNodeType.RESOURCE_WRITTEN));
            
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockMetadataApiBridge).isResourceReferenceAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.TRUE));
            
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentResourceReference(null, childURI, MetadataReferenceType.REFERENCE_TYPE_RESOURCE, childMimetype);
                will(returnValue(mockChildResourceReference));
            
            allowing(mockChildResourceReference).getMimetype(); will(returnValue(childMimetype));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, childMimetype, null, isInfoFile);
                will(returnValue(componentPath));
            oneOf(mockMetadataApiBridge).createComponentPathWithin(mockParentDocument, componentPath);
                will(returnValue(mockCmdiContainerMetadataElement));
            oneOf(mockMetadataApiBridge).addReferenceInComponent(mockCmdiContainerMetadataElement, mockChildResourceReference);
                will(returnValue(mockRetrievedResourceProxy));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);

            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
    }
    
    @Test
    public void linkNodesResourceFromArchive()
            throws MalformedURLException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 3;
        final URI childURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String childMimetype = "text/plain";
        final boolean isInfoFile = Boolean.FALSE;
        
        final String componentPath = "lat-session/WrittenResource";
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(null));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(childURI));
            
            allowing(mockChildNode).getType(); will(returnValue(WorkspaceNodeType.RESOURCE_WRITTEN));
            
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockMetadataApiBridge).isResourceReferenceAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.TRUE));
            
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentResourceReference(childURI, null, MetadataReferenceType.REFERENCE_TYPE_RESOURCE, childMimetype);
                will(returnValue(mockChildResourceReference));
            
            allowing(mockChildResourceReference).getMimetype(); will(returnValue(childMimetype));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, childMimetype, null, isInfoFile);
                will(returnValue(componentPath));
            oneOf(mockMetadataApiBridge).createComponentPathWithin(mockParentDocument, componentPath);
                will(returnValue(mockCmdiContainerMetadataElement));
            oneOf(mockMetadataApiBridge).addReferenceInComponent(mockCmdiContainerMetadataElement, mockChildResourceReference);
                will(returnValue(mockRetrievedResourceProxy));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
    }
    
    @Test
    public void linkNodesExternal()
            throws MalformedURLException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 3;
        final URI childURI = URI.create("http:/remote/folder/child.txt");
        final WorkspaceNodeType childType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final boolean isInfoFile = Boolean.FALSE;
        
        final String componentPath = "lat-session/WrittenResource";
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(null));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockMetadataApiBridge).isResourceReferenceAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.TRUE));
            
            oneOf(mockChildNode).getOriginURI(); will(returnValue(childURI));
            oneOf(mockChildNode).getFormat(); will(returnValue(null));
            oneOf(mockParentDocument).createDocumentResourceReference(childURI, null, MetadataReferenceType.REFERENCE_TYPE_RESOURCE, null);
                will(returnValue(mockChildResourceReference));
            
            oneOf(mockChildResourceReference).getMimetype(); will(returnValue(null));
            oneOf(mockChildNode).getType(); will(returnValue(childType));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, null, childType, isInfoFile);
                will(returnValue(componentPath));
            oneOf(mockMetadataApiBridge).createComponentPathWithin(mockParentDocument, componentPath);
                will(returnValue(mockCmdiContainerMetadataElement));
            oneOf(mockMetadataApiBridge).addReferenceInComponent(mockCmdiContainerMetadataElement, mockChildResourceReference);
                will(returnValue(mockRetrievedResourceProxy));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
    }
    
    @Test
    public void linkNodes_DoNotCreateMultipleParents() throws WorkspaceException, ProtectedNodeException {
        
        // if a child node already has parents, do not allow linking it to another parent (avoid creation of paralel structures)
        
        final int workspaceID = 1;
        final int childNodeID = 3;
        final int parentNodeID = 2;
        final boolean isInfoFile = Boolean.FALSE;
        
        final Collection<WorkspaceNode> existingParentNodes = new ArrayList<>();
        existingParentNodes.add(mockOtherParentNode);
        
        final String expectedExceptionMessage = "Child node (ID = " + childNodeID + ") already has a parent. Cannot be linked again.";
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(existingParentNodes));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
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
        final URI parentNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final boolean isInfoFile = Boolean.FALSE;
        
        final String expectedExceptionMessage = "Cannot proceed with linking because parent node (ID = " + parentNodeID + ") is protected (WS ID = " + workspaceID + ").";
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.TRUE));
            //log
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(parentNodeURI));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
        } catch(ProtectedNodeException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Exception node URI different from expected", parentNodeURI, ex.getNodeURI());
            assertEquals("Exception workspace ID different from expected", workspaceID, ex.getWorkspaceID());
        }
    }
    
    @Test
    public void linkNodesGetMetadataThrowsIOException()
            throws MalformedURLException, IOException, MetadataException, TransformerException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final boolean isInfoFile = Boolean.FALSE;
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Error retrieving metadata document for node " + parentNodeID;
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            //logger
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));

            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(throwException(expectedException));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void linkNodesGetMetadataThrowsMetadataException()
            throws MalformedURLException, IOException, MetadataException, TransformerException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final boolean isInfoFile = Boolean.FALSE;
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Error retrieving metadata document for node " + parentNodeID;
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            //logger
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));

            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(throwException(expectedException));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void linkNodesParentNotReferencingDocument()
            throws MalformedURLException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final boolean isInfoFile = Boolean.FALSE;
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Error retrieving referencing document for node " + parentNodeID;
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            //logger
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockNotReferencingDocument));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
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
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/x-cmdi+xml";
        final boolean isInfoFile = Boolean.FALSE;
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Error creating reference in document with node ID " + parentNodeID;
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            //logger
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            oneOf(mockMetadataApiBridge).isMetadataReferenceAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.TRUE));
            
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentMetadataReference(null, childURI, childMimetype);
                will(throwException(expectedException));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
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
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/x-cmdi+xml";
        final boolean isInfoFile = Boolean.FALSE;
        
        final String componentPath = "collection";
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Error creating reference in document with node ID " + parentNodeID;
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            
            allowing(mockChildNode).getType(); will(returnValue(WorkspaceNodeType.METADATA));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.TRUE));
            oneOf(mockMetadataApiBridge).isMetadataReferenceAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.TRUE));
            
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentMetadataReference(null, childURI, childMimetype);
                will(returnValue(mockChildMetadataReference));
            
            allowing(mockChildMetadataReference).getMimetype(); will(returnValue(childMimetype));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, childMimetype, null, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockMetadataApiBridge).createComponentPathWithin(mockParentDocument, componentPath);
                will(returnValue(mockCmdiContainerMetadataElement));
            oneOf(mockMetadataApiBridge).addReferenceInComponent(mockCmdiContainerMetadataElement, mockChildMetadataReference);
                will(returnValue(mockRetrievedResourceProxy));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
                will(throwException(expectedException));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
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
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/x-cmdi+xml";
        final boolean isInfoFile = Boolean.FALSE;
        
        final String componentPath = "collection";
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Error creating reference in document with node ID " + parentNodeID;
        final TransformerException expectedException = new TransformerException("some exception message");
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            
            allowing(mockChildNode).getType(); will(returnValue(WorkspaceNodeType.METADATA));
            
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.TRUE));
            oneOf(mockMetadataApiBridge).isMetadataReferenceAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.TRUE));
            
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentMetadataReference(null, childURI, childMimetype);
                will(returnValue(mockChildMetadataReference));
            
            allowing(mockChildMetadataReference).getMimetype(); will(returnValue(childMimetype));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, childMimetype, null, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockMetadataApiBridge).createComponentPathWithin(mockParentDocument, componentPath);
                will(returnValue(mockCmdiContainerMetadataElement));
            oneOf(mockMetadataApiBridge).addReferenceInComponent(mockCmdiContainerMetadataElement, mockChildMetadataReference);
                will(returnValue(mockRetrievedResourceProxy));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
                will(throwException(expectedException));
        }});
        
        try {
            nodeLinkManager.linkNodes(mockParentNode, mockChildNode, isInfoFile);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    //TODO URISyntaxException
    
    @Test
    public void linkNodesOnlyInDb() throws MalformedURLException, WorkspaceException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
                
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
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final WorkspaceNodeType childType = WorkspaceNodeType.METADATA;
        final String childMimetype = "text/x-cmdi+xml";
        final String childRefId = "ref_1234546u75645";
        final String componentPath = "/lat-corpus/CorpusLink";
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getType(); will(returnValue(childType));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURI); will (returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockNodeUtil).isNodeInfoFile(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildReferenceWithHandle).getMimetype(); will(returnValue(childMimetype));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, childMimetype, null, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockChildReferenceWithHandle).getId(); will(returnValue(childRefId));
            oneOf(mockMetadataApiBridge).getComponent(mockParentDocument, componentPath, childRefId); will(returnValue(mockChildComponent));
            oneOf(mockMetadataApiBridge).removeComponent(mockChildComponent);
            
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
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
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final URI childArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final WorkspaceNodeType childType = WorkspaceNodeType.METADATA;
        final String childMimetype = "text/x-cmdi+xml";
        final String childRefId = "ref_1234546u75645";
        final String componentPath = "/lat-corpus/CorpusLink";
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getType(); will(returnValue(childType));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURI); will (returnValue(null));
            exactly(2).of(mockChildNode).getArchiveURI(); will(returnValue(childArchiveURI));
            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockParentDocument, childArchiveURI); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockNodeUtil).isNodeInfoFile(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildReferenceWithHandle).getMimetype(); will(returnValue(childMimetype));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, childMimetype, null, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockChildReferenceWithHandle).getId(); will(returnValue(childRefId));
            oneOf(mockMetadataApiBridge).getComponent(mockParentDocument, componentPath, childRefId); will(returnValue(mockChildComponent));
            oneOf(mockMetadataApiBridge).removeComponent(mockChildComponent);
            
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
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
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final WorkspaceNodeType childType = WorkspaceNodeType.METADATA;
        final String childRefId = "ref_1234546u75645";
        final String componentPath = "/lat-corpus/CorpusLink";
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getType(); will(returnValue(childType));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));

            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURI); will (returnValue(null));
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockParentDocument, childURI); will(returnValue(mockChildReference));
            
            oneOf(mockNodeUtil).isNodeInfoFile(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildReference).getMimetype(); will(returnValue(null));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, null, childType, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockChildReference).getId(); will(returnValue(childRefId));
            oneOf(mockMetadataApiBridge).getComponent(mockParentDocument, componentPath, childRefId); will(returnValue(mockChildComponent));
            oneOf(mockMetadataApiBridge).removeComponent(mockChildComponent);
            
            oneOf(mockParentDocument).removeDocumentReference(mockChildReference); will(returnValue(mockChildReference));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
    }
    
    @Test
    public void unlinkNodesWithoutWorkspaceUrl() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 3;
        final URL childURL = new URL("file:/archive/parent/child.txt");
        final URI childURI = childURL.toURI();
        final WorkspaceNodeType childType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childMimetype = "text/plain";
        final String childRefId = "ref_1234546u75645";
        final String componentPath = "/lat-session/WrittenResource";
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(null));
            allowing(mockChildNode).getType(); will(returnValue(childType));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(childURI));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));

            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockParentDocument, childURI); will(returnValue(mockChildReference));
            
            oneOf(mockNodeUtil).isNodeInfoFile(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildReference).getMimetype(); will(returnValue(childMimetype));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, childMimetype, null, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockChildReference).getId(); will(returnValue(childRefId));
            oneOf(mockMetadataApiBridge).getComponent(mockParentDocument, componentPath, childRefId); will(returnValue(mockChildComponent));
            oneOf(mockMetadataApiBridge).removeComponent(mockChildComponent);
            
            oneOf(mockParentDocument).removeDocumentReference(mockChildReference); will(returnValue(mockChildReference));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
    }
    
    @Test
    public void unlinkExternalNode() throws MalformedURLException, IOException, MetadataException, TransformerException, WorkspaceException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 3;
        final URI childURI = URI.create("http://external/path/child.txt");
        final WorkspaceNodeType childType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childRefId = "ref_1234546u75645";
        final String componentPath = "/lat-session/WrittenResource";
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(null));
            allowing(mockChildNode).getType(); will(returnValue(childType));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            allowing(mockChildNode).getOriginURI(); will(returnValue(childURI));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockParentDocument, childURI); will(returnValue(mockChildReference));
            
            oneOf(mockNodeUtil).isNodeInfoFile(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildReference).getMimetype(); will(returnValue(null));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, null, childType, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockChildReference).getId(); will(returnValue(childRefId));
            oneOf(mockMetadataApiBridge).getComponent(mockParentDocument, componentPath, childRefId); will(returnValue(mockChildComponent));
            oneOf(mockMetadataApiBridge).removeComponent(mockChildComponent);
            
            oneOf(mockParentDocument).removeDocumentReference(mockChildReference); will(returnValue(mockChildReference));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinkManager.unlinkNodes(mockParentNode, mockChildNode);
    }
    
    @Test
    public void unlinkNodes_DoNotAllowUnlinkingFromProtectedParent() throws WorkspaceException {
        
        // if the parent node is protected, unlinking its children should not be allowed
        // if the child (or any other descendant) is protected, unlinking will proceed (this won't be checked)
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URI parentNodeURI = URI.create(UUID.randomUUID().toString());
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
    public void unlinkNodesGetMetadataThrowsIOException() throws MalformedURLException, IOException, MetadataException, TransformerException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        
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
    public void unlinkNodesGetMetadataThrowsMetadataException() throws MalformedURLException, IOException, MetadataException, TransformerException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        
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
    public void unlinkNodesParentNotReferencingDocument() throws MalformedURLException, IOException, MetadataException, TransformerException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int childNodeID = 3;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        
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
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final int childNodeID = 3;
        final WorkspaceNodeType childType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childRefId = "ref_1234546u75645";
        final String componentPath = "/lat-session/WrittenResource";
        
        final String expectedErrorMessage = "Error removing reference in document with node ID " + childNodeID;
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getType(); will(returnValue(childType));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURI); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockNodeUtil).isNodeInfoFile(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildReferenceWithHandle).getMimetype(); will(returnValue(null));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, null, childType, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockChildReferenceWithHandle).getId(); will(returnValue(childRefId));
            oneOf(mockMetadataApiBridge).getComponent(mockParentDocument, componentPath, childRefId); will(returnValue(mockChildComponent));
            oneOf(mockMetadataApiBridge).removeComponent(mockChildComponent);
            
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(throwException(expectedException));
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
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final int childNodeID = 3;
        final WorkspaceNodeType childType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childRefId = "ref_1234546u75645";
        final String componentPath = "/lat-session/WrittenResource";
        
        final String expectedErrorMessage = "Error removing reference in document with node ID " + childNodeID;
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getType(); will(returnValue(childType));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURI); will(returnValue(mockChildReferenceWithHandle));
                
            oneOf(mockNodeUtil).isNodeInfoFile(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildReferenceWithHandle).getMimetype(); will(returnValue(null));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, null, childType, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockChildReferenceWithHandle).getId(); will(returnValue(childRefId));
            oneOf(mockMetadataApiBridge).getComponent(mockParentDocument, componentPath, childRefId); will(returnValue(mockChildComponent));
            oneOf(mockMetadataApiBridge).removeComponent(mockChildComponent);
            
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle);
                will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
                will(throwException(expectedException));
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
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final int childNodeID = 3;
        final WorkspaceNodeType childType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childRefId = "ref_1234546u75645";
        final String componentPath = "/lat-session/WrittenResource";
        
        final String expectedErrorMessage = "Error removing reference in document with node ID " + childNodeID;
        final TransformerException expectedException = new TransformerException("some exception message");
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getType(); will(returnValue(childType));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURI); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockNodeUtil).isNodeInfoFile(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildReferenceWithHandle).getMimetype(); will(returnValue(null));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, null, childType, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockChildReferenceWithHandle).getId(); will(returnValue(childRefId));
            oneOf(mockMetadataApiBridge).getComponent(mockParentDocument, componentPath, childRefId); will(returnValue(mockChildComponent));
            oneOf(mockMetadataApiBridge).removeComponent(mockChildComponent);
            
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle);
                will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
                will(throwException(expectedException));
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
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final int childNodeID = 4;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final WorkspaceNodeType childType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childRefId = "ref_1234546u75645";
        final String componentPath = "/lat-session/WrittenResource";
        
        final Collection<WorkspaceNode> parentNodes = new ArrayList<>();
        parentNodes.add(mockParentNode);
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            allowing(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getType(); will(returnValue(childType));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            allowing(mockChildNode).getOriginURI(); will(returnValue(childURI));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(parentNodes));

            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURI); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockNodeUtil).isNodeInfoFile(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildReferenceWithHandle).getMimetype(); will(returnValue(null));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, null, childType, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockChildReferenceWithHandle).getId(); will(returnValue(childRefId));
            oneOf(mockMetadataApiBridge).getComponent(mockParentDocument, componentPath, childRefId); will(returnValue(mockChildComponent));
            oneOf(mockMetadataApiBridge).removeComponent(mockChildComponent);
            
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
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
        
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        final URL otherParentURL = new URL("file:/lamus/workspace/" + workspaceID + "/otherparent.cmdi");
        final URI otherParentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        
        final int childNodeID = 4;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final WorkspaceNodeType childType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childRefId = "ref_1234546u75645";
        final String componentPath = "/lat-session/WrittenResource";
        
        final Collection<WorkspaceNode> parentNodes = new ArrayList<>();
        parentNodes.add(mockParentNode);
        parentNodes.add(mockOtherParentNode);
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            allowing(mockOtherParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockOtherParentNode).getWorkspaceNodeID(); will(returnValue(otherParentNodeID));
            allowing(mockOtherParentNode).getWorkspaceURL(); will(returnValue(otherParentURL));
            allowing(mockOtherParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            allowing(mockOtherParentNode).getProfileSchemaURI(); will(returnValue(otherParentProfileLocation));
            
            allowing(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getType(); will(returnValue(childType));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            allowing(mockChildNode).getOriginURI(); will(returnValue(childURI));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(childNodeID); will(returnValue(parentNodes));
        }});
        
        // first iteration of the loop
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockParentDocument).getDocumentReferenceByLocation(childURI); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockNodeUtil).isNodeInfoFile(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildReferenceWithHandle).getMimetype(); will(returnValue(null));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, null, childType, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockChildReferenceWithHandle).getId(); will(returnValue(childRefId));
            oneOf(mockMetadataApiBridge).getComponent(mockParentDocument, componentPath, childRefId); will(returnValue(mockChildComponent));
            oneOf(mockMetadataApiBridge).removeComponent(mockChildComponent);
            
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
        }});
        
        // second iteration of the loop
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(otherParentURL); will(returnValue(mockOtherParentDocument));
            
            oneOf(mockOtherParentDocument).getDocumentReferenceByLocation(childURI); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockNodeUtil).isNodeInfoFile(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildReferenceWithHandle).getMimetype(); will(returnValue(null));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(otherParentProfileLocation, null, childType, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockChildReferenceWithHandle).getId(); will(returnValue(childRefId));
            oneOf(mockMetadataApiBridge).getComponent(mockOtherParentDocument, componentPath, childRefId); will(returnValue(mockChildComponent));
            oneOf(mockMetadataApiBridge).removeComponent(mockChildComponent);
            
            oneOf(mockOtherParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockOtherParentDocument, otherParentURL);
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, otherParentNodeID, childNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinkManager.unlinkNodeFromAllParents(mockChildNode);
    }
    
    @Test
    public void unlinkNodeFromReplacedParent() throws WorkspaceException, ProtectedNodeException, MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException {
        
        final int workspaceID = 1;
        final int oldParentNodeID = 2;
        final int newParentNodeID = 3;
        
        final URL oldParentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        
        final int childNodeID = 4;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final WorkspaceNodeType childType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childRefId = "ref_1234546u75645";
        final String componentPath = "/lat-session/WrittenResource";
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(newParentNodeID));
            
            allowing(mockOtherParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockOtherParentNode).getWorkspaceNodeID(); will(returnValue(oldParentNodeID));
            allowing(mockOtherParentNode).getWorkspaceURL(); will(returnValue(oldParentURL));
            allowing(mockOtherParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            allowing(mockOtherParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            allowing(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            allowing(mockChildNode).getType(); will(returnValue(childType));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            allowing(mockChildNode).getOriginURI(); will(returnValue(childURI));
            
            oneOf(mockWorkspaceDao).getOlderVersionOfNode(workspaceID, newParentNodeID); will(returnValue(mockOtherParentNode));
        }});
        
        // unlink nodes
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(oldParentURL); will(returnValue(mockOtherParentDocument));
            
            oneOf(mockOtherParentDocument).getDocumentReferenceByLocation(childURI); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockNodeUtil).isNodeInfoFile(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildReferenceWithHandle).getMimetype(); will(returnValue(null));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, null, childType, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockChildReferenceWithHandle).getId(); will(returnValue(childRefId));
            oneOf(mockMetadataApiBridge).getComponent(mockOtherParentDocument, componentPath, childRefId); will(returnValue(mockChildComponent));
            oneOf(mockMetadataApiBridge).removeComponent(mockChildComponent);
            
            oneOf(mockOtherParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockOtherParentDocument, oldParentURL);
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, oldParentNodeID, childNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinkManager.unlinkNodeFromReplacedParent(mockChildNode, mockParentNode);
    }
    
    @Test
    public void unlinkNodeFromReplacedParent_NotFound() throws WorkspaceNodeNotFoundException, WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        final int newParentNodeID = 3;
        
        final WorkspaceNodeNotFoundException exceptionToThrow = new WorkspaceNodeNotFoundException("some exception message", workspaceID, -1, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(newParentNodeID));
            
            oneOf(mockWorkspaceDao).getOlderVersionOfNode(workspaceID, newParentNodeID); will(throwException(exceptionToThrow));
        }});
        
        nodeLinkManager.unlinkNodeFromReplacedParent(mockChildNode, mockParentNode);
    }
    
    @Test
    public void replaceResourceNode() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
     
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final URI parentProfileLocation = URI.create("http:/schema/location/profile_bla_bla");
        
        final int oldChildNodeID = 3;
        final URL oldChildURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI oldChildURI = oldChildURL.toURI();
        
        final int newChildNodeID = 20;
        final URL newChildURL = new URL("file:/lamus/workspace/" + workspaceID + "/another_child.txt");
        final URI newChildURI = newChildURL.toURI();
        final WorkspaceNodeType childType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childMimetype = "text/plain";
        final String childRefId = "ref_1234546u75645";
        final String componentPath = "/lat-session/WrittenResource";
        //TODO SHOULD THIS BE JUST THE ELEMENT OR THE WHOLE PATH INTO THE ELEMENT???
        
        final Collection<WorkspaceNode> emptyParentNodes = new ArrayList<>();
        
        // unlink old node
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldChildNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newChildNodeID));
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            allowing(mockOldNode).getWorkspaceURL(); will(returnValue(oldChildURL));
            allowing(mockNewNode).getWorkspaceURL(); will(returnValue(newChildURL));
            allowing(mockOldNode).getType(); will(returnValue(childType));
            allowing(mockNewNode).getType(); will(returnValue(childType));
            
            oneOf(mockWorkspaceDao).isTopNodeOfWorkspace(workspaceID, oldChildNodeID); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(newChildNodeID); will(returnValue(emptyParentNodes));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockParentDocument).getDocumentReferenceByLocation(oldChildURI); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockNodeUtil).isNodeInfoFile(mockOldNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildReferenceWithHandle).getMimetype(); will(returnValue(childMimetype));
            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, childMimetype, null, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockChildReferenceWithHandle).getId(); will(returnValue(childRefId));
            oneOf(mockMetadataApiBridge).getComponent(mockParentDocument, componentPath, childRefId); will(returnValue(mockChildComponent));
            oneOf(mockMetadataApiBridge).removeComponent(mockChildComponent);
            
            oneOf(mockParentDocument).removeDocumentReference(mockChildReferenceWithHandle); will(returnValue(mockChildReferenceWithHandle));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, oldChildNodeID);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        
        // link new node
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            
            allowing(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileLocation));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockParentNode).isProtected(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockNewNode); will(returnValue(Boolean.FALSE));
            oneOf(mockMetadataApiBridge).isResourceReferenceAllowedInProfile(parentProfileLocation); will(returnValue(Boolean.TRUE));
            oneOf(mockNewNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockNewNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentResourceReference(null, newChildURI, MetadataReferenceType.REFERENCE_TYPE_RESOURCE, childMimetype);
                will(returnValue(mockChildResourceReference));
                
            allowing(mockChildResourceReference).getMimetype(); will(returnValue(childMimetype));

            oneOf(mockMetadataApiBridge).getComponentPathForProfileAndReferenceType(parentProfileLocation, childMimetype, null, Boolean.FALSE);
                will(returnValue(componentPath));
            oneOf(mockMetadataApiBridge).createComponentPathWithin(mockParentDocument, componentPath);
                will(returnValue(mockCmdiContainerMetadataElement));
            oneOf(mockMetadataApiBridge).addReferenceInComponent(mockCmdiContainerMetadataElement, mockChildResourceReference);
                will(returnValue(mockRetrievedResourceProxy));
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);

            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, newChildNodeID);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        //replace node in DB (create new version and set old node as replaced)
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).replaceNode(mockOldNode, mockNewNode);
        }});
        
        
        nodeLinkManager.replaceNode(mockParentNode, mockOldNode, mockNewNode, Boolean.FALSE);
    }
    
    @Test
    public void replaceResourceNode_NewNodeAlreadyLinked() throws WorkspaceException, ProtectedNodeException {
     
        final int workspaceID = 1;
        final int parentNodeID = 2;
        final int oldChildNodeID = 3;
        final int newChildNodeID = 20;
        
        //replace node in DB (create new version and set old node as replaced)
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldChildNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newChildNodeID));
            
            oneOf(mockWorkspaceDao).isTopNodeOfWorkspace(workspaceID, oldChildNodeID); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceDao).replaceNode(mockOldNode, mockNewNode);
        }});
        
        
        nodeLinkManager.replaceNode(mockParentNode, mockOldNode, mockNewNode, Boolean.TRUE);
    }
    
        @Test
    public void replaceTopNode() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceException, ProtectedNodeException {
     
        final int workspaceID = 1;
        final int oldNodeID = 3;
        final int newNodeID = 20;
        final URI newNodeURI = URI.create(UUID.randomUUID().toString());
        
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            allowing(mockNewNode).getArchiveURI(); will(returnValue(newNodeURI));
            allowing(mockNewNode).getArchiveURL(); will(returnValue(null));
            
            oneOf(mockWorkspaceDao).isTopNodeOfWorkspace(workspaceID, oldNodeID); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            oneOf(mockWorkspace).setTopNodeID(newNodeID);
            oneOf(mockWorkspace).setTopNodeArchiveURI(newNodeURI);
            oneOf(mockWorkspace).setTopNodeArchiveURL(null);
            oneOf(mockWorkspaceDao).updateWorkspaceTopNode(mockWorkspace);
            
            oneOf(mockWorkspaceDao).replaceNode(mockOldNode, mockNewNode);
        }});
        
        nodeLinkManager.replaceNode(null, mockOldNode, mockNewNode, Boolean.FALSE);
    }
    
    @Test
    public void removeArchiveUriFromResourceChildNode() throws WorkspaceException, MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException {
        
        final int workspaceID = 1;
        final int parentNodeID = 1;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 2;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final URI childArchiveURI = URI.create(UUID.randomUUID().toString());
        
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
            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockParentDocument, childArchiveURI); will(returnValue(mockChildDataResourceProxy));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildDataResourceProxy).setURI(childURI);

            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.FALSE));
            
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
        final URI childArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
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
            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockParentDocument, childArchiveURI); will(returnValue(mockChildMetadataResourceProxy));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildMetadataResourceProxy).setURI(childURI);

            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.TRUE));
            
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
    public void removeArchiveUri_MetadataException_GetParentDocument() throws MalformedURLException, IOException, MetadataException, WorkspaceException {
        
        final int workspaceID = 1;
        
        final int parentID = 100;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        
        final int childID = 200;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
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
        final URI childArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
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
            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockParentDocument, childArchiveURI); will(returnValue(mockChildDataResourceProxy));
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
        final URI childArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
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
            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockParentDocument, childArchiveURI); will(returnValue(mockChildMetadataResourceProxy));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildMetadataResourceProxy).setURI(childURI);

            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentURL);
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.TRUE));
            
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
    public void removeArchiveUri_nullArchiveUri() throws WorkspaceException {
        
        final int workspaceID = 1;
        final int parentNodeID = 1;
        final int childNodeID = 2;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockChildNode).getArchiveURI(); will(returnValue(null));
            
        }});
        
        nodeLinkManager.removeArchiveUriFromChildNode(mockParentNode, mockChildNode);
    }
    
    @Test
    public void removeArchiveUriFromUploadedNodeRecursively_NotUploaded() throws WorkspaceException {
        
        final int nodeId = 1;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.ARCHIVE_COPY;
        final Collection<WorkspaceNode> noChildren = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildNode).getStatus(); will(returnValue(nodeStatus));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(nodeId));
            oneOf(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeId); will(returnValue(noChildren));
        }});
        
        nodeLinkManager.removeArchiveUriFromUploadedNodeRecursively(mockChildNode, Boolean.TRUE);
    }
    
    @Test
    public void removeArchiveUriFromUploadedNodeRecursively_ResourceNodeUploaded() throws WorkspaceException, MalformedURLException, IOException, MetadataException, TransformerException {
        
        final int nodeId = 1;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.UPLOADED;
        final URL nodeURL = new URL("https://archive/location/node.cmdi");
        final URI nodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final Collection<WorkspaceNode> noChildren = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            allowing(mockChildNode).getStatus(); will(returnValue(nodeStatus));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(nodeURI));
            allowing(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.FALSE));

            //remove archive URI and URL
            oneOf(mockChildNode).setArchiveURI(null);
            oneOf(mockChildNode).setArchiveURL(null);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockChildNode);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockChildNode);
        }});
        
        nodeLinkManager.removeArchiveUriFromUploadedNodeRecursively(mockChildNode, Boolean.TRUE);
    }
    
    @Test
    public void removeArchiveUriFromUploadedNodeRecursively_ResourceNodeUploaded_ArchiveUriNull() throws WorkspaceException, MalformedURLException, IOException, MetadataException, TransformerException {
        
        final int nodeId = 1;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.UPLOADED;
        final URL nodeURL = new URL("https://archive/location/node.cmdi");
        final Collection<WorkspaceNode> noChildren = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            allowing(mockChildNode).getStatus(); will(returnValue(nodeStatus));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(null));
            allowing(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.FALSE));
        }});
        
        nodeLinkManager.removeArchiveUriFromUploadedNodeRecursively(mockChildNode, Boolean.TRUE);
    }
    
    @Test
    public void removeArchiveUriFromUploadedNodeRecursively_MetadataNodeUploadedWithoutChildren() throws WorkspaceException, MalformedURLException, IOException, MetadataException, TransformerException {
        
        final int nodeId = 1;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.UPLOADED;
        final URL nodeURL = new URL("https://archive/location/node.cmdi");
        final URI nodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final Collection<WorkspaceNode> noChildren = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            allowing(mockChildNode).getStatus(); will(returnValue(nodeStatus));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(nodeURI));
            allowing(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.TRUE));
            
            //remove self handle
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(nodeURL));
            oneOf(mockMetadataAPI).getMetadataDocument(nodeURL); will(returnValue(mockChildCmdiDocument));
            oneOf(mockChildCmdiDocument).setHandle(null);
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockChildCmdiDocument, nodeURL);

            //remove archive URI and URL
            oneOf(mockChildNode).setArchiveURI(null);
            oneOf(mockChildNode).setArchiveURL(null);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockChildNode);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockChildNode);
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(nodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeId); will(returnValue(noChildren));
        }});
        
        nodeLinkManager.removeArchiveUriFromUploadedNodeRecursively(mockChildNode, Boolean.TRUE);
    }
    
    @Test
    public void removeArchiveUriFromUploadedNodeRecursively_NodeWithChild() throws MalformedURLException, IOException, MetadataException, TransformerException, WorkspaceException, URISyntaxException {

        final int nodeId = 1;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.UPLOADED;
        final URL nodeURL = new URL("https://archive/location/node.cmdi");
        final URI nodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        
        final int childNodeId = 2;
        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.UPLOADED;
        final URL childURL = new URL("https://archive/location/child.cmdi");
        final URI childURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        
        final Collection<WorkspaceNode> children = new ArrayList<>();
        children.add(mockChildNode);
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getStatus(); will(returnValue(nodeStatus));
            allowing(mockParentNode).getArchiveURI(); will(returnValue(nodeURI));
            allowing(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            //remove self handle
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(nodeURL));
            allowing(mockMetadataAPI).getMetadataDocument(nodeURL); will(returnValue(mockParentDocument));
            oneOf(mockParentDocument).setHandle(null);
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, nodeURL);

            //remove archive URI and URL from DB
            oneOf(mockParentNode).setArchiveURI(null);
            oneOf(mockParentNode).setArchiveURL(null);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockParentNode);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockParentNode);
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(nodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeId); will(returnValue(children));
            
            // remove reference from parent to child
            allowing(mockChildNode).getStatus(); will(returnValue(childStatus));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(childURI));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockParentDocument, childURI); will(returnValue(mockChildReference));
            oneOf(mockChildReference).setURI(childURL.toURI());
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, nodeURL);
            
            //remove archive URI and URL from DB
            allowing(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockChildNode).setArchiveURI(null);
            oneOf(mockChildNode).setArchiveURL(null);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockChildNode);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockChildNode);
        }});
        
        nodeLinkManager.removeArchiveUriFromUploadedNodeRecursively(mockParentNode, Boolean.TRUE);
    }
    
    @Test
    public void removeArchiveUriFromUploadedNodeRecursively_NodeWithChildren() throws MalformedURLException, IOException, MetadataException, TransformerException, WorkspaceException, URISyntaxException {

        final int nodeId = 1;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.UPLOADED;
        final URL nodeURL = new URL("https://archive/location/node.cmdi");
        final URI nodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        
        final int childNodeId = 2;
        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.UPLOADED;
        final URL childURL = new URL("https://archive/location/child.cmdi");
        final URI childURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        
        final int anotherChildNodeId = 3;
        final WorkspaceNodeStatus anotherChildStatus = WorkspaceNodeStatus.UPLOADED;
        final URL anotherChildURL = new URL("https://archive/location/anotherChild.cmdi");
        final URI anotherChildURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        
        final int grandChildNodeId = 4;
        final WorkspaceNodeStatus grandChildStatus = WorkspaceNodeStatus.UPLOADED;
        final URL grandChildURL = new URL("https://archive/location/grandChild.cmdi");
        final URI grandChildURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        
        final Collection<WorkspaceNode> children = new ArrayList<>();
        children.add(mockChildNode);
        children.add(mockAnotherChildNode);
        
        final Collection<WorkspaceNode> grandChildren = new ArrayList<>();
        grandChildren.add(mockGrandChildNode);
        
        final Collection<WorkspaceNode> emptyGrandChildren = new ArrayList<>();
        
        
        context.checking(new Expectations() {{
            
            allowing(mockParentNode).getStatus(); will(returnValue(nodeStatus));
            allowing(mockParentNode).getArchiveURI(); will(returnValue(nodeURI));
            allowing(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            
            //remove self handle
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(nodeURL));
            allowing(mockMetadataAPI).getMetadataDocument(nodeURL); will(returnValue(mockParentDocument));
            oneOf(mockParentDocument).setHandle(null);
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, nodeURL);

            //remove archive URI and URL from DB
            oneOf(mockParentNode).setArchiveURI(null);
            oneOf(mockParentNode).setArchiveURL(null);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockParentNode);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockParentNode);
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(nodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeId); will(returnValue(children));
            
            // remove reference from parent to child
            allowing(mockChildNode).getStatus(); will(returnValue(childStatus));
            allowing(mockChildNode).getArchiveURI(); will(returnValue(childURI));
            allowing(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockParentDocument, childURI); will(returnValue(mockChildReference));
            oneOf(mockChildReference).setURI(childURL.toURI());
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, nodeURL);
            
            //remove archive URI and URL from DB
            allowing(mockNodeUtil).isNodeMetadata(mockChildNode); will(returnValue(Boolean.TRUE));
            
            //remove self handle
            allowing(mockMetadataAPI).getMetadataDocument(childURL); will(returnValue(mockChildCmdiDocument));
            oneOf(mockChildCmdiDocument).setHandle(null);
            
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockChildCmdiDocument, childURL);
            
            oneOf(mockChildNode).setArchiveURI(null);
            oneOf(mockChildNode).setArchiveURL(null);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockChildNode);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockChildNode);
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(childNodeId); will(returnValue(grandChildren));
            
            // remove reference from child to grandchild
            allowing(mockGrandChildNode).getStatus(); will(returnValue(grandChildStatus));
            allowing(mockGrandChildNode).getArchiveURI(); will(returnValue(grandChildURI));
            allowing(mockGrandChildNode).getWorkspaceURL(); will(returnValue(grandChildURL));
            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockChildCmdiDocument, grandChildURI); will(returnValue(mockGrandChildReference));
            oneOf(mockGrandChildReference).setURI(grandChildURL.toURI());
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockChildCmdiDocument, childURL);
            
            //remove archive URI and URL from DB
            allowing(mockNodeUtil).isNodeMetadata(mockGrandChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockGrandChildNode).setArchiveURI(null);
            oneOf(mockGrandChildNode).setArchiveURL(null);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockGrandChildNode);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockGrandChildNode);
            
            
            //another child
            
            // remove reference from parent to child
            allowing(mockAnotherChildNode).getStatus(); will(returnValue(anotherChildStatus));
            allowing(mockAnotherChildNode).getArchiveURI(); will(returnValue(anotherChildURI));
            allowing(mockAnotherChildNode).getWorkspaceURL(); will(returnValue(anotherChildURL));
            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockParentDocument, anotherChildURI); will(returnValue(mockAnotherChildReference));
            oneOf(mockAnotherChildReference).setURI(anotherChildURL.toURI());
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, nodeURL);
            
            //remove archive URI and URL from DB
            allowing(mockNodeUtil).isNodeMetadata(mockAnotherChildNode); will(returnValue(Boolean.FALSE));
            oneOf(mockAnotherChildNode).setArchiveURI(null);
            oneOf(mockAnotherChildNode).setArchiveURL(null);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockAnotherChildNode);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockAnotherChildNode);
        }});
        
        nodeLinkManager.removeArchiveUriFromUploadedNodeRecursively(mockParentNode, Boolean.TRUE);
    }
    
    
    
    
    //TODO ANY MORE TESTS????
    
    

    
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
            
            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockParentDocument, childURI); will(returnValue(mockChildReferenceWithHandle));
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