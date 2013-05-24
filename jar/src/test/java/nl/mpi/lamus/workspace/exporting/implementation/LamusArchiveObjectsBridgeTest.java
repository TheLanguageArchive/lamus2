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
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.ArchiveObjectsDBWrite;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.workspace.exporting.ArchiveObjectsBridge;
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
public class LamusArchiveObjectsBridgeTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock ArchiveObjectsDBWrite mockArchiveObjectsDBW;
    @Mock ArchiveAccessContext mockArchiveAccessContext;
    
    private ArchiveObjectsBridge archiveObjectsBridge;
    
    public LamusArchiveObjectsBridgeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        archiveObjectsBridge = new LamusArchiveObjectsBridge(mockArchiveObjectsDBW);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void updateArchiveObjectsNodeURLWithProperValues() throws MalformedURLException, URISyntaxException {
        
        final int testArchiveNodeID = 100;
        final URL testArchiveNodeOldURL = new URL("file:/some/old.url");
        final URL testArchiveNodeNewURL = new URL("file:/some/new.url");
        final URI testArchiveNodeNewURI = testArchiveNodeNewURL.toURI();
        final URI testArchiveNodeContextURI = new URI("http://corpus.something/some.url");
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveObjectsDBW).getArchiveRoots(); will(returnValue(mockArchiveAccessContext));
            oneOf(mockArchiveAccessContext).inTableContext(testArchiveNodeNewURI); will(returnValue(testArchiveNodeContextURI));
            
            oneOf(mockArchiveObjectsDBW).moveArchiveObject(NodeIdUtils.TONODEID(testArchiveNodeID), testArchiveNodeContextURI); will(returnValue(Boolean.TRUE));
        }});
        
        boolean result = archiveObjectsBridge.updateArchiveObjectsNodeURL(testArchiveNodeID, testArchiveNodeOldURL, testArchiveNodeNewURL);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void updateArchiveObjectsNodeURLWithNewURLInvalidURI() throws MalformedURLException {
        
        final int testArchiveNodeID = 100;
        final URL testArchiveNodeOldURL = new URL("file:/some/old.url");
        final URL testArchiveNodeNewURL = new URL("file:/so me/new.url");
        
        final String expectedErrorMessage = "new URL is not a valid URI";
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveObjectsDBW).getArchiveRoots(); will(returnValue(mockArchiveAccessContext));
        }});
        
        try {
            archiveObjectsBridge.updateArchiveObjectsNodeURL(testArchiveNodeID, testArchiveNodeOldURL, testArchiveNodeNewURL);
            fail("Should have thrown an Exception");
        } catch(IllegalArgumentException ex) {
            assertNotNull(ex);
            assertEquals("Error message different from expected", expectedErrorMessage, ex.getMessage());
            assertTrue("Cause should be a URISyntaxException", ex.getCause() instanceof URISyntaxException);
        }
    }
    
    @Test
    public void updateArchiveObjectsNodeURLWithNullNewURL() throws MalformedURLException, URISyntaxException {
        
        final int testArchiveNodeID = 100;
        final URL testArchiveNodeOldURL = new URL("file:/some/old.url");
        
        final String expectedErrorMessage = "LamusArchiveObjectsBridge.updateArchiveObjectsNodeURL: new URL is null";
        
        try {
            archiveObjectsBridge.updateArchiveObjectsNodeURL(testArchiveNodeID, testArchiveNodeOldURL, null);
            fail("Should have thrown an exception");
        } catch(IllegalArgumentException ex) {
            assertNotNull(ex);
            assertEquals("Error message different from expected", expectedErrorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void updateArchiveObjectsNodeURLWithNullOldURL() throws MalformedURLException, URISyntaxException {
        
        final URL testArchiveNodeNewURL = new URL("file:/some/new.url");
        
        boolean result = archiveObjectsBridge.updateArchiveObjectsNodeURL(-1, null, testArchiveNodeNewURL);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void updateArchiveObjectsNodeURLWithSimilarURLs() throws MalformedURLException, URISyntaxException {
        
        final URL testArchiveNodeURL = new URL("file:/some/new.url");
        
        boolean result = archiveObjectsBridge.updateArchiveObjectsNodeURL(-1, testArchiveNodeURL, testArchiveNodeURL);
        
        assertTrue("Result should be true", result);
    }
    
}