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

import nl.mpi.lamus.workspace.importing.implementation.FileImportProblem;
import nl.mpi.lamus.workspace.importing.implementation.ImportProblem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.typechecking.MetadataChecker;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadHelper;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import org.apache.commons.fileupload.FileItem;
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
import static org.hamcrest.Matchers.*;
import org.jmock.lib.concurrent.Synchroniser;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceUploaderTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    
    @Mock NodeDataRetriever mockNodeDataRetriever;
    @Mock WorkspaceDirectoryHandler mockWorkspaceDirectoryHandler;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock WorkspaceNodeFactory mockWorkspaceNodeFactory;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock WorkspaceUploadHelper mockWorkspaceUploadHelper;
    @Mock MetadataApiBridge mockMetadataApiBridge;
    @Mock MetadataChecker mockMetadataChecker;
    @Mock ArchiveFileLocationProvider mockArchiveFileLocationProvider;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    
    @Mock FileItem mockFileItem;
    @Mock InputStream mockInputStream;
    @Mock FileInputStream mockFileInputStream;
    @Mock File mockUploadedFile;
    @Mock File mockWorkspaceTopNodeFile;
    @Mock WorkspaceNode mockWorkspaceTopNode;
    @Mock TypecheckedResults mockTypecheckedResults;
    
    @Mock ZipInputStream mockZipInputStream;
    @Mock ZipEntry mockFirstZipEntry;
    @Mock ZipEntry mockSecondZipEntry;
    
    @Mock File mockFile1;
    @Mock File mockFile2;
    
    @Mock ImportProblem mockUploadProblem;
    
    private WorkspaceUploader uploader;
    
    private File workspaceBaseDirectory = new File("/lamus/workspaces");
    private String workspaceUploadDirectoryName = "upload";
