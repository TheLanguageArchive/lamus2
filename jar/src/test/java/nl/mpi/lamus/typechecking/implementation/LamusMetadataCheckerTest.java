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
import java.util.ArrayList;
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
    
    @Test
    public void validateUploadedFile_validMimeType() throws Exception {
        
        final File fileToCheck1 = getResourceFromLocation("cmdi_validation/testingReference_Metadata_validMimetype.cmdi");
        final File fileToCheck2 = getResourceFromLocation("cmdi_validation/testingReference_Resource_validMimetype.cmdi");
        
        Collection<MetadataValidationIssue> issues1 = metadataChecker.validateUploadedFile(fileToCheck1);
        Collection<MetadataValidationIssue> issues2 = metadataChecker.validateUploadedFile(fileToCheck2);
        
        assertTrue("Issues should be empty (1)", issues1.isEmpty());
        assertTrue("Issues should be empty (2)", issues2.isEmpty());
    }
    
    @Test
    public void validateUploadedFile_missingMimeType() throws Exception {
        
        final File fileToCheck1 = getResourceFromLocation("cmdi_validation/testingReference_Metadata_missingMimetype.cmdi");
        final File fileToCheck2 = getResourceFromLocation("cmdi_validation/testingReference_Resource_missingMimetype.cmdi");
        final String expectedTest = "current()/cmd:ResourceType/@mimetype";
        final String expectedMessage = "[CMDI Best Practice] Mimetype not present in ResourceProxy.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.WARN;
        
        Collection<MetadataValidationIssue> issues1 = metadataChecker.validateUploadedFile(fileToCheck1);
        Collection<MetadataValidationIssue> issues2 = metadataChecker.validateUploadedFile(fileToCheck2);
        
        assertAtLeastOneIssue(issues1, fileToCheck1, expectedTest, expectedMessage, expectedLevel);
        assertAtLeastOneIssue(issues2, fileToCheck2, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateUploadedFile_invalidMimeType() throws Exception {
        
        final File fileToCheck1 = getResourceFromLocation("cmdi_validation/testingReference_Metadata_invalidMimetype.cmdi");
        final File fileToCheck2 = getResourceFromLocation("cmdi_validation/testingReference_Resource_invalidMimetype.cmdi");
        final String expectedTest = "(current()/cmd:ResourceType[not(@mimetype)])"
                + " or (current()/cmd:ResourceType != 'Metadata' and current()/cmd:ResourceType != 'Resource')"
                + " or (current()/cmd:ResourceType = 'Metadata' and current()/cmd:ResourceType/@mimetype = 'text/x-cmdi+xml')"
                + " or (current()/cmd:ResourceType = 'Resource' and current()/cmd:ResourceType/@mimetype != 'text/x-cmdi+xml')";
        final String expectedMessage = "[CMDI Invalid reference] Mimetype not consistent with ResourceProxy type.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.ERROR;
        
        Collection<MetadataValidationIssue> issues1 = metadataChecker.validateUploadedFile(fileToCheck1);
        Collection<MetadataValidationIssue> issues2 = metadataChecker.validateUploadedFile(fileToCheck2);
        
        assertAtLeastOneIssue(issues1, fileToCheck1, expectedTest, expectedMessage, expectedLevel);
        assertAtLeastOneIssue(issues2, fileToCheck2, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateUploadedFile_otherResourceTypes() throws Exception {
        
        final File fileToCheck1 = getResourceFromLocation("cmdi_validation/testingReference_LandingPage.cmdi");
        final File fileToCheck2 = getResourceFromLocation("cmdi_validation/testingReference_SearchPage.cmdi");
        final File fileToCheck3 = getResourceFromLocation("cmdi_validation/testingReference_SearchService.cmdi");
        
        Collection<MetadataValidationIssue> issues1 = metadataChecker.validateUploadedFile(fileToCheck1);
        Collection<MetadataValidationIssue> issues2 = metadataChecker.validateUploadedFile(fileToCheck2);
        Collection<MetadataValidationIssue> issues3 = metadataChecker.validateUploadedFile(fileToCheck3);
        
        assertTrue("Issues should be empty (1)", issues1.isEmpty());
        assertTrue("Issues should be empty (2)", issues2.isEmpty());
        assertTrue("Issues should be empty (3)", issues3.isEmpty());
    }
    
    @Test
    public void validateUploadedFile_withInfoLink() throws Exception {
        
        final File fileToCheck1 = getResourceFromLocation("cmdi_validation/testingInfoLinks_Corpus.cmdi");
        final File fileToCheck2 = getResourceFromLocation("cmdi_validation/testingInfoLinks_Session.cmdi");
        
        Collection<MetadataValidationIssue> issues1 = metadataChecker.validateUploadedFile(fileToCheck1);
        Collection<MetadataValidationIssue> issues2 = metadataChecker.validateUploadedFile(fileToCheck2);
        
        assertTrue("Issues should be empty (1)", issues1.isEmpty());
        assertTrue("Issues should be empty (2)", issues2.isEmpty());
    }
    
    @Test
    public void validateUploadedCorpusFile_withResourceLink() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingResourceLinks_Corpus.cmdi");
        
        final String expectedTest = "not($profileName)"
                + " or ($profileName != 'lat-corpus' or (current()/cmd:ResourceType != 'Resource' or /cmd:CMD/cmd:Components/cmd:lat-corpus/cmd:InfoLink[@ref = current()/@id] ))";
        final String expectedMessage = "[CMDI Profile Restriction] 'lat-corpus' doesn't allow referencing to resources, unless they're info links.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.ERROR;
        Collection<MetadataValidationIssue> issues = metadataChecker.validateUploadedFile(fileToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    //validate submit phase
    
    @Test
    public void validateSubmittedFile_profileIsNotAllowed() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingProfile_notAllowed.cmdi");
        final Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(fileToCheck);
        
        final String expectedTest = "$allowedProfilesDocument//profile[@id = normalize-space(current()/cmd:Header/cmd:MdProfile)]"
                            + " or $allowedProfilesDocument//profile[@id = tokenize(normalize-space(current()/@xsi:schemaLocation), '/')[last() - 1]]";
        final String expectedMessage = "[CMDI Archive Restriction] the CMD profile of this record is not allowed in the archive.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.ERROR;
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(filesToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_noResourceProxy() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingReference_noResourceProxy.cmdi");
        final Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(fileToCheck);
        
        final String expectedTest = "count(cmd:ResourceProxy) ge 1";
        final String expectedMessage = "[CMDI Best Practice] There should be at least one /cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.ERROR;
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(filesToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_notAllowedResourceType() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingReference_notAllowedType.cmdi");
        final Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(fileToCheck);
        
        final String expectedTest = "not($profileName) or $profileAllowedReferenceTypes/allowedReferenceType[text() = current()/cmd:ResourceType]";
        final String expectedMessage = "[CMDI Profile Restriction] the CMD profile of this record doesn't allow for this resource type.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.ERROR;
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(filesToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_validMimeType() throws Exception {
        
        final File fileToCheck1 = getResourceFromLocation("cmdi_validation/testingReference_Metadata_validMimetype.cmdi");
        final File fileToCheck2 = getResourceFromLocation("cmdi_validation/testingReference_Resource_validMimetype.cmdi");
        final Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(fileToCheck1);
        filesToCheck.add(fileToCheck2);
        
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(filesToCheck);
        
        assertTrue("Issues should be empty (1)", issues.isEmpty());
    }
    
    @Test
    public void validateSubmittedFile_missingMimeType() throws Exception {
        
        final File fileToCheck1 = getResourceFromLocation("cmdi_validation/testingReference_Metadata_missingMimetype.cmdi");
        final File fileToCheck2 = getResourceFromLocation("cmdi_validation/testingReference_Resource_missingMimetype.cmdi");
        final Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(fileToCheck1);
        filesToCheck.add(fileToCheck2);
        
        final String expectedTest = "current()/cmd:ResourceType/@mimetype";
        final String expectedMessage = "[CMDI Best Practice] Mimetype not present in ResourceProxy.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.WARN;
        
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(filesToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck1, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_invalidMimeType() throws Exception {
        
        final File fileToCheck1 = getResourceFromLocation("cmdi_validation/testingReference_Metadata_invalidMimetype.cmdi");
        final File fileToCheck2 = getResourceFromLocation("cmdi_validation/testingReference_Resource_invalidMimetype.cmdi");
        final Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(fileToCheck1);
        filesToCheck.add(fileToCheck2);
        
        final String expectedTest = "(current()/cmd:ResourceType[not(@mimetype)])"
                + " or (current()/cmd:ResourceType != 'Metadata' and current()/cmd:ResourceType != 'Resource')"
                + " or (current()/cmd:ResourceType = 'Metadata' and current()/cmd:ResourceType/@mimetype = 'text/x-cmdi+xml')"
                + " or (current()/cmd:ResourceType = 'Resource' and current()/cmd:ResourceType/@mimetype != 'text/x-cmdi+xml')";
        final String expectedMessage = "[CMDI Invalid reference] Mimetype not consistent with ResourceProxy type.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.ERROR;
        
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(filesToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck1, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_otherResourceTypes() throws Exception {
        
        final File fileToCheck1 = getResourceFromLocation("cmdi_validation/testingReference_LandingPage.cmdi");
        final File fileToCheck2 = getResourceFromLocation("cmdi_validation/testingReference_SearchPage.cmdi");
        final File fileToCheck3 = getResourceFromLocation("cmdi_validation/testingReference_SearchService.cmdi");
        final Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(fileToCheck1);
        filesToCheck.add(fileToCheck2);
        filesToCheck.add(fileToCheck3);
        
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(filesToCheck);
        
        assertTrue("Issues should be empty (1)", issues.isEmpty());
    }
    
    @Test
    public void validateSubmittedFile_missingComponentReference() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingComponent_referenceMissing.cmdi");
        final Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(fileToCheck);
        
        final String expectedTest = "not($profileName)"
                + " or (current()/cmd:ResourceType != 'Metadata' and current()/cmd:ResourceType != 'Resource')"
                + " or ($profileName != 'lat-corpus' or /cmd:CMD/cmd:Components/cmd:lat-corpus/*[@ref = current()/@id])"
                + " and ($profileName != 'lat-session' or /cmd:CMD/cmd:Components/cmd:lat-session/cmd:Resources/*[@ref = current()/@id] or /cmd:CMD/cmd:Components/cmd:lat-session/*[@ref = current()/@id])";
        final String expectedMessage = "[CMDI Profile Restriction] There should be a 'ref' attribute for each resource proxy ('/cmd:CMD/cmd:Components/cmd:lat-corpus/*/@ref' for 'lat-corpus' and '/cmd:CMD/cmd:Components/cmd:lat-session/cmd:Resources/*/@ref' for 'lat-session'.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.ERROR;
        final Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(filesToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_missingTitle() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingComponent_titleMissing.cmdi");
        final Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(fileToCheck);
        
        final String expectedTest = "current()/*[normalize-space(cmd:Title) != '']";
        final String expectedMessage = "[CMDI Best Practice] /cmd:CMD/cmd:Components/*/cmd:Title shouldn't be empty.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.WARN;
        final Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(filesToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_missingDescription() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingComponent_descriptionMissing.cmdi");
        final Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(fileToCheck);
        
        final String expectedTest = "current()/*/cmd:descriptions[normalize-space(cmd:Description) != '']";
        final String expectedMessage = "[CMDI Best Practice] /cmd:CMD/cmd:Components/*/cmd:descriptions/cmd:Description shouldn't be empty.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.WARN;
        final Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(filesToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_withInfoLink() throws Exception {
        
        final File fileToCheck1 = getResourceFromLocation("cmdi_validation/testingInfoLinks_Corpus.cmdi");
        final File fileToCheck2 = getResourceFromLocation("cmdi_validation/testingInfoLinks_Session.cmdi");
        final Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(fileToCheck1);
        filesToCheck.add(fileToCheck2);
        
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(filesToCheck);
        
        assertTrue("Issues should be empty (1)", issues.isEmpty());
    }
    
    @Test
    public void validateSubmittedCorpusFile_withResourceLink() throws Exception {
        
        final File fileToCheck = getResourceFromLocation("cmdi_validation/testingResourceLinks_Corpus.cmdi");
        final Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(fileToCheck);
        
        final String expectedTest = "not($profileName)"
                + " or ($profileName != 'lat-corpus' or (current()/cmd:ResourceType != 'Resource' or /cmd:CMD/cmd:Components/cmd:lat-corpus/cmd:InfoLink[@ref = current()/@id] ))";
        final String expectedMessage = "[CMDI Profile Restriction] 'lat-corpus' doesn't allow referencing to resources, unless they're info links.";
        final MetadataValidationIssueLevel expectedLevel = MetadataValidationIssueLevel.ERROR;
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(filesToCheck);
        
        assertAtLeastOneIssue(issues, fileToCheck, expectedTest, expectedMessage, expectedLevel);
    }
    
    @Test
    public void validateSubmittedFile_everythingValid() throws Exception {
        
        File fileToCheck = getResourceFromLocation("cmdi_validation/testing_everythingValid.cmdi");
        final Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(fileToCheck);
        
        Collection<MetadataValidationIssue> issues = metadataChecker.validateSubmittedFile(filesToCheck);
        
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