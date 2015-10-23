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
import java.util.UUID;
import javax.xml.transform.TransformerException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.handle.util.implementation.HandleConstants;
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
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class ResourceNodeImporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
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
    @Mock NodeUtil mockNodeUtil;
    @Mock HandleParser mockHandleParser;
    
    private Workspace testWorkspace;
    
    @Mock ReferencingMetadataDocument mockReferencingMetadataDocument;
    @Mock Reference mockChildLinkWithoutHandle;
    @Mock ResourceProxy mockChildLinkWithHandle;
    
    @Mock TypecheckedResults mockTypecheckedResults;
    @Mock CorpusNode mockCorpusNode;
    @Mock File mockFile;
    @Mock InputStream mockInputStream;
    
    private final int workspaceID = 1;
    
    private final String handlePrefixWithSlash = "11142/";
    private final String handleProxyPlusPrefixWithSlash = HandleConstants.HDL_SHORT_PROXY + ":" + handlePrefixWithSlash;
    
    
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
        ReflectionTestUtils.setField(nodeImporter, "nodeUtil", mockNodeUtil);
        ReflectionTestUtils.setField(nodeImporter, "handleParser", mockHandleParser);
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
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final String childRawHandle = UUID.randomUUID().toString();
        final URI childURI = URI.create(handlePrefixWithSlash + childRawHandle);
        final URI completeChildURI = URI.create(handleProxyPlusPrefixWithSlash + childRawHandle);
        final String childFilename = "childname.txt";
        final URL childWsURL = new URL("file:/workspace/folder/" + childFilename);
        final URI childOriginURI = URI.create("file:/some.uri/" + childFilename);
        final URL childArchiveURL = childOriginURI.toURL();

        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.VIRTUAL;
        final boolean childOnSite = Boolean.TRUE;
        final boolean childProtected = Boolean.FALSE;
        
        final URI parentURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.ARCHIVE_COPY;
        final boolean parentProtected = Boolean.FALSE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, parentStatus, parentProtected, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childURI, childArchiveURL, childOriginURI, childStatus, childProtected, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(childURI); will(returnValue(completeChildURI));
            oneOf(mockChildLinkWithHandle).setHandle(completeChildURI);
            
            oneOf(mockCorpusStructureProvider).getNode(completeChildURI); will(returnValue(mockCorpusNode));
            
            allowing(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(mockFile));
            oneOf(mockFile).toURI(); will(returnValue(childOriginURI));
            
            oneOf(mockChildLinkWithHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithHandle, mockFile, mockCorpusNode);
                will(returnValue(Boolean.TRUE));

            oneOf(mockFile).getName(); will(returnValue(childFilename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(childArchiveURL, childFilename);
                will(returnValue(mockTypecheckedResults));
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(mockFile, mockChildLinkWithHandle, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockNodeUtil).convertMimetype(childNodeMimetype); will(returnValue(childNodeType));
            oneOf(mockNodeDataRetriever).isNodeToBeProtected(completeChildURI); will(returnValue(childProtected));
            
            oneOf(mockMetadataApiBridge).isReferenceAnInfoLink(mockReferencingMetadataDocument, mockChildLinkWithHandle); will(returnValue(Boolean.FALSE));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNode(
                    testWorkspace.getWorkspaceID(), completeChildURI, childArchiveURL,
                    mockChildLinkWithHandle, childNodeMimetype, childNodeType,
                    childNodeName, childOnSite, childProtected);
                will(returnValue(testChildNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceDao).lockNode(completeChildURI, testWorkspace.getWorkspaceID());

            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
                will(returnValue(testNodeLink));
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
            
            oneOf(mockChildLinkWithHandle).setLocation(null);
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockReferencingMetadataDocument, parentWsURL);
        }});
        
        nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
    }
    
    @Test
    public void importResourceNodeWithHandle_InfoLink()
            throws MalformedURLException, TypeCheckerException, WorkspaceImportException, IOException, TransformerException, MetadataException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final WorkspaceNodeType childInfoNodeType = WorkspaceNodeType.RESOURCE_INFO;
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final String childRawHandle = UUID.randomUUID().toString();
        final URI childURI = URI.create(handlePrefixWithSlash + childRawHandle);
        final URI completeChildURI = URI.create(handleProxyPlusPrefixWithSlash + childRawHandle);
        final String childFilename = "childname.txt";
        final URL childWsURL = new URL("file:/workspace/folder/" + childFilename);
        final URI childOriginURI = URI.create("file:/some.uri/" + childFilename);
        final URL childArchiveURL = childOriginURI.toURL();

        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.VIRTUAL;
        final boolean childOnSite = Boolean.TRUE;
        final boolean childProtected = Boolean.FALSE;
        
        final URI parentURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.ARCHIVE_COPY;
        final boolean parentProtected = Boolean.FALSE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, parentStatus, parentProtected, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childInfoNodeType, childWsURL, childURI, childArchiveURL, childOriginURI, childStatus, childProtected, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(childURI); will(returnValue(completeChildURI));
            oneOf(mockChildLinkWithHandle).setHandle(completeChildURI);
            
            oneOf(mockCorpusStructureProvider).getNode(completeChildURI); will(returnValue(mockCorpusNode));
            
            allowing(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(mockFile));
            oneOf(mockFile).toURI(); will(returnValue(childOriginURI));
            
            oneOf(mockChildLinkWithHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithHandle, mockFile, mockCorpusNode);
                will(returnValue(Boolean.TRUE));

            oneOf(mockFile).getName(); will(returnValue(childFilename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(childArchiveURL, childFilename);
                will(returnValue(mockTypecheckedResults));
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(mockFile, mockChildLinkWithHandle, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockNodeUtil).convertMimetype(childNodeMimetype); will(returnValue(childNodeType));
            oneOf(mockNodeDataRetriever).isNodeToBeProtected(completeChildURI); will(returnValue(childProtected));
            
            oneOf(mockMetadataApiBridge).isReferenceAnInfoLink(mockReferencingMetadataDocument, mockChildLinkWithHandle); will(returnValue(Boolean.TRUE));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNode(
                    testWorkspace.getWorkspaceID(), completeChildURI, childArchiveURL,
                    mockChildLinkWithHandle, childNodeMimetype, childInfoNodeType,
                    childNodeName, childOnSite, childProtected);
                will(returnValue(testChildNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceDao).lockNode(completeChildURI, testWorkspace.getWorkspaceID());

            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
                will(returnValue(testNodeLink));
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
            
            oneOf(mockChildLinkWithHandle).setLocation(null);
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockReferencingMetadataDocument, parentWsURL);
        }});
        
        nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
    }
    
    @Test
    public void importResourceNodeWithoutHandle()
            throws MalformedURLException, TypeCheckerException, WorkspaceImportException, IOException, TransformerException, MetadataException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final String childFilename = "childname.txt";
        final URL childWsURL = new URL("file:/workspace/folder/" + childFilename);
        final URI childOriginURI = URI.create("file:/some.uri/" + childFilename);
        final URL childArchiveURL = childOriginURI.toURL();

        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.VIRTUAL;
        final boolean childOnSite = Boolean.TRUE;
        final boolean childProtected = Boolean.FALSE;
        
        final URI parentURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.ARCHIVE_COPY;
        final boolean parentProtected = Boolean.FALSE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, parentStatus, parentProtected, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childOriginURI, childArchiveURL, childOriginURI, childStatus, childProtected, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithoutHandle).getURI(); will(returnValue(childOriginURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childOriginURI); will(returnValue(mockCorpusNode));
            
            allowing(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(mockFile));
            oneOf(mockFile).toURI(); will(returnValue(childOriginURI));
            
            oneOf(mockChildLinkWithoutHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithoutHandle, mockFile, mockCorpusNode);
                will(returnValue(Boolean.TRUE));
                
            oneOf(mockFile).getName(); will(returnValue(childFilename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(childArchiveURL, childFilename);
                will(returnValue(mockTypecheckedResults));
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(mockFile, mockChildLinkWithoutHandle, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockNodeUtil).convertMimetype(childNodeMimetype); will(returnValue(childNodeType));
            oneOf(mockNodeDataRetriever).isNodeToBeProtected(childOriginURI); will(returnValue(childProtected));
            
            oneOf(mockMetadataApiBridge).isReferenceAnInfoLink(mockReferencingMetadataDocument, mockChildLinkWithoutHandle); will(returnValue(Boolean.FALSE));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNode(
                    testWorkspace.getWorkspaceID(), childOriginURI, childArchiveURL,
                    mockChildLinkWithoutHandle, childNodeMimetype, childNodeType,
                    childNodeName, childOnSite, childProtected);
                will(returnValue(testChildNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceDao).lockNode(childOriginURI, testWorkspace.getWorkspaceID());

            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
                will(returnValue(testNodeLink));
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
            
            oneOf(mockChildLinkWithoutHandle).setLocation(null);
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockReferencingMetadataDocument, parentWsURL);
        }});
        
        nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithoutHandle);
    }
    
    @Test
    public void importResourceNode_NotOnSite()
            throws MalformedURLException, TypeCheckerException, WorkspaceImportException, IOException, TransformerException, MetadataException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = URI.create("http://some.location");
        final String childFilename = "childname.txt";
        final URL childWsURL = null;
        final URI childOriginURI = URI.create("http://some.uri/" + childFilename);
        final URL childArchiveURL = childOriginURI.toURL();

        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.VIRTUAL;
        final boolean childOnSite = Boolean.FALSE;
        final boolean childProtected = Boolean.FALSE;
        
        final URI parentURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.ARCHIVE_COPY;
        final boolean parentProtected = Boolean.FALSE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, parentStatus, parentProtected, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(childWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                childNodeName, "", childNodeType, childWsURL, childOriginURI, childArchiveURL, childOriginURI, childStatus, childProtected, childNodeMimetype);
        final WorkspaceNodeLink testNodeLink = new LamusWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithoutHandle).getURI(); will(returnValue(childOriginURI));
            
            oneOf(mockCorpusStructureProvider).getNode(childOriginURI); will(returnValue(mockCorpusNode));
            
            allowing(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(childArchiveURL));
            
            oneOf(mockChildLinkWithoutHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithoutHandle, null, mockCorpusNode);
                will(returnValue(Boolean.FALSE));
            
            oneOf(mockNodeUtil).convertMimetype(childNodeMimetype); will(returnValue(childNodeType));
            oneOf(mockNodeDataRetriever).isNodeToBeProtected(childOriginURI); will(returnValue(childProtected));
            
            oneOf(mockMetadataApiBridge).isReferenceAnInfoLink(mockReferencingMetadataDocument, mockChildLinkWithoutHandle); will(returnValue(Boolean.FALSE));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNode(
                    testWorkspace.getWorkspaceID(), childOriginURI, childArchiveURL,
                    mockChildLinkWithoutHandle, childNodeMimetype, childNodeType,
                    childNodeName, childOnSite, childProtected);
                will(returnValue(testChildNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceDao).lockNode(childOriginURI, testWorkspace.getWorkspaceID());

            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
                will(returnValue(testNodeLink));
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
            
            oneOf(mockChildLinkWithoutHandle).setLocation(null);
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockReferencingMetadataDocument, parentWsURL);
        }});
        
        nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithoutHandle);
    }
    
    @Test
    public void importResourceNodeThrowsIOException()
            throws MalformedURLException, TypeCheckerException, WorkspaceImportException, IOException, TransformerException, MetadataException {

        final int parentWorkspaceNodeID = 1;
        final int childWorkspaceNodeID = 10;
        final String childNodeName = "file name label";
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final String childRawHandle = UUID.randomUUID().toString();
        final URI childURI = URI.create(handlePrefixWithSlash + childRawHandle);
        final URI completeChildURI = URI.create(handleProxyPlusPrefixWithSlash + childRawHandle);
        final String childFilename = "childname.txt";
        final URL childWsURL = new URL("file:/workspace/folder/" + childFilename);
        final URI childOriginURI = URI.create("file:/some.uri/" + childFilename);
        final URL childArchiveURL = childOriginURI.toURL();

        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.VIRTUAL;
        final boolean childOnSite = Boolean.TRUE;
        final boolean childProtected = Boolean.FALSE;
        
        final URI parentURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.ARCHIVE_COPY;
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
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(childURI); will(returnValue(completeChildURI));
            oneOf(mockChildLinkWithHandle).setHandle(completeChildURI);
            
            oneOf(mockCorpusStructureProvider).getNode(completeChildURI); will(returnValue(mockCorpusNode));
            
            allowing(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(mockFile));
            oneOf(mockFile).toURI(); will(returnValue(childOriginURI));
            
            oneOf(mockChildLinkWithHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithHandle, mockFile, mockCorpusNode);
                will(returnValue(Boolean.TRUE));
                
            oneOf(mockFile).getName(); will(returnValue(childFilename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(childArchiveURL, childFilename);
                will(returnValue(mockTypecheckedResults));
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(mockFile, mockChildLinkWithHandle, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockNodeUtil).convertMimetype(childNodeMimetype); will(returnValue(childNodeType));
            oneOf(mockNodeDataRetriever).isNodeToBeProtected(completeChildURI); will(returnValue(childProtected));
            
            oneOf(mockMetadataApiBridge).isReferenceAnInfoLink(mockReferencingMetadataDocument, mockChildLinkWithHandle); will(returnValue(Boolean.FALSE));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNode(
                    testWorkspace.getWorkspaceID(), completeChildURI, childArchiveURL,
                    mockChildLinkWithHandle, childNodeMimetype, childNodeType,
                    childNodeName, childOnSite, childProtected);
                will(returnValue(testChildNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceDao).lockNode(completeChildURI, testWorkspace.getWorkspaceID());

            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
                will(returnValue(testNodeLink));
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
            
            oneOf(mockChildLinkWithHandle).setLocation(null);
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockReferencingMetadataDocument, parentWsURL);
                will(throwException(expectedException));
        }});
        
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
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final String childRawHandle = UUID.randomUUID().toString();
        final URI childURI = URI.create(handlePrefixWithSlash + childRawHandle);
        final URI completeChildURI = URI.create(handleProxyPlusPrefixWithSlash + childRawHandle);
        final String childFilename = "childname.txt";
        final URL childWsURL = new URL("file:/workspace/folder/" + childFilename);
        final URI childOriginURI = URI.create("file:/some.uri/" + childFilename);
        final URL childArchiveURL = childOriginURI.toURL();

        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.VIRTUAL;
        final boolean childOnSite = Boolean.TRUE;
        final boolean childProtected = Boolean.FALSE;
        
        final URI parentURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.ARCHIVE_COPY;
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
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(childURI); will(returnValue(completeChildURI));
            oneOf(mockChildLinkWithHandle).setHandle(completeChildURI);
            
            oneOf(mockCorpusStructureProvider).getNode(completeChildURI); will(returnValue(mockCorpusNode));
            
            allowing(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(mockFile));
            oneOf(mockFile).toURI(); will(returnValue(childOriginURI));
            
            oneOf(mockChildLinkWithHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithHandle, mockFile, mockCorpusNode);
                will(returnValue(Boolean.TRUE));
                
            oneOf(mockFile).getName(); will(returnValue(childFilename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(childArchiveURL, childFilename);
                will(returnValue(mockTypecheckedResults));
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(mockFile, mockChildLinkWithHandle, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockNodeUtil).convertMimetype(childNodeMimetype); will(returnValue(childNodeType));
            oneOf(mockNodeDataRetriever).isNodeToBeProtected(completeChildURI); will(returnValue(childProtected));
            
            oneOf(mockMetadataApiBridge).isReferenceAnInfoLink(mockReferencingMetadataDocument, mockChildLinkWithHandle); will(returnValue(Boolean.FALSE));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNode(
                    testWorkspace.getWorkspaceID(), completeChildURI, childArchiveURL,
                    mockChildLinkWithHandle, childNodeMimetype, childNodeType,
                    childNodeName, childOnSite, childProtected);
                will(returnValue(testChildNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceDao).lockNode(completeChildURI, testWorkspace.getWorkspaceID());

            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
                will(returnValue(testNodeLink));
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
            
            oneOf(mockChildLinkWithHandle).setLocation(null);
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockReferencingMetadataDocument, parentWsURL);
                will(throwException(expectedException));
        }});
        
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
        final WorkspaceNodeType childNodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String childNodeMimetype = "text/plain";
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final String childRawHandle = UUID.randomUUID().toString();
        final URI childURI = URI.create(handlePrefixWithSlash + childRawHandle);
        final URI completeChildURI = URI.create(handleProxyPlusPrefixWithSlash + childRawHandle);
        final String childFilename = "childname.txt";
        final URL childWsURL = new URL("file:/workspace/folder/" + childFilename);
        final URI childOriginURI = URI.create("file:/some.uri/" + childFilename);
        final URL childArchiveURL = childOriginURI.toURL();

        final WorkspaceNodeStatus childStatus = WorkspaceNodeStatus.VIRTUAL;
        final boolean childOnSite = Boolean.TRUE;
        final boolean childProtected = Boolean.FALSE;
        
        final URI parentURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.ARCHIVE_COPY;
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
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(childURI); will(returnValue(completeChildURI));
            oneOf(mockChildLinkWithHandle).setHandle(completeChildURI);
            
            oneOf(mockCorpusStructureProvider).getNode(completeChildURI); will(returnValue(mockCorpusNode));
            
            allowing(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(mockFile));
            oneOf(mockFile).toURI(); will(returnValue(childOriginURI));
            
            oneOf(mockChildLinkWithHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithHandle, mockFile, mockCorpusNode);
                will(returnValue(Boolean.TRUE));
                
            oneOf(mockFile).getName(); will(returnValue(childFilename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(childArchiveURL, childFilename);
                will(returnValue(mockTypecheckedResults));
                
            oneOf(mockNodeDataRetriever).verifyTypecheckedResults(mockFile, mockChildLinkWithHandle, mockTypecheckedResults);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));
            
            oneOf(mockNodeUtil).convertMimetype(childNodeMimetype); will(returnValue(childNodeType));
            oneOf(mockNodeDataRetriever).isNodeToBeProtected(completeChildURI); will(returnValue(childProtected));
            
            oneOf(mockMetadataApiBridge).isReferenceAnInfoLink(mockReferencingMetadataDocument, mockChildLinkWithHandle); will(returnValue(Boolean.FALSE));
            
            oneOf(mockCorpusNode).getName(); will(returnValue(childNodeName));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNode(
                    testWorkspace.getWorkspaceID(), completeChildURI, childArchiveURL,
                    mockChildLinkWithHandle, childNodeMimetype, childNodeType,
                    childNodeName, childOnSite, childProtected);
                will(returnValue(testChildNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceDao).lockNode(completeChildURI, testWorkspace.getWorkspaceID());

            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, childWorkspaceNodeID);
                will(returnValue(testNodeLink));
            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
            
            oneOf(mockChildLinkWithHandle).setLocation(null);
            oneOf(mockMetadataApiBridge).saveMetadataDocument(mockReferencingMetadataDocument, parentWsURL);
                will(throwException(expectedException));
        }});
        
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
        final URI parentURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());

        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE, "cmdi");
        
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
        final String childRawHandle = UUID.randomUUID().toString();
        final URI childURI = URI.create(handlePrefixWithSlash + childRawHandle);
        final URI completeChildURI = URI.create(handleProxyPlusPrefixWithSlash + childRawHandle);
        final boolean childOnSite = Boolean.TRUE;
        
        final URI parentURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE, "cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(childURI); will(returnValue(completeChildURI));
            oneOf(mockChildLinkWithHandle).setHandle(completeChildURI);
            
            oneOf(mockCorpusStructureProvider).getNode(completeChildURI); will(returnValue(mockCorpusNode));
            
            allowing(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(null));
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
            fail("Should have thrown exception");
        } catch(IllegalArgumentException ex) {
            String expectedErrorMessage = "ResourceNodeImporter.importNode: error getting URL for link " + completeChildURI;
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
        
    }
    
    @Test
    public void nodeIsNotFound()
            throws MalformedURLException, TypeCheckerException, WorkspaceImportException {

        final int parentWorkspaceNodeID = 1;
        final URI childNodeSchemaLocation = URI.create("file:/some.location");
        final String childRawHandle = UUID.randomUUID().toString();
        final URI childURI = URI.create(handlePrefixWithSlash + childRawHandle);
        final URI completeChildURI = URI.create(handleProxyPlusPrefixWithSlash + childRawHandle);
        
        final URI parentURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE, "cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(childURI); will(returnValue(completeChildURI));
            oneOf(mockChildLinkWithHandle).setHandle(completeChildURI);
            
            oneOf(mockCorpusStructureProvider).getNode(completeChildURI); will(returnValue(null));
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockChildLinkWithHandle);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String expectedErrorMessage = "ResourceNodeImporter.importNode: error getting node " + completeChildURI;
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
        final String childRawHandle = UUID.randomUUID().toString();
        final URI childURI = URI.create(handlePrefixWithSlash + childRawHandle);
        final URI completeChildURI = URI.create(handleProxyPlusPrefixWithSlash + childRawHandle);
        final String childFilename = "childname.txt";
        final URI childOriginURI = URI.create("file:/some.uri/" + childFilename);
        final URL childArchiveURL = childOriginURI.toURL();
        final boolean childOnSite = Boolean.TRUE;

        final URI parentURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URI parentOriginURI = URI.create("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURI.toURL();
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), childNodeSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURI, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE, "cmdi");
        
        final TypecheckedResults typecheckedResults = new LamusTypecheckedResults(childNodeMimetype, "some analysis", TypecheckerJudgement.UNARCHIVABLE);
        final TypeCheckerException expectedException = new TypeCheckerException(typecheckedResults, "some exception message", null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildLinkWithHandle).getHandle(); will(returnValue(childURI));
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(childURI); will(returnValue(completeChildURI));
            oneOf(mockChildLinkWithHandle).setHandle(completeChildURI);
            
            oneOf(mockCorpusStructureProvider).getNode(completeChildURI); will(returnValue(mockCorpusNode));
            
            allowing(mockCorpusNode).isOnSite(); will(returnValue(childOnSite));
            
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(mockFile));
            oneOf(mockFile).toURI(); will(returnValue(childOriginURI));
            
            oneOf(mockChildLinkWithHandle).getMimetype(); will(returnValue(childNodeMimetype));
            oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLinkWithHandle, mockFile, mockCorpusNode);
                will(returnValue(Boolean.TRUE));
            
            oneOf(mockFile).getName(); will(returnValue(childFilename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(childArchiveURL, childFilename);
                will(throwException(expectedException));
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
}
