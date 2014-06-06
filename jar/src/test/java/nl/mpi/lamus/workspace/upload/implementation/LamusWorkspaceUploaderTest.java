/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ArchiveNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadHelper;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import nl.mpi.util.OurURL;
import org.apache.commons.fileupload.FileItem;
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
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author guisil
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({FileUtils.class})
public class LamusWorkspaceUploaderTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    
    @Mock NodeDataRetriever mockNodeDataRetriever;
    @Mock WorkspaceDirectoryHandler mockWorkspaceDirectoryHandler;
    @Mock WorkspaceNodeFactory mockWorkspaceNodeFactory;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock WorkspaceUploadHelper mockWorkspaceUploadHelper;
    @Mock MetadataApiBridge mockMetadataApiBridge;
    
    @Mock FileItem mockFileItem;
    @Mock InputStream mockInputStream;
    @Mock FileInputStream mockFileInputStream;
    @Mock File mockUploadedFile;
    @Mock File mockWorkspaceTopNodeFile;
    @Mock WorkspaceNode mockWorkspaceTopNode;
    @Mock TypecheckedResults mockTypecheckedResults;
    
    @Mock File mockFile1;
    @Mock File mockFile2;
    
    
    private WorkspaceUploader uploader;
    
    private File workspaceBaseDirectory = new File("/lamus/workspaces");
    private String workspaceUploadDirectoryName = "upload";
//    private File workspaceUploadDirectory = new File("/lamus/workspace/upload");
    
    public LamusWorkspaceUploaderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        uploader = new LamusWorkspaceUploader(mockNodeDataRetriever,
                mockWorkspaceDirectoryHandler, mockWorkspaceNodeFactory,
                mockWorkspaceDao, mockWorkspaceUploadHelper, mockMetadataApiBridge);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void getUploadDirectory() {
        final int workspaceID = 1;
        final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID);
                will(returnValue(workspaceUploadDirectory));
        }});
        
        File result = uploader.getWorkspaceUploadDirectory(workspaceID);
        
        assertEquals("Retrieved file different from expected", workspaceUploadDirectory, result);
    }
    
    @Test
    public void uploadFileIsArchivable()
            throws TypeCheckerException, URISyntaxException, MalformedURLException,
                IOException, WorkspaceNodeNotFoundException, WorkspaceException,
                ArchiveNodeNotFoundException {
        
        final int workspaceID = 1;
        final String filename = "someFile.cmdi";
        final TypecheckerJudgement acceptableJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final OurURL uploadedFileOurURL = new OurURL(uploadedFileURL);
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE;
        final String fileMimetype = "text/x-cmdi+xml";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.NODE_UPLOADED);
        uploadedNode.setType(fileType);
        uploadedNode.setFormat(fileMimetype);
        uploadedNode.setWorkspaceURL(uploadedFileURL);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
                
            oneOf(mockUploadedFile).toURI(); will(returnValue(uploadedFileURI));
            
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockInputStream, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
                
