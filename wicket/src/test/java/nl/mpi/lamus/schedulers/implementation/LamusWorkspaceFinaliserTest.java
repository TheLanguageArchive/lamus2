package nl.mpi.lamus.schedulers.implementation;

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


import nl.mpi.lamus.exception.CrawlerStateRetrievalException;
import nl.mpi.lamus.workspace.exporting.WorkspaceCrawlerChecker;
import nl.mpi.lamus.schedulers.WorkspaceFinaliser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceFinaliserTest {
    
    @Mock WorkspaceCrawlerChecker mockWorkspaceCrawlerChecker;
    
    private WorkspaceFinaliser workspaceFinaliser;
    
    
    public LamusWorkspaceFinaliserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        MockitoAnnotations.initMocks(this);
        
        workspaceFinaliser = new LamusWorkspaceFinaliser(mockWorkspaceCrawlerChecker);
    }
    
    @After
    public void tearDown() {
    }
    

    @Test
    public void checkAndFinaliseWorkspaces() throws CrawlerStateRetrievalException {

        workspaceFinaliser.checkAndFinaliseWorkspaces();
        
        verify(mockWorkspaceCrawlerChecker).checkCrawlersForSubmittedWorkspaces();
    }
    
    @Test
    public void checkAndFinaliseWorkspaces_Exception() throws CrawlerStateRetrievalException {
        
        final CrawlerStateRetrievalException expectedException = new CrawlerStateRetrievalException("some exception message", null);

        doThrow(expectedException).when(mockWorkspaceCrawlerChecker).checkCrawlersForSubmittedWorkspaces();
        
        try {
            workspaceFinaliser.checkAndFinaliseWorkspaces();
            fail("should have thrown exception");
        } catch(CrawlerStateRetrievalException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
        
        verify(mockWorkspaceCrawlerChecker).checkCrawlersForSubmittedWorkspaces();
    }
}