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
import java.net.URL;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import nl.mpi.lamus.archive.CorpusStructureServiceBridge;
import nl.mpi.lamus.archive.permissions.PermissionAdjuster;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.CrawlerInvocationException;
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
    @Mock NodeExporterFactory mockNodeExporterFactory;
    @Mock UnlinkedAndDeletedNodesExportHandler mockUnlinkedAndDeletedNodesExportHandler;
    @Mock CorpusStructureServiceBridge mockCorpusStructureServiceBridge;
    @Mock PermissionAdjuster mockPermissionAdjuster;
    
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
                mockCorpusStructureServiceBridge, mockPermissionAdjuster);
        workspaceExportRunner.setWorkspace(mockWorkspace);
    }
    
    @After
    public void tearDown() {
    }
    

    @Test
    public void callExporterForGeneralNodeWithoutVersions()
            throws MalformedURLException, InterruptedException,
            ExecutionException, WorkspaceNodeNotFoundException, WorkspaceExportException,
            VersionCreationException, CrawlerInvocationException {
        
        final int workspaceID = 1;
        final int wsNodeID = 10;
        final URI archiveNodeURI = URI.create(UUID.randomUUID().toString());
        final URL wsNodeURL = new URL("file:/workspace/folder/someName.cmdi");
        final URI originURI = URI.create("http://some.url/someName.cmdi");
        final URL archiveNodeURL = originURI.toURL();
        final String testDisplayValue = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "";
        final URI testSchemaLocation = URI.create("http://some.location");
        final WorkspaceNode testNode = new LamusWorkspaceNode(wsNodeID, workspaceID, testSchemaLocation,
                testDisplayValue, "", testNodeType, wsNodeURL, archiveNodeURI, archiveNodeURL, originURI, WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, testNodeFormat);
        
        final String crawlerID = UUID.randomUUID().toString();
        
        final States exporting = context.states("exporting");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
                when(exporting.isNot("finished"));
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(testNode));
                when(exporting.isNot("finished"));
            
            oneOf(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, testNode); will(returnValue(mockNodeExporter));
                when(exporting.isNot("finished"));
            
            oneOf(mockNodeExporter).exportNode(mockWorkspace, null, testNode);
                when(exporting.isNot("finished"));
                
            oneOf(mockUnlinkedAndDeletedNodesExportHandler).exploreUnlinkedAndDeletedNodes(mockWorkspace);
                when(exporting.isNot("finished"));
            
            oneOf(mockCorpusStructureServiceBridge).callCrawler(archiveNodeURI); will(returnValue(crawlerID));
                when(exporting.isNot("finished"));
            oneOf(mockWorkspace).setCrawlerID(crawlerID);
                when(exporting.isNot("finished"));
            oneOf(mockWorkspaceDao).updateWorkspaceCrawlerID(mockWorkspace);
                when(exporting.isNot("finished"));
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
                when(exporting.isNot("finished"));
            oneOf(mockPermissionAdjuster).adjustPermissions(workspaceID);
                then(exporting.is("finished"));
        }});
        
        boolean result = executeRunner();
        
        long timeoutInMs = 2000L;
        synchroniser.waitUntil(exporting.is("finished"), timeoutInMs);
        
        assertTrue("Execution result should have been successful (true)", result);
    }
    
    @Test
    public void callExporterForGeneralNodeWithoutVersions_CrawlerInvocationException()
            throws MalformedURLException, InterruptedException,
            ExecutionException, WorkspaceNodeNotFoundException, WorkspaceExportException,
            VersionCreationException, CrawlerInvocationException {
        
        final int workspaceID = 1;
        final int wsNodeID = 10;
        final URI archiveNodeURI = URI.create(UUID.randomUUID().toString());
        final URL wsNodeURL = new URL("file:/workspace/folder/someName.cmdi");
        final URI originURI = URI.create("http://some.url/someName.cmdi");
        final URL archiveNodeURL = originURI.toURL();
        final String testDisplayValue = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "";
        final URI testSchemaLocation = URI.create("http://some.location");
        final WorkspaceNode testNode = new LamusWorkspaceNode(wsNodeID, workspaceID, testSchemaLocation,
                testDisplayValue, "", testNodeType, wsNodeURL, archiveNodeURI, archiveNodeURL, originURI, WorkspaceNodeStatus.NODE_ISCOPY, Boolean.FALSE, testNodeFormat);
        
        final CrawlerInvocationException expectedCause = new CrawlerInvocationException("some exception message", null);
        
        final States exporting = context.states("exporting");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
                when(exporting.isNot("finished"));
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(testNode));
                when(exporting.isNot("finished"));
            
            oneOf(mockNodeExporterFactory).getNodeExporterForNode(mockWorkspace, testNode); will(returnValue(mockNodeExporter));
                when(exporting.isNot("finished"));
            
            oneOf(mockNodeExporter).exportNode(mockWorkspace, null, testNode);
                when(exporting.isNot("finished"));
                
            oneOf(mockUnlinkedAndDeletedNodesExportHandler).exploreUnlinkedAndDeletedNodes(mockWorkspace);
                when(exporting.isNot("finished"));
            
            oneOf(mockCorpusStructureServiceBridge).callCrawler(archiveNodeURI); will(throwException(expectedCause));
                then(exporting.is("finished"));
        }});
        
        try {
            executeRunner();
            fail("should have thrown exception");
        } catch(ExecutionException ex) {
            assertEquals("Exception cause different from expected", expectedCause, ex.getCause());
        }
        
        long timeoutInMs = 2000L;
        synchroniser.waitUntil(exporting.is("finished"), timeoutInMs);
    }
    
    //TODO Test exceptions
    
    
    private boolean executeRunner() throws InterruptedException, ExecutionException, CrawlerInvocationException {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> result = executorService.submit(workspaceExportRunner);
        return result.get();
    }
}