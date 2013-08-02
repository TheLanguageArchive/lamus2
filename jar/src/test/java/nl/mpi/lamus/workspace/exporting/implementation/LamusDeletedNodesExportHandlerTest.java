/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.exporting.DeletedNodesExportHandler;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import org.jmock.Expectations;
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
 * @author guisil
 */
public class LamusDeletedNodesExportHandlerTest {
    
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    private DeletedNodesExportHandler deletedNodesExportHandler;
    
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock NodeExporterFactory mockNodeExporterFactory;
    
    @Mock NodeExporter mockNodeExporter;
    @Mock Workspace mockWorkspace;
    
    
    public LamusDeletedNodesExportHandlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        deletedNodesExportHandler = new LamusDeletedNodesExportHandler(mockWorkspaceDao, mockNodeExporterFactory);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of exploreDeletedNodes method, of class LamusDeletedNodesExportHandler.
     */
    @Test
    public void exploreDeletedTopNodes() throws MalformedURLException, URISyntaxException {

        final int workspaceID = 1;
        
        final Collection<WorkspaceNode> deletedTopNodes = new ArrayList<WorkspaceNode>();
        
        final int firstNodeID = 10;
        final int firstNodeArchiveID = 100;
        final URL firstNodeURL = new URL("http://some.url/node.something");
        final String firstNodeDisplayValue = "someName";
        final WorkspaceNodeType firstNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String firstNodeFormat = "";
        final URI firstNodeSchemaLocation = new URI("http://some.location");
        final String firstNodePid = UUID.randomUUID().toString();
        final WorkspaceNode firstNode = new LamusWorkspaceNode(firstNodeID, workspaceID, firstNodeArchiveID, firstNodeSchemaLocation,
                firstNodeDisplayValue, "", firstNodeType, firstNodeURL, firstNodeURL, firstNodeURL, WorkspaceNodeStatus.NODE_ISCOPY, firstNodePid, firstNodeFormat);
        
        final int secondNodeID = 10;
        final int secondNodeArchiveID = 100;
        final URL secondNodeURL = new URL("http://some.url/node.something");
        final String secondNodeDisplayValue = "someName";
        final WorkspaceNodeType secondNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String secondNodeFormat = "";
        final URI secondNodeSchemaLocation = new URI("http://some.location");
        final String secondNodePid = UUID.randomUUID().toString();
        final WorkspaceNode secondNode = new LamusWorkspaceNode(secondNodeID, workspaceID, secondNodeArchiveID, secondNodeSchemaLocation,
                secondNodeDisplayValue, "", secondNodeType, secondNodeURL, secondNodeURL, secondNodeURL, WorkspaceNodeStatus.NODE_ISCOPY, secondNodePid, secondNodeFormat);
        
        deletedTopNodes.add(firstNode);
        deletedTopNodes.add(secondNode);
        
    
        context.checking(new Expectations() {{
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspaceDao).getDeletedTopNodes(workspaceID); will(returnValue(deletedTopNodes));
        }});
        
        for(final WorkspaceNode deletedNode : deletedTopNodes) {
            
            context.checking(new Expectations() {{
                oneOf(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, deletedNode); will(returnValue(mockNodeExporter));
                oneOf(mockNodeExporter).exportNode(null, deletedNode);
            }});
        }
    
        deletedNodesExportHandler.exploreDeletedNodes(mockWorkspace);
    }

}