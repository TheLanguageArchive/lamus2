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
package nl.mpi.lamus.workspace.importing.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeLinker;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.WorkspaceParentNodeReference;
import nl.mpi.metadata.api.model.Reference;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceNodeLinkerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    private WorkspaceNodeLinker nodeLinker;
    
    @Mock WorkspaceParentNodeReferenceFactory mockWorkspaceParentNodeReferenceFactory;
    @Mock WorkspaceNodeLinkFactory mockWorkspaceNodeLinkFactory;
    @Mock WorkspaceDao mockWorkspaceDao;
    
    @Mock WorkspaceParentNodeReference mockWorkspaceParentNodeReference;
    @Mock WorkspaceNodeLink mockWorkspaceNodeLink;
    
    @Mock Workspace mockWorkspace;
    @Mock WorkspaceNode mockParentNode;
    @Mock WorkspaceNode mockChildNode;
    @Mock Reference mockChildReference;
            
    public LamusWorkspaceNodeLinkerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        nodeLinker = new LamusWorkspaceNodeLinker(mockWorkspaceParentNodeReferenceFactory, mockWorkspaceNodeLinkFactory, mockWorkspaceDao);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void linkNodes() throws URISyntaxException {
        
        final int parentNodeID = 1;
        final int childNodeID = 2;
        final URI childURI = new URI("http://some.uri");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(mockParentNode, mockChildReference);
                will(returnValue(mockWorkspaceParentNodeReference));
            
            oneOf(mockWorkspaceParentNodeReference).getParentWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockChildReference).getURI(); will(returnValue(childURI));
                
            oneOf(mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentNodeID, childNodeID, childURI);
                will(returnValue(mockWorkspaceNodeLink));
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        
        nodeLinker.linkNodes(mockParentNode, mockChildNode, mockChildReference);
    }

    @Test
    public void linkNodesWithNullParentNode() throws MalformedURLException {
        
        final int childNodeID = 2;
        final int childNodeArchiveID = 4;
        final URL childNodeURL = new URL("http://some.url");
        final int workspaceID = 1;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(null, mockChildReference);
                will(returnValue(null));
            
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspace).setTopNodeID(childNodeID);
            oneOf(mockChildNode).getArchiveNodeID(); will(returnValue(childNodeArchiveID));
            oneOf(mockWorkspace).setTopNodeArchiveID(childNodeArchiveID);
            oneOf(mockChildNode).getArchiveURL(); will(returnValue(childNodeURL));
            oneOf(mockWorkspace).setTopNodeArchiveURL(childNodeURL);
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            oneOf(mockWorkspaceDao).updateWorkspaceTopNode(mockWorkspace);
        }});
        
        nodeLinker.linkNodes(null, mockChildNode, mockChildReference);
    }
  
    //TODO
    
//    @Test
//    public void linkNodesWithNullChildReference() {
//        
////        context.checking(new Expectations() {{
////            
////            
////        }});
////        
////        nodeLinker.linkNodes(mockWorkspace, mockParentNode, mockChildNode, null);
//        
//        fail("What should happen when just the child reference is null?");
//    }

}