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
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.ArchiveUser;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.AccessInfoProvider;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.CorpusStructureAccessChecker;
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
 * @author guisil
 */
public class LamusCorpusStructureAccessCheckerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock AccessInfoProvider mockAccessInfoProvider;
    
    @Mock ArchiveUser mockFirstUserWithWriteAccess;
    @Mock ArchiveUser mockSecondUserWithWriteAccess;
    
    private CorpusStructureAccessChecker corpusStructureAccessChecker;
    
    public LamusCorpusStructureAccessCheckerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        corpusStructureAccessChecker = new LamusCorpusStructureAccessChecker(mockAccessInfoProvider);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void userHasWriteAccess() throws URISyntaxException {
        
        final String firstUserID = "firstUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        
        final Collection<ArchiveUser> writers = new ArrayList<ArchiveUser>();
        writers.add(mockFirstUserWithWriteAccess);
        
        context.checking(new Expectations() {{
            
            oneOf(mockAccessInfoProvider).getWriteRights(archiveNodeURI);
                will(returnValue(writers));
            
            oneOf(mockFirstUserWithWriteAccess).getUid(); will(returnValue(firstUserID));
        }});
        
        boolean result = corpusStructureAccessChecker.hasWriteAccess(firstUserID, archiveNodeURI);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void userHasWriteAccessLargerList() throws URISyntaxException {
        
        final String firstUserID = "firstUser";
        final String secondUserID = "secondUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        
        final Collection<ArchiveUser> writers = new ArrayList<ArchiveUser>();
        writers.add(mockFirstUserWithWriteAccess);
        writers.add(mockSecondUserWithWriteAccess);
        
        context.checking(new Expectations() {{
            
            oneOf(mockAccessInfoProvider).getWriteRights(archiveNodeURI);
                will(returnValue(writers));
            
            oneOf(mockFirstUserWithWriteAccess).getUid(); will(returnValue(firstUserID));
            oneOf(mockSecondUserWithWriteAccess).getUid(); will(returnValue(secondUserID));
        }});
        
        boolean result = corpusStructureAccessChecker.hasWriteAccess(secondUserID, archiveNodeURI);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void userHasNoWriteAccess() throws URISyntaxException {
        
        final String firstUserID = "firstUser";
        final String secondUserID = "secondUser";
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        
        final Collection<ArchiveUser> writers = new ArrayList<ArchiveUser>();
        writers.add(mockFirstUserWithWriteAccess);
        
        context.checking(new Expectations() {{
            
            oneOf(mockAccessInfoProvider).getWriteRights(archiveNodeURI);
                will(returnValue(writers));
            
            oneOf(mockFirstUserWithWriteAccess).getUid(); will(returnValue(firstUserID));
        }});
        
        boolean result = corpusStructureAccessChecker.hasWriteAccess(secondUserID, archiveNodeURI);
        
        assertFalse("Result should be false", result);
    }
}