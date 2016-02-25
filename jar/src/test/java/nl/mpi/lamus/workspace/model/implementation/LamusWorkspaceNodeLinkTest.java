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
package nl.mpi.lamus.workspace.model.implementation;

import java.net.URISyntaxException;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceNodeLinkTest {
    
    private final int parentWorkspaceNodeID = 1;
    private final int childWorkspaceNodeID = 2;
    
    public LamusWorkspaceNodeLinkTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws URISyntaxException {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test for one of the constructors
     */
    @Test
    public void constructorWithAllParametersProperlyCreatesWorkspace() {

        WorkspaceNodeLink testLink = new LamusWorkspaceNodeLink(this.parentWorkspaceNodeID, this.childWorkspaceNodeID);
        
        assertEquals("Value for 'parentWorkspaceNodeID' is not the expected one.",
                this.parentWorkspaceNodeID, testLink.getParentWorkspaceNodeID());
        assertEquals("Value for 'childWorkspaceNodeID' is not the expected one.",
                this.childWorkspaceNodeID, testLink.getChildWorkspaceNodeID());
    }

    /**
     * Test of equals method, of class LamusWorkspace.
     */
    @Test
    public void nodesAreEqual() {

        WorkspaceNodeLink testWorkspaceNodeLink1 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID);
        
        WorkspaceNodeLink testWorkspaceNodeLink2 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID);
        
        assertEquals("Workspace objects are not equal.", testWorkspaceNodeLink1, testWorkspaceNodeLink2);
    }
    
    @Test
    public void nodesHaveSameHashCode() {

        WorkspaceNodeLink testWorkspaceNodeLink1 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID);
        
        WorkspaceNodeLink testWorkspaceNodeLink2 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID);
        
        assertEquals("Workspace objects don't have the same hashcode.", testWorkspaceNodeLink1.hashCode(), testWorkspaceNodeLink2.hashCode());
    }
    
    @Test
    public void nodesComparedWithObjectOfDifferentType() {

        WorkspaceNodeLink testWorkspaceNodeLink1 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID);
        
        WorkspaceNodeLink testWorkspaceNodeLink2 = new SomeOtherWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID);
        
        assertFalse("Workspace objects should not be equal.",
                testWorkspaceNodeLink1.equals(testWorkspaceNodeLink2));
    }

    @Test
    public void testToString() {
        
        WorkspaceNodeLink testWorkspaceNodeLink = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID);
        
        String expectedString = "Parent Workspace Node ID: " + testWorkspaceNodeLink.getParentWorkspaceNodeID()
                + ", Child Workspace Node ID: " + testWorkspaceNodeLink.getChildWorkspaceNodeID();
        
        String actualString = testWorkspaceNodeLink.toString();
        
        assertEquals(expectedString, actualString);
    }
}

class SomeOtherWorkspaceNodeLink implements WorkspaceNodeLink {

    private final int parentWorkspaceNodeID;
    private final int childWorkspaceNodeID;
    
    public SomeOtherWorkspaceNodeLink(int parentWorkspaceNodeID, int childWorkspaceNodeID) {
        this.parentWorkspaceNodeID = parentWorkspaceNodeID;
        this.childWorkspaceNodeID = childWorkspaceNodeID;
    }
    
    @Override
    public int getParentWorkspaceNodeID() {
        return this.parentWorkspaceNodeID;
    }

    @Override
    public int getChildWorkspaceNodeID() {
        return this.childWorkspaceNodeID;
    }
}