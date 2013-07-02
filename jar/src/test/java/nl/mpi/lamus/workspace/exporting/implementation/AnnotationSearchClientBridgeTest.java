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

import java.sql.SQLException;
import nl.mpi.annot.search.lib.SearchClient;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
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
import org.junit.runner.RunWith;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchClient.class)
public class AnnotationSearchClientBridgeTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock SearchClient mockSearchClient;
    
    private SearchClientBridge searchClientBridge;
    
    public AnnotationSearchClientBridgeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        searchClientBridge = new AnnotationSearchClientBridge(mockSearchClient);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void addNode() throws SQLException {
        
        final int testArchiveNodeID = 100;
        
        context.checking(new Expectations() {{
            
            oneOf(mockSearchClient).add(NodeIdUtils.TONODEID(testArchiveNodeID));
            oneOf(mockSearchClient).close();
        }});
        
        searchClientBridge.addNode(testArchiveNodeID);
        
        //TODO assertions
    }
    
    @Test
    public void removeNode() throws SQLException {
        
        final int testArchiveNodeID = 100;
        
        context.checking(new Expectations() {{
            oneOf(mockSearchClient).remove(NodeIdUtils.TONODEID(testArchiveNodeID));
            oneOf(mockSearchClient).close();
        }});
     
        searchClientBridge.removeNode(testArchiveNodeID);
        
        //TODO assertions
    }
    
    //TODO test exceptions
    
    @Test
    public void formatIsSearchable() {
        
        final String format = "application/pdf";
        final String[] someFormats = {"text/plain", "application/pdf"};
        
        stub(method(SearchClient.class, "getSearchableFormats")).toReturn(someFormats);
        
        boolean result = searchClientBridge.isFormatSearchable(format);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void formatIsNotSearchable() {
        
        final String format = "application/pdf";
        final String[] someFormats = {"text/plain"};
        
        stub(method(SearchClient.class, "getSearchableFormats")).toReturn(someFormats);
        
        boolean result = searchClientBridge.isFormatSearchable(format);
        
        assertFalse("Result should be false", result);
    }
}