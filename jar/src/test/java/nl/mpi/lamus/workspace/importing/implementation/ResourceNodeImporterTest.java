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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import javax.xml.transform.TransformerException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import nl.mpi.lamus.typechecking.implementation.LamusTypecheckedResults;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
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
import org.springframework.test.util.ReflectionTestUtils;

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
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock MetadataApiBridge mockMetadataApiBridge;
    @Mock NodeDataRetriever mockNodeDataRetriever;
    @Mock WorkspaceNodeFactory mockWorkspaceNodeFactory;
    @Mock WorkspaceNodeLinkFactory mockWorkspaceNodeLinkFactory;
    
    private Workspace testWorkspace;
    
    @Mock ReferencingMetadataDocument mockReferencingMetadataDocument;
    @Mock Reference mockChildLinkWithoutHandle;
    @Mock ResourceProxy mockChildLinkWithHandle;
    
    @Mock TypecheckedResults mockTypecheckedResults;
    @Mock CorpusNode mockCorpusNode;
    @Mock File mockFile;
    @Mock InputStream mockInputStream;
    
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
        testWorkspace = new LamusWorkspace(workspaceID, "someUser", -1, null, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.INITIALISING, "Workspace initialising", "");
        
        nodeImporter = new ResourceNodeImporter();
        ReflectionTestUtils.setField(nodeImporter, "corpusStructureProvider", mockCorpusStructureProvider);
        ReflectionTestUtils.setField(nodeImporter, "nodeResolver", mockNodeResolver);
        ReflectionTestUtils.setField(nodeImporter, "workspaceDao", mockWorkspaceDao);
        ReflectionTestUtils.setField(nodeImporter, "metadataApiBridge", mockMetadataApiBridge);
        ReflectionTestUtils.setField(nodeImporter, "nodeDataRetriever", mockNodeDataRetriever);
        ReflectionTestUtils.setField(nodeImporter, "workspaceNodeFactory", mockWorkspaceNodeFactory);
        ReflectionTestUtils.setField(nodeImporter, "workspaceNodeLinkFactory", mockWorkspaceNodeLinkFactory);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void importResourceNodeWithHandle()
            throws MalformedURLException, TypeCheckerException, WorkspaceImportException, IOException, TransformerException, MetadataException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE; //TODO WHat to use here?
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final URI childURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final String childFilename = "childname.txt";
        final URL childWsURL = new URL("file:/workspace/folder/" + childFilename);
        final URI childOriginURI = URI.create("file:/some.uri/" + childFilename);
        final URL childArchiveURL = childOriginURI.toURL();

        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.NODE_VIRTUAL;
        final boolean childOnSite = Boolean.TRUE;
        final boolean childProtected = Boolean.FALSE;
        
        final URI parentURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean parentProtected = Boolean.FALSE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, parentStatus, parentProtected, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childURI, childArchiveURL, childOriginURI, childStatus, childProtected, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(mockFile));
            oneOf(mockFile).toURI(); will(returnValue(childOriginURI));
            
            oneOf(mockChildLinkWithHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithHandle, mockFile, mockCorpusNode);
                will(returnValue(true));

            oneOf(mockNodeResolver).getInputStream(mockCorpusNode); will(returnValue(mockInputStream));
            oneOf(mockFile).getName(); will(returnValue(childFilename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockInputStream, childFilename);
                will(returnValue(mockTypecheckedResults));
            oneOf(mockInputStream).close();
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(mockFile, mockChildLinkWithHandle, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeDataRetriever).isNodeToBeProtected(childURI); will(returnValue(childProtected));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceResourceNode(testWorkspace.getWorkspaceID(),
                    childURI, childArchiveURL, mockChildLinkWithHandle, childNodeMimetype, childNodeName, childOnSite, childProtected);
                will(returnValue(testChildNode));

            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);

            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
                will(returnValue(testNodeLink));
            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
            
            oneOf(mockChildLinkWithHandle).setLocation(null);
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockReferencingMetadataDocument, parentWsURL);
        }});
        
        //TODO PID SHOULD BE COMING FROM THE CHILD LINK (HandleCarrier)
        nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
    }
    
    @Test
    public void importResourceNodeWithoutHandle()
            throws MalformedURLException, TypeCheckerException, WorkspaceImportException, IOException, TransformerException, MetadataException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE; //TODO WHat to use here?
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final String childFilename = "childname.txt";
        final URL childWsURL = new URL("file:/workspace/folder/" + childFilename);
        final URI childOriginURI = URI.create("file:/some.uri/" + childFilename);
        final URL childArchiveURL = childOriginURI.toURL();

        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.NODE_VIRTUAL;
        final boolean childOnSite = Boolean.TRUE;
        final boolean childProtected = Boolean.FALSE;
        
        final URI parentURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean parentProtected = Boolean.FALSE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, parentStatus, parentProtected, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childOriginURI, childArchiveURL, childOriginURI, childStatus, childProtected, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithoutHandle).getURI(); will(returnValue(childOriginURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childOriginURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(mockFile));
            oneOf(mockFile).toURI(); will(returnValue(childOriginURI));
            
            oneOf(mockChildLinkWithoutHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithoutHandle, mockFile, mockCorpusNode);
                will(returnValue(true));
                
            oneOf(mockNodeResolver).getInputStream(mockCorpusNode); will(returnValue(mockInputStream));
            oneOf(mockFile).getName(); will(returnValue(childFilename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockInputStream, childFilename);
                will(returnValue(mockTypecheckedResults));
            oneOf(mockInputStream).close();
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(mockFile, mockChildLinkWithoutHandle, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeDataRetriever).isNodeToBeProtected(childOriginURI); will(returnValue(childProtected));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceResourceNode(testWorkspace.getWorkspaceID(),
                    childOriginURI, childArchiveURL, mockChildLinkWithoutHandle, childNodeMimetype, childNodeName, childOnSite, childProtected);
                will(returnValue(testChildNode));

            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);

            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
                will(returnValue(testNodeLink));
            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
            
            oneOf(mockChildLinkWithoutHandle).setLocation(null);
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockReferencingMetadataDocument, parentWsURL);
        }});
        
        //TODO PID SHOULD BE COMING FROM THE CHILD LINK (HandleCarrier)
        nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithoutHandle);
    }
    
    @Test
    public void importResourceNodeThrowsIOException()
            throws MalformedURLException, TypeCheckerException, WorkspaceImportException, IOException, TransformerException, MetadataException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE; //TODO WHat to use here?
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final URI childURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final String childFilename = "childname.txt";
        final URL childWsURL = new URL("file:/workspace/folder/" + childFilename);
        final URI childOriginURI = URI.create("file:/some.uri/" + childFilename);
        final URL childArchiveURL = childOriginURI.toURL();

        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.NODE_VIRTUAL;
        final boolean childOnSite = Boolean.TRUE;
        final boolean childProtected = Boolean.FALSE;
        
        final URI parentURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean parentProtected = Boolean.FALSE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, parentStatus, parentProtected, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childURI, childArchiveURL, childOriginURI, childStatus, childProtected, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
        
        final IOException expectedException =
                new IOException("this is an exception thrown by the method 'saveMetadataDocument'");
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(mockFile));
            oneOf(mockFile).toURI(); will(returnValue(childOriginURI));
            
            oneOf(mockChildLinkWithHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithHandle, mockFile, mockCorpusNode);
                will(returnValue(true));
                
            oneOf(mockNodeResolver).getInputStream(mockCorpusNode); will(returnValue(mockInputStream));
            oneOf(mockFile).getName(); will(returnValue(childFilename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockInputStream, childFilename);
                will(returnValue(mockTypecheckedResults));
            oneOf(mockInputStream).close();
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(mockFile, mockChildLinkWithHandle, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeDataRetriever).isNodeToBeProtected(childURI); will(returnValue(childProtected));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceResourceNode(testWorkspace.getWorkspaceID(),
                    childURI, childArchiveURL, mockChildLinkWithHandle, childNodeMimetype, childNodeName, childOnSite, childProtected);
                will(returnValue(testChildNode));

            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);

            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
                will(returnValue(testNodeLink));
            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
            
            oneOf(mockChildLinkWithHandle).setLocation(null);
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockReferencingMetadataDocument, parentWsURL);
                will(throwException(expectedException));
        }});
        
        //TODO PID SHOULD BE COMING FROM THE CHILD LINK (HandleCarrier)
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String errorMessage = "Failed to save file " + parentWsURL + " in workspace " + testWorkspace.getWorkspaceID();
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void importResourceNodeThrowsTransformerException()
            throws MalformedURLException, TypeCheckerException, WorkspaceImportException, IOException, TransformerException, MetadataException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE; //TODO WHat to use here?
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final URI childURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final String childFilename = "childname.txt";
        final URL childWsURL = new URL("file:/workspace/folder/" + childFilename);
        final URI childOriginURI = URI.create("file:/some.uri/" + childFilename);
        final URL childArchiveURL = childOriginURI.toURL();

        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.NODE_VIRTUAL;
        final boolean childOnSite = Boolean.TRUE;
        final boolean childProtected = Boolean.FALSE;
        
        final URI parentURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean parentProtected = Boolean.FALSE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, parentStatus, parentProtected, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childURI, childArchiveURL, childOriginURI, childStatus, childProtected, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
        
        final TransformerException expectedException =
                new TransformerException("this is an exception thrown by the method 'saveMetadataDocument'");
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(mockFile));
            oneOf(mockFile).toURI(); will(returnValue(childOriginURI));
            
            oneOf(mockChildLinkWithHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithHandle, mockFile, mockCorpusNode);
                will(returnValue(true));
                
            oneOf(mockNodeResolver).getInputStream(mockCorpusNode); will(returnValue(mockInputStream));
            oneOf(mockFile).getName(); will(returnValue(childFilename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockInputStream, childFilename);
                will(returnValue(mockTypecheckedResults));
            oneOf(mockInputStream).close();
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(mockFile, mockChildLinkWithHandle, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeDataRetriever).isNodeToBeProtected(childURI); will(returnValue(childProtected));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceResourceNode(testWorkspace.getWorkspaceID(),
                    childURI, childArchiveURL, mockChildLinkWithHandle, childNodeMimetype, childNodeName, childOnSite, childProtected);
                will(returnValue(testChildNode));

            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);

            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
                will(returnValue(testNodeLink));
            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
            
            oneOf(mockChildLinkWithHandle).setLocation(null);
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockReferencingMetadataDocument, parentWsURL);
                will(throwException(expectedException));
        }});
        
        //TODO PID SHOULD BE COMING FROM THE CHILD LINK (HandleCarrier)
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String errorMessage = "Failed to save file " + parentWsURL + " in workspace " + testWorkspace.getWorkspaceID();
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void importResourceNodeThrowsMetadataException()
            throws MalformedURLException, TypeCheckerException, WorkspaceImportException, IOException, TransformerException, MetadataException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE; //TODO WHat to use here?
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final URI childURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final String childFilename = "childname.txt";
        final URL childWsURL = new URL("file:/workspace/folder/" + childFilename);
        final URI childOriginURI = URI.create("file:/some.uri/" + childFilename);
        final URL childArchiveURL = childOriginURI.toURL();

        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.NODE_VIRTUAL;
        final boolean childOnSite = Boolean.TRUE;
        final boolean childProtected = Boolean.FALSE;
        
        final URI parentURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean parentProtected = Boolean.FALSE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, parentStatus, parentProtected, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childURI, childArchiveURL, childOriginURI, childStatus, childProtected, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
        
        final MetadataException expectedException =
                new MetadataException("this is an exception thrown by the method 'saveMetadataDocument'");
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(mockFile));
            oneOf(mockFile).toURI(); will(returnValue(childOriginURI));
            
            oneOf(mockChildLinkWithHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithHandle, mockFile, mockCorpusNode);
                will(returnValue(true));
                
            oneOf(mockNodeResolver).getInputStream(mockCorpusNode); will(returnValue(mockInputStream));
            oneOf(mockFile).getName(); will(returnValue(childFilename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockInputStream, childFilename);
                will(returnValue(mockTypecheckedResults));
            oneOf(mockInputStream).close();
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(mockFile, mockChildLinkWithHandle, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeDataRetriever).isNodeToBeProtected(childURI); will(returnValue(childProtected));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceResourceNode(testWorkspace.getWorkspaceID(),
                    childURI, childArchiveURL, mockChildLinkWithHandle, childNodeMimetype, childNodeName, childOnSite, childProtected);
                will(returnValue(testChildNode));

            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);

            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
                will(returnValue(testNodeLink));
            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
            
            oneOf(mockChildLinkWithHandle).setLocation(null);
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockReferencingMetadataDocument, parentWsURL);
                will(throwException(expectedException));
        }});
        
        //TODO PID SHOULD BE COMING FROM THE CHILD LINK (HandleCarrier)
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String errorMessage = "Failed to save file " + parentWsURL + " in workspace " + testWorkspace.getWorkspaceID();
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void workspaceWasNotSet() throws MalformedURLException, WorkspaceImportException {
        
        final int parentWorkspaceNodeID = 1;
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        final URI parentURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000001");

        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, "cmdi");
        
        try {
            nodeImporter.importNode(null, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
            fail("Should have thrown exception");
        } catch(IllegalArgumentException ex) {
            String expectedErrorMessage = "ResourceNodeImporter.importNode: workspace not set";
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void nodeFileNull()
            throws MalformedURLException, TypeCheckerException, WorkspaceImportException {

        final int parentWorkspaceNodeID = 1;
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final URI childURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        
        final URI parentURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, "cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childURI); will(returnValue(mockCorpusNode));
            
            //TODO Maybe use the method getStream instead, so it can be passed directly to the typechecker?
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(null));
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
            fail("Should have thrown exception");
        } catch(IllegalArgumentException ex) {
            String expectedErrorMessage = "ResourceNodeImporter.importNode: error getting URL for link " + childURI;
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
        
    }
    
    @Test
    public void nodeIsNotFound()
            throws MalformedURLException, TypeCheckerException, WorkspaceImportException {

        final int parentWorkspaceNodeID = 1;
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final URI childURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        
        final URI parentURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, "cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childURI); will(returnValue(null));
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String expectedErrorMessage = "ResourceNodeImporter.importNode: error getting node " + childURI;
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Cause different from expected", null, ex.getCause());
        }
        
    }
    
    @Test
    public void typecheckerExceptionThrown()
            throws MalformedURLException, TypeCheckerException, WorkspaceImportException, IOException {

        final int parentWorkspaceNodeID = 1;
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final URI childURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final String childFilename = "childname.txt";
        final URI childOriginURI = URI.create("file:/some.uri/" + childFilename);

        final URI parentURI = URI.create("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, "cmdi");
        
        final TypecheckedResults typecheckedResults = new LamusTypecheckedResults(childNodeMimetype, "some analysis", TypecheckerJudgement.UNARCHIVABLE);
        final TypeCheckerException expectedException = new TypeCheckerException(typecheckedResults, "some exception message", null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(mockFile));
            oneOf(mockFile).toURI(); will(returnValue(childOriginURI));
            
            oneOf(mockChildLinkWithHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithHandle, mockFile, mockCorpusNode);
                will(returnValue(true));
            
            oneOf(mockNodeResolver).getInputStream(mockCorpusNode); will(returnValue(mockInputStream));
            oneOf(mockFile).getName(); will(returnValue(childFilename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockInputStream, childFilename);
                will(throwException(expectedException));
            oneOf(mockInputStream).close();
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String expectedErrorMessage = "ResourceNodeImporter.importNode: error during type checking";
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
        
    }
    
    
    //TODO MalformedURLException?
    
    //TODO IOException
    
    //TODO test if/else possible branches
}
