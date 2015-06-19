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
package nl.mpi.lamus.cmdi.profile;

import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author guisil
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
public class AllowedCmdiProfilesTest {
    
    @Configuration
    @Profile("testing")
    static class AllowedCmdiProfilesConfig {
        
        @Bean
        public AllowedCmdiProfiles allowedProfiles() throws JAXBException {
            
            JAXBContext jaxbContext = JAXBContext.newInstance(AllowedCmdiProfiles.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            
            File allowedProfiles_testFile = new File(URLDecoder.decode(getClass().getClassLoader().getResource("cmdi_validation/cmdi_allowed_profiles.xml").getFile()));
            
            AllowedCmdiProfiles profiles = (AllowedCmdiProfiles) jaxbUnmarshaller.unmarshal(allowedProfiles_testFile);
            return profiles;
        }
    }
    
    @Autowired
    private AllowedCmdiProfiles allowedCmdiProfiles;
    
    
    public AllowedCmdiProfilesTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void beanProperlyLoadedFromXmlFile() {
        
        assertNotNull("Bean should not be null", allowedCmdiProfiles);
        
        List<CmdiProfile> profiles = allowedCmdiProfiles.getProfiles();
        assertNotNull("List of profiles should not be null", profiles);
        assertFalse("List of profiles should not be empty", profiles.isEmpty());
        
        //test case - collection
        boolean collectionFound = false;
        boolean corpusFound = false;
        boolean sessionFound = false;
        for(CmdiProfile profile : profiles) { // check if some profiles are properly loaded
            if("clarin.eu:cr1:p_1345561703620".equals(profile.getId())) { //collection
                collectionFound = true;
                assertEquals("Collection profile name different from expected", "collection", profile.getName());
                assertEquals("Collection profile location different from expected", URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1345561703620"), profile.getLocation());
                assertNotNull("Collection allowed reference types list should not be null", profile.getAllowedReferenceTypes());
                assertFalse("Collection allowed reference types list should not be empty", profile.getAllowedReferenceTypes().isEmpty());
                assertTrue("Collection should only have four allowed reference types", profile.getAllowedReferenceTypes().size() == 4);
                assertEquals("Collection should only allow the Metadata reference type", "Metadata", profile.getAllowedReferenceTypes().iterator().next());
                assertNull("Collection component reference map should be null", profile.getComponentMap());
            }
            
            if("clarin.eu:cr1:p_1407745712064".equals(profile.getId())) { //lat-corpus
                corpusFound = true;
                
                assertEquals("Lat-Corpus profile name different from expected", "lat-corpus", profile.getName());
                assertEquals("Lat-Corpus profile location different from expected", URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1407745712064"), profile.getLocation());
                assertNotNull("Lat-Corpus allowed reference types list should not be null", profile.getAllowedReferenceTypes());
                assertFalse("Lat-Corpus allowed reference types list should not be empty", profile.getAllowedReferenceTypes().isEmpty());
                assertTrue("Lat-Corpus should have five allowed reference types", profile.getAllowedReferenceTypes().size() == 5);
                assertTrue("Lat-Corpus should allow Metadata and Resource reference types", profile.getAllowedReferenceTypes().contains("Metadata") && profile.getAllowedReferenceTypes().contains("Resource"));
                assertNotNull("Lat-Corpus component reference map should not be null", profile.getComponentMap());
                assertFalse("Lat-Corpus component reference map should not be empty", profile.getComponentMap().isEmpty());
                assertTrue("Lat-Corpus component reference map should contain two entries", profile.getComponentMap().size() == 2);
                Set<Entry<String, String>> entrySet = profile.getComponentMap().entrySet();
                boolean cmdiMatched = false;
                boolean otherMatched = false;
                for(Entry<String, String> entry : entrySet) {
                    if(Pattern.matches(entry.getKey(), "text/x-cmdi+xml")) {
                        cmdiMatched = true;
                        assertEquals("Lat-Corpus component reference map entry value different from expected (cmdi)", entry.getValue(), "lat-corpus/CorpusLink");
                    }
                    if(Pattern.matches(entry.getKey(), "audio/wave")) {
                        otherMatched = true;
                        assertEquals("Lat-Corpus component reference map entry value different from expected (other)", entry.getValue(), "lat-corpus/InfoLink");
                    }
                }
                assertTrue("Lat-Corpus component reference map entry for CMDI not found", cmdiMatched);
                assertTrue("Lat-Corpus component reference map entry for other types not found", otherMatched);
            }
            
            if("clarin.eu:cr1:p_1407745712035".equals(profile.getId())) { //lat-session
                sessionFound = true;
                
                assertEquals("Lat-Session profile name different from expected", "lat-session", profile.getName());
                assertEquals("Lat-Session profile location different from expected", URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1407745712035"), profile.getLocation());
                assertNotNull("Lat-Session allowed reference types list should not be null", profile.getAllowedReferenceTypes());
                assertFalse("Lat-Session allowed reference types list should not be empty", profile.getAllowedReferenceTypes().isEmpty());
                assertTrue("Lat-Session should only have four allowed reference types", profile.getAllowedReferenceTypes().size() == 4);
                assertEquals("Lat-Session should only allow the Metadata reference type", "Resource", profile.getAllowedReferenceTypes().iterator().next());
                assertNotNull("Lat-Session component reference map should not be null", profile.getComponentMap());
                assertFalse("Lat-Session component reference map should not be empty", profile.getComponentMap().isEmpty());
                assertTrue("Lat-Session component reference map should contain two entries", profile.getComponentMap().size() == 2);
                Set<Entry<String, String>> entrySet = profile.getComponentMap().entrySet();
                boolean cmdiMatched = false;
                boolean mediaMatched = false;
                boolean writtenMatched = false;
                for(Entry<String, String> entry : entrySet) {
                    if(Pattern.matches(entry.getKey(), "text/x-cmdi+xml")) {
                        cmdiMatched = true;
                    }
                    if(Pattern.matches(entry.getKey(), "audio/wave")) {
                        mediaMatched = true;
                        assertEquals("Lat-Session component reference map entry value different from expected (media)", entry.getValue(), "lat-session/Resources/MediaFile");
                    }
                    if(Pattern.matches(entry.getKey(), "text/plain")) {
                        writtenMatched = true;
                        assertEquals("Lat-Session component reference map entry value different from expected (written)", entry.getValue(), "lat-session/Resources/WrittenResource");
                    }
                }
                assertFalse("Lat-Session component reference map entry for CMDI should not exist", cmdiMatched);
                assertTrue("Lat-Session component reference map entry for media resources not found", mediaMatched);
                assertTrue("Lat-Session component reference map entry for written resources not found", writtenMatched);
            }
        }
        
        assertTrue("Collection profile not found", collectionFound);
        assertTrue("Lat-Corpus profile not found", corpusFound);
        assertTrue("Lat-Session profile not found", sessionFound);
    }
    
    @Test
    public void getProfile() {
        
        final String profileToCheck = "clarin.eu:cr1:p_1407745712035";
        final URI expectedProfileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1407745712035");
        final String expectedProfileName = "lat-session";
        final String expectedTranslateType = "session";
        
        CmdiProfile retrievedProfile = allowedCmdiProfiles.getProfile(profileToCheck);
        
        assertNotNull("Retrieved profile should not be null", retrievedProfile);
        assertEquals("Profile location different from expected", expectedProfileLocation, retrievedProfile.getLocation());
        assertEquals("Profile name different from expected", expectedProfileName, retrievedProfile.getName());
        assertEquals("Profile translate type different from expected", expectedTranslateType, retrievedProfile.getTranslateType());
    }
    
    @Test
    public void getProfileNull() {
        
        final String profileToCheck = "clarin.eu:cr1:p_5555555555555";
        
        CmdiProfile retrievedProfile = allowedCmdiProfiles.getProfile(profileToCheck);
        
        assertNull("Retrieved profile should be null", retrievedProfile);
    }
}
