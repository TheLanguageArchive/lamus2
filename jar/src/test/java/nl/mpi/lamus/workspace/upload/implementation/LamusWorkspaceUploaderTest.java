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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.handle.util.implementation.HandleConstants;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.DisallowedPathException;
import nl.mpi.lamus.exception.MetadataValidationException;
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
import nl.mpi.lamus.typechecking.WorkspaceFileValidator;
import nl.mpi.lamus.typechecking.implementation.MetadataValidationIssue;
import nl.mpi.lamus.typechecking.implementation.MetadataValidationIssueLevel;
import nl.mpi.lamus.typechecking.testing.ValidationIssueCollectionMatcher;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.upload.WorkspaceUploadHelper;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.type.MetadataDocumentType;

import org.apache.commons.fileupload.FileItem;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
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
    @Mock MetadataAPI mockMetadataAPI;
    @Mock MetadataApiBridge mockMetadataApiBridge;
    @Mock WorkspaceFileValidator mockWorkspaceFileValidator;
    @Mock ArchiveFileLocationProvider mockArchiveFileLocationProvider;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock NodeUtil mockNodeUtil;
    @Mock HandleParser mockHandleParser;
    
    @Mock FileItem mockFileItem;
    @Mock InputStream mockInputStream;
    @Mock FileInputStream mockFileInputStream;
    @Mock File mockUploadedFile;
    @Mock File mockWorkspaceTopNodeFile;
    @Mock WorkspaceNode mockWorkspaceTopNode;
    @Mock TypecheckedResults mockTypecheckedResults;
    
    @Mock MetadataDocument mockMetadataDocument;
    @Mock MetadataDocumentType mockMetadataDocumentType;
    
    @Mock ZipInputStream mockZipInputStream;
    @Mock ZipEntry mockFirstZipEntry;
    @Mock ZipEntry mockSecondZipEntry;
    @Mock ZipEntry mockThirdZipEntry;
    
    @Mock File mockFile1;
    @Mock File mockFile2;
    
    @Mock MetadataValidationIssue mockValidationIssue1;
    @Mock MetadataValidationIssue mockValidationIssue2;
    @Mock MetadataValidationException mockValidationException;
    @Mock ImportProblem mockUploadProblem;
    
    @Factory
    public static Matcher<Collection<MetadataValidationIssue>> equivalentValidationIssueCollection(Collection<MetadataValidationIssue> collection) {
        return new ValidationIssueCollectionMatcher(collection);
    }
    
    private WorkspaceUploader uploader;
    
    private final File workspaceBaseDirectory = new File("/lamus/workspaces");
    private final String workspaceUploadDirectoryName = "upload";
