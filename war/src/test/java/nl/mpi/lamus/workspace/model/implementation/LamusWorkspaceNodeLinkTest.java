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

import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceNodeLinkTest {
    
    private int parentWorkspaceNodeID = 1;
    private int childWorkspaceNodeID = 2;
    private URI childURI;
    
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
        
        this.childURI = new URI("http://some.uri");
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test for one of the constructors
     */
    @Test
    public void constructorWithAllParametersProperlyCreatesWorkspace() {

        WorkspaceNodeLink testLink = new LamusWorkspaceNodeLink(this.parentWorkspaceNodeID, this.childWorkspaceNodeID, this.childURI);
        
        assertEquals("Value for 'parentWorkspaceNodeID' is not the expected one.",
                this.parentWorkspaceNodeID, testLink.getParentWorkspaceNodeID());
        assertEquals("Value for 'childWorkspaceNodeID' is not the expected one.",
                this.childWorkspaceNodeID, testLink.getChildWorkspaceNodeID());
        assertEquals("Value for 'childURI' is not the expected one.", this.childURI, testLink.getChildURI());
    }

    /**
     * Test of equals method, of class LamusWorkspace.
     */
    @Test
    public void nodesAreEqual() {

        WorkspaceNodeLink testWorkspaceNodeLink1 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID, this.childURI);
        
        WorkspaceNodeLink testWorkspaceNodeLink2 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID, this.childURI);
        
        assertEquals("Workspace objects are not equal.", testWorkspaceNodeLink1, testWorkspaceNodeLink2);
    }
    
    @Test
    public void nodesHaveSameHashCode() {

        WorkspaceNodeLink testWorkspaceNodeLink1 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID, this.childURI);
        
        WorkspaceNodeLink testWorkspaceNodeLink2 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID, this.childURI);
        
        assertEquals("Workspace objects don't have the same hashcode.", testWorkspaceNodeLink1.hashCode(), testWorkspaceNodeLink2.hashCode());
    }
    
    /**
     * Test of equals method, of class LamusWorkspace.
     */
    @Test
    public void nodesAreNotEqual() throws URISyntaxException {
        
        URI differentURI = new URI("http://some/different/uri");
        WorkspaceNodeLink testWorkspaceNodeLink1 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID, this.childURI);
        
        WorkspaceNodeLink testWorkspaceNodeLink2 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID, differentURI);
        
        assertFalse("Workspace objects should not be equal.",
                testWorkspaceNodeLink1.equals(testWorkspaceNodeLink2));
    }
    
    @Test
    public void nodesHaveDifferentHashCodes() throws URISyntaxException {
        
        URI differentURI = new URI("http://some/different/uri");
        WorkspaceNodeLink testWorkspaceNodeLink1 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID, this.childURI);
        
        WorkspaceNodeLink testWorkspaceNodeLink2 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID, differentURI);
        
        assertFalse("Workspace objects should not have the same hashcode.",
                testWorkspaceNodeLink1.hashCode() == testWorkspaceNodeLink2.hashCode());
    }
    
    @Test
    public void nodesComparedWithObjectOfDifferentType() {

        WorkspaceNodeLink testWorkspaceNodeLink1 = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID, this.childURI);
        
        WorkspaceNodeLink testWorkspaceNodeLink2 = new SomeOtherWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID, this.childURI);
        
        assertFalse("Workspace objects should not be equal.",
                testWorkspaceNodeLink1.equals(testWorkspaceNodeLink2));
    }

    @Test
    public void testToString() {
        
        WorkspaceNodeLink testWorkspaceNodeLink = new LamusWorkspaceNodeLink(
                this.parentWorkspaceNodeID, this.childWorkspaceNodeID, this.childURI);
        
        String expectedString = "Parent Workspace Node ID: " + testWorkspaceNodeLink.getParentWorkspaceNodeID()
                + ", Child Workspace Node ID: " + testWorkspaceNodeLink.getChildWorkspaceNodeID()
                + ", Child URI: " + testWorkspaceNodeLink.getChildURI();
        
        String actualString = testWorkspaceNodeLink.toString();
        
        assertEquals(expectedString, actualString);
    }
}

class SomeOtherWorkspaceNodeLink implements WorkspaceNodeLink {

    private int parentWorkspaceNodeID;
    private int childWorkspaceNodeID;
    private URI childURI;
    
    public SomeOtherWorkspaceNodeLink(int parentWorkspaceNodeID, int childWorkspaceNodeID, URI childResourceProxyURI) {
        this.parentWorkspaceNodeID = parentWorkspaceNodeID;
        this.childWorkspaceNodeID = childWorkspaceNodeID;
        this.childURI = childResourceProxyURI;
    }
    
    public int getParentWorkspaceNodeID() {
        return this.parentWorkspaceNodeID;
    }

    public int getChildWorkspaceNodeID() {
        return this.childWorkspaceNodeID;
    }

    public URI getChildURI() {
        return this.childURI;
    }
}