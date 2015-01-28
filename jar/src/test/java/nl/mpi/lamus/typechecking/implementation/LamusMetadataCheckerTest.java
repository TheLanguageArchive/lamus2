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
import nl.mpi.lamus.typechecking.MetadataChecker;
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
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class LamusMetadataCheckerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
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
        ReflectionTestUtils.setField(metadataChecker, "schematronFile",
                new File(URLDecoder.decode(getClass().getClassLoader().getResource("cmdi_schematron.sch").getFile())));
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void profileIsAllowed() throws Exception {
        
        final File fileToCheck = new File(getClass().getClassLoader().getResource("testingProfile_allowed.cmdi").getFile());
        
        boolean result = metadataChecker.isProfileAllowed(fileToCheck);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void profileIsAllowed_WithoutMdProfile() throws Exception {
        
        final File fileToCheck = new File(getClass().getClassLoader().getResource("testingProfile_allowed_noMdProfileElement.cmdi").getFile());
        
        boolean result = metadataChecker.isProfileAllowed(fileToCheck);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void profileIsNotAllowed() throws Exception {
        
        final File fileToCheck = new File(getClass().getClassLoader().getResource("testingProfile_notAllowed.cmdi").getFile());
        
        boolean result = metadataChecker.isProfileAllowed(fileToCheck);
        
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void invalidSchematron() throws Exception {
        
        ReflectionTestUtils.setField(metadataChecker, "schematronFile", mockSchematronFile);
        final File fileToCheck = new File(getClass().getClassLoader().getResource("testingProfile_allowed.cmdi").getFile());
        
        context.checking(new Expectations() {{
            allowing(mockSchematronFile);
        }});
        
        try {
            metadataChecker.isProfileAllowed(fileToCheck);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Invalid Schematron", ex.getMessage());
        }
    }
}