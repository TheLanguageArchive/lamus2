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
import static org.junit.Assert.*;
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

    @Test
    public void getFileBasenameWithSlashes() {
        
        String baseName = "baseName.txt";
        String fullName = "something/with/some/slashes/" + baseName;
        
        String retrievedName = testArchiveFileHelper.getFileBasename(fullName);
        
        assertEquals(baseName, retrievedName);
        
    }
    
    @Test
    public void getFileBasenameWithoutSlashes() {
        
        String fullName = "something_without__slashes.txt";
        
        String retrievedName = testArchiveFileHelper.getFileBasename(fullName);
        
        assertEquals(fullName, retrievedName);
        
    }
    
    @Test
    public void getFileTitleWithBaseName() {
        
        String baseName = "baseName.txt";
        String fullName = "something/with/slashes/and/" + baseName;
        
        String retrievedName = testArchiveFileHelper.getFileTitle(fullName);
        assertEquals(baseName, retrievedName);
    }
    
    @Test
    public void getFileTitleWithoutBaseName() {
        
        final int maxDirLength = 100;
        String nameBeforeFirstSlash = "something";
        String fullName = nameBeforeFirstSlash + "/with/slashes/";
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getMaxDirectoryNameLength(); will(returnValue(maxDirLength));
        }});
        
        String retrievedName = testArchiveFileHelper.getFileTitle(fullName);
        assertEquals(nameBeforeFirstSlash, retrievedName);
    }
    
    @Test
    public void getFileTitleWithUrlName() {
        
        final int maxDirLength = 100;
        String domainName = "mpi";
        String fullName = "file:/" + domainName + "/with/slashes/";
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getMaxDirectoryNameLength(); will(returnValue(maxDirLength));
        }});
        
        String retrievedName = testArchiveFileHelper.getFileTitle(fullName);
        assertEquals(domainName, retrievedName);
    }
    
    @Test
    public void getFileTitleWithoutSlashes() {
        
        String fullName = "no_slashes";
        
        String retrievedName = testArchiveFileHelper.getFileTitle(fullName);
        assertEquals(fullName, retrievedName);
    }

    @Test
    public void correctPathElementWithInvalidCharacters() {
        
        final int maxDirLength = 100;
        String someReason = "because";
        String input = "this#file&has**invalid@characters.txt";
        String expectedOutput = "this_file_has_invalid_characters.txt";
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getMaxDirectoryNameLength(); will(returnValue(maxDirLength));
        }});
        
        String actualOutput = testArchiveFileHelper.correctPathElement(input, someReason);
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    public void correctPathElementWithoutInvalidCharacters() {
        
        final int maxDirLength = 100;
        String someReason = "because";
        String input = "this_file_has_no_invalid_characters.txt";
        String expectedOutput = input;
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getMaxDirectoryNameLength(); will(returnValue(maxDirLength));
        }});
        
        String actualOutput = testArchiveFileHelper.correctPathElement(input, someReason);
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    public void correctPathElementAboveMaxDirLength() {
        
        final int maxDirLength = 10;
        String someReason = "because";
        String firstThreeCharacters = "thi";
        String lastCharacters = "s_has_several_characters";
        String extension = ".txt";
        String threePoints = "...";
        String input = firstThreeCharacters + lastCharacters + extension;
        String expectedOutput = firstThreeCharacters + threePoints + extension;
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getMaxDirectoryNameLength(); will(returnValue(maxDirLength));
        }});
        
        String actualOutput = testArchiveFileHelper.correctPathElement(input, someReason);
        
        assertEquals(expectedOutput, actualOutput);
    }
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