//    private File workspaceUploadDirectory = new File("/lamus/workspace/upload");
    
    private final int workspaceID = 1;
    private final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
    private final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
    
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
                mockWorkspaceDirectoryHandler, mockWorkspaceFileHandler,
                mockWorkspaceNodeFactory,
                mockWorkspaceDao, mockWorkspaceUploadHelper,
                mockMetadataApiBridge, mockMetadataChecker,
                mockArchiveFileLocationProvider, mockArchiveFileHelper);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void getUploadDirectory() {
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID);
                will(returnValue(workspaceUploadDirectory));
        }});
        
        File result = uploader.getWorkspaceUploadDirectory(workspaceID);
        
        assertEquals("Retrieved file different from expected", workspaceUploadDirectory, result);
    }
    
    @Test
    public void uploadFile_noNameChange() throws IOException {
        
        final String filename = "file.cmdi";
        final File expectedFile = new File(workspaceUploadDirectory, filename);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockArchiveFileHelper).getFinalFile(workspaceUploadDirectory, filename); will(returnValue(expectedFile));
            oneOf(mockWorkspaceFileHandler).copyInputStreamToTargetFile(mockInputStream, expectedFile);
        }});
        
        File resultingFile = uploader.uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
        
        assertEquals("Resulting file different from expected", expectedFile, resultingFile);
    }
    
    @Test
    public void uploadFile_nameChanged() throws IOException {
        
        final String filename = "file.cmdi";
        final File file = new File(workspaceUploadDirectory, filename);
        final String changedFilename = "file_01.cmdi";
        final File expectedFile = new File(workspaceUploadDirectory, changedFilename);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockArchiveFileHelper).getFinalFile(workspaceUploadDirectory, filename); will(returnValue(expectedFile));
            oneOf(mockWorkspaceFileHandler).copyInputStreamToTargetFile(mockInputStream, expectedFile);
        }});
        
        File resultingFile = uploader.uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
        
        assertEquals("Resulting file different from expected", expectedFile, resultingFile);
    }
    
    @Test
    public void uploadFile_ThrowsException() throws IOException {
        
        final String filename = "file.cmdi";
        final File file = new File(workspaceUploadDirectory, filename);
        final String changedFilename = "file_01.cmdi";
        final File expectedFile = new File(workspaceUploadDirectory, changedFilename);
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockArchiveFileHelper).getFinalFile(workspaceUploadDirectory, filename); will(returnValue(expectedFile));
            oneOf(mockWorkspaceFileHandler).copyInputStreamToTargetFile(mockInputStream, expectedFile); will(throwException(expectedException));
        }});
        
        try {
            uploader.uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
            fail("should have thrown exception");
        } catch(IOException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void uploadZipFile_IsDirectory() throws IOException {
        
        final String firstEntryName = "directory";
        
        final Collection<File> expectedCopiedFiles = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockFirstZipEntry));
            oneOf(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
            oneOf(mockFirstZipEntry).isDirectory(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
            oneOf(mockWorkspaceDirectoryHandler).createDirectoryInWorkspace(workspaceID, firstEntryName);
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(null));
        }});
        
        Collection<File> result = uploader.uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
        
        assertEquals("Result different from expected", expectedCopiedFiles, result);
    }
    
    @Test
    public void uploadZipFile_IsNotDirectory() throws IOException {
        
        final String firstEntryName = "file.cmdi";
        final File firstEntryFile = new File(workspaceUploadDirectory, firstEntryName);
        
        final Collection<File> expectedCopiedFiles = new ArrayList<>();
        expectedCopiedFiles.add(firstEntryFile);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockFirstZipEntry));
            oneOf(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
            oneOf(mockFirstZipEntry).isDirectory(); will(returnValue(Boolean.FALSE));
            oneOf(mockArchiveFileHelper).getFinalFile(workspaceUploadDirectory, firstEntryName); will(returnValue(firstEntryFile));
            oneOf(mockWorkspaceFileHandler).copyInputStreamToTargetFile(mockZipInputStream, firstEntryFile);
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(null));
        }});
        
        Collection<File> result = uploader.uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
        
        assertEquals("Result different from expected", expectedCopiedFiles, result);
    }
    
    @Test
    public void uploadZipFile_IsNotDirectory_fileNeedsRenaming() throws IOException {
        
        final String firstEntryName = "file.cmdi";
        final String changedFirstEntryName = "file_01.cmdi";
        final File changedFirstEntryFile = new File(workspaceUploadDirectory, changedFirstEntryName);
        
        final Collection<File> expectedCopiedFiles = new ArrayList<>();
        expectedCopiedFiles.add(changedFirstEntryFile);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockFirstZipEntry));
            oneOf(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
            oneOf(mockFirstZipEntry).isDirectory(); will(returnValue(Boolean.FALSE));
            oneOf(mockArchiveFileHelper).getFinalFile(workspaceUploadDirectory, firstEntryName); will(returnValue(changedFirstEntryFile));
            oneOf(mockWorkspaceFileHandler).copyInputStreamToTargetFile(mockZipInputStream, changedFirstEntryFile);
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(null));
        }});
        
        Collection<File> result = uploader.uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
        
        assertEquals("Result different from expected", expectedCopiedFiles, result);
    }
    
    @Test
    public void uploadZipFile_DirectoryAndFile() throws IOException {
        
        final String firstEntryName = "directory/";
        final File createdDirectory = new File(workspaceUploadDirectory, firstEntryName);
        final String secondEntryName = "directory/file.cmdi";
        final File createdFile = new File(workspaceUploadDirectory, secondEntryName);
        
        final Collection<File> expectedCopiedFiles = new ArrayList<>();
        expectedCopiedFiles.add(createdFile);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockFirstZipEntry));
            oneOf(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
            oneOf(mockFirstZipEntry).isDirectory(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
            oneOf(mockWorkspaceDirectoryHandler).createDirectoryInWorkspace(workspaceID, firstEntryName); will(returnValue(createdDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockSecondZipEntry));

            // second loop iteration
            
            oneOf(mockSecondZipEntry).getName(); will(returnValue(secondEntryName));
            oneOf(mockSecondZipEntry).isDirectory(); will(returnValue(Boolean.FALSE));
            oneOf(mockArchiveFileHelper).getFinalFile(workspaceUploadDirectory, createdFile.getName()); will(returnValue(createdFile));
            oneOf(mockWorkspaceFileHandler).copyInputStreamToTargetFile(mockZipInputStream, createdFile);
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(null));
        }});
        
        Collection<File> result = uploader.uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
        
        assertEquals("Result different from expected", expectedCopiedFiles, result);
    }
    
    //TODO Following test was commented out until it's known if folder names in the workspace have to be restricted or not
    
