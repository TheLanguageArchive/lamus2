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
import nl.mpi.bcarchive.typecheck.FileType;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.TypecheckHandler;
import nl.mpi.lamus.typechecking.TypecheckerJudgement;
import nl.mpi.lamus.workspace.exception.TypeCheckerException;
import nl.mpi.lamus.workspace.model.NodeTypeMapper;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.util.OurURL;
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
public class LamusFileTypeHandlerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private FileTypeHandler fileTypeHandler;
    @Mock TypecheckHandler mockTypecheckHandler;
    @Mock NodeTypeMapper mockTypeMapper;
    
    @Mock OurURL mockOurURL;
    @Mock InputStream mockInputStream;
    
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
        fileTypeHandler = new LamusFileTypeHandler(mockTypecheckHandler, mockTypeMapper);
    }
    
    @After
    public void tearDown() {
    }
    
    
    @Test
    public void checkTypeWithKnownMimetype() throws TypeCheckerException, MalformedURLException {
        
        String testFileName = "someFileName.txt";
        final String expectedMimetype = "text/plain";
        final WorkspaceNodeType expectedNodeType = WorkspaceNodeType.RESOURCE_WR;
        String expectedAnalysis = "okay";
        
        context.checking(new Expectations() {{
            oneOf (mockTypeMapper).getNodeTypeForMimetype(expectedMimetype); will(returnValue(expectedNodeType));
        }});
        
        fileTypeHandler.checkType(mockOurURL, testFileName, expectedMimetype);
        
        assertEquals(expectedMimetype, fileTypeHandler.getMimetype());
        assertEquals(expectedNodeType, fileTypeHandler.getNodeType());
        assertEquals(expectedAnalysis, fileTypeHandler.getAnalysis());
    }
    
    @Test
    public void checkTypeWithNullMimetypeAndKnownURL() throws MalformedURLException, TypeCheckerException, IOException {
        
        final String testFileName = "someFilename.txt";
        final String expectedMimetype = "text/plain";
        final WorkspaceNodeType expectedNodeType = WorkspaceNodeType.RESOURCE_WR;
        String expectedAnalysis = "okay (content, name)";
        final String testCheckResult = "true ARCHIVABLE text/plain";
        
        context.checking(new Expectations() {{
            oneOf (mockOurURL).openStream(); will(returnValue(mockInputStream));
            oneOf (mockTypecheckHandler).typecheck(mockInputStream, testFileName.toLowerCase());
            oneOf (mockTypecheckHandler).getTypecheckMimetype(); will(returnValue(expectedMimetype));
            oneOf (mockInputStream).close();
            oneOf (mockTypecheckHandler).getTypecheckResult(); will(returnValue(testCheckResult));
            oneOf (mockTypeMapper).getNodeTypeForMimetype(expectedMimetype); will(returnValue(expectedNodeType));
        }});
        
        fileTypeHandler.checkType(mockOurURL, testFileName, null);
        
        assertEquals(expectedMimetype, fileTypeHandler.getMimetype());
        assertEquals(expectedNodeType, fileTypeHandler.getNodeType());
        assertEquals(expectedAnalysis, fileTypeHandler.getAnalysis());
    }
    
    @Test
    public void checkTypeWithUnknownMimetypeAndKnownURL() throws MalformedURLException, TypeCheckerException, IOException {
        
        final String testFileName = "someFilename.txt";
        final String expectedMimetype = "text/plain";
        final WorkspaceNodeType expectedNodeType = WorkspaceNodeType.RESOURCE_WR;
        String expectedAnalysis = "okay (content, name)";
        final String testCheckResult = "true ARCHIVABLE text/plain";
        
        context.checking(new Expectations() {{
            oneOf (mockOurURL).openStream(); will(returnValue(mockInputStream));
            oneOf (mockTypecheckHandler).typecheck(mockInputStream, testFileName.toLowerCase());
            oneOf (mockTypecheckHandler).getTypecheckMimetype(); will(returnValue(expectedMimetype));
            oneOf (mockInputStream).close();
            oneOf (mockTypecheckHandler).getTypecheckResult(); will(returnValue(testCheckResult));
            oneOf (mockTypeMapper).getNodeTypeForMimetype(expectedMimetype); will(returnValue(expectedNodeType));
        }});
        
        fileTypeHandler.checkType(mockOurURL, testFileName, "Unknown");
        
        assertEquals(expectedMimetype, fileTypeHandler.getMimetype());
        assertEquals(expectedNodeType, fileTypeHandler.getNodeType());
        assertEquals(expectedAnalysis, fileTypeHandler.getAnalysis());
    }
    
    @Test
    public void checkTypeWithUnspecifiedMimetypeAndKnownURL() throws MalformedURLException, TypeCheckerException, IOException {
        
        final String testFileName = "someFilename.txt";
        final String expectedMimetype = "text/plain";
        final WorkspaceNodeType expectedNodeType = WorkspaceNodeType.RESOURCE_WR;
        String expectedAnalysis = "okay (content, name)";
        final String testCheckResult = "true ARCHIVABLE text/plain";
        
        context.checking(new Expectations() {{
            oneOf (mockOurURL).openStream(); will(returnValue(mockInputStream));
            oneOf (mockTypecheckHandler).typecheck(mockInputStream, testFileName.toLowerCase());
            oneOf (mockTypecheckHandler).getTypecheckMimetype(); will(returnValue(expectedMimetype));
            oneOf (mockInputStream).close();
            oneOf (mockTypecheckHandler).getTypecheckResult(); will(returnValue(testCheckResult));
            oneOf (mockTypeMapper).getNodeTypeForMimetype(expectedMimetype); will(returnValue(expectedNodeType));
        }});
        
        fileTypeHandler.checkType(mockOurURL, testFileName, "Unspecified");
        
        assertEquals(expectedMimetype, fileTypeHandler.getMimetype());
        assertEquals(expectedNodeType, fileTypeHandler.getNodeType());
        assertEquals(expectedAnalysis, fileTypeHandler.getAnalysis());
    }
    
    @Test
    public void checkTypeWithNullMimetypeAndKnownURLAndBadResult() throws MalformedURLException, TypeCheckerException, IOException {
        
        final String testFileName = "somefilename.jjj";
        final String expectedMimetype = "Unknown";
        final WorkspaceNodeType expectedNodeType = WorkspaceNodeType.UNKNOWN;
        String expectedAnalysis = "outrageous file";
        final String testCheckResult = "false " + expectedAnalysis;
        
        context.checking(new Expectations() {{
            oneOf (mockOurURL).openStream(); will(returnValue(mockInputStream));
            oneOf (mockTypecheckHandler).typecheck(mockInputStream, testFileName.toLowerCase());
            oneOf (mockTypecheckHandler).getTypecheckMimetype(); will(returnValue(expectedMimetype));
            oneOf (mockInputStream).close();
            oneOf (mockTypecheckHandler).getTypecheckResult(); will(returnValue(testCheckResult));
            oneOf (mockTypeMapper).getNodeTypeForMimetype(expectedMimetype); will(returnValue(expectedNodeType));
        }});
        
        fileTypeHandler.checkType(mockOurURL, testFileName, null);
        
        assertEquals(expectedMimetype, fileTypeHandler.getMimetype());
        assertEquals(expectedNodeType, fileTypeHandler.getNodeType());
        assertEquals(expectedAnalysis, fileTypeHandler.getAnalysis());
    }
    
    
    
    //TODO Reference to checkType in following tests became ambiguous.
        //TODO Remove one of the methods and adjust tests accordingly.
    
    
    
