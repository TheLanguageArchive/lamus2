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
package nl.mpi.lamus.workspace.management.implementation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.CorpusStructureBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.PreLockedNodeException;
import nl.mpi.lamus.workspace.management.PreLockChecker;
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

/**
 *
 * @author guisil
 */
public class LamusPreLockCheckerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock CorpusStructureBridge mockCorpusStructureBridge;
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock WorkspaceDao mockWorkspaceDao;
    
    private PreLockChecker preLockChecker;
    
    
    public LamusPreLockCheckerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        preLockChecker = new LamusPreLockChecker(
                mockCorpusStructureBridge, mockCorpusStructureProvider,
                mockNodeResolver, mockWorkspaceDao);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void throwsException_NullUri() throws Exception {

        String exceptionMessage = "Node URI cannot be null";
        
        try {
            preLockChecker.ensureNoNodesInPathArePreLocked(null);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", exceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void emptyList() throws PreLockedNodeException {
        
        final URI nodeURI = URI.create("hdl:12345/" + UUID.randomUUID().toString());
        final Collection<URI> emptyList = new ArrayList<>();
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureBridge).getURIsOfAncestorsAndDescendants(nodeURI); will(returnValue(emptyList));
        }});
        
        preLockChecker.ensureNoNodesInPathArePreLocked(nodeURI);
    }
    
    @Test
    public void withSomePreLockedNodes() {
        
        final URI nodeURI = URI.create("hdl:12345/" + UUID.randomUUID().toString());
        final URI ancestorURI = URI.create("hdl:12345/" + UUID.randomUUID().toString());
        final URI descendantURI = URI.create("hdl:12345/" + UUID.randomUUID().toString());
        final List<String> ancestorsAndDescendants = new ArrayList<>();
        ancestorsAndDescendants.add(ancestorURI.toString());
        ancestorsAndDescendants.add(descendantURI.toString());
        String expectedMessage = "A workspace is already being created in the path of node " + nodeURI;
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureBridge).getURIsOfAncestorsAndDescendants(nodeURI); will(returnValue(ancestorsAndDescendants));
            oneOf(mockWorkspaceDao).isAnyOfNodesPreLocked(ancestorsAndDescendants); will(returnValue(Boolean.TRUE));
        }});
        
        try {
            preLockChecker.ensureNoNodesInPathArePreLocked(nodeURI);
            fail("should have thrown exception");
        } catch(PreLockedNodeException ex) {
            assertEquals("Exception node URI different from expected", nodeURI, ex.getNodeURI());
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void withoutPreLockedNodes() throws PreLockedNodeException {
        
        final URI nodeURI = URI.create("hdl:12345/" + UUID.randomUUID().toString());
        final URI ancestorURI = URI.create("hdl:12345/" + UUID.randomUUID().toString());
        final URI descendantURI = URI.create("hdl:12345/" + UUID.randomUUID().toString());
        final List<String> ancestorsAndDescendants = new ArrayList<>();
        ancestorsAndDescendants.add(ancestorURI.toString());
        ancestorsAndDescendants.add(descendantURI.toString());
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusStructureBridge).getURIsOfAncestorsAndDescendants(nodeURI); will(returnValue(ancestorsAndDescendants));
            oneOf(mockWorkspaceDao).isAnyOfNodesPreLocked(ancestorsAndDescendants); will(returnValue(Boolean.FALSE));
        }});
        
        preLockChecker.ensureNoNodesInPathArePreLocked(nodeURI);
    }
}
