/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
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
import java.net.URL;
import nl.mpi.bcarchive.typecheck.FileType;
import nl.mpi.lamus.configuration.Configuration;
import nl.mpi.lamus.typechecking.FileTypeFactory;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusFileTypeFactoryTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private FileTypeFactory testFileTypeFactory;
    
    public LamusFileTypeFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        testFileTypeFactory = new LamusFileTypeFactory();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getNewFileTypeWithConfigFile method, of class LamusFileTypeFactory.
     */
    @Test
    public void newFileTypeWithConfigFileIsNotNull() {

        URL testURL = getClass().getClassLoader().getResource("filetypes-with-nbl.txt");
        File testFile = new File(testURL.getFile());
//        FileType expectedFileType = new FileType(testFile);
        FileType retrievedFileType = testFileTypeFactory.getNewFileTypeWithConfigFile(testFile);
        
//        assertEquals(expectedFileType, retrievedFileType);
        assertNotNull(retrievedFileType);
    }

    /**
     * Test of getNewFileTypeWithDefaultConfigFile method, of class LamusFileTypeFactory.
     */
    @Test
    public void newFileTypeWithDefaultConfigFileIsNotNull() {
        
//        FileType expectedFileType = new FileType();
        FileType retrievedFileType = testFileTypeFactory.getNewFileTypeWithDefaultConfigFile();
        
//        assertEquals(expectedFileType, retrievedFileType);
        assertNotNull(retrievedFileType);
    }
}
