/*
 * Copyright (C) 2016 Max Planck Institute for Psycholinguistics
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
import java.util.UUID;
import nl.mpi.lamus.archive.CorpusStructureServiceBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.CrawlerInvocationException;
import nl.mpi.lamus.workspace.exporting.WorkspaceCorpusStructureExporter;
import nl.mpi.lamus.workspace.model.Workspace;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
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
import org.junit.rules.ExpectedException;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceCorpusStructureExporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Rule public ExpectedException exceptionCheck = ExpectedException.none();
    
    @Mock CorpusStructureServiceBridge mockCorpusStructureServiceBridge;
    @Mock WorkspaceDao mockWorkspaceDao;
    
    @Mock Workspace mockWorkspace;
    
    private WorkspaceCorpusStructureExporter workspaceCorpusStructureExporter;
    
    public LamusWorkspaceCorpusStructureExporterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        workspaceCorpusStructureExporter = new LamusWorkspaceCorpusStructureExporter(mockCorpusStructureServiceBridge, mockWorkspaceDao);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void triggerWorkspaceReCrawl() throws CrawlerInvocationException {
        
        final URI archiveNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final String crawlerID = UUID.randomUUID().toString();
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspace).getTopNodeArchiveURI(); will(returnValue(archiveNodeURI));
            oneOf(mockCorpusStructureServiceBridge).callCrawler(archiveNodeURI); will(returnValue(crawlerID));
            oneOf(mockWorkspace).setCrawlerID(crawlerID);
            oneOf(mockWorkspaceDao).updateWorkspaceCrawlerID(mockWorkspace);
        }});
        
        workspaceCorpusStructureExporter.triggerWorkspaceCrawl(mockWorkspace);
    }
    
    @Test
    public void triggerWorkspaceReCrawl_throwsException() throws CrawlerInvocationException {
        
        final CrawlerInvocationException expectedException = new CrawlerInvocationException("some error message", null);
        exceptionCheck.expect(Matchers.sameInstance(expectedException));
        
        final URI archiveNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspace).getTopNodeArchiveURI(); will(returnValue(archiveNodeURI));
            oneOf(mockCorpusStructureServiceBridge).callCrawler(archiveNodeURI); will(throwException(expectedException));
        }});
        
        workspaceCorpusStructureExporter.triggerWorkspaceCrawl(mockWorkspace);
    }
}
