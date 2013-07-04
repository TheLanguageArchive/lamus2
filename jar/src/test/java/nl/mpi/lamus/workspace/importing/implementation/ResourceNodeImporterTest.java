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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.workspace.exception.NodeExplorerException;
import nl.mpi.lamus.workspace.exception.NodeImporterException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceParentNodeReference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import nl.mpi.util.OurURL;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(//classes = {ResourceFileImporterTestProperties.class},
        loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
public class ResourceNodeImporterTest {
    
    @Configuration
    @Profile("testing")
    static class ResourceNodeImporterTestProperties {
        
        @Bean
        @Qualifier("orphansDirectoryBaseName")
        public String orphansDirectoryBaseName() {
            return "sessions";
        }
    }
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private NodeImporter nodeImporter;
    private NodeImporter nodeImporterWithoutWorkspace;
    @Mock ArchiveObjectsDB mockArchiveObjectsDB;
    @Mock WorkspaceDao mockWorkspaceDao;
    
    @Mock NodeDataRetriever mockNodeDataRetriever;
    
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock WorkspaceNodeFactory mockWorkspaceNodeFactory;
    @Mock WorkspaceParentNodeReferenceFactory mockWorkspaceParentNodeReferenceFactory;
    @Mock WorkspaceNodeLinkFactory mockWorkspaceNodeLinkFactory;
    private Workspace testWorkspace;
    
    @Mock ReferencingMetadataDocument mockReferencingMetadataDocument;
    @Mock ResourceProxy mockChildLink;
    @Mock FileTypeHandler mockFileTypeHandler;
    
    @Mock TypecheckedResults mockTypecheckedResults;
    
    private final int workspaceID = 1;
    
    public ResourceNodeImporterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        testWorkspace = new LamusWorkspace(workspaceID, "someUser", -1, -1, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.INITIALISING, "Workspace initialising", "archiveInfo/something");
        nodeImporter = new ResourceNodeImporter(mockArchiveObjectsDB, mockWorkspaceDao, mockNodeDataRetriever,
                mockArchiveFileHelper, mockFileTypeHandler, mockWorkspaceNodeFactory,
                mockWorkspaceParentNodeReferenceFactory, mockWorkspaceNodeLinkFactory);
//        nodeImporter.setWorkspace(testWorkspace);
        nodeImporterWithoutWorkspace = new ResourceNodeImporter(mockArchiveObjectsDB, mockWorkspaceDao, mockNodeDataRetriever,
                mockArchiveFileHelper, mockFileTypeHandler, mockWorkspaceNodeFactory,
                mockWorkspaceParentNodeReferenceFactory, mockWorkspaceNodeLinkFactory);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of importNode method, of class ResourceNodeImporter.
     */
    @Test
    public void importResourceNodeSuccessfully() throws Exception {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final int childNodeArchiveID = 100;
        final String childNodeLabel = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE_WR; //TODO WHat to use here?
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = new URI("file:/some.location");
        final String childNodePid = UUID.randomUUID().toString();
        final URI childLinkURI = new URI("http://some.uri/filename.txt"); //TODO Where to get this from? What to do with it?
        final OurURL childNodeUrl = new OurURL(childLinkURI.toURL());
        final OurURL childNodeUrlWithContext = new OurURL("file:/some.uri/filename.txt");
        final URL parentURL = new URL("file:/some.uri/filename.cmdi");
        final String parentPid = UUID.randomUUID().toString();
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), 1, childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentURL, parentURL, parentURL, WorkspaceNodeStatus.NODE_ISCOPY, parentPid, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeArchiveID, childNodeSchemaLocation,
                childNodeLabel, "", childNodeType, childLinkURI.toURL(), childLinkURI.toURL(), childLinkURI.toURL(), WorkspaceNodeStatus.NODE_CREATED, childNodePid, childNodeMimetype);
        final WorkspaceParentNodeReference testParentNodeReference = new LamusWorkspaceParentNodeReference(parentWorkspaceNodeID, mockChildLink);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childLinkURI);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLink).getURI(); will(returnValue(childLinkURI));
            oneOf(mockNodeDataRetriever).getResourceURL(mockChildLink); will(returnValue(childNodeUrl));

            oneOf (mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(childNodeArchiveID), ArchiveAccessContext.getFileUrlContext());
                will(returnValue(childNodeUrlWithContext));
            
            oneOf(mockChildLink).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLink, childNodeUrlWithContext, childNodeArchiveID);
                will(returnValue(true));
                
            oneOf(mockNodeDataRetriever).getResourceFileChecked(childNodeArchiveID, mockChildLink, childNodeUrl, childNodeUrlWithContext);
                will(returnValue(mockTypecheckedResults));
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(childNodeUrl, mockChildLink, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedNodeType(); will(returnValue(childNodeType));
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
                
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceResourceNode(testWorkspace.getWorkspaceID(), childNodeArchiveID,
                    childNodeUrlWithContext.toURL(), mockChildLink, childNodeType, childNodeMimetype);
                will(returnValue(testChildNode));

            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);

            oneOf (mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(testParentNode, mockChildLink);
                will(returnValue(testParentNodeReference));
            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childLinkURI);
                will(returnValue(testNodeLink));
            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
        }});
        
        //TODO PID SHOULD BE COMING FROM THE CHILD LINK (HandleCarrier)
        nodeImporter.importNode(testWorkspace.getWorkspaceID(), testParentNode, mockReferencingMetadataDocument, mockChildLink, childNodeArchiveID);
        
    }
    
    @Test
    public void workspaceWasNotSet() throws URISyntaxException, MalformedURLException, NodeExplorerException {
        
        final int parentWorkspaceNodeID = 1;
        final URI childNodeSchemaLocation = new URI("file:/some.location");
        final URL parentURL = new URL("file:/some.uri/filename.cmdi");
        final String parentPid = UUID.randomUUID().toString();
        final int childNodeArchiveID = 100;
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), 1, childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentURL, parentURL, parentURL, WorkspaceNodeStatus.NODE_ISCOPY, parentPid, "cmdi");
        
        try {
            nodeImporterWithoutWorkspace.importNode(-1, testParentNode, mockReferencingMetadataDocument, mockChildLink, childNodeArchiveID);
            fail("Should have thrown NodeImporterException");
        } catch(NodeImporterException ex) {
            assertNotNull(ex);
            String expectedErrorMessage = "ResourceNodeImporter.importNode: workspace not set";
            assertEquals(expectedErrorMessage, ex.getMessage());
            assertEquals(-1, ex.getWorkspaceID());
            assertEquals(ResourceNodeImporter.class, ex.getNodeImporterType());
            assertNull(ex.getCause());
        }
    }
    
    //TODO TEST OTHER POSSIBILITIES/EXCEPTIONS
    
