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

import java.io.InputStream;
import nl.mpi.bcarchive.typecheck.FileType;
import nl.mpi.lamus.typechecking.TypecheckHandler;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;
import static org.junit.Assert.*;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusTypecheckHandlerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private TypecheckHandler typecheckHandler;
    @Mock FileType mockTypechecker;
    @Mock InputStream mockInputStream;
    
    public LamusTypecheckHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        typecheckHandler = new LamusTypecheckHandler(mockTypechecker);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of typecheck method, of class LamusTypecheckHandler.
     */
    @Test
    public void testTypecheck() throws Exception {
        
        final String fileName = "someFile.txt";
        final String result = "true something";
        
        context.checking(new Expectations() {{
            
            oneOf(mockTypechecker).checkStream(mockInputStream, fileName.toLowerCase()); will(returnValue(result));
        }});
        
        typecheckHandler.typecheck(mockInputStream, fileName);
        
        assertEquals("Stored result is different from expected", result, typecheckHandler.getTypecheckResult());
    }

    /**
     * Test of isFileArchivable method, of class LamusTypecheckHandler.
     * 
     * It's not really a unit test, since the isFileArchivable method
     * calls a static method from FileType, which cannot be mocked with JMock.
     * It is therefore also testing the method resultToBoolean from FileType.
     */
    @Test
    public void isFileArchivableResultTrue() {
        
        ReflectionTestUtils.setField(typecheckHandler, "typecheckResult", "true something");
        
        boolean result = typecheckHandler.isFileArchivable();
        assertTrue(result);
    }
    
    /**
     * Test of isFileArchivable method, of class LamusTypecheckHandler.
     * 
     * It's not really a unit test, since the isFileArchivable method
     * calls a static method from FileType, which cannot be mocked with JMock.
     * It is therefore also testing the method resultToBoolean from FileType.
     */
    @Test
    public void isFileArchivableResultFalse() {
        
        ReflectionTestUtils.setField(typecheckHandler, "typecheckResult", "something else");
        
        boolean result = typecheckHandler.isFileArchivable();
        assertFalse(result);
    }
    
    /**
     * Test of isFileArchivable method, of class LamusTypecheckHandler.
     * 
     * It's not really a unit test, since the isFileArchivable method
     * calls a static method from FileType, which cannot be mocked with JMock.
     * It is therefore also testing the method resultToBoolean from FileType.
     */
    @Test
    public void isFileArchivableResultNull() {
        
        ReflectionTestUtils.setField(typecheckHandler, "typecheckResult", null);
        
        boolean result = typecheckHandler.isFileArchivable();
        assertFalse(result);
    }

    /**
     * Test of getTypecheckResult method, of class LamusTypecheckHandler.
     */
    @Test
    public void testGetTypecheckResult() {
        
        String expectedResult = "some test result";
        ReflectionTestUtils.setField(typecheckHandler, "typecheckResult", expectedResult);
        
        String retrievedResult = typecheckHandler.getTypecheckResult();
        assertEquals("Typecheck result different from expected", expectedResult, retrievedResult);
    }

    /**
     * Test of getTypecheckJudgement method, of class LamusTypecheckHandler.
     * 
     * It's not really a unit test, since the getTypecheckJudgement method
     * calls a static method from FileType, which cannot be mocked with JMock.
     * It is therefore also testing the method resultToJudgement from FileType.
     */
    @Test
    public void getTypecheckJudgementLongTerm() {
        
        TypecheckerJudgement expectedJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        ReflectionTestUtils.setField(typecheckHandler, "typecheckResult", "true GOOD something");
        
        TypecheckerJudgement retrievedJudgement = typecheckHandler.getTypecheckJudgement();
        assertEquals("Typecheck judgement different from expected", expectedJudgement, retrievedJudgement);
    }
    
    /**
     * Test of getTypecheckJudgement method, of class LamusTypecheckHandler.
     * 
     * It's not really a unit test, since the getTypecheckJudgement method
     * calls a static method from FileType, which cannot be mocked with JMock.
     * It is therefore also testing the method resultToJudgement from FileType.
     */
    @Test
    public void getTypecheckJudgementShortTerm() {
        
        TypecheckerJudgement expectedJudgement = TypecheckerJudgement.ARCHIVABLE_SHORTTERM;
        ReflectionTestUtils.setField(typecheckHandler, "typecheckResult", "true OKAYFORAWHILE something");
        
        TypecheckerJudgement retrievedJudgement = typecheckHandler.getTypecheckJudgement();
        assertEquals("Typecheck judgement different from expected", expectedJudgement, retrievedJudgement);
    }

    /**
     * Test of getTypecheckJudgement method, of class LamusTypecheckHandler.
     * 
     * It's not really a unit test, since the getTypecheckJudgement method
     * calls a static method from FileType, which cannot be mocked with JMock.
     * It is therefore also testing the method resultToJudgement from FileType.
     */
    @Test
    public void getTypecheckJudgementUnarchivable() {
        
        TypecheckerJudgement expectedJudgement = TypecheckerJudgement.UNARCHIVABLE;
        ReflectionTestUtils.setField(typecheckHandler, "typecheckResult", "false BAD something");
        
        TypecheckerJudgement retrievedJudgement = typecheckHandler.getTypecheckJudgement();
        assertEquals("Typecheck judgement different from expected", expectedJudgement, retrievedJudgement);
    }
    
    /**
     * Test of getTypecheckJudgement method, of class LamusTypecheckHandler.
     * 
     * It's not really a unit test, since the getTypecheckJudgement method
     * calls a static method from FileType, which cannot be mocked with JMock.
     * It is therefore also testing the method resultToJudgement from FileType.
     */
    @Test
    public void getTypecheckJudgementBadResultString() {
        
        TypecheckerJudgement expectedJudgement = TypecheckerJudgement.UNARCHIVABLE;
        ReflectionTestUtils.setField(typecheckHandler, "typecheckResult", "false BADMALFORMEDRESULTSTRING something");
        
        TypecheckerJudgement retrievedJudgement = typecheckHandler.getTypecheckJudgement();
        assertEquals("Typecheck judgement different from expected", expectedJudgement, retrievedJudgement);
    }

    /**
     * Test of getTypecheckMimetype method, of class LamusTypecheckHandler.
     * 
     * It's not really a unit test, since the getTypecheckJudgement method
     * calls a static method from FileType, which cannot be mocked with JMock.
     * It is therefore also testing the method resultToMPIType from FileType.
     */
    @Test
    public void testGetTypecheckMimetype() {

        String expectedMimetype = "text/plain";
        String result = "true GOOD " + expectedMimetype;
        ReflectionTestUtils.setField(typecheckHandler, "typecheckResult", result);
        
        String retrievedMimetype = typecheckHandler.getTypecheckMimetype();
        assertEquals("Mimetype is different from expected", expectedMimetype, retrievedMimetype);
    }
    
    /**
     * Test of getTypecheckMimetype method, of class LamusTypecheckHandler.
     * 
     * It's not really a unit test, since the getTypecheckJudgement method
     * calls a static method from FileType, which cannot be mocked with JMock.
     * It is therefore also testing the method resultToMPIType from FileType.
     */
    @Test
    public void getTypecheckMimetypeResultTrue() {

        String expectedMimetype = "text/plain";
        String result = "true GOOD " + expectedMimetype;
        ReflectionTestUtils.setField(typecheckHandler, "typecheckResult", result);
        
        String retrievedMimetype = typecheckHandler.getTypecheckMimetype();
        assertEquals("Mimetype is different from expected", expectedMimetype, retrievedMimetype);
    }
    
    /**
     * Test of getTypecheckMimetype method, of class LamusTypecheckHandler.
     * 
     * It's not really a unit test, since the getTypecheckJudgement method
     * calls a static method from FileType, which cannot be mocked with JMock.
     * It is therefore also testing the method resultToMPIType from FileType.
     */
    @Test
    public void getTypecheckMimetypeResultFalse() {

        String result = "false BAD something";
        ReflectionTestUtils.setField(typecheckHandler, "typecheckResult", result);
        
        String retrievedMimetype = typecheckHandler.getTypecheckMimetype();
        assertEquals("Mimetype is different from expected", null, retrievedMimetype);
    }
    
    /**
     * Test of getTypecheckMimetype method, of class LamusTypecheckHandler.
     * 
     * It's not really a unit test, since the getTypecheckJudgement method
     * calls a static method from FileType, which cannot be mocked with JMock.
     * It is therefore also testing the method resultToMPIType from FileType.
     */
    @Test
    public void getTypecheckMimetypeResultNull() {

        String result = null;
        ReflectionTestUtils.setField(typecheckHandler, "typecheckResult", result);
        
        String retrievedMimetype = typecheckHandler.getTypecheckMimetype();
        assertEquals("Mimetype is different from expected", null, retrievedMimetype);
    }
}
