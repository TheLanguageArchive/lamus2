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

import nl.mpi.bcarchive.typecheck.FileType;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.workspace.model.TypeMapper;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.assertEquals;
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
    @Mock FileType mockConfiguredTypeChecker;
    @Mock TypeMapper mockTypeMapper;
    
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
        fileTypeHandler = new LamusFileTypeHandler(mockConfiguredTypeChecker, mockTypeMapper);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of setValues method, of class LamusFileTypeHandler.
     */
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
}
