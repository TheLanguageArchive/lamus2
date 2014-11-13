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
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.NodeReplaceCheckerFactory;
import nl.mpi.lamus.workspace.replace.NodeReplaceExplorer;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
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
public class LamusNodeReplaceCheckerFactoryTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock ReplaceActionManager mockReplaceActionManager;
    @Mock ReplaceActionFactory mockReplaceActionFactory;
    @Mock NodeReplaceExplorer mockNodeReplaceExplorer;
    
    private NodeReplaceCheckerFactory nodeReplaceManagerFactory;
    
    public LamusNodeReplaceCheckerFactoryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        nodeReplaceManagerFactory = new LamusNodeReplaceCheckerFactory();
        
        ReflectionTestUtils.setField(nodeReplaceManagerFactory, "corpusStructureProvider", mockCorpusStructureProvider);
        ReflectionTestUtils.setField(nodeReplaceManagerFactory, "archiveFileHelper", mockArchiveFileHelper);
        ReflectionTestUtils.setField(nodeReplaceManagerFactory, "replaceActionManager", mockReplaceActionManager);
        ReflectionTestUtils.setField(nodeReplaceManagerFactory, "replaceActionFactory", mockReplaceActionFactory);
        ReflectionTestUtils.setField(nodeReplaceManagerFactory, "nodeReplaceExplorer", mockNodeReplaceExplorer);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void getReplaceManagerForMetadataNode() throws MalformedURLException, URISyntaxException {
        
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
        
        NodeReplaceChecker retrievedNodeReplacedManager = nodeReplaceManagerFactory.getReplaceCheckerForNode(node);
        
        assertNotNull(retrievedNodeReplacedManager);
        assertTrue("Retrieved node replace manager has a different type from expected", retrievedNodeReplacedManager instanceof MetadataNodeReplaceChecker);
    }
    
    @Test
    public void getReplaceManagerForResourceNode() throws MalformedURLException, URISyntaxException {
        
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
        
        NodeReplaceChecker retrievedNodeReplacedManager = nodeReplaceManagerFactory.getReplaceCheckerForNode(node);
        
        assertNotNull(retrievedNodeReplacedManager);
        assertTrue("Retrieved node replace manager has a different type from expected", retrievedNodeReplacedManager instanceof ResourceNodeReplaceChecker);
    }
}