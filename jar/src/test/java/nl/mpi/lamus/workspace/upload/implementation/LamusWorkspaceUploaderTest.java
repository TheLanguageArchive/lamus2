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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.typechecking.TypecheckerConfiguration;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

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
    @Mock TypecheckerConfiguration mockTypecheckerConfiguration;
    @Mock FileTypeHandler mockFileTypeHandler;
    
    @Mock FileItem mockFileItem;
    @Mock InputStream mockInputStream;
    @Mock File mockUploadedFile;
    @Mock File mockWorkspaceTopNodeFile;
    @Mock WorkspaceNode mockWorkspaceTopNode;
    @Mock TypecheckedResults mockTypecheckedResults;
    
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
                mockWorkspaceDao, mockTypecheckerConfiguration,
                mockFileTypeHandler);
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
    public void uploadFiles() throws Exception {
        
        final int workspaceID = 1;
        final URL workspaceTopNodeArchiveURL = new URL("file:/archive/some/node.cmdi");
        final File workspaceTopNodeArchivePath = new File("/archive/some/node.cmdi");
        final URI workspaceTopNodeArchiveURI = new URI(UUID.randomUUID().toString());
        
        final TypecheckerJudgement acceptableJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        final String fileItemName = "randomWrittenResourceFile.txt";
        
        final File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        final File workspaceUploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        
        final File uploadedFile = new File(workspaceUploadDirectory, fileItemName);
        final URI uploadedFileURI = uploadedFile.toURI();
        final URL uploadedFileURL = uploadedFileURI.toURL();
        final OurURL uploadedFileOurURL = new OurURL(uploadedFileURL);
        final WorkspaceNodeType fileType = WorkspaceNodeType.RESOURCE_WR;
        final String fileMimetype = "text/plain";
        
        final Collection<FileItem> fileItems = new ArrayList<FileItem>();
        fileItems.add(mockFileItem);
        // only one item in this case, so the expectations don't need a loop
        
        //TODO Is the originURL really necessary??
        final WorkspaceNode uploadedNode = new LamusWorkspaceNode(workspaceID, null, null);
        uploadedNode.setName(fileItemName);
        uploadedNode.setStatus(WorkspaceNodeStatus.NODE_UPLOADED);
        uploadedNode.setType(fileType);
        uploadedNode.setFormat(fileMimetype);
        uploadedNode.setWorkspaceURL(uploadedFileURL);
        
        
        //TODO copy each file from its current location into the workspace directory
        //TODO create a new node for each file and add it to the database, with appropriate links
        //TODO how to check the linked files?
            
            context.checking(new Expectations() {{
                
                oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceTopNode));
                oneOf(mockWorkspaceDirectoryHandler).getUploadDirectoryForWorkspace(workspaceID); will(returnValue(workspaceUploadDirectory));
                oneOf(mockWorkspaceDirectoryHandler).createUploadDirectoryForWorkspace(workspaceID);
                
                oneOf(mockFileItem).getName(); will(returnValue(fileItemName));
                oneOf(mockUploadedFile).toURI(); will(returnValue(uploadedFileURI));
                
                // Assume that all resource files should be typechecked, independently of size?
                
                oneOf(mockFileItem).getInputStream(); will(returnValue(mockInputStream));
//                oneOf(mockNodeDataRetriever).triggerResourceFileCheck(uploadedFileOurURL);
                oneOf(mockNodeDataRetriever).triggerResourceFileCheck(mockInputStream, fileItemName);
                    will(returnValue(mockTypecheckedResults));
                    
                //TODO Is this method really necessary???
//                oneOf(mockNodeDataRetriever).verifyTypecheckedResults(fileOurUrl, mockChildLink, mockTypecheckedResults);
                    
                oneOf(mockWorkspaceTopNode).getArchiveURI(); will(returnValue(workspaceTopNodeArchiveURI));
                oneOf(mockNodeDataRetriever).getNodeArchiveURL(workspaceTopNodeArchiveURI); will(returnValue(workspaceTopNodeArchiveURL));
                    
//                oneOf(mockWorkspaceTopNode).getArchiveURL(); will(returnValue(workspaceTopNodeArchiveURL));
                
                oneOf(mockTypecheckerConfiguration).getAcceptableJudgementForLocation(mockWorkspaceTopNodeFile);
                    will(returnValue(acceptableJudgement));
                oneOf(mockFileTypeHandler).isResourceArchivable(
                        with(equal(uploadedFileOurURL)), with(same(acceptableJudgement)), with(any(StringBuilder.class)));
                    will(returnValue(Boolean.TRUE));
                
                
                //everything ok?
                
                oneOf(mockFileItem).write(mockUploadedFile);

                oneOf(mockTypecheckedResults).getCheckedNodeType(); will(returnValue(fileType));
                oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(fileMimetype));
                
                oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(
                        workspaceID, null, uploadedFileURL, fileType, fileMimetype, WorkspaceNodeStatus.NODE_UPLOADED);
                    will(returnValue(uploadedNode));

                oneOf(mockWorkspaceDao).addWorkspaceNode(uploadedNode);
            }});
            
            stub(method(FileUtils.class, "getFile", File.class, String.class)).toReturn(mockUploadedFile);
            stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockWorkspaceTopNodeFile);
        
        //checklinks???
        //checklinks???
        //checklinks???

        uploader.uploadFiles(workspaceID, fileItems);
    }
}