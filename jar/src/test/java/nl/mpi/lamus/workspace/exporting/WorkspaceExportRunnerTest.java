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
package nl.mpi.lamus.workspace.exporting;

import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import org.jmock.Expectations;
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

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class WorkspaceExportRunnerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock WorkspaceDao mockWorkspaceDao;
    
    
    public WorkspaceExportRunnerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    

    /**
     * Test of call method, of class WorkspaceExportRunner.
     */
    @Test
    public void testCall() {
        
        fail("not implemented yet");
        
        final int workspaceID = 1;
        Collection<WorkspaceNode> workspaceNodes = new ArrayList<WorkspaceNode>();
        
//        final int testChildWorkspaceNodeID = 10;
//        final int testChildArchiveID = 100;
//        final OurURL testChildURL = new OurURL("http://some.url/node.something");
//        final String testDisplayValue = "someName";
//        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
//        final String testNodeFormat = "";
//        final URI testSchemaLocation = new URI("http://some.location");
//        final String testPid = "somePID";
//        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testChildArchiveID, testSchemaLocation,
//                testDisplayValue, "", testNodeType, testChildURL.toURL(), testChildURL.toURL(), testChildURL.toURL(), WorkspaceNodeStatus.NODE_ISCOPY, testPid, testNodeFormat);
//        
        
        //1.0 synchronise files in the workspace with the lamus database
            // NOT necessary in the new lamus... every action in the workspace should be immediately reflected in the database

        //2.0 consistency checks: status of amsbridge, corpusstructure database, lamus database, creation (if needed) of the orphans directory
            // MOSTLY NOT necessary - some of these checks should already be made before the call

        //2.9 get the default prefix (path) for sessions
            //TODO NOW
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getNodesForWorkspace(workspaceID);
            
            //oneOf(mockArchiveFileHelper).
        }});
        
        // update message according to what's being done?
        
        //3.0 send removed files (deleted) to the trashcan (SetIngestLocations.trashDeletedFiles)
            //TODO LATER (after basic mini-lamus)

        //3.4 version links (first time) - move replaced files which don't get a version (equivalent to corpus???) to a place where they will be overwritten
            //TODO LATER (after basic mini-lamus)
        
        //3.3 rename replaced virtual resources, to avoid name clashes later (?)
            //TODO LATER (after basic mini-lamus)

        //3.2 determine archive urls for nodes that weren't in the archive and update those in the lamus db
            //TODO NOW
        
        //4 update links in the metadata files, so that they point to the right location when in the archive
            // NOW
        
        
        
        
        
        //5.1 close all imdi files (remove them from cache) in the workspace and save them - NEEDED ?
        //5.2 copy the files into the archive and update urls in the csdb where needed
        //5.3 allocate urids (handles) and ao entries for all new nodes to enter the archive
        //5.4 version links (second time) - linking between new and old versions of updated resources
        //5.5 unlink all children of unlinked/free nodes in order to make them free too (???)
        //5.6 clean up free nodes (?)
        
        //6.1 clean up workspace database
        //6.2 call archive crawler, update csdb
         // fetch top node id and adjust unix permissions
         // set access rights to the top node for nobody and call ams2 recalculation
         // update status and message
        
        //7 update user, ingest request information and send email
        
    }
}