//    @Test
//    public void childLinkIsNotURL() throws URISyntaxException, MalformedURLException, NodeExplorerException {
//        
//        final int parentWorkspaceNodeID = 1;
//        final URI childNodeSchemaLocation = new URI("file:/some.location");
//        final URL parentURL = new URL("file:/some.uri/filename.cmdi");
//        final int childNodeArchiveID = 100;
//        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), 1, childNodeSchemaLocation,
//                "parent label", "", WorkspaceNodeType.METADATA, parentURL, parentURL, parentURL, WorkspaceNodeStatus.NODE_ISCOPY, "aPid", "cmdi");
//        final URI validUriButInvalidUrl = new URI("urn:isbn:1234567890");
//        
//        context.checking(new Expectations() {{
//            
//            oneOf (mockChildLink).getURI(); will(returnValue(validUriButInvalidUrl));
//        }});
//        
//        try {
//            nodeImporter.importNode(testParentNode, mockReferencingMetadataDocument, mockChildLink, childNodeArchiveID);
//        } catch (NodeImporterException ex) {
//            assertNotNull(ex);
//            String expectedErrorMessage = "Error getting URL for link " + validUriButInvalidUrl;
//            assertEquals(expectedErrorMessage, ex.getMessage());
//            assertEquals(testWorkspace, ex.getWorkspace());
//            assertEquals(ResourceNodeImporter.class, ex.getNodeImporterType());
//            assertTrue(ex.getCause() instanceof MalformedURLException);
//        }
//    }
//    
//    @Test
//    public void childIsNotOnsite() throws URISyntaxException, MalformedURLException, NodeImporterException, NodeExplorerException {
//        
//        final int parentWorkspaceNodeID = 1;
//        final URI childNodeSchemaLocation = new URI("file:/some.location");
//        final URL parentURL = new URL("file:/some.uri/filename.cmdi");
//        final int childNodeArchiveID = 100;
//        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), 1, childNodeSchemaLocation,
//                "parent label", "", WorkspaceNodeType.METADATA, parentURL, parentURL, parentURL, WorkspaceNodeStatus.NODE_ISCOPY, "aPid", "cmdi");
//        
//        final URI childLinkURI = new URI("http:/some.external.uri/filename.txt");
//        final OurURL childNodeUrl = new OurURL(childLinkURI.toURL());
//        final String childNodeName = "filename.txt";
//        final String childNodeLabel = "file name label";
//        final String childNodeMimetype = "txt";
//        final OurURL archiveFileUrlWithContext = new OurURL("http:/some.external.uri/filename.txt");
//        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE_WR;
//        final int childWorkspaceNodeID = 10;
//        final String childNodePid = "somePid";
//        
//        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeArchiveID, childNodeSchemaLocation,
//                childNodeLabel, "", childNodeType, childLinkURI.toURL(), childLinkURI.toURL(), childLinkURI.toURL(), WorkspaceNodeStatus.NODE_CREATED, childNodePid, childNodeMimetype);
//        
//        final WorkspaceParentNodeReference testParentNodeReference = new LamusWorkspaceParentNodeReference(parentWorkspaceNodeID, mockChildLink);
//        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childLinkURI);
//
//        context.checking(new Expectations() {{
//            
//            oneOf (mockChildLink).getURI(); will(returnValue(childLinkURI));
//            
//            oneOf(mockChildLink).getHandle(); will(returnValue(childNodePid));
//            oneOf(mockArchiveObjectsDB).getObjectURLForPid(childNodePid); will(returnValue(childNodeUrl));
//            
//            oneOf (mockArchiveFileHelper).getFileBasename(childNodeUrl.toString()); will(returnValue(childNodeName));
//            oneOf (mockArchiveFileHelper).getFileTitle(childNodeUrl.toString()); will(returnValue(childNodeLabel));
//            
//            oneOf (mockChildLink).getMimetype(); will(returnValue(childNodeMimetype));
//            
////            oneOf (mockFileTypeHandlerFactory).getNewFileTypeHandlerForWorkspace(testWorkspace); will(returnValue(mockFileTypeHandler));
//            
//            oneOf (mockArchiveObjectsDB).isOnSite(NodeIdUtils.TONODEID(childNodeArchiveID)); will(returnValue(false));
//            
//            oneOf (mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(childNodeArchiveID), ArchiveAccessContext.getFileUrlContext());
//                will(returnValue(archiveFileUrlWithContext));
//            
//            oneOf (mockFileTypeHandler).setValues(childNodeMimetype);
//            oneOf (mockFileTypeHandler).getMimetype(); will(returnValue(childNodeMimetype));
//            oneOf (mockFileTypeHandler).getNodeType(); will(returnValue(childNodeType));
//            
//            oneOf (mockWorkspaceNodeFactory).getNewWorkspaceNode(testWorkspace.getWorkspaceID(), childNodeArchiveID, childLinkURI.toURL());
//                will(returnValue(testChildNode));
//            
//            oneOf(mockChildLink).getHandle(); will(returnValue(childNodePid));
//            
//            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
//
//            oneOf (mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(testParentNode, mockChildLink);
//                will(returnValue(testParentNodeReference));
//            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID, childLinkURI);
//                will(returnValue(testNodeLink));
//            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);            
//        }});
//
//        
//        nodeImporter.importNode(testParentNode, mockReferencingMetadataDocument, mockChildLink, childNodeArchiveID);
//    }
}
