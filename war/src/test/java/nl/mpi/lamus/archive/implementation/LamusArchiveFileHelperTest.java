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
package nl.mpi.lamus.archive.implementation;

import java.io.File;
import java.io.IOException;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.configuration.Configuration;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusArchiveFileHelperTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private ArchiveFileHelper testArchiveFileHelper;
    @Mock Configuration mockConfiguration;
    @Mock File mockFile;
    
    public LamusArchiveFileHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        testArchiveFileHelper = new LamusArchiveFileHelper(mockConfiguration);
    }
    
    @After
    public void tearDown() {
    }

//    /**
//     * Test of getFileBasename method, of class LamusArchiveFileHelper.
//     */
//    @Test
//    public void testGetFileBasename() {
//         fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getFileTitle method, of class LamusArchiveFileHelper.
//     */
//    @Test
//    public void testGetFileTitle() {
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of correctPathElement method, of class LamusArchiveFileHelper.
//     */
//    @Test
//    public void testCorrectPathElement() {
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getOrphansDirectory method, of class LamusArchiveFileHelper.
//     */
//    @Test
//    public void testGetOrphansDirectory() {
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getOrphansDirectoryName method, of class LamusArchiveFileHelper.
//     */
//    @Test
//    public void testGetOrphansDirectoryName() {
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of isFileSizeAboveTypeReCheckSizeLimit method, of class LamusArchiveFileHelper.
     */
    @Test
    public void fileSizeIsAboveTypeReCheckSizeLimit() throws IOException {
        
        final long actualFileSize = 3 * 1024;
        final long typeReCheckSizeLimit = 2 * 1024;
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getTypeReCheckSizeLimit(); will(returnValue(typeReCheckSizeLimit));
            oneOf (mockFile).length(); will(returnValue(actualFileSize));
        }});
        
        boolean isSizeAboveLimit = testArchiveFileHelper.isFileSizeAboveTypeReCheckSizeLimit(mockFile);
        
        assertTrue(isSizeAboveLimit);
    }
    
    @Test
    public void fileSizeIsBelowTypeReCheckSizeLimit() {

        final long actualFileSize = 1 * 1024;
        final long typeReCheckSizeLimit = 10 * 1024;
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getTypeReCheckSizeLimit(); will(returnValue(typeReCheckSizeLimit));
            oneOf (mockFile).length(); will(returnValue(actualFileSize));
        }});
        
        boolean isSizeAboveLimit = testArchiveFileHelper.isFileSizeAboveTypeReCheckSizeLimit(mockFile);
        
        assertFalse(isSizeAboveLimit);
    }
}
