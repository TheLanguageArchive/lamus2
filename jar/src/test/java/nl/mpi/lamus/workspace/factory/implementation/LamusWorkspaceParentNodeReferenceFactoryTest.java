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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceParentNodeReference;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.cmdi.api.model.MetadataResourceProxy;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceParentNodeReferenceFactoryTest {
    
    private WorkspaceParentNodeReferenceFactory factory;
    
    public LamusWorkspaceParentNodeReferenceFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        factory = new LamusWorkspaceParentNodeReferenceFactory();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getNewWorkspaceParentNodeReference method, of class LamusWorkspaceParentNodeReferenceFactory.
     */
    @Test
    public void workspaceParentNodeReferenceObjectIsCorrectlyInitialised() throws MalformedURLException, URISyntaxException {

        WorkspaceNode testParentNode = new LamusWorkspaceNode(1, new URI(UUID.randomUUID().toString()), new URL("file:/archive/folder/node.cmdi"));
        Reference testChildLink = new MetadataResourceProxy("someID", new URI("http://some.uri"), "cmdi");
        
        WorkspaceParentNodeReference testParentNodeReference = factory.getNewWorkspaceParentNodeReference(testParentNode, testChildLink);
        
        assertEquals(testParentNode.getWorkspaceNodeID(), testParentNodeReference.getParentWorkspaceNodeID());
        assertEquals(testChildLink, testParentNodeReference.getReferenceInParentDocument());
    }
}
