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
package nl.mpi.lamus.archive.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchivePidHelper;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
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
public class LamusArchivePidHelperTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    private ArchivePidHelper archivePidHelper;
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    
    @Mock CorpusNode mockCorpusNode;
    
    
    public LamusArchivePidHelperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        archivePidHelper = new LamusArchivePidHelper(mockCorpusStructureProvider, mockNodeResolver);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getPidForNullNode() throws URISyntaxException {
        
        final URI nodeUri = new URI("node:001");
        
        final String expectedMessage = "Node with URI '" + nodeUri + "' not found";
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(nodeUri); will(returnValue(null));
        }});
        
        try {
            archivePidHelper.getPidForNode(nodeUri);
            fail("should have thrown an exception");
        } catch(NodeNotFoundException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception node URI different from expected", nodeUri, ex.getNode());
        }
    }
    
    @Test
    public void getPidForNullPid() throws URISyntaxException, NodeNotFoundException {
        
        final URI nodeUri = new URI("node:001");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(nodeUri); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getPID(mockCorpusNode); will(returnValue(null));
        }});
        
        URI retrievedPid = archivePidHelper.getPidForNode(nodeUri);
        
        assertNull("Retrieved PID should be null", retrievedPid);
    }
    
    @Test
    public void getPidForNode() throws URISyntaxException, NodeNotFoundException {
        
        final URI nodeUri = new URI("node:001");
        final URI expectedPid = new URI("hdl:" + UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(nodeUri); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getPID(mockCorpusNode); will(returnValue(expectedPid));
        }});
        
        URI retrievedPid = archivePidHelper.getPidForNode(nodeUri);
        
        assertEquals("Retrieved PID different from expected", expectedPid, retrievedPid);
    }
}