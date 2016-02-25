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
package nl.mpi.lamus.workspace.factory.implementation;

import java.net.URISyntaxException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceNodeLinkFactoryTest {
    
    private WorkspaceNodeLinkFactory factory;
    
    public LamusWorkspaceNodeLinkFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        factory = new LamusWorkspaceNodeLinkFactory();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getNewWorkspaceNodeLink method, of class LamusWorkspaceNodeLinkFactory.
     */
    @Test
    public void workspaceNodeLinkObjectIsCorrectlyInitialised() throws URISyntaxException {

        int testParentWorkspaceNodeID = 10;
        int testChildWorkspaceNodeID = 20;
        
        WorkspaceNodeLink testWorkspaceNodeLink = 
                factory.getNewWorkspaceNodeLink(testParentWorkspaceNodeID, testChildWorkspaceNodeID);
        
        assertEquals(testParentWorkspaceNodeID, testWorkspaceNodeLink.getParentWorkspaceNodeID());
        assertEquals(testChildWorkspaceNodeID, testWorkspaceNodeLink.getChildWorkspaceNodeID());
    }
}
