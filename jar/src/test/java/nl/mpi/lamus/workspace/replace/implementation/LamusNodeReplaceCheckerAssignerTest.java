/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.replace.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.NodeReplaceCheckerAssigner;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class LamusNodeReplaceCheckerAssignerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock MetadataNodeReplaceChecker mockMetadataNodeReplaceChecker;
    @Mock ResourceNodeReplaceChecker mockResourceNodeReplaceChecker;
    
    private NodeReplaceCheckerAssigner nodeReplaceCheckerAssigner;
    
    public LamusNodeReplaceCheckerAssignerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        nodeReplaceCheckerAssigner = new LamusNodeReplaceCheckerAssigner();
        
        ReflectionTestUtils.setField(nodeReplaceCheckerAssigner, "metadataNodeReplaceChecker", mockMetadataNodeReplaceChecker);
        ReflectionTestUtils.setField(nodeReplaceCheckerAssigner, "resourceNodeReplaceChecker", mockResourceNodeReplaceChecker);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void getReplaceCheckerForMetadataNode() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 1;
        final int topNodeID = 1;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URL nodeOriginURL = new URL("file:/some.url/someName.cmdi");
        final URL nodeArchiveURL = nodeOriginURL;
        final URI nodeURI = new URI(UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "";
        final URI nodeSchemaLocation = new URI("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(topNodeID, workspaceID, nodeSchemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURL, WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, nodeFormat);
        
        NodeReplaceChecker retrievedNodeReplacedChecker = nodeReplaceCheckerAssigner.getReplaceCheckerForNode(node);
        
        assertNotNull(retrievedNodeReplacedChecker);
        assertTrue("Retrieved node replace checker has a different type from expected", retrievedNodeReplacedChecker instanceof MetadataNodeReplaceChecker);
        assertEquals("Retrieved node replace checker different from expected", mockMetadataNodeReplaceChecker, retrievedNodeReplacedChecker);
    }
    
    @Test
    public void getReplaceCheckerForResourceNode() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 1;
        final int topNodeID = 1;
        final URL nodeWsURL = new URL("file:/workspace/folder/someName.txt");
        final URL nodeOriginURL = new URL("file:/some.url/someName.txt");
        final URL nodeArchiveURL = nodeOriginURL;
        final URI nodeURI = new URI(UUID.randomUUID().toString());
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE; //TODO change this
        final String nodeFormat = "";
        final URI nodeSchemaLocation = new URI("http://some.location");
        final WorkspaceNode node = new LamusWorkspaceNode(topNodeID, workspaceID, nodeSchemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeURI, nodeArchiveURL, nodeOriginURL, WorkspaceNodeStatus.NODE_VIRTUAL, Boolean.FALSE, nodeFormat);
        
        NodeReplaceChecker retrievedNodeReplacedChecker = nodeReplaceCheckerAssigner.getReplaceCheckerForNode(node);
        
        assertNotNull(retrievedNodeReplacedChecker);
        assertTrue("Retrieved node replace checker has a different type from expected", retrievedNodeReplacedChecker instanceof ResourceNodeReplaceChecker);
        assertEquals("Retrieved node replace checker different from expected", mockResourceNodeReplaceChecker, retrievedNodeReplacedChecker);
    }
}