//            oneOf(mockTypecheckerConfiguration).getAcceptableJudgementForLocation(mockWorkspaceTopNodeFile);
//                will(returnValue(acceptableJudgement));
//            oneOf(mockFileTypeHandler).isCheckedResourceArchivable(with(same(acceptableJudgement)), with(any(StringBuilder.class)));
//                will(returnValue(Boolean.TRUE));
              
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
                
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype));
            
            oneOf(mockMetadataApiBridge).getSelfHandleFromFile(uploadedFileURL); will(returnValue(null));
                
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(
                    workspaceID, null, null, uploadedFileURL, fileMimetype, WorkspaceNodeStatus.NODE_UPLOADED);
                will(returnValue(uploadedNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode);

        }});
        
        stub(method(FileUtils.class, "getFile", File.class, String.class)).toReturn(mockUploadedFile);
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockWorkspaceTopNodeFile);
        suppress(method(FileUtils.class, "copyInputStreamToFile", InputStream.class, File.class));
        
        uploader.uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
    }
    
    @Test
    public void uploadFileIsNotArchivable()
            throws TypeCheckerException, URISyntaxException, MalformedURLException,
                IOException, WorkspaceNodeNotFoundException, WorkspaceException,
                ArchiveNodeNotFoundException {
        
        final int workspaceID = 1;
        final String filename = "someFile.cmdi";
        final TypecheckerJudgement acceptableJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
                
            oneOf(mockUploadedFile).toURI(); will(returnValue(uploadedFileURI));
            
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockInputStream, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
                
//            oneOf(mockTypecheckerConfiguration).getAcceptableJudgementForLocation(mockWorkspaceTopNodeFile);
//                will(returnValue(acceptableJudgement));
//            oneOf(mockFileTypeHandler).isCheckedResourceArchivable(with(same(acceptableJudgement)), with(any(StringBuilder.class)));
//                will(returnValue(Boolean.FALSE));
                
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.FALSE));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockWorkspaceTopNodeFile);
        
        try {
            uploader.uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
            fail("An exception should have been thrown");
        } catch(TypeCheckerException ex) {
            assertNotNull(ex);
        }
    }
    
    @Test
    public void uploadFileCopyFails()
            throws URISyntaxException, MalformedURLException, IOException,
                TypeCheckerException, WorkspaceNodeNotFoundException, WorkspaceException,
                ArchiveNodeNotFoundException {
        
        final int workspaceID = 1;
        final String filename = "someFile.cmdi";
        final TypecheckerJudgement acceptableJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final IOException ioException = new IOException("some error message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
                
            oneOf(mockUploadedFile).toURI(); will(returnValue(uploadedFileURI));
            
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockInputStream, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
                
//            oneOf(mockTypecheckerConfiguration).getAcceptableJudgementForLocation(mockWorkspaceTopNodeFile);
//                will(returnValue(acceptableJudgement));
//            oneOf(mockFileTypeHandler).isCheckedResourceArchivable(with(same(acceptableJudgement)), with(any(StringBuilder.class)));
//                will(returnValue(Boolean.TRUE));
                
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockWorkspaceTopNodeFile);
        stub(method(FileUtils.class, "copyInputStreamToFile", InputStream.class, File.class)).toThrow(ioException);
        
        try {
            uploader.uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
            fail("An exception should have been thrown");
        } catch(IOException ex) {
            assertEquals("Exception thrown different from expected", ioException, ex);
        }
    }
    
    @Test
    public void uploadFileUrlException()
            throws TypeCheckerException, URISyntaxException, MalformedURLException,
                IOException, WorkspaceNodeNotFoundException, WorkspaceException {
        
        final int workspaceID = 1;
        final String filename = "someFile.cmdi";
        final TypecheckerJudgement acceptableJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final String uploadedFilePath = uploadedFile.getPath();
        final URI uriWhichIsNotUrl = new URI("node:0");
        
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final OurURL uploadedFileOurURL = new OurURL(uploadedFileURL);
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE;
        final String fileMimetype = "text/plain";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.NODE_UPLOADED);
        uploadedNode.setType(fileType);
        uploadedNode.setFormat(fileMimetype);
        uploadedNode.setWorkspaceURL(uploadedFileURL);
        
        final String expectedErrorMessage = "Error retrieving URL from file " + uploadedFile.getPath();
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
                
            oneOf(mockUploadedFile).toURI(); will(returnValue(uriWhichIsNotUrl));
            
            oneOf(mockUploadedFile).getPath(); will(returnValue(uploadedFilePath));
        }});
        
        stub(method(FileUtils.class, "getFile", File.class, String.class)).toReturn(mockUploadedFile);

        try {
            uploader.uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertTrue("Cause has different type from expected", ex.getCause() instanceof MalformedURLException);
        }
    }
    
    @Test
    public void uploadFileUnknownNodeException()
            throws TypeCheckerException, URISyntaxException, MalformedURLException,
                IOException, WorkspaceNodeNotFoundException, WorkspaceException,
                ArchiveNodeNotFoundException {
        
        final int workspaceID = 1;
        final String filename = "someFile.cmdi";
        final TypecheckerJudgement acceptableJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final OurURL uploadedFileOurURL = new OurURL(uploadedFileURL);
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE;
        final String fileMimetype = "text/plain";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.NODE_UPLOADED);
        uploadedNode.setType(fileType);
        uploadedNode.setFormat(fileMimetype);
        uploadedNode.setWorkspaceURL(uploadedFileURL);
        
        final String expectedErrorMessage = "Error retrieving archive URL from the top node of workspace " + workspaceID;
        final ArchiveNodeNotFoundException expectedException = new ArchiveNodeNotFoundException("some exception message", workspaceTopNodeArchiveURI, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
                
            oneOf(mockUploadedFile).toURI(); will(returnValue(uploadedFileURI));
            
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockInputStream, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(throwException(expectedException));

        }});
        
        stub(method(FileUtils.class, "getFile", File.class, String.class)).toReturn(mockUploadedFile);
        
        try {
            uploader.uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void processOneUploadedResourceFile() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, ArchiveNodeNotFoundException {
        
        final int workspaceID = 1;
        final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        final String filename = "someFile.txt";
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE;
        final String fileMimetype = "text/plain";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.NODE_UPLOADED);
        uploadedNode.setType(fileType);
        uploadedNode.setFormat(fileMimetype);
        uploadedNode.setWorkspaceURL(uploadedFileURL);
        
        final Collection<File> uploadedFiles = new ArrayList<File>();
        uploadedFiles.add(mockFile1);
        
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<WorkspaceNode>();
        uploadedNodes.add(uploadedNode);
        
        //only one file in the collection, so only one loop cycle
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //loop

            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockFileInputStream, filename);
                will(returnValue(mockTypecheckedResults));
            oneOf(mockFileInputStream).close();
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            oneOf(mockFile1).getName(); will(returnValue(filename));
                
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(
                    workspaceID, null, null, uploadedFileURL, fileMimetype, WorkspaceNodeStatus.NODE_UPLOADED);
                will(returnValue(uploadedNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode);
            
            
            //check links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
        }});
        
        stub(method(FileUtils.class, "openInputStream", File.class)).toReturn(mockFileInputStream);
        suppress(method(FileUtils.class, "copyInputStreamToFile", InputStream.class, File.class));
        
        Map<File, String> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Map of file with a failed upload should not be null", result);
        assertTrue("Map of file with a failed upload should be empty", result.isEmpty());
    }
    
    @Test
    public void processOneUploadedMetadataFile() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, ArchiveNodeNotFoundException {
        
        final int workspaceID = 1;
        final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        final String filename = "someFile.cmdi";
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URI uploadedFileArchiveURI = new URI(UUID.randomUUID().toString());
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE;
        final String fileMimetype = "text/x-cmdi-xml";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.NODE_UPLOADED);
        uploadedNode.setType(fileType);
        uploadedNode.setFormat(fileMimetype);
        uploadedNode.setWorkspaceURL(uploadedFileURL);
        uploadedNode.setArchiveURI(uploadedFileArchiveURI);
        
        final Collection<File> uploadedFiles = new ArrayList<File>();
        uploadedFiles.add(mockFile1);
        
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<WorkspaceNode>();
        uploadedNodes.add(uploadedNode);
        
        //only one file in the collection, so only one loop cycle
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //loop

            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockFileInputStream, filename);
                will(returnValue(mockTypecheckedResults));
            oneOf(mockFileInputStream).close();
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype));
            
            oneOf(mockMetadataApiBridge).getSelfHandleFromFile(uploadedFileURL); will(returnValue(uploadedFileArchiveURI));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(
                    workspaceID, uploadedFileArchiveURI, null, uploadedFileURL, fileMimetype, WorkspaceNodeStatus.NODE_UPLOADED);
                will(returnValue(uploadedNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode);
            
            
            //check links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
        }});
        
        stub(method(FileUtils.class, "openInputStream", File.class)).toReturn(mockFileInputStream);
        suppress(method(FileUtils.class, "copyInputStreamToFile", InputStream.class, File.class));
        
        Map<File, String> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Map of file with a failed upload should not be null", result);
        assertTrue("Map of file with a failed upload should be empty", result.isEmpty());
    }
    
    @Test
    public void processTwoUploadedFiles() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, ArchiveNodeNotFoundException {
        
        final int workspaceID = 1;
        final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final String filename1 = "someFile.txt";
        final File uploadedFile1 = new File(workspaceUploadDirectory, filename1);
        final URI uploadedFileURI1 = uploadedFile1.toURI();
        final URL uploadedFileURL1 = uploadedFileURI1.toURL();
        final WorkspaceNodeType fileType1 = WorkspaceNodeType.RESOURCE;
        final String fileMimetype1 = "text/plain";
        
        final String filename2 = "someOtherFile.jpg";
        final File uploadedFile2 = new File(workspaceUploadDirectory, filename2);
        final URI uploadedFileURI2 = uploadedFile2.toURI();
        final URL uploadedFileURL2 = uploadedFileURI2.toURL();
        final WorkspaceNodeType fileType2 = WorkspaceNodeType.RESOURCE;
        final String fileMimetype2 = "image/jpeg";
        
        final WorkspaceNode uploadedNode1 = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode1.setName(filename1);
        uploadedNode1.setStatus(WorkspaceNodeStatus.NODE_UPLOADED);
        uploadedNode1.setType(fileType1);
        uploadedNode1.setFormat(fileMimetype1);
        uploadedNode1.setWorkspaceURL(uploadedFileURL1);
        
        final WorkspaceNode uploadedNode2 = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode2.setName(filename2);
        uploadedNode2.setStatus(WorkspaceNodeStatus.NODE_UPLOADED);
        uploadedNode2.setType(fileType2);
        uploadedNode2.setFormat(fileMimetype2);
        uploadedNode2.setWorkspaceURL(uploadedFileURL2);
        
        final Collection<File> uploadedFiles = new ArrayList<File>();
        uploadedFiles.add(mockFile1);
        uploadedFiles.add(mockFile2);
        
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<WorkspaceNode>();
        uploadedNodes.add(uploadedNode1);
        uploadedNodes.add(uploadedNode2);
        
        //only one file in the collection, so only one loop cycle
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //first loop cycle

            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI1));
            oneOf(mockFile1).getName(); will(returnValue(filename1));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockFileInputStream, filename1);
                will(returnValue(mockTypecheckedResults));
            oneOf(mockFileInputStream).close();
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            oneOf(mockFile1).getName(); will(returnValue(filename1));
                
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype1));
                
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(
                    workspaceID, null, null, uploadedFileURL1, fileMimetype1, WorkspaceNodeStatus.NODE_UPLOADED);
                will(returnValue(uploadedNode1));

            oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode1);
            
            //second loop cycle

            oneOf(mockFile2).toURI(); will(returnValue(uploadedFileURI2));
            oneOf(mockFile2).getName(); will(returnValue(filename2));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockFileInputStream, filename2);
                will(returnValue(mockTypecheckedResults));
            oneOf(mockFileInputStream).close();
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            oneOf(mockFile2).getName(); will(returnValue(filename2));
                
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype2));
                
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(
                    workspaceID, null, null, uploadedFileURL2, fileMimetype2, WorkspaceNodeStatus.NODE_UPLOADED);
                will(returnValue(uploadedNode2));

            oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode2);
            
            
            //check links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
        }});
        
        stub(method(FileUtils.class, "openInputStream", File.class)).toReturn(mockFileInputStream);
        suppress(method(FileUtils.class, "copyInputStreamToFile", InputStream.class, File.class));
        
        Map<File, String> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Map of file with a failed upload should not be null", result);
        assertTrue("Map of file with a failed upload should be empty", result.isEmpty());
    }
    
    @Test
    public void processUploadedFileWorkspaceException() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, ArchiveNodeNotFoundException {
        
        final int workspaceID = 1;
        final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        final String filename = "someFile.txt";
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE;
        final String fileMimetype = "text/plain";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.NODE_UPLOADED);
        uploadedNode.setType(fileType);
        uploadedNode.setFormat(fileMimetype);
        uploadedNode.setWorkspaceURL(uploadedFileURL);
        
        Collection<File> uploadedFiles = new ArrayList<File>();
        uploadedFiles.add(mockFile1);
        
        final String expectedErrorMessage = "Error retrieving archive URL from the top node of workspace " + workspaceID;
        final ArchiveNodeNotFoundException expectedException = new ArchiveNodeNotFoundException("some exception message", workspaceTopNodeArchiveURI, null);
        
        //only one file in the collection, so only one loop cycle
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(throwException(expectedException));
        }});
        
        stub(method(FileUtils.class, "openInputStream", File.class)).toReturn(mockFileInputStream);
        suppress(method(FileUtils.class, "copyInputStreamToFile", InputStream.class, File.class));
        
        try {
            uploader.processUploadedFiles(workspaceID, uploadedFiles);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void processUploadedFileUrlException() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, ArchiveNodeNotFoundException {
        
        final int workspaceID = 1;
        final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        final String filename = "someFile.txt";
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE;
        final String fileMimetype = "text/plain";
        final String uploadedFilePath = uploadedFile.getPath();
        final URI uriWhichIsNotUrl = new URI("node:0");
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.NODE_UPLOADED);
        uploadedNode.setType(fileType);
        uploadedNode.setFormat(fileMimetype);
        
        final Collection<File> uploadedFiles = new ArrayList<File>();
        uploadedFiles.add(mockFile1);
        
        //no successful uploads
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<WorkspaceNode>();
        
        final String expectedErrorMessage = "Error retrieving URL from file " + uploadedFile.getPath();
        
        //only one file in the collection, so only one loop cycle
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //loop

            oneOf(mockFile1).toURI(); will(returnValue(uriWhichIsNotUrl));
            
            oneOf(mockFile1).getPath(); will(returnValue(uploadedFilePath));
            
            
            //still calls method to process links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
        }});
        
        stub(method(FileUtils.class, "openInputStream", File.class)).toReturn(mockFileInputStream);
        suppress(method(FileUtils.class, "copyInputStreamToFile", InputStream.class, File.class));
        
        Map<File, String> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Map of file with a failed upload should not be null", result);
        assertTrue("Map of file with a failed upload should be empty", result.size() == 1);
        assertTrue("File added to the map of failed files is different from expected", result.containsKey(mockFile1));
        assertEquals("Reason for failure of file upload is different from expected", expectedErrorMessage, result.get(mockFile1));
    }
    
    @Test
    public void processUploadedFileUnarchivable() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, ArchiveNodeNotFoundException {
        
        final int workspaceID = 1;
        final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        final String filename = "someFile.txt";
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE;
        final String fileMimetype = "text/plain";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.NODE_UPLOADED);
        uploadedNode.setType(fileType);
        uploadedNode.setFormat(fileMimetype);
        uploadedNode.setWorkspaceURL(uploadedFileURL);
        
        final Collection<File> uploadedFiles = new ArrayList<File>();
        uploadedFiles.add(mockFile1);
        
        //no successful uploads
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<WorkspaceNode>();
        
        String partExpectedErrorMessage = "File [" + filename + "] not archivable: ";
        
        //only one file in the collection, so only one loop cycle
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //loop

            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockFileInputStream, filename);
                will(returnValue(mockTypecheckedResults));
            oneOf(mockFileInputStream).close();
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.FALSE));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            
            
            //still calls method to process links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
        }});
        
        stub(method(FileUtils.class, "openInputStream", File.class)).toReturn(mockFileInputStream);
        suppress(method(FileUtils.class, "copyInputStreamToFile", InputStream.class, File.class));
        suppress(method(FileUtils.class, "forceDelete", File.class));
        
        Map<File, String> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Map of file with a failed upload should not be null", result);
        assertTrue("Map of file with a failed upload should be empty", result.size() == 1);
        assertTrue("File added to the map of failed files is different from expected", result.containsKey(mockFile1));
        assertTrue("Reason for failure of file upload is different from expected", result.get(mockFile1).contains(partExpectedErrorMessage));
    }
}