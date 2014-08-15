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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import nl.mpi.lamus.ams.AmsServiceBridge;
import nl.mpi.lamus.archive.CorpusStructureServiceBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.CrawlerStateRetrievalException;
import nl.mpi.lamus.exception.VersionCreationException;
import nl.mpi.lamus.workspace.exporting.WorkspaceMailer;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
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
 * @author guisil
 */
public class LamusWorkspaceCrawlerCheckerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock CorpusStructureServiceBridge mockCorpusStructureServiceBridge;
    @Mock WorkspaceMailer mockWorkspaceMailer;
    @Mock AmsServiceBridge mockAmsBridge;
    
    @Mock Workspace mockSuccessfulSubmittedWorkspace1;
    @Mock Workspace mockSuccessfulSubmittedWorkspace2;
    @Mock Workspace mockFailedSubmittedWorkspace;
    @Mock WorkspaceNodeReplacement mockNodeReplacement1;
    @Mock WorkspaceNodeReplacement mockNodeReplacement2;
    
    
    private LamusWorkspaceCrawlerChecker workspaceCrawlerChecker;
    
    
    public LamusWorkspaceCrawlerCheckerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        workspaceCrawlerChecker = new LamusWorkspaceCrawlerChecker(mockWorkspaceDao,
                mockCorpusStructureServiceBridge, mockWorkspaceMailer, mockAmsBridge);
    }
    
    @After
    public void tearDown() {
    }

    
    
    @Test
    public void checkCrawlersForSubmittedWorkspaces_NoSubmittedWorkspacesFound() throws InterruptedException, CrawlerStateRetrievalException {
        
        final Collection<Workspace> submittedWorkspaces = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspacesInFinalStage(); will(returnValue(submittedWorkspaces));
        }});
        
        workspaceCrawlerChecker.checkCrawlersForSubmittedWorkspaces();
    }
    
    @Test
    public void checkCrawlersForSubmittedWorkspaces_OneSuccessfulSubmittedWorkspaceFound_WithoutVersions() throws InterruptedException, CrawlerStateRetrievalException, URISyntaxException {
        
        final int workspaceID_1 = 10;
        final URI topNodeURI_1 = new URI(UUID.randomUUID().toString());
        
        final Collection<Workspace> submittedWorkspaces = new ArrayList<>();
        submittedWorkspaces.add(mockSuccessfulSubmittedWorkspace1);
        
        final String crawlerID = UUID.randomUUID().toString();
        final String crawlerState = "SUCCESS";
        
        final Collection<WorkspaceNodeReplacement> nodeReplacements = new ArrayList<>();
        
        final WorkspaceStatus successfulStatus = WorkspaceStatus.DATA_MOVED_SUCCESS;
        final String successfulMessage = "Data was successfully moved to the archive and the crawler was successful.";
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspacesInFinalStage(); will(returnValue(submittedWorkspaces));
            
            //loop
            
            //logger
            allowing(mockSuccessfulSubmittedWorkspace1).getWorkspaceID(); will(returnValue(workspaceID_1));
            
            oneOf(mockSuccessfulSubmittedWorkspace1).getCrawlerID(); will(returnValue(crawlerID));
            oneOf(mockCorpusStructureServiceBridge).getCrawlerState(crawlerID); will(returnValue(crawlerState));
            
            oneOf(mockWorkspaceDao).getAllNodeReplacements(); will(returnValue(nodeReplacements));
            
            oneOf(mockSuccessfulSubmittedWorkspace1).setStatus(successfulStatus);
            oneOf(mockSuccessfulSubmittedWorkspace1).setMessage(successfulMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockSuccessfulSubmittedWorkspace1);
            
            oneOf(mockWorkspaceDao).cleanWorkspaceNodesAndLinks(mockSuccessfulSubmittedWorkspace1);
            
            oneOf(mockSuccessfulSubmittedWorkspace1).getTopNodeArchiveURI(); will(returnValue(topNodeURI_1));
            oneOf(mockAmsBridge).triggerAccessRightsRecalculation(topNodeURI_1);
            
            oneOf(mockWorkspaceMailer).sendWorkspaceFinalMessage(mockSuccessfulSubmittedWorkspace1, Boolean.TRUE, Boolean.TRUE);
        }});
        
        workspaceCrawlerChecker.checkCrawlersForSubmittedWorkspaces();
    }
    
    @Test
    public void runTwoSuccessfulSubmittedWorkspacesFound_WithoutVersions() throws InterruptedException, CrawlerStateRetrievalException, URISyntaxException {
        
        final int workspaceID_1 = 10;
        final URI topNodeURI_1 = new URI(UUID.randomUUID().toString());
        final int workspaceID_2 = 20;
        final URI topNodeURI_2 = new URI(UUID.randomUUID().toString());
        
        final Collection<Workspace> submittedWorkspaces = new ArrayList<>();
        submittedWorkspaces.add(mockSuccessfulSubmittedWorkspace1);
        submittedWorkspaces.add(mockSuccessfulSubmittedWorkspace2);
        
        final String firstCrawlerID = UUID.randomUUID().toString();
        final String firstCrawlerState = "SUCCESS";
        final String secondCrawlerID = UUID.randomUUID().toString();
        final String secondCrawlerState = "SUCCESS";
        
        final Collection<WorkspaceNodeReplacement> firstNodeReplacements = new ArrayList<>();
        final Collection<WorkspaceNodeReplacement> secondNodeReplacements = new ArrayList<>();
        
        final WorkspaceStatus successfulStatus = WorkspaceStatus.DATA_MOVED_SUCCESS;
        final String successfulMessage = "Data was successfully moved to the archive and the crawler was successful.";
        
        context.checking(new Expectations() {{

            oneOf(mockWorkspaceDao).getWorkspacesInFinalStage(); will(returnValue(submittedWorkspaces));
            
            //loop - first iteration
            
            //logger
            allowing(mockSuccessfulSubmittedWorkspace1).getWorkspaceID(); will(returnValue(workspaceID_1));
            
            oneOf(mockSuccessfulSubmittedWorkspace1).getCrawlerID(); will(returnValue(firstCrawlerID));
            oneOf(mockCorpusStructureServiceBridge).getCrawlerState(firstCrawlerID); will(returnValue(firstCrawlerState));
            
            oneOf(mockWorkspaceDao).getAllNodeReplacements(); will(returnValue(firstNodeReplacements));
            
            oneOf(mockSuccessfulSubmittedWorkspace1).setStatus(successfulStatus);
            oneOf(mockSuccessfulSubmittedWorkspace1).setMessage(successfulMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockSuccessfulSubmittedWorkspace1);
            
            oneOf(mockWorkspaceDao).cleanWorkspaceNodesAndLinks(mockSuccessfulSubmittedWorkspace1);
            
            oneOf(mockSuccessfulSubmittedWorkspace1).getTopNodeArchiveURI(); will(returnValue(topNodeURI_1));
            oneOf(mockAmsBridge).triggerAccessRightsRecalculation(topNodeURI_1);
            
            oneOf(mockWorkspaceMailer).sendWorkspaceFinalMessage(mockSuccessfulSubmittedWorkspace1, Boolean.TRUE, Boolean.TRUE);
            
            //loop - second iteration
            
            //logger
            allowing(mockSuccessfulSubmittedWorkspace2).getWorkspaceID(); will(returnValue(workspaceID_2));
            
            oneOf(mockSuccessfulSubmittedWorkspace2).getCrawlerID(); will(returnValue(secondCrawlerID));
            oneOf(mockCorpusStructureServiceBridge).getCrawlerState(secondCrawlerID); will(returnValue(secondCrawlerState));
            
            oneOf(mockWorkspaceDao).getAllNodeReplacements(); will(returnValue(secondNodeReplacements));
            
            oneOf(mockSuccessfulSubmittedWorkspace2).setStatus(successfulStatus);
            oneOf(mockSuccessfulSubmittedWorkspace2).setMessage(successfulMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockSuccessfulSubmittedWorkspace2);
            
            oneOf(mockWorkspaceDao).cleanWorkspaceNodesAndLinks(mockSuccessfulSubmittedWorkspace2);
            
            oneOf(mockSuccessfulSubmittedWorkspace2).getTopNodeArchiveURI(); will(returnValue(topNodeURI_2));
            oneOf(mockAmsBridge).triggerAccessRightsRecalculation(topNodeURI_2);
            
            oneOf(mockWorkspaceMailer).sendWorkspaceFinalMessage(mockSuccessfulSubmittedWorkspace2, Boolean.TRUE, Boolean.TRUE);
        }});
        
        workspaceCrawlerChecker.checkCrawlersForSubmittedWorkspaces();
    }
    
    @Test
    public void runOneFailedSubmittedWorkspaceFound() throws InterruptedException, CrawlerStateRetrievalException {
        
        final int workspaceID_1 = 10;
        
        final Collection<Workspace> submittedWorkspaces = new ArrayList<>();
        submittedWorkspaces.add(mockFailedSubmittedWorkspace);
        
        final String crawlerID = UUID.randomUUID().toString();
        final String crawlerState = "CRASHED";
        
        final Collection<WorkspaceNodeReplacement> nodeReplacements = new ArrayList<>();
        
        final WorkspaceStatus failedStatus = WorkspaceStatus.CRAWLER_ERROR;
        final String failedMessage = "Data was successfully moved to the archive but the crawler failed.";
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspacesInFinalStage(); will(returnValue(submittedWorkspaces));
            
            //loop
            
            //logger
            allowing(mockFailedSubmittedWorkspace).getWorkspaceID(); will(returnValue(workspaceID_1));
            
            oneOf(mockFailedSubmittedWorkspace).getCrawlerID(); will(returnValue(crawlerID));
            oneOf(mockCorpusStructureServiceBridge).getCrawlerState(crawlerID); will(returnValue(crawlerState));
            
            oneOf(mockWorkspaceDao).getAllNodeReplacements(); will(returnValue(nodeReplacements));
            
            oneOf(mockFailedSubmittedWorkspace).setStatus(failedStatus);
            oneOf(mockFailedSubmittedWorkspace).setMessage(failedMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockFailedSubmittedWorkspace);
            
            oneOf(mockWorkspaceMailer).sendWorkspaceFinalMessage(mockFailedSubmittedWorkspace, Boolean.FALSE, Boolean.TRUE);
        }});
        
        workspaceCrawlerChecker.checkCrawlersForSubmittedWorkspaces();
    }
    
    @Test
    public void checkCrawlersForSubmittedWorkspaces_OneSuccessfulSubmittedWorkspaceFound_WithSuccessfulVersions() throws InterruptedException, VersionCreationException, CrawlerStateRetrievalException, URISyntaxException {
        
        final int workspaceID_1 = 10;
        final URI topNodeURI_1 = new URI(UUID.randomUUID().toString());
        final String userID = "someUser";
        
        final Collection<Workspace> submittedWorkspaces = new ArrayList<>();
        submittedWorkspaces.add(mockSuccessfulSubmittedWorkspace1);
        
        final String crawlerID = UUID.randomUUID().toString();
        final String crawlerState = "SUCCESS";
        
        final Collection<WorkspaceNodeReplacement> nodeReplacements = new ArrayList<>();
        nodeReplacements.add(mockNodeReplacement1);
        nodeReplacements.add(mockNodeReplacement2);
        
        final WorkspaceStatus successfulStatus = WorkspaceStatus.DATA_MOVED_SUCCESS;
        final String successfulMessage = "Data was successfully moved to the archive and the crawler was successful.";
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspacesInFinalStage(); will(returnValue(submittedWorkspaces));
            
            //loop
            
            //logger
            allowing(mockSuccessfulSubmittedWorkspace1).getWorkspaceID(); will(returnValue(workspaceID_1));
            
            oneOf(mockSuccessfulSubmittedWorkspace1).getCrawlerID(); will(returnValue(crawlerID));
            oneOf(mockCorpusStructureServiceBridge).getCrawlerState(crawlerID); will(returnValue(crawlerState));
            
            oneOf(mockWorkspaceDao).getAllNodeReplacements(); will(returnValue(nodeReplacements));
            oneOf(mockCorpusStructureServiceBridge).createVersions(nodeReplacements);
            
            oneOf(mockSuccessfulSubmittedWorkspace1).getUserID(); will(returnValue(userID));
            oneOf(mockAmsBridge).triggerAmsNodeReplacements(nodeReplacements, userID);
            
            oneOf(mockSuccessfulSubmittedWorkspace1).setStatus(successfulStatus);
            oneOf(mockSuccessfulSubmittedWorkspace1).setMessage(successfulMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockSuccessfulSubmittedWorkspace1);
            
            oneOf(mockWorkspaceDao).cleanWorkspaceNodesAndLinks(mockSuccessfulSubmittedWorkspace1);
            
//            exactly(2).of(mockSuccessfulSubmittedWorkspace1).getTopNodeArchiveURI(); will(returnValue(topNodeURI_1));
//            oneOf(mockAmsBridge).triggerAccessRightsRecalculation(topNodeURI_1);
//            oneOf(mockAmsBridge).triggerAccessRightsRecalculationForVersionedNodes(nodeReplacements, topNodeURI_1);
            oneOf(mockSuccessfulSubmittedWorkspace1).getTopNodeArchiveURI(); will(returnValue(topNodeURI_1));
            oneOf(mockAmsBridge).triggerAccessRightsRecalculationWithVersionedNodes(topNodeURI_1, nodeReplacements);
            
            oneOf(mockWorkspaceMailer).sendWorkspaceFinalMessage(mockSuccessfulSubmittedWorkspace1, Boolean.TRUE, Boolean.TRUE);
        }});
        
        workspaceCrawlerChecker.checkCrawlersForSubmittedWorkspaces();
    }
    
    @Test
    public void checkCrawlersForSubmittedWorkspaces_OneSuccessfulSubmittedWorkspaceFound_WithFailedVersions() throws InterruptedException, VersionCreationException, CrawlerStateRetrievalException {
        
        final int workspaceID_1 = 10;
        
        final Collection<Workspace> submittedWorkspaces = new ArrayList<>();
        submittedWorkspaces.add(mockSuccessfulSubmittedWorkspace1);
        
        final String crawlerID = UUID.randomUUID().toString();
        final String crawlerState = "SUCCESS";
        
        final Collection<WorkspaceNodeReplacement> nodeReplacements = new ArrayList<>();
        nodeReplacements.add(mockNodeReplacement1);
        nodeReplacements.add(mockNodeReplacement2);
        
        final VersionCreationException expectedException = new VersionCreationException("some error with versioning", null);
        
        final WorkspaceStatus failedStatus = WorkspaceStatus.VERSIONING_ERROR;
        final String failedMessage = "Data was successfully moved to the archive, the crawler was successful but archive versioning failed.";
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspacesInFinalStage(); will(returnValue(submittedWorkspaces));
            
            //loop
            
            //logger
            allowing(mockSuccessfulSubmittedWorkspace1).getWorkspaceID(); will(returnValue(workspaceID_1));
            
            oneOf(mockSuccessfulSubmittedWorkspace1).getCrawlerID(); will(returnValue(crawlerID));
            oneOf(mockCorpusStructureServiceBridge).getCrawlerState(crawlerID); will(returnValue(crawlerState));
            
            oneOf(mockWorkspaceDao).getAllNodeReplacements(); will(returnValue(nodeReplacements));
            oneOf(mockCorpusStructureServiceBridge).createVersions(nodeReplacements); will(throwException(expectedException));
            
            oneOf(mockSuccessfulSubmittedWorkspace1).setStatus(failedStatus);
            oneOf(mockSuccessfulSubmittedWorkspace1).setMessage(failedMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockSuccessfulSubmittedWorkspace1);
            
            oneOf(mockWorkspaceMailer).sendWorkspaceFinalMessage(mockSuccessfulSubmittedWorkspace1, Boolean.TRUE, Boolean.FALSE);
        }});
        
        workspaceCrawlerChecker.checkCrawlersForSubmittedWorkspaces();
    }
    
    @Test
    public void checkCrawlersSubmittedWorkspaces_Exception() throws CrawlerStateRetrievalException {
        
        final int workspaceID_1 = 10;
        
        final Collection<Workspace> submittedWorkspaces = new ArrayList<>();
        submittedWorkspaces.add(mockSuccessfulSubmittedWorkspace1);
        
        final String crawlerID = UUID.randomUUID().toString();
        
        final CrawlerStateRetrievalException expectedException = new CrawlerStateRetrievalException("some exception message", null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspacesInFinalStage(); will(returnValue(submittedWorkspaces));
            
            //loop
            
            //logger
            allowing(mockSuccessfulSubmittedWorkspace1).getWorkspaceID(); will(returnValue(workspaceID_1));
            
            oneOf(mockSuccessfulSubmittedWorkspace1).getCrawlerID(); will(returnValue(crawlerID));
            oneOf(mockCorpusStructureServiceBridge).getCrawlerState(crawlerID); will(throwException(expectedException));
        }});
        
        try {
            workspaceCrawlerChecker.checkCrawlersForSubmittedWorkspaces();
            fail("should have thrown exception");
        } catch(CrawlerStateRetrievalException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
}