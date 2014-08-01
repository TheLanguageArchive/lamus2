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

import nl.mpi.lamus.workspace.exporting.implementation.WorkspaceExportRunner;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import nl.mpi.lamus.archive.CorpusStructureServiceBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.VersionCreationException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.NodeExporterFactory;
import nl.mpi.lamus.workspace.exporting.UnlinkedAndDeletedNodesExportHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
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
    
    Synchroniser synchroniser = new Synchroniser();
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(synchroniser);
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock Workspace mockWorkspace;
    @Mock WorkspaceDao mockWorkspaceDao;
//    @Mock WorkspaceTreeExporter mockWorkspaceTreeExporter;
    @Mock NodeExporterFactory mockNodeExporterFactory;
    @Mock UnlinkedAndDeletedNodesExportHandler mockUnlinkedAndDeletedNodesExportHandler;
    @Mock CorpusStructureServiceBridge mockCorpusStructureServiceBridge;
    
    @Mock NodeExporter mockNodeExporter;
    
    @Mock Collection<WorkspaceNodeReplacement> mockNodeReplacementsCollection;
    
    private WorkspaceExportRunner workspaceExportRunner;
    
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
        workspaceExportRunner = new WorkspaceExportRunner(
                mockWorkspaceDao, mockNodeExporterFactory,
                mockUnlinkedAndDeletedNodesExportHandler,
                mockCorpusStructureServiceBridge);
        workspaceExportRunner.setWorkspace(mockWorkspace);
    }
    
    @After
    public void tearDown() {
    }
    

    @Test
    public void callExporterForGeneralNodeWithoutVersions() throws MalformedURLException, URISyntaxException, InterruptedException, ExecutionException, WorkspaceNodeNotFoundException, WorkspaceExportException, VersionCreationException {
        
        final int workspaceID = 1;
        
        
        
        final Collection<WorkspaceNode> workspaceNodes = new ArrayList<WorkspaceNode>();
        
        final int testChildWorkspaceNodeID = 10;
        final URI testChildArchiveURI = new URI(UUID.randomUUID().toString());
        final URL testChildWsURL = new URL("file:/workspace/folder/someName.cmdi");
        final URL testChildOriginURL = new URL("http://some.url/someName.cmdi");
        final URL testChildArchiveURL = testChildOriginURL;
        final String testDisplayValue = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final WorkspaceNode testNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, workspaceID, testSchemaLocation,
                testDisplayValue, "", testNodeType, testChildWsURL, testChildArchiveURI, testChildArchiveURL, testChildOriginURL, WorkspaceNodeStatus.NODE_ISCOPY, testNodeFormat);
        
        workspaceNodes.add(testNode);
        
        final String crawlerID = UUID.randomUUID().toString();
        
        //1.0 synchronise files in the workspace with the lamus database
            // NOT necessary in the new lamus... every action in the workspace should be immediately reflected in the database

        //2.0 consistency checks: status of amsbridge, corpusstructure database, lamus database, creation (if needed) of the orphans directory
            // MOSTLY NOT necessary - some of these checks should already be made before the call

        //2.9 get the default prefix (path) for sessions
            //TODO NOW
        
        final States exporting = context.states("exporting");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
                when(exporting.isNot("finished"));
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(testNode));
                when(exporting.isNot("finished"));
            
            oneOf(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, testNode); will(returnValue(mockNodeExporter));
                when(exporting.isNot("finished"));
            
            oneOf(mockNodeExporter).exportNode(null, testNode);
                when(exporting.isNot("finished"));
                
            oneOf(mockUnlinkedAndDeletedNodesExportHandler).exploreUnlinkedAndDeletedNodes(mockWorkspace);
                when(exporting.isNot("finished"));
                
            oneOf(mockCorpusStructureServiceBridge).callCrawler(testChildArchiveURI); will(returnValue(crawlerID));
                when(exporting.isNot("finished"));
            oneOf(mockWorkspace).setCrawlerID(crawlerID);
                when(exporting.isNot("finished"));
            oneOf(mockWorkspaceDao).updateWorkspaceCrawlerID(mockWorkspace);
                then(exporting.is("finished"));
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
        
        
        boolean result = executeRunner();
        
        long timeoutInMs = 2000L;
        synchroniser.waitUntil(exporting.is("finished"), timeoutInMs);
        
        assertTrue("Execution result should have been successful (true)", result);
    }
    
    //TODO Test exceptions
    
    
    private boolean executeRunner() throws InterruptedException, ExecutionException {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> result = executorService.submit(workspaceExportRunner);
        return result.get();
    }
}