//    @Test
//    public void uploadZipFile_DirectoryAndFile_DirectoryNameHadToBeChanged() throws IOException {
//        
//        final String firstEntryName = "temp/";
//        final String changedFirstEntryName = "temp_1/";
//        final File createdDirectory = new File(workspaceUploadDirectory, changedFirstEntryName);
//        final String secondEntryName = "temp/file.cmdi";
//        final String changedSecondEntryName = "temp_1/file.cmdi";
//        final File createdFile = new File(workspaceUploadDirectory, changedSecondEntryName);
//        
//        final Collection<File> expectedCopiedFiles = new ArrayList<>();
//        expectedCopiedFiles.add(createdFile);
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
//            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockFirstZipEntry));
//            oneOf(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
//            oneOf(mockFirstZipEntry).isDirectory(); will(returnValue(Boolean.TRUE));
//            
//            oneOf(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
//            oneOf(mockWorkspaceDirectoryHandler).createDirectoryInWorkspace(workspaceID, firstEntryName); will(returnValue(createdDirectory));
//            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockSecondZipEntry));
//
//            // second loop iteration
//            
//            oneOf(mockSecondZipEntry).getName(); will(returnValue(secondEntryName));
//            oneOf(mockSecondZipEntry).isDirectory(); will(returnValue(Boolean.FALSE));
//            
//            oneOf(mockWorkspaceFileHandler).copyInputStreamToTargetFile(mockZipInputStream, createdFile);
//            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(null));
//        }});
//        
//        Collection<File> result = uploader.uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
//        
//        assertEquals("Result different from expected", expectedCopiedFiles, result);
//    }
    
    @Test
    public void uploadZipFile_ThrowsException() throws IOException {
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(throwException(expectedException));
        }});
        
        try {
            uploader.uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
            fail("should have thrown exception");
        } catch(IOException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void processOneUploadedResourceFile() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException {
        
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
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        uploadedNodes.add(uploadedNode);
        
        //only one file in the collection, so only one loop cycle
        
        final Collection<ImportProblem> failedLinks = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //loop

            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            oneOf(mockFile1).getName(); will(returnValue(filename));
                
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(
                    workspaceID, null, null, uploadedFileURL, fileMimetype, WorkspaceNodeStatus.NODE_UPLOADED, Boolean.FALSE);
                will(returnValue(uploadedNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode);
            
            
            //check links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
                will(returnValue(failedLinks));
        }});
        
        Collection<ImportProblem> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Collection with failed uploads should not be null", result);
        assertTrue("Collection with failed uploads should be empty", result.isEmpty());
    }
    
    @Test
    public void processOneUploadedResourceFile_IsInOrphansDirectory() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException {
        
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
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        uploadedNodes.add(uploadedNode);
        
        //only one file in the collection, so only one loop cycle
        
        final Collection<ImportProblem> failedLinks = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //loop

            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            oneOf(mockFile1).getName(); will(returnValue(filename));
                
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(
                    workspaceID, null, uploadedFileURI, uploadedFileURL, fileMimetype, WorkspaceNodeStatus.NODE_UPLOADED, Boolean.FALSE);
                will(returnValue(uploadedNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode);
            
            
            //check links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
                will(returnValue(failedLinks));
        }});
        
        Collection<ImportProblem> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Collection with failed uploads should not be null", result);
        assertTrue("Collection with failed uploads should be empty", result.isEmpty());
    }
    
    @Test
    public void processOneUploadedMetadataFile_ProfileIsAllowed() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException, Exception {
        
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
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        uploadedNodes.add(uploadedNode);
        
        //only one file in the collection, so only one loop cycle
        
        final Collection<ImportProblem> failedLinks = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //loop

            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            
            oneOf(mockMetadataApiBridge).isMetadataFileValid(uploadedFileURL); will(returnValue(Boolean.TRUE));
            
            oneOf(mockMetadataChecker).isProfileAllowed(mockFile1); will(returnValue(Boolean.TRUE));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype));
            
            oneOf(mockMetadataApiBridge).getSelfHandleFromFile(uploadedFileURL); will(returnValue(uploadedFileArchiveURI));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(
                    workspaceID, uploadedFileArchiveURI, null, uploadedFileURL, fileMimetype, WorkspaceNodeStatus.NODE_UPLOADED, Boolean.FALSE);
                will(returnValue(uploadedNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode);
            
            
            //check links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
                will(returnValue(failedLinks));
        }});
        
        Collection<ImportProblem> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Collection with failed uploads should not be null", result);
        assertTrue("Collection with failed uploads should be empty", result.isEmpty());
    }
    
    @Test
    public void processOneUploadedMetadataFile_ProfileNotAllowed() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException, Exception {
        
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
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Profile of metadata file [" + filename + "] not allowed.";
        
        //only one file in the collection, so only one loop cycle
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //loop

            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            
            oneOf(mockMetadataApiBridge).isMetadataFileValid(uploadedFileURL); will(returnValue(Boolean.TRUE));
            
            oneOf(mockMetadataChecker).isProfileAllowed(mockFile1); will(returnValue(Boolean.FALSE));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceFileHandler).deleteFile(mockFile1);
            
            
            //still calls method to process links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
        }});
        
        Collection<ImportProblem> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Collection with failed uploads should not be null", result);
        assertTrue("Collection with failed uploads should be empty", result.size() == 1);

        ImportProblem problem = result.iterator().next();
        
        assertTrue("Upload problem different from expected", problem instanceof FileImportProblem);
        assertEquals("File added to the upload problem is different from expected", mockFile1, ((FileImportProblem) problem).getProblematicFile());
        assertEquals("Reason for failure of file upload is different from expected", expectedErrorMessage, ((FileImportProblem) problem).getErrorMessage());
    }
    
    @Test
    public void processOneUploadedMetadataFile_MetadataFileNotValid() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException, Exception {
        
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
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Metadata file [" + filename + "] is invalid";
        
        //only one file in the collection, so only one loop cycle
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //loop

            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            
            oneOf(mockMetadataApiBridge).isMetadataFileValid(uploadedFileURL); will(returnValue(Boolean.FALSE));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceFileHandler).deleteFile(mockFile1);
            
            
            //still calls method to process links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
        }});
        
        Collection<ImportProblem> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Collection with failed uploads should not be null", result);
        assertTrue("Collection with failed uploads should be empty", result.size() == 1);

        ImportProblem problem = result.iterator().next();
        
        assertTrue("Upload problem different from expected", problem instanceof FileImportProblem);
        assertEquals("File added to the upload problem is different from expected", mockFile1, ((FileImportProblem) problem).getProblematicFile());
        assertEquals("Reason for failure of file upload is different from expected", expectedErrorMessage, ((FileImportProblem) problem).getErrorMessage());
    }
    
    @Test
    public void processTwoUploadedFiles() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException {
        
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
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        uploadedFiles.add(mockFile2);
        
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        uploadedNodes.add(uploadedNode1);
        uploadedNodes.add(uploadedNode2);
        
        //two files in the collection, so two loop cycles
        
        final Collection<ImportProblem> failedLinks = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //first loop cycle

            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI1));
            oneOf(mockFile1).getName(); will(returnValue(filename1));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL1, filename1);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            oneOf(mockFile1).getName(); will(returnValue(filename1));
                
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype1));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(
                    workspaceID, null, null, uploadedFileURL1, fileMimetype1, WorkspaceNodeStatus.NODE_UPLOADED, Boolean.FALSE);
                will(returnValue(uploadedNode1));

            oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode1);
            
            //second loop cycle

            oneOf(mockFile2).toURI(); will(returnValue(uploadedFileURI2));
            oneOf(mockFile2).getName(); will(returnValue(filename2));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL2, filename2);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            oneOf(mockFile2).getName(); will(returnValue(filename2));
                
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype2));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile2); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(
                    workspaceID, null, null, uploadedFileURL2, fileMimetype2, WorkspaceNodeStatus.NODE_UPLOADED, Boolean.FALSE);
                will(returnValue(uploadedNode2));

            oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode2);
            
            
            //check links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
                will(returnValue(failedLinks));
        }});
        
        Collection<ImportProblem> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Collection with failed uploads should not be null", result);
        assertTrue("Collection with failed uploads should be empty", result.isEmpty());
    }
    
    @Test
    public void processTwoUploadedFiles_LinkingFailed() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException {
        
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
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        uploadedFiles.add(mockFile2);
        
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        uploadedNodes.add(uploadedNode1);
        uploadedNodes.add(uploadedNode2);
        
        //two files in the collection, so two loop cycles
        
        final Collection<ImportProblem> failedLinks = new ArrayList<>();
        failedLinks.add(mockUploadProblem);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //first loop cycle

            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI1));
            oneOf(mockFile1).getName(); will(returnValue(filename1));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL1, filename1);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            oneOf(mockFile1).getName(); will(returnValue(filename1));
                
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype1));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(
                    workspaceID, null, null, uploadedFileURL1, fileMimetype1, WorkspaceNodeStatus.NODE_UPLOADED, Boolean.FALSE);
                will(returnValue(uploadedNode1));

            oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode1);
            
            //second loop cycle

            oneOf(mockFile2).toURI(); will(returnValue(uploadedFileURI2));
            oneOf(mockFile2).getName(); will(returnValue(filename2));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL2, filename2);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            oneOf(mockFile2).getName(); will(returnValue(filename2));
                
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype2));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile2); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(
                    workspaceID, null, null, uploadedFileURL2, fileMimetype2, WorkspaceNodeStatus.NODE_UPLOADED, Boolean.FALSE);
                will(returnValue(uploadedNode2));

            oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode2);
            
            
            //check links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
                will(returnValue(failedLinks));
        }});
        
        Collection<ImportProblem> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Collection with failed uploads should not be null", result);
        assertFalse("Collection with failed uploads should not be empty", result.isEmpty());
        assertTrue("Collection with failed uploads different from expected", result.containsAll(failedLinks));
    }
    
    @Test
    public void processUploadedFileWorkspaceException() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException {
        
        final String filename = "someFile.txt";
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
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
        
        Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        
        final String expectedErrorMessage = "Error retrieving archive URL from the top node of workspace " + workspaceID;
        final NodeNotFoundException expectedException = new NodeNotFoundException(workspaceTopNodeArchiveURI, "some exception message");
        
        //only one file in the collection, so only one loop cycle
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(throwException(expectedException));
        }});
        
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
    public void processUploadedFileUrlException() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException {
        
        final String filename = "someFile.txt";
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE;
        final String fileMimetype = "text/plain";
        final String uploadedFilePath = uploadedFile.getPath();
        final URI uriWhichIsNotUrl = new URI("node:0");
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.NODE_UPLOADED);
        uploadedNode.setType(fileType);
        uploadedNode.setFormat(fileMimetype);
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        
        //no successful uploads
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        
        final String expectedErrorMessage = "Error retrieving URL from file " + uploadedFile.getPath();
        
        //only one file in the collection, so only one loop cycle
        
        final Collection<ImportProblem> failedLinks = new ArrayList<>();
        
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
                will(returnValue(failedLinks));
        }});
        
        Collection<ImportProblem> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Collection with failed uploads should not be null", result);
        assertTrue("Collection with failed uploads should be empty", result.size() == 1);
        
        ImportProblem problem = result.iterator().next();
        
        assertTrue("Upload problem different from expected", problem instanceof FileImportProblem);
        assertEquals("File added to the upload problem is different from expected", mockFile1, ((FileImportProblem) problem).getProblematicFile());
        assertEquals("Reason for failure of file upload is different from expected", expectedErrorMessage, ((FileImportProblem) problem).getErrorMessage());
    }
    
    @Test
    public void processUploadedFileUnarchivable() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException {
        
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
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        
        //no successful uploads
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        
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
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.FALSE));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceFileHandler).deleteFile(mockFile1);
            
            
            //still calls method to process links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
        }});
        
        Collection<ImportProblem> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Collection with failed uploads should not be null", result);
        assertTrue("Collection with failed uploads should be empty", result.size() == 1);

        ImportProblem problem = result.iterator().next();
        
        assertTrue("Upload problem different from expected", problem instanceof FileImportProblem);
        assertEquals("File added to the upload problem is different from expected", mockFile1, ((FileImportProblem) problem).getProblematicFile());
        assertEquals("Reason for failure of file upload is different from expected", partExpectedErrorMessage, ((FileImportProblem) problem).getErrorMessage());
    }
    
    @Test
    public void processUploadedFileUnarchivable_IsInOrphansDirectory() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException {
        
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
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        
        //no successful uploads
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        
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
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.FALSE));
            oneOf(mockFile1).getName(); will(returnValue(filename));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.TRUE));
            
            
            //still calls method to process links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
        }});
        
        Collection<ImportProblem> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Collection with failed uploads should not be null", result);
        assertTrue("Collection with failed uploads should be empty", result.size() == 1);

        ImportProblem problem = result.iterator().next();
        
        assertTrue("Upload problem different from expected", problem instanceof FileImportProblem);
        assertEquals("File added to the upload problem is different from expected", mockFile1, ((FileImportProblem) problem).getProblematicFile());
        assertEquals("Reason for failure of file upload is different from expected", partExpectedErrorMessage, ((FileImportProblem) problem).getErrorMessage());
    }
}