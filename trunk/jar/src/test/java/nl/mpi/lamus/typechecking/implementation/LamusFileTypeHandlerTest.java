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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.TypecheckHandler;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.util.OurURL;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusFileTypeHandlerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private FileTypeHandler fileTypeHandler;
    @Mock TypecheckHandler mockTypecheckHandler;
    
    @Mock OurURL mockOurURL;
    @Mock InputStream mockInputStream;
    
    @Mock TypecheckedResults mockTypecheckedResults;
    
    public LamusFileTypeHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        fileTypeHandler = new LamusFileTypeHandler(mockTypecheckHandler);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void checkType() throws MalformedURLException, TypeCheckerException, IOException {
        
        final String testFilename = "someFilename.txt";
        final URL testFileUrl = new URL("file:/some/location/" + testFilename);
        final String expectedMimetype = "text/plain";
        final String expectedAnalysis = "okay (content, name)";
        final String testCheckResult = "true ARCHIVABLE text/plain";
        final TypecheckerJudgement expectedJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        
        final TypecheckedResults expectedTypecheckedResults = new LamusTypecheckedResults(expectedMimetype, expectedAnalysis, expectedJudgement);
        
        context.checking(new Expectations() {{
            oneOf(mockTypecheckHandler).deepTypecheck(testFileUrl, testFilename); will(returnValue(testCheckResult));
            oneOf(mockTypecheckHandler).getTypecheckMimetype(testCheckResult); will(returnValue(expectedMimetype));
            oneOf(mockTypecheckHandler).getTypecheckJudgement(testCheckResult); will(returnValue(expectedJudgement));
        }});
        
        TypecheckedResults retrievedTypecheckedResults = fileTypeHandler.checkType(testFileUrl, testFilename);
        
        assertEquals("TypecheckedResults different from expected", expectedTypecheckedResults, retrievedTypecheckedResults);
    }
    
    
    @Test
    public void checkTypeBadResult() throws MalformedURLException, TypeCheckerException, IOException {
        
        final String testFileName = "somefilename.jjj";
        final URL testFileUrl = new URL("file:/some/location/" + testFileName);
        final String expectedMimetype = "Unknown";
        final String expectedAnalysis = "false outrageous file";
        final String testCheckResult = expectedAnalysis;
        final TypecheckerJudgement expectedJudgement = TypecheckerJudgement.UNARCHIVABLE;
        
        final TypecheckedResults expectedTypecheckedResults = new LamusTypecheckedResults(expectedMimetype, expectedAnalysis, expectedJudgement);
        
        context.checking(new Expectations() {{
            oneOf(mockTypecheckHandler).deepTypecheck(testFileUrl, testFileName); will(returnValue(testCheckResult));
            oneOf(mockTypecheckHandler).getTypecheckMimetype(testCheckResult); will(returnValue(null));
            oneOf(mockTypecheckHandler).getTypecheckJudgement(testCheckResult); will(returnValue(expectedJudgement));
        }});
        
        TypecheckedResults retrievedTypecheckedResults = fileTypeHandler.checkType(testFileUrl, testFileName);
        
        assertEquals("TypecheckedResults different from expected", expectedTypecheckedResults, retrievedTypecheckedResults);
    }
    
    @Test
    public void checkTypeIOException() throws IOException, TypeCheckerException {
        
        final String testFileName = "somefilename.jjj";
        final URL testFileUrl = new URL("file:/some/location/" + testFileName);
        final String expectedMimetype = "Unknown";
        final String exceptionMessage = "some extremely frightening error message";
        final String expectedAnalysis = "Read error for " + testFileName + " - " + exceptionMessage;
        
        final IOException firstException = new IOException(exceptionMessage);
        final String expectedMainExceptionMessage = "LamusFileTypeHandler.checkType: File type checker could not access file: " + testFileName;
        
        final TypecheckedResults expectedTypecheckedResults = new LamusTypecheckedResults(expectedMimetype, expectedAnalysis, null);
        
        context.checking(new Expectations() {{
            oneOf(mockTypecheckHandler).deepTypecheck(testFileUrl, testFileName); will(throwException(firstException));
        }});
        
        try {
            fileTypeHandler.checkType(testFileUrl, testFileName);
        } catch(TypeCheckerException ex) {
            assertEquals("Exception message different from expected", expectedMainExceptionMessage, ex.getMessage());
            assertEquals("Exception TypecheckedResults different from expected", expectedTypecheckedResults, ex.getTypecheckedResults());
        }
    }
    
    @Test
    public void checkedResourceIsArchivable() {
        
        final TypecheckerJudgement actualJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        final TypecheckerJudgement acceptableJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        StringBuilder message = new StringBuilder();
        String expectedMessage = "Resource is archivable. Judgement '" + actualJudgement + "' acceptable.";
        
        context.checking(new Expectations() {{
            oneOf(mockTypecheckedResults).getTypecheckerJudgement(); will(returnValue(actualJudgement));
        }});
        
        boolean isArchivable = fileTypeHandler.isCheckedResourceArchivable(mockTypecheckedResults, acceptableJudgement, message);
        
        assertTrue("Result should have been true", isArchivable);
        assertEquals(message.toString(), expectedMessage);
    }
    
    @Test
    public void checkedResourceIsNotArchivable() {
        
        final TypecheckerJudgement actualJudgement = TypecheckerJudgement.ARCHIVABLE_SHORTTERM;
        final TypecheckerJudgement acceptableJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        StringBuilder message = new StringBuilder();
        
        context.checking(new Expectations() {{
            oneOf(mockTypecheckedResults).getTypecheckerJudgement(); will(returnValue(actualJudgement));
        }});
        
        boolean isArchivable = fileTypeHandler.isCheckedResourceArchivable(mockTypecheckedResults, acceptableJudgement, message);
        
        assertFalse("Result should have been false", isArchivable);
    }
}