//    @Test
//    public void checkTypeWithNullMimetypeAndNullURLAndBadResult() throws MalformedURLException, TypeCheckerException, IOException {
//        
//        final String testFileName = "somefilename.jjj";
//        final String expectedMimetype = "Unknown";
//        final WorkspaceNodeType expectedNodeType = WorkspaceNodeType.UNKNOWN;
//        String expectedAnalysis = "outrageous file";
//        final String testCheckResult = "false " + expectedAnalysis;
//        
//        context.checking(new Expectations() {{
//            oneOf (mockTypecheckHandler).typecheck(null, testFileName.toLowerCase());
//            oneOf (mockTypecheckHandler).getTypecheckMimetype(); will(returnValue(expectedMimetype));
//            oneOf (mockTypecheckHandler).getTypecheckResult(); will(returnValue(testCheckResult));
//            oneOf (mockTypeMapper).getNodeTypeForMimetype(expectedMimetype); will(returnValue(expectedNodeType));
//        }});
//        
//        fileTypeHandler.checkType(null, testFileName, null);
//        
//        assertEquals(expectedMimetype, fileTypeHandler.getMimetype());
//        assertEquals(expectedNodeType, fileTypeHandler.getNodeType());
//        assertEquals(expectedAnalysis, fileTypeHandler.getAnalysis());
//    }
//    
//    @Test
//    public void checkTypeWithNullMimetypeAndNullURLAndBadTrueResult() throws MalformedURLException, TypeCheckerException, IOException {
//        
//        final String testFileName = "somefilename.jjj";
//        final String expectedMimetype = "Unknown";
//        final WorkspaceNodeType expectedNodeType = WorkspaceNodeType.UNKNOWN;
//        String expectedAnalysis = "outrageous file";
//        final String testCheckResult = "true " + expectedAnalysis;
//        
//        context.checking(new Expectations() {{
//            oneOf (mockTypecheckHandler).typecheck(null, testFileName.toLowerCase());
//            oneOf (mockTypecheckHandler).getTypecheckMimetype(); will(returnValue(expectedMimetype));
//            oneOf (mockTypecheckHandler).getTypecheckResult(); will(returnValue(testCheckResult));
//            oneOf (mockTypeMapper).getNodeTypeForMimetype(expectedMimetype); will(returnValue(expectedNodeType));
//        }});
//        
//        fileTypeHandler.checkType(null, testFileName, null);
//        
//        assertEquals(expectedMimetype, fileTypeHandler.getMimetype());
//        assertEquals(expectedNodeType, fileTypeHandler.getNodeType());
//        assertEquals(expectedAnalysis, fileTypeHandler.getAnalysis());
//    }
//    
//    @Test
//    public void checkTypeWithNullMimetypeAndNullURL() throws MalformedURLException, TypeCheckerException, IOException {
//        
//        final String testFileName = "somefilename.txt";
//        final String expectedMimetype = "text/plain";
//        final WorkspaceNodeType expectedNodeType = WorkspaceNodeType.RESOURCE_WR;
//        String expectedAnalysis = "okay (name)";
//        final String testCheckResult = "true ARCHIVABLE text/plain";
//        
//        context.checking(new Expectations() {{
//            oneOf (mockTypecheckHandler).typecheck(null, testFileName.toLowerCase());
//            oneOf (mockTypecheckHandler).getTypecheckMimetype(); will(returnValue(expectedMimetype));
//            oneOf (mockTypecheckHandler).getTypecheckResult(); will(returnValue(testCheckResult));
//            oneOf (mockTypeMapper).getNodeTypeForMimetype(expectedMimetype); will(returnValue(expectedNodeType));
//        }});
//        
//        fileTypeHandler.checkType(null, testFileName, null);
//        
//        assertEquals(expectedMimetype, fileTypeHandler.getMimetype());
//        assertEquals(expectedNodeType, fileTypeHandler.getNodeType());
//        assertEquals(expectedAnalysis, fileTypeHandler.getAnalysis());
//    }

    //TODO use some more real examples of checkResult and analysis
    
    //TODO test NBL types in order to check the latest changes
    
    @Test
    public void testSetValues() {

        final String expectedMimetype = "image/jpeg";
        final WorkspaceNodeType expectedNodeType = WorkspaceNodeType.RESOURCE_MR;
        
        context.checking(new Expectations() {{
            oneOf (mockTypeMapper).getNodeTypeForMimetype(expectedMimetype); will(returnValue(expectedNodeType));
        }});

        fileTypeHandler.setValues(expectedMimetype);
        
        assertEquals("Mimetype is different from expected", expectedMimetype, fileTypeHandler.getMimetype());
        assertEquals("Node type is different from expected", expectedNodeType, fileTypeHandler.getNodeType());
    }
    
    @Test
    public void isResourceArchivable() throws MalformedURLException {
        
        final TypecheckerJudgement expectedJudgement = TypecheckerJudgement.ARCHIVABLE_LONGTERM;
        OurURL testURL = new OurURL("http://resource.imdi");
        StringBuilder message = new StringBuilder();
        String expectedMessage = "Resource with URL '" + testURL 
                    + "' is archivable. Judgement '" + expectedJudgement + "' acceptable.";
        
        context.checking(new Expectations() {{
            
            oneOf(mockTypecheckHandler).getTypecheckJudgement(); will(returnValue(expectedJudgement));
        }});
        
        boolean isArchivable = fileTypeHandler.isResourceArchivable(testURL, TypecheckerJudgement.ARCHIVABLE_LONGTERM, message);
        
        assertTrue(isArchivable);
        assertEquals(message.toString(), expectedMessage);
    }
}
