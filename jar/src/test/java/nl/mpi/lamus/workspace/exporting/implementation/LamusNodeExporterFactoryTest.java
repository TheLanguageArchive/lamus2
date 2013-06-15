/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.workspace.exporting.ArchiveObjectsBridge;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.exporting.TrashCanHandler;
import nl.mpi.lamus.workspace.exporting.TrashVersioningHandler;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusNodeExporterFactoryTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock TrashVersioningHandler mockTrashVersioningHandler;
    @Mock TrashCanHandler mockTrashCanHandler;
    @Mock ArchiveObjectsBridge mockArchiveObjectsBridge;
    @Mock SearchClientBridge mockSearchClientBridge;
    
    private NodeExporterFactory exporterFactory;
    
    public LamusNodeExporterFactoryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        exporterFactory = new LamusNodeExporterFactory(
                mockTrashVersioningHandler, mockTrashCanHandler, mockArchiveObjectsBridge, mockSearchClientBridge);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void getNodeExporterForUploadedNode() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 1;
        final int workspaceNodeID = 10;
        final int archiveNodeID = 100;
        final URL nodeURL = new URL("http://some.url/node.something");
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String nodeFormat = "";
        final URI nodeSchemaLocation = new URI("http://some.location");
        final String nodePid = "somePID";
        final WorkspaceNode node = new LamusWorkspaceNode(workspaceNodeID, workspaceID, archiveNodeID, nodeSchemaLocation,
                nodeName, "", nodeType, nodeURL, nodeURL, nodeURL, WorkspaceNodeStatus.NODE_UPLOADED, nodePid, nodeFormat);
        
        NodeExporter retrievedExporter = exporterFactory.getNodeExporterForNode(node);
        
        assertTrue("Retrieved node exporter has a different type from expected", retrievedExporter instanceof AddedNodeExporter);
    }
}