//    private File workspaceUploadDirectory = new File("/lamus/workspace/upload");
    
    private final int workspaceID = 1;
    private final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
    private final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
    
    private final String handlePrefixWithSlash = "11142/";
    private final String handleProxyPlusPrefixWithSlash = HandleConstants.HDL_SHORT_PROXY + ":" + handlePrefixWithSlash;
    
    
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
                mockWorkspaceNodeFactory, mockWorkspaceDao,
                mockWorkspaceUploadHelper, mockMetadataAPI,
                mockMetadataApiBridge, mockWorkspaceFileValidator,
                mockArchiveFileLocationProvider, mockArchiveFileHelper,
                mockNodeUtil, mockHandleParser);
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
    public void uploadFile_nameNotAllowed() throws IOException, DisallowedPathException {
        
        final String filename = "temp";
        final DisallowedPathException expectedException = new DisallowedPathException(filename, "path is not allowed and so on");
        
        context.checking(new Expectations() {{
        
            oneOf(mockWorkspaceDirectoryHandler).ensurePathIsAllowed(filename); will(throwException(expectedException));
        }});
        
        try {
            uploader.uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
            fail("should have thrown exception");
        } catch(DisallowedPathException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void uploadFile_noNameChange() throws IOException, DisallowedPathException {
        
        final String filename = "file.cmdi";
        final File expectedFile = new File(workspaceUploadDirectory, filename);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).ensurePathIsAllowed(filename);
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockArchiveFileHelper).getFinalFile(workspaceUploadDirectory, filename); will(returnValue(expectedFile));
            oneOf(mockWorkspaceFileHandler).copyInputStreamToTargetFile(mockInputStream, expectedFile);
        }});
        
        File resultingFile = uploader.uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
        
        assertEquals("Resulting file different from expected", expectedFile, resultingFile);
    }
    
    @Test
    public void uploadFile_nameChanged() throws IOException, DisallowedPathException {
        
        final String filename = "file.cmdi";
        final File file = new File(workspaceUploadDirectory, filename);
        final String changedFilename = "file_01.cmdi";
        final File expectedFile = new File(workspaceUploadDirectory, changedFilename);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).ensurePathIsAllowed(filename);
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockArchiveFileHelper).getFinalFile(workspaceUploadDirectory, filename); will(returnValue(expectedFile));
            oneOf(mockWorkspaceFileHandler).copyInputStreamToTargetFile(mockInputStream, expectedFile);
        }});
        
        File resultingFile = uploader.uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
        
        assertEquals("Resulting file different from expected", expectedFile, resultingFile);
    }
    
    @Test
    public void uploadFile_ThrowsException() throws IOException, DisallowedPathException {
        
        final String filename = "file.cmdi";
        final File file = new File(workspaceUploadDirectory, filename);
        final String changedFilename = "file_01.cmdi";
        final File expectedFile = new File(workspaceUploadDirectory, changedFilename);
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).ensurePathIsAllowed(filename);
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
    public void uploadZipFile_IsDirectory() throws IOException, DisallowedPathException {
        
        final String firstEntryName = "directory";
        
        final Collection<File> expectedCopiedFiles = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockFirstZipEntry));
            allowing(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
            oneOf(mockWorkspaceDirectoryHandler).ensurePathIsAllowed(firstEntryName);
            oneOf(mockFirstZipEntry).isDirectory(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceDirectoryHandler).createDirectoryInWorkspace(workspaceID, firstEntryName);
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(null));
        }});
        
        Collection<File> result = uploader.uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
        
        assertEquals("Result different from expected", expectedCopiedFiles, result);
    }
    
    @Test
    public void uploadZipFile_IsNotDirectory() throws IOException, DisallowedPathException {
        
        final String firstEntryName = "file.cmdi";
        final File firstEntryFile = new File(workspaceUploadDirectory, firstEntryName);
        
        final Collection<File> expectedCopiedFiles = new ArrayList<>();
        expectedCopiedFiles.add(firstEntryFile);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockFirstZipEntry));
            allowing(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
            oneOf(mockWorkspaceDirectoryHandler).ensurePathIsAllowed(firstEntryName);
            oneOf(mockFirstZipEntry).isDirectory(); will(returnValue(Boolean.FALSE));
            oneOf(mockArchiveFileHelper).getFinalFile(workspaceUploadDirectory, firstEntryName); will(returnValue(firstEntryFile));
            oneOf(mockWorkspaceFileHandler).copyInputStreamToTargetFile(mockZipInputStream, firstEntryFile);
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(null));
        }});
        
        Collection<File> result = uploader.uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
        
        assertEquals("Result different from expected", expectedCopiedFiles, result);
    }
    
    @Test
    public void uploadZipFile_IsNotDirectory_fileNeedsRenaming() throws IOException, DisallowedPathException {
        
        final String firstEntryName = "file.cmdi";
        final String changedFirstEntryName = "file_01.cmdi";
        final File changedFirstEntryFile = new File(workspaceUploadDirectory, changedFirstEntryName);
        
        final Collection<File> expectedCopiedFiles = new ArrayList<>();
        expectedCopiedFiles.add(changedFirstEntryFile);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockFirstZipEntry));
            allowing(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
            oneOf(mockWorkspaceDirectoryHandler).ensurePathIsAllowed(firstEntryName);
            oneOf(mockFirstZipEntry).isDirectory(); will(returnValue(Boolean.FALSE));
            oneOf(mockArchiveFileHelper).getFinalFile(workspaceUploadDirectory, firstEntryName); will(returnValue(changedFirstEntryFile));
            oneOf(mockWorkspaceFileHandler).copyInputStreamToTargetFile(mockZipInputStream, changedFirstEntryFile);
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(null));
        }});
        
        Collection<File> result = uploader.uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
        
        assertEquals("Result different from expected", expectedCopiedFiles, result);
    }
    
    @Test
    public void uploadZipFile_DirectoryAndFile() throws IOException, DisallowedPathException {
        
        final String firstEntryName = "directory/";
        final File createdDirectory = new File(workspaceUploadDirectory, firstEntryName);
        final String secondEntryName = "directory/file.cmdi";
        final File createdFile = new File(workspaceUploadDirectory, secondEntryName);
        
        final Collection<File> expectedCopiedFiles = new ArrayList<>();
        expectedCopiedFiles.add(createdFile);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockFirstZipEntry));
            allowing(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
            oneOf(mockWorkspaceDirectoryHandler).ensurePathIsAllowed(firstEntryName);
            oneOf(mockFirstZipEntry).isDirectory(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceDirectoryHandler).createDirectoryInWorkspace(workspaceID, firstEntryName); will(returnValue(createdDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockSecondZipEntry));

            // second loop iteration
            
            allowing(mockSecondZipEntry).getName(); will(returnValue(secondEntryName));
            oneOf(mockWorkspaceDirectoryHandler).ensurePathIsAllowed(secondEntryName);
            oneOf(mockSecondZipEntry).isDirectory(); will(returnValue(Boolean.FALSE));
            oneOf(mockArchiveFileHelper).getFinalFile(createdDirectory, createdFile.getName()); will(returnValue(createdFile));
            oneOf(mockWorkspaceFileHandler).copyInputStreamToTargetFile(mockZipInputStream, createdFile);
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(null));
        }});
        
        Collection<File> result = uploader.uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
        
        assertEquals("Result different from expected", expectedCopiedFiles, result);
    }
    
    @Test
    public void uploadZipFile_DirectoryAndFile_NameNotAllowed() throws IOException, DisallowedPathException {
        
        final String firstEntryName = "temp/";
        final String secondEntryName = "temp/file.cmdi";
        
        final DisallowedPathException expectedException = new DisallowedPathException(firstEntryName, "Path not allowed and so on...");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockFirstZipEntry));
            allowing(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
            oneOf(mockWorkspaceDirectoryHandler).ensurePathIsAllowed(firstEntryName); will(throwException(expectedException));
            
            // nothing was created, so nothing has to be deleted
        }});
        
        try {
            uploader.uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
            fail("should have thrown exception");
        } catch(DisallowedPathException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void uploadZipFile_MoreThanOneDirectoryAndFile_NameNotAllowed() throws IOException, DisallowedPathException {
        
        final String firstEntryName = "dir";
        final File createdDirectory = new File(workspaceUploadDirectory, firstEntryName);
        final String secondEntryName = "dir/file.cmdi";
        final File createdFile = new File(workspaceUploadDirectory, secondEntryName);
        final String thirdEntryName = "temp/";
        final String fourthEntryName = "temp/anotherfile.cmdi";
        
        final DisallowedPathException expectedException = new DisallowedPathException(firstEntryName, "Path not allowed and so on...");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockFirstZipEntry));
            allowing(mockFirstZipEntry).getName(); will(returnValue(firstEntryName));
            oneOf(mockWorkspaceDirectoryHandler).ensurePathIsAllowed(firstEntryName);
            oneOf(mockFirstZipEntry).isDirectory(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceDirectoryHandler).createDirectoryInWorkspace(workspaceID, firstEntryName); will(returnValue(createdDirectory));
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockSecondZipEntry));

            // second loop iteration
            
            allowing(mockSecondZipEntry).getName(); will(returnValue(secondEntryName));
            oneOf(mockWorkspaceDirectoryHandler).ensurePathIsAllowed(secondEntryName);
            oneOf(mockSecondZipEntry).isDirectory(); will(returnValue(Boolean.FALSE));
            oneOf(mockArchiveFileHelper).getFinalFile(createdDirectory, createdFile.getName()); will(returnValue(createdFile));
            oneOf(mockWorkspaceFileHandler).copyInputStreamToTargetFile(mockZipInputStream, createdFile);
            oneOf(mockZipInputStream).getNextEntry(); will(returnValue(mockThirdZipEntry));
            
            // third loop iteration
            
            allowing(mockThirdZipEntry).getName(); will(returnValue(thirdEntryName));
            oneOf(mockWorkspaceDirectoryHandler).ensurePathIsAllowed(thirdEntryName); will(throwException(expectedException));
            
            // folder and file were created, so will have to be deleted in this case
            oneOf(mockWorkspaceFileHandler).deleteFile(createdFile);
            oneOf(mockWorkspaceFileHandler).deleteFile(createdDirectory);
        }});
        
        try {
            uploader.uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
            fail("should have thrown exception");
        } catch(DisallowedPathException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void uploadZipFile_ThrowsException() throws IOException, DisallowedPathException {
        
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
        final URI workspaceTopNodeArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final WorkspaceNodeType fileNodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String fileMimetype = "text/plain";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.UPLOADED);
        uploadedNode.setType(fileNodeType);
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
            oneOf(mockNodeUtil).convertMimetype(fileMimetype); will(returnValue(fileNodeType));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(workspaceID, null, null, uploadedFileURL, null, fileMimetype, fileNodeType,
                    WorkspaceNodeStatus.UPLOADED, Boolean.FALSE);
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
        final URI workspaceTopNodeArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final WorkspaceNodeType fileNodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String fileMimetype = "text/plain";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.UPLOADED);
        uploadedNode.setType(fileNodeType);
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
            oneOf(mockNodeUtil).convertMimetype(fileMimetype); will(returnValue(fileNodeType));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(workspaceID, null, uploadedFileURI, uploadedFileURL, null, fileMimetype, fileNodeType,
                    WorkspaceNodeStatus.UPLOADED, Boolean.FALSE);
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
    public void processOneUploadedMetadataFile_NoValidationIssues() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException, Exception {
        
        final String filename = "someFile.cmdi";
        final URI workspaceTopNodeArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final String uploadedFileRawHandle = "UUID.randomUUID().toString()";
        final URI uploadedFileArchiveURI = URI.create(handlePrefixWithSlash + uploadedFileRawHandle);
        final URI completeFileArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + uploadedFileRawHandle);
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final URI schemaLocation = URI.create("http://some/location/schema.xsd");
        final WorkspaceNodeType fileNodeType = WorkspaceNodeType.METADATA;
        final String fileMimetype = "text/x-cmdi-xml";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.UPLOADED);
        uploadedNode.setType(fileNodeType);
        uploadedNode.setFormat(fileMimetype);
        uploadedNode.setWorkspaceURL(uploadedFileURL);
        uploadedNode.setArchiveURI(uploadedFileArchiveURI);
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        
        final Collection<MetadataValidationIssue> issues = new ArrayList<>();
        
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

            allowing(mockFile1).getName(); will(returnValue(filename));
                
            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            
            oneOf(mockMetadataAPI).getMetadataDocument(uploadedFileURL); will(returnValue(mockMetadataDocument));
                
            oneOf(mockMetadataApiBridge).isMetadataDocumentValid(mockMetadataDocument); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceFileValidator).validateMetadataFile(workspaceID, mockFile1);
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype));
            oneOf(mockNodeUtil).convertMimetype(fileMimetype); will(returnValue(fileNodeType));
            
            oneOf(mockMetadataApiBridge).getSelfHandleFromDocument(mockMetadataDocument); will(returnValue(uploadedFileArchiveURI));
            oneOf(mockHandleParser).prepareHandleWithHdlPrefix(uploadedFileArchiveURI); will(returnValue(completeFileArchiveURI));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            
            oneOf(mockMetadataDocument).getDocumentType(); will(returnValue(mockMetadataDocumentType));
            oneOf(mockMetadataDocumentType).getSchemaLocation(); will(returnValue(schemaLocation));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(workspaceID, completeFileArchiveURI, null, uploadedFileURL, schemaLocation, fileMimetype, fileNodeType,
                    WorkspaceNodeStatus.UPLOADED, Boolean.FALSE);
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
    public void processOneUploadedMetadataFile_withOneValidationIssue_Error() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException, Exception {
        
        final String filename = "someFile.cmdi";
        final URI workspaceTopNodeArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URI uploadedFileArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final WorkspaceNodeType fileType = WorkspaceNodeType.METADATA;
        final String fileMimetype = "text/x-cmdi-xml";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.UPLOADED);
        uploadedNode.setType(fileType);
        uploadedNode.setFormat(fileMimetype);
        uploadedNode.setWorkspaceURL(uploadedFileURL);
        uploadedNode.setArchiveURI(uploadedFileArchiveURI);
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        
        final Collection<MetadataValidationIssue> issues = new ArrayList<>();
        issues.add(mockValidationIssue1);
        
        final String assertionErrorMessage = "[CMDI Archive Restriction] the CMD profile of this record is not allowed in the archive.";
        final String validationIssuesString = "Validation issue for file '" + filename + "' - " + MetadataValidationIssueLevel.ERROR.toString() + ": " + assertionErrorMessage + ".\n";
        
        final MetadataValidationException expectedException = new MetadataValidationException(validationIssuesString, workspaceID, null);
        expectedException.addValidationIssues(issues);
        
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        
        
        //only one file in the collection, so only one loop cycle
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //loop

            allowing(mockFile1).getName(); will(returnValue(filename));
                
            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            
            oneOf(mockMetadataAPI).getMetadataDocument(uploadedFileURL); will(returnValue(mockMetadataDocument));
            
            oneOf(mockMetadataApiBridge).isMetadataDocumentValid(mockMetadataDocument); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceFileValidator).validateMetadataFile(workspaceID, mockFile1); will(throwException(expectedException));
            oneOf(mockWorkspaceFileValidator).validationIssuesToString(with(equivalentValidationIssueCollection(issues))); will(returnValue(validationIssuesString));
            oneOf(mockWorkspaceFileValidator).validationIssuesContainErrors(with(equivalentValidationIssueCollection(issues))); will(returnValue(Boolean.TRUE));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceFileHandler).deleteFile(mockFile1);
            
            
            //still calls method to process links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
        }});
        
        Collection<ImportProblem> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Collection with failed uploads should not be null", result);
        assertTrue("Collection with failed uploads should have one element", result.size() == 1);

        ImportProblem problem = result.iterator().next();
        
        assertTrue("Upload problem different from expected", problem instanceof FileImportProblem);
        assertEquals("File added to the upload problem is different from expected", mockFile1, ((FileImportProblem) problem).getProblematicFile());
        assertEquals("Reason for failure of file upload is different from expected", validationIssuesString.trim(), ((FileImportProblem) problem).getErrorMessage().trim());
    }
    
    @Test
    public void processOneUploadedMetadataFile_withTwoValidationIssues_Error() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException, Exception {
        
        final String filename = "someFile.cmdi";
        final URI workspaceTopNodeArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URI uploadedFileArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final WorkspaceNodeType fileType = WorkspaceNodeType.METADATA;
        final String fileMimetype = "text/x-cmdi-xml";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.UPLOADED);
        uploadedNode.setType(fileType);
        uploadedNode.setFormat(fileMimetype);
        uploadedNode.setWorkspaceURL(uploadedFileURL);
        uploadedNode.setArchiveURI(uploadedFileArchiveURI);
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        
        final Collection<MetadataValidationIssue> issues = new ArrayList<>();
        issues.add(mockValidationIssue1);
        issues.add(mockValidationIssue2);
        
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        
        final String assertionErrorMessage1 = "[CMDI Archive Restriction] the CMD profile of this record is not allowed in the archive.";
        final String assertionErrorMessage2 = "[CMDI Archive Restriction] Something completely different went wrong.";
        final String validationIssuesString = "Validation issue for file '" + filename + "' - " + MetadataValidationIssueLevel.ERROR.toString() + ": " + assertionErrorMessage1 + ".\n" +
                "Validation issue for file '" + filename + "' - " + MetadataValidationIssueLevel.ERROR.toString() + ": " + assertionErrorMessage2 + ".\n";
        
        final MetadataValidationException expectedException = new MetadataValidationException(validationIssuesString, workspaceID, null);
        expectedException.addValidationIssues(issues);
        
        //only one file in the collection, so only one loop cycle
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //loop

            allowing(mockFile1).getName(); will(returnValue(filename));
                
            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            
            oneOf(mockMetadataAPI).getMetadataDocument(uploadedFileURL); will(returnValue(mockMetadataDocument));
            
            oneOf(mockMetadataApiBridge).isMetadataDocumentValid(mockMetadataDocument); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceFileValidator).validateMetadataFile(workspaceID, mockFile1); will(throwException(expectedException));
            oneOf(mockWorkspaceFileValidator).validationIssuesToString(with(equivalentValidationIssueCollection(issues))); will(returnValue(validationIssuesString));
            oneOf(mockWorkspaceFileValidator).validationIssuesContainErrors(with(equivalentValidationIssueCollection(issues))); will(returnValue(Boolean.TRUE));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceFileHandler).deleteFile(mockFile1);
            
            
            //still calls method to process links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
        }});
        
        Collection<ImportProblem> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Collection with failed uploads should not be null", result);
        assertTrue("Collection with failed uploads should have one element", result.size() == 1);

        ImportProblem problem = result.iterator().next();
        
        assertTrue("Upload problem different from expected", problem instanceof FileImportProblem);
        assertEquals("File added to the upload problem is different from expected", mockFile1, ((FileImportProblem) problem).getProblematicFile());
        assertEquals("Reason for failure of file upload is different from expected", validationIssuesString.trim(), ((FileImportProblem) problem).getErrorMessage().trim());
    }
    
    @Test
    public void processOneUploadedMetadataFile_withOneValidationIssue_Warning() throws URISyntaxException, MalformedURLException, WorkspaceNodeNotFoundException, NodeNotFoundException, TypeCheckerException, MetadataValidationException, WorkspaceException, IOException, MetadataException {
        
        final String filename = "someFile.cmdi";
        final URI workspaceTopNodeArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final String uploadedFileRawHandle = UUID.randomUUID().toString();
        final URI uploadedFileArchiveURI = URI.create(handlePrefixWithSlash + uploadedFileRawHandle);
        final URI completeFileArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + uploadedFileRawHandle);
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final URI schemaLocation = URI.create("http://some/location/schema.xsd");
        final WorkspaceNodeType fileNodeType = WorkspaceNodeType.METADATA;
        final String fileMimetype = "text/x-cmdi-xml";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.UPLOADED);
        uploadedNode.setType(fileNodeType);
        uploadedNode.setFormat(fileMimetype);
        uploadedNode.setWorkspaceURL(uploadedFileURL);
        uploadedNode.setArchiveURI(uploadedFileArchiveURI);
        
        final Collection<File> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(mockFile1);
        
        final Collection<MetadataValidationIssue> issues = new ArrayList<>();
        issues.add(mockValidationIssue1);
        
        final String assertionErrorMessage = "[CMDI Best Practice] /cmd:CMD/cmd:Components/*/cmd:Title shouldn't be empty.";
        final String validationIssuesString = "Validation issue for file '" + filename + "' - " + MetadataValidationIssueLevel.WARN.toString() + ": " + assertionErrorMessage + ".\n";
        
        final MetadataValidationException expectedException = new MetadataValidationException(validationIssuesString, workspaceID, null);
        expectedException.addValidationIssues(issues);
        
        final Collection<WorkspaceNode> uploadedNodes = new ArrayList<>();
        uploadedNodes.add(uploadedNode);
        
        
        //only one file in the collection, so only one loop cycle
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
            oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
            oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI);
                will(returnValue(workspaceTopNodeArchiveURL));
            
            //loop

            allowing(mockFile1).getName(); will(returnValue(filename));
                
            oneOf(mockFile1).toURI(); will(returnValue(uploadedFileURI));
            oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileURL, filename);
                will(returnValue(mockTypecheckedResults));
            
            oneOf(mockNodeDataRetriever).isCheckedResourceArchivable(with(same(mockTypecheckedResults)), with(same(workspaceTopNodeArchiveURL)), with(any(StringBuilder.class)));
                will(returnValue(Boolean.TRUE));
            
            oneOf(mockMetadataAPI).getMetadataDocument(uploadedFileURL); will(returnValue(mockMetadataDocument));
            
            oneOf(mockMetadataApiBridge).isMetadataDocumentValid(mockMetadataDocument); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceFileValidator).validateMetadataFile(workspaceID, mockFile1); will(throwException(expectedException));
            oneOf(mockWorkspaceFileValidator).validationIssuesToString(with(equivalentValidationIssueCollection(issues))); will(returnValue(validationIssuesString));
            oneOf(mockWorkspaceFileValidator).validationIssuesContainErrors(with(equivalentValidationIssueCollection(issues))); will(returnValue(Boolean.FALSE));
            
            oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype));
            oneOf(mockNodeUtil).convertMimetype(fileMimetype); will(returnValue(fileNodeType));
            
            oneOf(mockMetadataApiBridge).getSelfHandleFromDocument(mockMetadataDocument); will(returnValue(uploadedFileArchiveURI));
            oneOf(mockHandleParser).prepareHandleWithHdlPrefix(uploadedFileArchiveURI); will(returnValue(completeFileArchiveURI));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            
            oneOf(mockMetadataDocument).getDocumentType(); will(returnValue(mockMetadataDocumentType));
            oneOf(mockMetadataDocumentType).getSchemaLocation(); will(returnValue(schemaLocation));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(workspaceID, completeFileArchiveURI, null, uploadedFileURL, schemaLocation, fileMimetype, fileNodeType,
                    WorkspaceNodeStatus.UPLOADED, Boolean.FALSE);
                will(returnValue(uploadedNode));

            oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode);
            
            
            //still calls method to process links
            oneOf(mockWorkspaceUploadHelper).assureLinksInWorkspace(workspaceID, uploadedNodes);
        }});
        
        Collection<ImportProblem> result = uploader.processUploadedFiles(workspaceID, uploadedFiles);
        
        assertNotNull("Collection with failed uploads should not be null", result);
        assertTrue("Collection with failed uploads should be empty", result.isEmpty());
    }

    
    @Test
    public void processOneUploadedMetadataFile_MetadataFileNotValid() throws IOException, WorkspaceNodeNotFoundException, URISyntaxException, WorkspaceException, NodeNotFoundException, TypeCheckerException, Exception {
        
        final String filename = "someFile.cmdi";
        final URI workspaceTopNodeArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URI uploadedFileArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final WorkspaceNodeType fileType = WorkspaceNodeType.METADATA;
        final String fileMimetype = "text/x-cmdi-xml";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.UPLOADED);
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
            
            oneOf(mockMetadataAPI).getMetadataDocument(uploadedFileURL); will(returnValue(mockMetadataDocument));
            
            oneOf(mockMetadataApiBridge).isMetadataDocumentValid(mockMetadataDocument); will(returnValue(Boolean.FALSE));
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
        
        final URI workspaceTopNodeArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final String filename1 = "someFile.txt";
        final File uploadedFile1 = new File(workspaceUploadDirectory, filename1);
        final URI uploadedFileURI1 = uploadedFile1.toURI();
        final URL uploadedFileURL1 = uploadedFileURI1.toURL();
        final WorkspaceNodeType fileNodeType1 = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String fileMimetype1 = "text/plain";
        
        final String filename2 = "someOtherFile.jpg";
        final File uploadedFile2 = new File(workspaceUploadDirectory, filename2);
        final URI uploadedFileURI2 = uploadedFile2.toURI();
        final URL uploadedFileURL2 = uploadedFileURI2.toURL();
        final WorkspaceNodeType fileNodeType2 = WorkspaceNodeType.RESOURCE_IMAGE;
        final String fileMimetype2 = "image/jpeg";
        
        final WorkspaceNode uploadedNode1 = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode1.setName(filename1);
        uploadedNode1.setStatus(WorkspaceNodeStatus.UPLOADED);
        uploadedNode1.setType(fileNodeType1);
        uploadedNode1.setFormat(fileMimetype1);
        uploadedNode1.setWorkspaceURL(uploadedFileURL1);
        
        final WorkspaceNode uploadedNode2 = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode2.setName(filename2);
        uploadedNode2.setStatus(WorkspaceNodeStatus.UPLOADED);
        uploadedNode2.setType(fileNodeType2);
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
            oneOf(mockNodeUtil).convertMimetype(fileMimetype1); will(returnValue(fileNodeType1));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(workspaceID, null, null, uploadedFileURL1, null, fileMimetype1, fileNodeType1,
                    WorkspaceNodeStatus.UPLOADED, Boolean.FALSE);
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
            oneOf(mockNodeUtil).convertMimetype(fileMimetype2); will(returnValue(fileNodeType2));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile2); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(workspaceID, null, null, uploadedFileURL2, null, fileMimetype2, fileNodeType2,
                    WorkspaceNodeStatus.UPLOADED, Boolean.FALSE);
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
        
        final URI workspaceTopNodeArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final String filename1 = "someFile.txt";
        final File uploadedFile1 = new File(workspaceUploadDirectory, filename1);
        final URI uploadedFileURI1 = uploadedFile1.toURI();
        final URL uploadedFileURL1 = uploadedFileURI1.toURL();
        final WorkspaceNodeType fileNodeType1 = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String fileMimetype1 = "text/plain";
        
        final String filename2 = "someOtherFile.jpg";
        final File uploadedFile2 = new File(workspaceUploadDirectory, filename2);
        final URI uploadedFileURI2 = uploadedFile2.toURI();
        final URL uploadedFileURL2 = uploadedFileURI2.toURL();
        final WorkspaceNodeType fileNodeType2 = WorkspaceNodeType.RESOURCE_IMAGE;
        final String fileMimetype2 = "image/jpeg";
        
        final WorkspaceNode uploadedNode1 = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode1.setName(filename1);
        uploadedNode1.setStatus(WorkspaceNodeStatus.UPLOADED);
        uploadedNode1.setType(fileNodeType1);
        uploadedNode1.setFormat(fileMimetype1);
        uploadedNode1.setWorkspaceURL(uploadedFileURL1);
        
        final WorkspaceNode uploadedNode2 = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode2.setName(filename2);
        uploadedNode2.setStatus(WorkspaceNodeStatus.UPLOADED);
        uploadedNode2.setType(fileNodeType2);
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
            oneOf(mockNodeUtil).convertMimetype(fileMimetype1); will(returnValue(fileNodeType1));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile1); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(workspaceID, null, null, uploadedFileURL1, null, fileMimetype1, fileNodeType1,
                    WorkspaceNodeStatus.UPLOADED, Boolean.FALSE);
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
            oneOf(mockNodeUtil).convertMimetype(fileMimetype2); will(returnValue(fileNodeType2));
            
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(mockFile2); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(workspaceID, null, null, uploadedFileURL2, null, fileMimetype2, fileNodeType2,
                    WorkspaceNodeStatus.UPLOADED, Boolean.FALSE);
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
        final URI workspaceTopNodeArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String fileMimetype = "text/plain";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.UPLOADED);
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
        final URI workspaceTopNodeArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String fileMimetype = "text/plain";
        final String uploadedFilePath = uploadedFile.getPath();
        final URI uriWhichIsNotUrl = URI.create("node:0");
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.UPLOADED);
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
        final URI workspaceTopNodeArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String fileMimetype = "text/plain";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.UPLOADED);
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
        final URI workspaceTopNodeArchiveURI = URI.create(handleProxyPlusPrefixWithSlash + UUID.randomUUID().toString());
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File uploadedFile = new File(workspaceUploadDirectory, filename);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String fileMimetype = "text/plain";
        
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(filename);
        uploadedNode.setStatus(WorkspaceNodeStatus.UPLOADED);
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
