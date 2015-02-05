/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.importing.implementation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.importing.OrphanNodesImportHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
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

/**
 *
 * @author guisil
 */
public class LamusOrphanNodesImportHandlerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock WorkspaceUploader mockWorkspaceUploader;
    
    @Mock Workspace mockWorkspace;
    @Mock Collection<File> mockFiles;
    @Mock File mockSomeFile;
    
    private OrphanNodesImportHandler orphanNodesImportHandler;
    
    
    public LamusOrphanNodesImportHandlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        orphanNodesImportHandler = new LamusOrphanNodesImportHandler(mockWorkspaceFileHandler, mockWorkspaceUploader);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void exploreOrphanNodes_NoProblems() throws IOException, TypeCheckerException, WorkspaceException {
        
        final int workspaceID = 1;
        
        final Collection<ImportProblem> problems = new ArrayList<>();
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceFileHandler).getFilesInOrphanDirectory(mockWorkspace); will(returnValue(mockFiles));
            ignoring(mockFiles);
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspaceUploader).processUploadedFiles(workspaceID, mockFiles); will(returnValue(problems));
        }});
        
        Collection<ImportProblem> retrievedProblems = orphanNodesImportHandler.exploreOrphanNodes(mockWorkspace);
        
        assertEquals("Retrieved list of problems different from expected", problems, retrievedProblems);
    }
    
    @Test
    public void exploreOrphanNodes_OneProblem() throws IOException, TypeCheckerException, WorkspaceException {
        
        final int workspaceID = 1;
        
        final Collection<ImportProblem> problems = new ArrayList<>();
        problems.add(new FileImportProblem(mockSomeFile, "some error message", null));
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceFileHandler).getFilesInOrphanDirectory(mockWorkspace); will(returnValue(mockFiles));
            ignoring(mockFiles);
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspaceUploader).processUploadedFiles(workspaceID, mockFiles); will(returnValue(problems));
        }});
        
        Collection<ImportProblem> retrievedProblems = orphanNodesImportHandler.exploreOrphanNodes(mockWorkspace);
        
        assertEquals("Retrieved list of problems different from expected", problems, retrievedProblems);
    }
    
    @Test
    public void exploreOrphanNode_throwsException() throws WorkspaceException {
        
        final int workspaceID = 1;
        
        final WorkspaceException exceptionToThrow = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceFileHandler).getFilesInOrphanDirectory(mockWorkspace); will(returnValue(mockFiles));
            ignoring(mockFiles);
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspaceUploader).processUploadedFiles(workspaceID, mockFiles); will(throwException(exceptionToThrow));
        }});
        
        try {
            orphanNodesImportHandler.exploreOrphanNodes(mockWorkspace);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", exceptionToThrow, ex);
        }
    }
}