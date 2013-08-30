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
package nl.mpi.lamus.workspace.importing.implementation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeLinker;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceParentNodeReference;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.MetadataReference;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.model.ResourceReference;
import nl.mpi.metadata.cmdi.api.model.MetadataResourceProxy;
import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
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
public class LamusWorkspaceNodeLinkerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private WorkspaceNodeLinker nodeLinker;
    
    @Mock WorkspaceParentNodeReferenceFactory mockWorkspaceParentNodeReferenceFactory;
    @Mock WorkspaceNodeLinkFactory mockWorkspaceNodeLinkFactory;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    
    @Mock WorkspaceParentNodeReference mockWorkspaceParentNodeReference;
    @Mock WorkspaceNodeLink mockWorkspaceNodeLink;
    
    @Mock Workspace mockWorkspace;
    @Mock WorkspaceNode mockParentNode;
    @Mock WorkspaceNode mockChildNode;
    @Mock Reference mockChildReference;

    @Mock ReferencingMetadataDocument mockParentDocument;
    @Mock MetadataReference mockChildMetadataReference;
    @Mock ResourceReference mockChildResourceReference;
    
    @Mock File mockParentFile;
    @Mock StreamResult mockParentStreamResult;
            
    public LamusWorkspaceNodeLinkerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        nodeLinker = new LamusWorkspaceNodeLinker(
                mockWorkspaceParentNodeReferenceFactory, mockWorkspaceNodeLinkFactory,
                mockWorkspaceDao, mockMetadataAPI, mockWorkspaceFileHandler);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void linkNodesWithReference() throws URISyntaxException {
        
        final int parentNodeID = 1;
        final int childNodeID = 2;
        final URI childURI = new URI("http://some.uri");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(mockParentNode, mockChildReference);
                will(returnValue(mockWorkspaceParentNodeReference));
            
            oneOf(mockWorkspaceParentNodeReference).getParentWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockChildReference).getURI(); will(returnValue(childURI));
                
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID, childURI);
                will(returnValue(mockWorkspaceNodeLink));
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        nodeLinker.linkNodesWithReference(mockParentNode, mockChildNode, mockChildReference);
    }

    @Test
    public void linkNodesWithReferenceWithNullParentNode() throws MalformedURLException {
        
        final int childNodeID = 2;
        final int childNodeArchiveID = 4;
        final URL childNodeURL = new URL("http://some.url");
        final int workspaceID = 1;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(null, mockChildReference);
                will(returnValue(null));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspace).setTopNodeID(childNodeID);
            oneOf(mockChildNode).getArchiveNodeID(); will(returnValue(childNodeArchiveID));
            oneOf(mockWorkspace).setTopNodeArchiveID(childNodeArchiveID);
            oneOf(mockChildNode).getArchiveURL(); will(returnValue(childNodeURL));
            oneOf(mockWorkspace).setTopNodeArchiveURL(childNodeURL);
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            oneOf(mockWorkspaceDao).updateWorkspaceTopNode(mockWorkspace);
        }});
        
        nodeLinker.linkNodesWithReference(null, mockChildNode, mockChildReference);
    }
  
    //TODO
    
//    @Test
//    public void linkNodesWithNullChildReference() {
//        
////        context.checking(new Expectations() {{
////            
////            
////        }});
////        
////        nodeLinker.linkNodesWithReference(mockWorkspace, mockParentNode, mockChildNode, null);
//        
//        fail("What should happen when just the child reference is null?");
//    }

    @Test
    public void linkNodesMetadata() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException {
        
        final int workspaceID = 1;
        final int parentNodeID = 1;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 2;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.cmdi");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/x-cmdi+xml";
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentMetadataReference(childURI, childMimetype);
                will(returnValue(mockChildMetadataReference));
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID, childURI);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinker.linkNodes(mockParentNode, mockChildNode);
    }
    
    @Test
    public void linkNodesResource() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException {
        
        final int workspaceID = 1;
        final int parentNodeID = 1;
        final URL parentURL = new URL("file:/lamus/workspace/" + workspaceID + "/parent.cmdi");
        final int childNodeID = 2;
        final URL childURL = new URL("file:/lamus/workspace/" + workspaceID + "/child.txt");
        final URI childURI = childURL.toURI();
        final String childMimetype = "text/x-cmdi+xml";
        final WorkspaceNodeType childWsType = WorkspaceNodeType.RESOURCE_WR;
        final String childStringType = childWsType.toString();
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentURL); will(returnValue(mockParentDocument));
            
            oneOf(mockChildNode).isMetadata(); will(returnValue(Boolean.FALSE));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            oneOf(mockChildNode).getType(); will(returnValue(childWsType));
            oneOf(mockChildNode).getFormat(); will(returnValue(childMimetype));
            oneOf(mockParentDocument).createDocumentResourceReference(childURI, childStringType, childMimetype);
                will(returnValue(mockChildResourceReference));
                
            oneOf(mockParentNode).getWorkspaceURL(); will(returnValue(parentURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockParentFile); will(returnValue(mockParentStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockParentDocument, mockParentStreamResult);
            
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(childURL));
            
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID, childURI);
                will(returnValue(mockWorkspaceNodeLink));
            
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockParentFile);
        
        nodeLinker.linkNodes(mockParentNode, mockChildNode);
    }
}