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
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceUploaderTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    private WorkspaceUploader uploader;
    
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    
    @Mock NodeDataRetriever mockNodeDataRetriever;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock WorkspaceNodeFactory mockWorkspaceNodeFactory;
    @Mock WorkspaceDao mockWorkspaceDao;
    
    private File workspaceUploadDirectory = new File("/lamus/workspace/upload");
    
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
        uploader = new LamusWorkspaceUploader(mockNodeDataRetriever, mockWorkspaceFileHandler, mockWorkspaceNodeFactory, mockWorkspaceDao);
        ReflectionTestUtils.setField(uploader, "workspaceUploadDirectory", workspaceUploadDirectory);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of uploadFiles method, of class LamusWorkspaceUploader.
     */
    @Test
    public void uploadFiles() {
        
        final int workspaceID = 1;
        final Collection<FileItem> fileItems = new ArrayList<FileItem>();
        
        //TODO copy each file from its current location into the workspace directory
        //TODO create a new node for each file and add it to the database, with appropriate links
        //TODO how to check the linked files?
        
        for(FileItem item : fileItems) {
            
            context.checking(new Expectations() {{
                
                //typechecker
                    //nodeDataRetriever.shouldResourceBeTypechecked - based on its eventual location in the workspace
                    //nodeDataRetriever.getResourceTypechecked
                    //nodeDataRetriever.verifyTypecheckResults

//                oneOf(mockNodeDataRetriever).shouldResourceBeTypechecked(mockChildLink, childNodeUrlWithContext, childNodeArchiveID);
//                    will(returnValue(true));
//                
//                oneOf(mockNodeDataRetriever).getResourceFileChecked(childNodeArchiveID, mockChildLink, childNodeUrl, childNodeUrlWithContext);
//                    will(returnValue(mockTypecheckedResults));
//                
//                oneOf(mockNodeDataRetriever).verifyTypecheckedResults(childNodeUrl, mockChildLink, mockTypecheckedResults);
//                
//                oneOf(mockTypecheckedResults).getCheckedNodeType(); will(returnValue(childNodeType));
//                oneOf(mockTypecheckedResults).getCheckedMimetype(); will(returnValue(childNodeMimetype));

                
                //everything ok?
                
                //copyFileToUnlinkedNodesFolder
                    // workspaceFileHandler.copyFile
//                oneOf(mockWorkspaceFileHandler).copyFile(workspaceID, workspaceUploadDirectory, workspaceUploadDirectory);
                
                //workspaceNodeFactory.getNewWorkspaceNodeFromFile - from destination file
//                oneOf(mockWorkspaceNodeFactory).getNewWorkspaceNodeFromFile(workspaceID, null, null, WorkspaceNodeType.UNKNOWN, null, WorkspaceNodeStatus.NODE_ISCOPY);

                //workspaceDao.addWorkspaceNode
//                oneOf(mockWorkspaceDao).addWorkspaceNode(null);
            }});
        }
        

        
        
        //checklinks???
        //checklinks???
        //checklinks???

        uploader.uploadFiles(workspaceID, fileItems);
    }
}