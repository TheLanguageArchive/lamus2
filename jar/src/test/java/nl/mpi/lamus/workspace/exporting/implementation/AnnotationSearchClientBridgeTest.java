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

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
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
    public void testSomeMethod() throws SQLException {
        
        final int testArchiveNodeID = 100;
        
        context.checking(new Expectations() {{
            oneOf(mockSearchClient).remove(NodeIdUtils.TONODEID(testArchiveNodeID));
            oneOf(mockSearchClient).close();
        }});
     
        searchClientBridge.removeNode(testArchiveNodeID);
        
        //TODO assert what?
    }
    
    //TODO test exceptions
}