/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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
import java.net.URLDecoder;
import java.util.Collection;
import nl.mpi.lamus.typechecking.MetadataChecker;
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
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class LamusMetadataCheckerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock File mockSchematronFile;
    
    private MetadataChecker metadataChecker;

    
    public LamusMetadataCheckerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        metadataChecker = new LamusMetadataChecker();
        ReflectionTestUtils.setField(metadataChecker, "schematronFile_upload",
                new File(URLDecoder.decode(getClass().getClassLoader().getResource("cmdi_validation/cmdi_schematron_upload.sch").getFile())));
        ReflectionTestUtils.setField(metadataChecker, "schematronFile_submit",
                new File(URLDecoder.decode(getClass().getClassLoader().getResource("cmdi_validation/cmdi_schematron_submit.sch").getFile())));
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void invalidSchematron() throws Exception {
        
        ReflectionTestUtils.setField(metadataChecker, "schematronFile_upload", mockSchematronFile);
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingProfile_allowed.cmdi");
        
        context.checking(new Expectations() {{
            allowing(mockSchematronFile);
        }});
        
        try {
            metadataChecker.validateUploadedFile(fileToCheck);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Invalid Schematron", ex.getMessage());
        }
    }
    
    //validate upload phase
    
    @Test
    public void validateUploadedFile_profileIsNotAllowed() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingProfile_notAllowed.cmdi");
        final String expectedTest = "$allowedProfilesDocument//profile[@id = normalize-space(current()/cmd:Header/cmd:MdProfile)]"
                            + " or $allowedProfilesDocument//profile[@id = tokenize(normalize-space(current()/@xsi:schemaLocation), '/')[last() - 1]]";
        final String expectedMessage = "[CMDI Archive Restriction] the CMD profile of this record is not allowed in the archive.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.ERROR;
        Collection<MetadataValidationIssue> issues = metadataChecker.validateUploadedFile(fileToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateUploadedFile_profileIsAllowed() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingProfile_allowed.cmdi");
        Collection<MetadataValidationIssue> issues = metadataChecker.validateUploadedFile(fileToCheck);
        
        assertTrue("Issues should be empty", issues.isEmpty());
    }
    
    @Test
    public void validateUploadedFile_profileIsAllowed_WithoutMdProfile() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingProfile_allowed_noMdProfileElement.cmdi");
        Collection<MetadataValidationIssue> issues = metadataChecker.validateUploadedFile(fileToCheck);
        
        assertTrue("Issues should be empty", issues.isEmpty());
    }
    
    //validate submit phase
    
    @Test
    public void validateSubmittedFile_profileIsNotAllowed() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingProfile_notAllowed.cmdi");
        final String expectedTest = "$allowedProfilesDocument//profile[@id = normalize-space(current()/cmd:Header/cmd:MdProfile)]"
                            + " or $allowedProfilesDocument//profile[@id = tokenize(normalize-space(current()/@xsi:schemaLocation), '/')[last() - 1]]";
        final String expectedMessage = "[CMDI Archive Restriction] the CMD profile of this record is not allowed in the archive.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.ERROR;
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(fileToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_noResourceProxy() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingReference_noResourceProxy.cmdi");
        final String expectedTest = "count(cmd:ResourceProxy) ge 1";
        final String expectedMessage = "[CMDI Best Practice] There should be at least one /cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.ERROR;
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(fileToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_notAllowedResourceType() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingReference_notAllowedType.cmdi");
        final String expectedTest = "$profileAllowedReferenceTypes/allowedReferenceType[text() = current()/cmd:ResourceType]";
        final String expectedMessage = "[CMDI Profile Restriction] the CMD profile of this record doesn't allow for this resource type.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.ERROR;
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(fileToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_invalidMimeType() throws Exception {
        
        //TODO How to make this work? Metadata files seem to have no mimetype at all.
        
        fail("not tested yet");
    }
    
    @Test
    public void validateSubmittedFile_missingComponentReference() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingComponent_referenceMissing.cmdi");
        final String expectedTest = "($profileName != 'lat-corpus' or contains(/cmd:CMD/cmd:Components/cmd:lat-corpus/@ref, current()/@id))"
                            + " and ($profileName != 'lat-session' or contains(/cmd:CMD/cmd:Components/cmd:lat-session/@ref, current()/@id))";
        final String expectedMessage = "[CMDI Profile Restriction] There should be a '/cmd:CMD/cmd:Components/*/@ref' attribute for each /cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.ERROR;
        final Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(fileToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_missingTitle() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingComponent_titleMissing.cmdi");
        final String expectedTest = "current()/*[normalize-space(cmd:Title) != '']";
        final String expectedMessage = "[CMDI Best Practice] /cmd:CMD/cmd:Components/*/cmd:Title shouldn't be empty.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.WARN;
        final Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(fileToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_missingDescription() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingComponent_descriptionMissing.cmdi");
        final String expectedTest = "current()/*/cmd:descriptions[normalize-space(cmd:Description) != '']";
        final String expectedMessage = "[CMDI Best Practice] /cmd:CMD/cmd:Components/*/cmd:descriptions/cmd:Description shouldn't be empty.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.WARN;
        final Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(fileToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_everythingValid() throws Exception {
        
        File fileToCheck = getResourceFromLocation("cmdi_validation/testing_everythingValid.cmdi");
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(fileToCheck);
        
        assertTrue("Issues should be empty", issues.isEmpty());
    }
    
    
    private File getResourceFromLocation(String location) {
        return new File(URLDecoder.decode(getClass().getClassLoader().getResource(location).getFile()));
    }
   
    private void assertAtLeastOneIssue(Collection<MetadataValidationIssue> issues, File fileToCheck, String expectedTest, String expectedMessage, MetadataValidationIssueLevel expectedLevel) {
        assertFalse("Issues collection should not be empty", issues.isEmpty());
        
        MetadataValidationIssue found = null;
        for(MetadataValidationIssue issue : issues) {
            if(expectedTest.equals(issue.getAssertionTest())) {
                found = issue;
                break;
            }
        }
        
        assertNotNull("Expected issue not found", found);
        
        assertEquals("File different from expected", fileToCheck, found.getMetadataFile());
        assertEquals("Message different from expected", expectedMessage, found.getAssertionErrorMessage());
        assertEquals("Level different from expected", expectedLevel, found.getAssertionErrorLevel());
    }
}