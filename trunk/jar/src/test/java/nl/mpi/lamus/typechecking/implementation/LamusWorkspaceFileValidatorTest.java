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
    public void validateMetadataFilesInWorkspace_Succeeds() throws MalformedURLException, Exception {
        
        final Collection<File> filesToValidate = new ArrayList<>();
        final URL nodeUrl_1 = new URL("file:/workspace/" + workspaceID + "/node_1.cmdi");
        final File nodeFile_1 = new File(nodeUrl_1.getPath());
        filesToValidate.add(nodeFile_1);
        final URL nodeUrl_2 = new URL("file:/workspace/" + workspaceID + "/node_2.cmdi");
        final File nodeFile_2 = new File(nodeUrl_2.getPath());
        filesToValidate.add(nodeFile_2);
        final URL nodeUrl_3 = new URL("file:/workspace/" + workspaceID + "/node_3.cmdi");
        final File nodeFile_3 = new File(nodeUrl_3.getPath());
        filesToValidate.add(nodeFile_3);
        
        final Collection<WorkspaceNode> metadataNodesInTree = new ArrayList<>();
        metadataNodesInTree.add(mockNode_1);
        metadataNodesInTree.add(mockNode_2);
        metadataNodesInTree.add(mockNode_3);
        
        final Collection<MetadataValidationIssue> emptyIssues = new ArrayList<>();
        
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getMetadataNodesInTreeForWorkspace(workspaceID); will(returnValue(metadataNodesInTree));
            
            //loop
            oneOf(mockNode_1).getWorkspaceURL(); will(returnValue(nodeUrl_1));
            oneOf(mockNode_2).getWorkspaceURL(); will(returnValue(nodeUrl_2));
            oneOf(mockNode_3).getWorkspaceURL(); will(returnValue(nodeUrl_3));
            oneOf(mockMetadataChecker).validateSubmittedFile(filesToValidate); will(returnValue(emptyIssues));
        }});
        
        workspaceFileValidator.validateMetadataFilesInWorkspace(workspaceID);
    }
    
    @Test
    public void validateMetadataFilesInWorkspace_Exception() throws MalformedURLException, Exception {
        
        final Collection<File> filesToValidate = new ArrayList<>();
        final URL nodeUrl_1 = new URL("file:/workspace/" + workspaceID + "/node_1.cmdi");
        final File nodeFile_1 = new File(nodeUrl_1.getPath());
        filesToValidate.add(nodeFile_1);
        final URL nodeUrl_2 = new URL("file:/workspace/" + workspaceID + "/node_2.cmdi");
        final File nodeFile_2 = new File(nodeUrl_2.getPath());
        filesToValidate.add(nodeFile_2);
        final URL nodeUrl_3 = new URL("file:/workspace/" + workspaceID + "/node_3.cmdi");
        final File nodeFile_3 = new File(nodeUrl_3.getPath());
        filesToValidate.add(nodeFile_3);
        
        final Collection<WorkspaceNode> metadataNodesInTree = new ArrayList<>();
        metadataNodesInTree.add(mockNode_1);
        metadataNodesInTree.add(mockNode_2);
        metadataNodesInTree.add(mockNode_3);
        
        final Collection<MetadataValidationIssue> emptyIssues = new ArrayList<>();
        
        final String expectedErrorMessage = "Problems with schematron metadata validation";
        final Exception expectedCause = new Exception("something");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getMetadataNodesInTreeForWorkspace(workspaceID); will(returnValue(metadataNodesInTree));
            
            //loop
            oneOf(mockNode_1).getWorkspaceURL(); will(returnValue(nodeUrl_1));
            oneOf(mockNode_2).getWorkspaceURL(); will(returnValue(nodeUrl_2));
            oneOf(mockNode_3).getWorkspaceURL(); will(returnValue(nodeUrl_3));
            oneOf(mockMetadataChecker).validateSubmittedFile(filesToValidate); will(throwException(expectedCause));
        }});
        
        try {
            workspaceFileValidator.validateMetadataFilesInWorkspace(workspaceID);
            fail("should have thrown an exception");
        } catch(MetadataValidationException ex) {
            assertEquals("Exception message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedCause, ex.getCause());
            assertEquals("Exception workspaceID different from expected", workspaceID, ex.getWorkspaceID());
            assertTrue("List of issues contained in the exception should be empty", ex.getValidationIssues().isEmpty());
        }
    }
    
    @Test
    public void validateMetadataFilesInWorkspace_withIssues() throws MalformedURLException, Exception {
        
        final Collection<File> filesToValidate = new ArrayList<>();
        final URL nodeUrl_1 = new URL("file:/workspace/" + workspaceID + "/node_1.cmdi");
        final File nodeFile_1 = new File(nodeUrl_1.getPath());
        filesToValidate.add(nodeFile_1);
        final URL nodeUrl_2 = new URL("file:/workspace/" + workspaceID + "/node_2.cmdi");
        final File nodeFile_2 = new File(nodeUrl_2.getPath());
        filesToValidate.add(nodeFile_2);
        final URL nodeUrl_3 = new URL("file:/workspace/" + workspaceID + "/node_3.cmdi");
        final File nodeFile_3 = new File(nodeUrl_3.getPath());
        filesToValidate.add(nodeFile_3);
        
        final Collection<WorkspaceNode> metadataNodesInTree = new ArrayList<>();
        metadataNodesInTree.add(mockNode_1);
        metadataNodesInTree.add(mockNode_2);
        metadataNodesInTree.add(mockNode_3);
        
        final Collection<MetadataValidationIssue> issues = new ArrayList<>();
        final MetadataValidationIssue issue_1 = new MetadataValidationIssue(nodeFile_1, "some assertion test", "something wrong happened", MetadataValidationIssueLevel.ERROR.toString());
        issues.add(issue_1);
        final MetadataValidationIssue issue_3 = new MetadataValidationIssue(nodeFile_3, "another assertion test", "something else went wrong", MetadataValidationIssueLevel.ERROR.toString());
        issues.add(issue_3);
        
        final String expectedErrorMessage = "Problems with schematron metadata validation";
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getMetadataNodesInTreeForWorkspace(workspaceID); will(returnValue(metadataNodesInTree));
            
            //loop
            oneOf(mockNode_1).getWorkspaceURL(); will(returnValue(nodeUrl_1));
            oneOf(mockNode_2).getWorkspaceURL(); will(returnValue(nodeUrl_2));
            oneOf(mockNode_3).getWorkspaceURL(); will(returnValue(nodeUrl_3));
            oneOf(mockMetadataChecker).validateSubmittedFile(filesToValidate); will(returnValue(issues));
        }});
        
        try {
            workspaceFileValidator.validateMetadataFilesInWorkspace(workspaceID);
            fail("should have thrown an exception");
        } catch(MetadataValidationException ex) {
            assertTrue("Exception is missing expected issue_1", ex.getValidationIssues().contains(issue_1));
            assertTrue("Exception is missing expected issue_3", ex.getValidationIssues().contains(issue_3));
            assertEquals("Exception message different from expected", expectedErrorMessage, ex.getMessage());
            assertNull("Exception cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void validateMetadataFile_Succeeds() throws MalformedURLException, Exception {
        
        final URL nodeUrl_1 = new URL("file:/workspace/" + workspaceID + "/node_1.cmdi");
        final File nodeFile_1 = new File(nodeUrl_1.getPath());
        
        final Collection<MetadataValidationIssue> emptyIssues_1 = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataChecker).validateUploadedFile(nodeFile_1); will(returnValue(emptyIssues_1));
        }});
        
        workspaceFileValidator.validateMetadataFile(workspaceID, nodeFile_1);
    }
    
    @Test
    public void validateMetadataFile_Exception() throws MalformedURLException, Exception {
        
        final URL nodeUrl_1 = new URL("file:/workspace/" + workspaceID + "/node_1.cmdi");
        final File nodeFile_1 = new File(nodeUrl_1.getPath());
        
        final String expectedErrorMessage = "Problems with schematron metadata validation";
        final Exception expectedCause = new Exception("something");
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataChecker).validateUploadedFile(nodeFile_1); will(throwException(expectedCause));
        }});
        
        try {
            workspaceFileValidator.validateMetadataFile(workspaceID, nodeFile_1);
            fail("should have thrown an exception");
        } catch(MetadataValidationException ex) {
            assertEquals("Exception message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedCause, ex.getCause());
            assertEquals("Exception workspaceID different from expected", workspaceID, ex.getWorkspaceID());
            assertTrue("List of issues contained in the exception should be empty", ex.getValidationIssues().isEmpty());
        }
    }
    
    @Test
    public void validateMetadataFile_withIssues() throws MalformedURLException, Exception {
        
        final URL nodeUrl_1 = new URL("file:/workspace/" + workspaceID + "/node_1.cmdi");
        final File nodeFile_1 = new File(nodeUrl_1.getPath());
        
        final Collection<MetadataValidationIssue> issues_1 = new ArrayList<>();
        final MetadataValidationIssue issue_1 = new MetadataValidationIssue(nodeFile_1, "some assertion test", "something wrong happened", MetadataValidationIssueLevel.ERROR.toString());
        issues_1.add(issue_1);
        
        final String expectedErrorMessage = "Problems with schematron metadata validation";
        
        context.checking(new Expectations() {{

            oneOf(mockMetadataChecker).validateUploadedFile(nodeFile_1); will(returnValue(issues_1));
        }});
        
        try {
            workspaceFileValidator.validateMetadataFile(workspaceID, nodeFile_1);
            fail("should have thrown an exception");
        } catch(MetadataValidationException ex) {
            assertTrue("Exception is missing expected issue_1", ex.getValidationIssues().contains(issue_1));
            assertEquals("Exception message different from expected", expectedErrorMessage, ex.getMessage());
            assertNull("Exception cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void validationIssuesContainErrors() throws MalformedURLException {
        
        final URL nodeUrl_1 = new URL("file:/workspace/" + workspaceID + "/node_1.cmdi");
        final File nodeFile_1 = new File(nodeUrl_1.getPath());
        
        final Collection<MetadataValidationIssue> issues = new ArrayList<>();
        final MetadataValidationIssue issue_1 = new MetadataValidationIssue(nodeFile_1, "some assertion test", "something wrong happened", MetadataValidationIssueLevel.ERROR.toString());
        issues.add(issue_1);
        final MetadataValidationIssue issue_2 = new MetadataValidationIssue(nodeFile_1, "some assertion test", "something wrong happened", MetadataValidationIssueLevel.WARN.toString());
        issues.add(issue_2);
        
        boolean result = workspaceFileValidator.validationIssuesContainErrors(issues);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void validationIssuesContainNoErrors() throws MalformedURLException {
        
        final URL nodeUrl_1 = new URL("file:/workspace/" + workspaceID + "/node_1.cmdi");
        final File nodeFile_1 = new File(nodeUrl_1.getPath());
        
        final Collection<MetadataValidationIssue> issues = new ArrayList<>();
        final MetadataValidationIssue issue_1 = new MetadataValidationIssue(nodeFile_1, "some assertion test", "something wrong happened", MetadataValidationIssueLevel.WARN.toString());
        issues.add(issue_1);
        final MetadataValidationIssue issue_2 = new MetadataValidationIssue(nodeFile_1, "some assertion test", "something wrong happened", MetadataValidationIssueLevel.WARN.toString());
        issues.add(issue_2);
        
        boolean result = workspaceFileValidator.validationIssuesContainErrors(issues);
        
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void validationIssuesEmpty() throws MalformedURLException {
        
        final Collection<MetadataValidationIssue> issues = new ArrayList<>();
        
        boolean result = workspaceFileValidator.validationIssuesContainErrors(issues);
        
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void validationIssuesToString() throws MalformedURLException {
        
        final URL nodeUrl_1 = new URL("file:/workspace/" + workspaceID + "/node_1.cmdi");
        final File nodeFile_1 = new File(nodeUrl_1.getPath());
        
        final Collection<MetadataValidationIssue> issues = new ArrayList<>();
        final MetadataValidationIssue issue_1 = new MetadataValidationIssue(nodeFile_1, "some assertion test 1", "something wrong happened 1", MetadataValidationIssueLevel.ERROR.toString());
        issues.add(issue_1);
        final MetadataValidationIssue issue_2 = new MetadataValidationIssue(nodeFile_1, "some assertion test 2", "something wrong happened 2", MetadataValidationIssueLevel.WARN.toString());
        issues.add(issue_2);
        
        final String expectedResult = "Validation issue for file 'node_1.cmdi' - ERROR: something wrong happened 1.\nValidation issue for file 'node_1.cmdi' - WARN: something wrong happened 2.\n";
        
        String result = workspaceFileValidator.validationIssuesToString(issues);
        
        assertEquals("Result different from expected", expectedResult, result);
    }
}