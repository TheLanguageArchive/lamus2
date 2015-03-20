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
package nl.mpi.lamus.typechecking.implementation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.MetadataValidationException;
import nl.mpi.lamus.typechecking.MetadataChecker;
import nl.mpi.lamus.typechecking.WorkspaceFileValidator;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
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
public class LamusWorkspaceFileValidatorTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock MetadataChecker mockMetadataChecker;
    
    @Mock Workspace mockWorkspace;
    @Mock WorkspaceNode mockNode_1;
    @Mock WorkspaceNode mockNode_2;
    @Mock WorkspaceNode mockNode_3;
    
    private WorkspaceFileValidator workspaceFileValidator;
    
    private final int workspaceID = 10;
    
    public LamusWorkspaceFileValidatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        workspaceFileValidator = new LamusWorkspaceFileValidator(mockWorkspaceDao, mockMetadataChecker);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void validateWorkspaceFiles_Succeeds() throws MalformedURLException, Exception {
        
        final URL nodeUrl_1 = new URL("file:/workspace/" + workspaceID + "/node_1.cmdi");
        final File nodeFile_1 = new File(nodeUrl_1.toString());
        final URL nodeUrl_2 = new URL("file:/workspace/" + workspaceID + "/node_2.cmdi");
        final File nodeFile_2 = new File(nodeUrl_2.toString());
        final URL nodeUrl_3 = new URL("file:/workspace/" + workspaceID + "/node_3.cmdi");
        final File nodeFile_3 = new File(nodeUrl_3.toString());
        
        final Collection<WorkspaceNode> metadataNodesInTree = new ArrayList<>();
        metadataNodesInTree.add(mockNode_1);
        metadataNodesInTree.add(mockNode_2);
        metadataNodesInTree.add(mockNode_3);
        
        final Collection<MetadataValidationIssue> emptyIssues_1 = new ArrayList<>();
        final Collection<MetadataValidationIssue> emptyIssues_2 = new ArrayList<>();
        final Collection<MetadataValidationIssue> emptyIssues_3 = new ArrayList<>();
        
        
        context.checking(new Expectations() {{
            
            allowing(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspaceDao).getMetadataNodesInTreeForWorkspace(workspaceID); will(returnValue(metadataNodesInTree));
            
            //loop
            oneOf(mockNode_1).getWorkspaceURL(); will(returnValue(nodeUrl_1));
            oneOf(mockMetadataChecker).validateSubmittedFile(nodeFile_1); will(returnValue(emptyIssues_1));
            oneOf(mockNode_2).getWorkspaceURL(); will(returnValue(nodeUrl_2));
            oneOf(mockMetadataChecker).validateSubmittedFile(nodeFile_2); will(returnValue(emptyIssues_2));
            oneOf(mockNode_3).getWorkspaceURL(); will(returnValue(nodeUrl_3));
            oneOf(mockMetadataChecker).validateSubmittedFile(nodeFile_3); will(returnValue(emptyIssues_3));
        }});
        
        workspaceFileValidator.validateWorkspaceFiles(mockWorkspace);
    }
    
    @Test
    public void validateWorkspaceFiles_Exception() throws MalformedURLException, Exception {
        
        final URL nodeUrl_1 = new URL("file:/workspace/" + workspaceID + "/node_1.cmdi");
        final File nodeFile_1 = new File(nodeUrl_1.toString());
        final URL nodeUrl_2 = new URL("file:/workspace/" + workspaceID + "/node_2.cmdi");
        final File nodeFile_2 = new File(nodeUrl_2.toString());
        final URL nodeUrl_3 = new URL("file:/workspace/" + workspaceID + "/node_3.cmdi");
        final File nodeFile_3 = new File(nodeUrl_3.toString());
        
        final Collection<WorkspaceNode> metadataNodesInTree = new ArrayList<>();
        metadataNodesInTree.add(mockNode_1);
        metadataNodesInTree.add(mockNode_2);
        metadataNodesInTree.add(mockNode_3);
        
        final Collection<MetadataValidationIssue> emptyIssues_1 = new ArrayList<>();
        final Collection<MetadataValidationIssue> emptyIssues_2 = new ArrayList<>();
        
        final String expectedErrorMessage = "Problems with metadata validation";
        final Exception expectedCause = new Exception("something");
        
        context.checking(new Expectations() {{
            
            allowing(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspaceDao).getMetadataNodesInTreeForWorkspace(workspaceID); will(returnValue(metadataNodesInTree));
            
            //loop
            oneOf(mockNode_1).getWorkspaceURL(); will(returnValue(nodeUrl_1));
            oneOf(mockMetadataChecker).validateSubmittedFile(nodeFile_1); will(returnValue(emptyIssues_1));
            oneOf(mockNode_2).getWorkspaceURL(); will(returnValue(nodeUrl_2));
            oneOf(mockMetadataChecker).validateSubmittedFile(nodeFile_2); will(returnValue(emptyIssues_2));
            oneOf(mockNode_3).getWorkspaceURL(); will(returnValue(nodeUrl_3));
            oneOf(mockMetadataChecker).validateSubmittedFile(nodeFile_3); will(throwException(expectedCause));
        }});
        
        try {
            workspaceFileValidator.validateWorkspaceFiles(mockWorkspace);
            fail("should have thrown an exception");
        } catch(MetadataValidationException ex) {
            assertEquals("Exception message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedCause, ex.getCause());
            assertEquals("Exception workspaceID different from expected", workspaceID, ex.getWorkspaceID());
            assertTrue("List of issues contained in the exception should be empty", ex.getValidationIssues().isEmpty());
        }
    }
    
    @Test
    public void validateWorkspaceFiles_withErrors() throws MalformedURLException, Exception {
        
        final URL nodeUrl_1 = new URL("file:/workspace/" + workspaceID + "/node_1.cmdi");
        final File nodeFile_1 = new File(nodeUrl_1.toString());
        final URL nodeUrl_2 = new URL("file:/workspace/" + workspaceID + "/node_2.cmdi");
        final File nodeFile_2 = new File(nodeUrl_2.toString());
        final URL nodeUrl_3 = new URL("file:/workspace/" + workspaceID + "/node_3.cmdi");
        final File nodeFile_3 = new File(nodeUrl_3.toString());
        
        final Collection<WorkspaceNode> metadataNodesInTree = new ArrayList<>();
        metadataNodesInTree.add(mockNode_1);
        metadataNodesInTree.add(mockNode_2);
        metadataNodesInTree.add(mockNode_3);
        
        final Collection<MetadataValidationIssue> issues_1 = new ArrayList<>();
        final MetadataValidationIssue issue_1 = new MetadataValidationIssue(nodeFile_1, "some assertion test", "something wrong happened", MetadataValidationIssueLevel.ERROR.toString());
        issues_1.add(issue_1);
        final Collection<MetadataValidationIssue> emptyIssues_2 = new ArrayList<>();
        final Collection<MetadataValidationIssue> issues_3 = new ArrayList<>();
        final MetadataValidationIssue issue_3 = new MetadataValidationIssue(nodeFile_3, "another assertion test", "something else went wrong", MetadataValidationIssueLevel.ERROR.toString());
        issues_3.add(issue_3);
        
        final String expectedErrorMessage = "Problems with metadata validation";
        
        context.checking(new Expectations() {{
            
            allowing(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspaceDao).getMetadataNodesInTreeForWorkspace(workspaceID); will(returnValue(metadataNodesInTree));
            
            //loop
            oneOf(mockNode_1).getWorkspaceURL(); will(returnValue(nodeUrl_1));
            oneOf(mockMetadataChecker).validateSubmittedFile(nodeFile_1); will(returnValue(issues_1));
            oneOf(mockNode_2).getWorkspaceURL(); will(returnValue(nodeUrl_2));
            oneOf(mockMetadataChecker).validateSubmittedFile(nodeFile_2); will(returnValue(emptyIssues_2));
            oneOf(mockNode_3).getWorkspaceURL(); will(returnValue(nodeUrl_3));
            oneOf(mockMetadataChecker).validateSubmittedFile(nodeFile_3); will(returnValue(issues_3));
        }});
        
        try {
            workspaceFileValidator.validateWorkspaceFiles(mockWorkspace);
            fail("should have thrown an exception");
        } catch(MetadataValidationException ex) {
            assertTrue("Exception is missing expected issue_1", ex.getValidationIssues().contains(issue_1));
            assertTrue("Exception is missing expected issue_3", ex.getValidationIssues().contains(issue_3));
            assertEquals("Exception message different from expected", expectedErrorMessage, ex.getMessage());
            assertNull("Exception cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void validateWorkspaceFiles_withWarnings() {
        fail("not tested yet");
    }
}