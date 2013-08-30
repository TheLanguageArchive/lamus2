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
package nl.mpi.lamus.typechecking.implementation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import nl.mpi.lamus.typechecking.TypecheckerConfiguration;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class LamusTypecheckerConfigurationTest {
    
    //TODO should be wiring automatically the bean (having a test config class, for instance),
        // but after trying in a few unsuccessful attempts skipped it, for now
    
    private TypecheckerConfiguration typecheckerConfiguration;
    
    private Map<String, String> customTypecheckerFolderToConfigFileMap;
    
    public LamusTypecheckerConfigurationTest() {
        
        customTypecheckerFolderToConfigFileMap = new HashMap<String, String>();
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        typecheckerConfiguration = new LamusTypecheckerConfiguration();
        
        ReflectionTestUtils.setField(typecheckerConfiguration, "customTypecheckerFolderToConfigFileMap", customTypecheckerFolderToConfigFileMap);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getAcceptableJudgementForNormalLocation() {
        
        //TODO change this in order to support multiple configurations
//        TypecheckerJudgement expectedJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        TypecheckerJudgement expectedJudgement = TypecheckerJudgement.UNARCHIVABLE;
        File location = new File("/random_folder");
        
        TypecheckerJudgement retrievedJudgement = typecheckerConfiguration.getAcceptableJudgementForLocation(location);
        
        assertEquals("Retrieved judgement different from expected", expectedJudgement, retrievedJudgement);
    }
    
    @Test
    public void getAcceptableJudgementForSpecialLocation() {
        fail("not implemented yet");
    }
}