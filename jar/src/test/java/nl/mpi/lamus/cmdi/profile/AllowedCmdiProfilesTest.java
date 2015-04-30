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
        for(CmdiProfile profile : profiles) {
            if("clarin.eu:cr1:p_1345561703620".equals(profile.getId())) {
                collectionFound = true;
                assertEquals("Collection profile name different from expected", "collection", profile.getName());
                assertEquals("Collection profile location different from expected", URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1345561703620"), profile.getLocation());
                assertNotNull("Collection allowed reference types list should not be null", profile.getAllowedReferenceTypes());
                assertFalse("Collection allowed reference types list should not be empty", profile.getAllowedReferenceTypes().isEmpty());
                assertTrue("Collection should only have one allowed reference type", profile.getAllowedReferenceTypes().size() == 1);
                assertEquals("Collection should only allow the Metadata reference type", "Metadata", profile.getAllowedReferenceTypes().iterator().next());
                assertNotNull("Collection component reference map should not be null", profile.getComponentMap());
                assertFalse("Collection component reference map should not be empty", profile.getComponentMap().isEmpty());
                assertTrue("Collection component reference map should contain one entry", profile.getComponentMap().size() == 1);
                assertTrue("Collection component reference map entry key different from expected", profile.getComponentMap().containsKey("^.+$"));
                assertTrue("Collection component reference map entry value different from expected", profile.getComponentMap().containsValue("collection"));
            }
        }
        
        assertTrue("Collection profile not found", collectionFound);
    }
}
