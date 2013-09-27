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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.AccessInfo;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
//import nl.mpi.corpusstructure.AccessInfo;
//import nl.mpi.corpusstructure.ArchiveAccessContext;
//import nl.mpi.corpusstructure.ArchiveObjectsDBWrite;
//import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.util.DateTimeHelper;
import nl.mpi.lamus.workspace.exporting.CorpusStructureBridge;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.util.Checksum;
import nl.mpi.util.OurURL;
import org.apache.commons.io.FileUtils;
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
@PrepareForTest({FileUtils.class, Checksum.class})
public class LamusCorpusStructureBridgeTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
//    @Mock ArchiveObjectsDBWrite mockArchiveObjectsDBW;
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
//    @Mock ArchiveAccessContext mockArchiveAccessContext;
    @Mock AccessInfo mockAccessInfo;
    @Mock DateTimeHelper mockDateTimeHelper;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock WorkspaceAccessChecker mockWorkspaceAccessChecker;
    
    @Mock File mockFile;
    
    private CorpusStructureBridge corpusStructureBridge;
    
    public LamusCorpusStructureBridgeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        corpusStructureBridge = new LamusCorpusStructureBridge(mockCorpusStructureProvider, mockDateTimeHelper, mockArchiveFileHelper, mockWorkspaceAccessChecker);
    }
    
    @After
    public void tearDown() {
    }


//    @Test
//    public void updateArchiveObjectsNodeURLWithProperValues() throws MalformedURLException, URISyntaxException {
//        
//        final URI testArchiveNodeURI = new URI(UUID.randomUUID().toString());
//        final URL testArchiveNodeOldURL = new URL("file:/some/old.url");
//        final URL testArchiveNodeNewURL = new URL("file:/some/new.url");
//        final URI testArchiveNodeNewURI = testArchiveNodeNewURL.toURI();
//        final URI testArchiveNodeContextURI = new URI("http://corpus.something/some.url");
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockArchiveObjectsDBW).getArchiveRoots(); will(returnValue(mockArchiveAccessContext));
//            oneOf(mockArchiveAccessContext).inTableContext(testArchiveNodeNewURI); will(returnValue(testArchiveNodeContextURI));
//            
//            oneOf(mockArchiveObjectsDBW).moveArchiveObject(NodeIdUtils.TONODEID(testArchiveNodeID), testArchiveNodeContextURI); will(returnValue(Boolean.TRUE));
//        }});
//        
//        boolean result = corpusStructureBridge.updateArchiveObjectsNodeURL(testArchiveNodeID, testArchiveNodeOldURL, testArchiveNodeNewURL);
//        
//        assertTrue("Result should be true", result);
//    }
    
//    @Test
//    public void updateArchiveObjectsNodeURLWithNewURLInvalidURI() throws MalformedURLException {
//        
//        final int testArchiveNodeID = 100;
//        final URL testArchiveNodeOldURL = new URL("file:/some/old.url");
//        final URL testArchiveNodeNewURL = new URL("file:/so me/new.url");
//        
//        final String expectedErrorMessage = "new URL is not a valid URI";
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockArchiveObjectsDBW).getArchiveRoots(); will(returnValue(mockArchiveAccessContext));
//        }});
//        
//        try {
//            corpusStructureBridge.updateArchiveObjectsNodeURL(testArchiveNodeID, testArchiveNodeOldURL, testArchiveNodeNewURL);
//            fail("Should have thrown an Exception");
//        } catch(IllegalArgumentException ex) {
//            assertNotNull(ex);
//            assertEquals("Error message different from expected", expectedErrorMessage, ex.getMessage());
//            assertTrue("Cause should be a URISyntaxException", ex.getCause() instanceof URISyntaxException);
//        }
//    }
    
//    @Test
//    public void updateArchiveObjectsNodeURLWithNullNewURL() throws MalformedURLException, URISyntaxException {
//        
//        final int testArchiveNodeID = 100;
//        final URL testArchiveNodeOldURL = new URL("file:/some/old.url");
//        
//        final String expectedErrorMessage = "LamusArchiveObjectsBridge.updateArchiveObjectsNodeURL: new URL is null";
//        
//        try {
//            corpusStructureBridge.updateArchiveObjectsNodeURL(testArchiveNodeID, testArchiveNodeOldURL, null);
//            fail("Should have thrown an exception");
//        } catch(IllegalArgumentException ex) {
//            assertNotNull(ex);
//            assertEquals("Error message different from expected", expectedErrorMessage, ex.getMessage());
//            assertNull("Cause should be null", ex.getCause());
//        }
//    }
    
//    @Test
//    public void updateArchiveObjectsNodeURLWithNullOldURL() throws MalformedURLException, URISyntaxException {
//        
//        final URL testArchiveNodeNewURL = new URL("file:/some/new.url");
//        
//        boolean result = corpusStructureBridge.updateArchiveObjectsNodeURL(-1, null, testArchiveNodeNewURL);
//        
//        assertTrue("Result should be true", result);
//    }
    
//    @Test
//    public void updateArchiveObjectsNodeURLWithSimilarURLs() throws MalformedURLException, URISyntaxException {
//        
//        final URL testArchiveNodeURL = new URL("file:/some/new.url");
//        
//        boolean result = corpusStructureBridge.updateArchiveObjectsNodeURL(-1, testArchiveNodeURL, testArchiveNodeURL);
//        
//        assertTrue("Result should be true", result);
//    }
    
//    @Test
//    public void addNewNodeToCorpusStructure() throws MalformedURLException, URISyntaxException {
//        
//        final URL nodeArchiveURL = new URL("file:/archive/some/folder/node.pdf");
//        final OurURL nodeArchiveOurURL = new OurURL(nodeArchiveURL);
//        final URI nodeURI = nodeArchiveURL.toURI();
//        final URI nodeURIWithContext = new URI("file:/context/something/archive/folder/node.pdf");
//        final Date currentDate = Calendar.getInstance().getTime();
//        final Timestamp currentTimestamp = new Timestamp(currentDate.getTime());
//        
//        final String pid = UUID.randomUUID().toString();
//        
//        final String userID = "someUser";
//        
//        final boolean onsite = Boolean.TRUE;
//        
//        final long size = 1; //TODO pass the real size? it will eventually be fixed by the crawler... (?)
//        
//        final String newNodeID = "MPI111#";
//        
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockArchiveObjectsDBW).getArchiveRoots(); will(returnValue(mockArchiveAccessContext));
//            oneOf(mockArchiveAccessContext).inTableContext(nodeURI); will(returnValue(nodeURIWithContext));
//            
//            oneOf(mockDateTimeHelper).getCurrentDateTime(); will(returnValue(currentDate));
//            
//            oneOf(mockArchiveFileHelper).isUrlLocal(nodeArchiveOurURL); will(returnValue(onsite));
//            
//            
//            oneOf(mockWorkspaceAccessChecker).getDefaultAccessInfoForUser(userID); will(returnValue(mockAccessInfo));
//            
//            //TODO determine pid before???
//            
//            oneOf(mockArchiveObjectsDBW).newArchiveObject(nodeURIWithContext, pid, currentTimestamp, onsite, size, currentTimestamp, mockAccessInfo);
//                will(returnValue(newNodeID));
//        }});
//        
//        int result = corpusStructureBridge.addNewNodeToCorpusStructure(nodeArchiveURL, pid, userID);
//        
//        assertEquals("Resulting nodeID different from expected", NodeIdUtils.TOINT(newNodeID), result);
//    }
    
    @Test
    public void getChecksum() throws MalformedURLException {
        
        final URL nodeArchiveURL = new URL("file:/archive/some/url/file.cmdi");
        
        final String fakeChecksum = "thisisafakechecksum";
        
        context.checking(new Expectations() {{
            
            oneOf(mockFile).exists(); will(returnValue(Boolean.TRUE));
            oneOf(mockFile).canRead(); will(returnValue(Boolean.TRUE));
            oneOf(mockFile).isFile(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockFile).getPath(); will(returnValue(nodeArchiveURL.getPath()));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
        stub(method(Checksum.class, "create", String.class)).toReturn(fakeChecksum);
        
        String result = corpusStructureBridge.getChecksum(nodeArchiveURL);
        
        assertEquals("Returned checksum different from expected", fakeChecksum, result);
    }
    
//    @Test
//    public void ensureChecksumNewlyAddedNode() throws MalformedURLException {
//        
//        final int nodeArchiveID = 11;
//        final URL nodeArchiveURL = new URL("file:/archive/some/url/file.cmdi");
//        
//        final String fakeChecksum = "thisisafakechecksum";
//
//        
//        context.checking(new Expectations() {{
//            
//            //TODO is this necessary?
//            oneOf(mockArchiveObjectsDBW).isOnSite(NodeIdUtils.TONODEID(nodeArchiveID)); will(returnValue(Boolean.TRUE));
//            
//            oneOf(mockFile).exists(); will(returnValue(Boolean.TRUE));
//            oneOf(mockFile).canRead(); will(returnValue(Boolean.TRUE));
//            oneOf(mockFile).isFile(); will(returnValue(Boolean.TRUE));
//            oneOf(mockFile).getPath(); will(returnValue(nodeArchiveURL.getPath()));
//            
//            oneOf(mockArchiveObjectsDBW).getObjectChecksum(NodeIdUtils.TONODEID(nodeArchiveID)); will(returnValue(null));
//            
//            //TODO large file?
//            oneOf(mockArchiveObjectsDBW).setObjectChecksum(NodeIdUtils.TONODEID(nodeArchiveID), fakeChecksum);
//            
//        }});
//        
//        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
//        stub(method(Checksum.class, "create", String.class)).toReturn(fakeChecksum);
//        
//        boolean result = corpusStructureBridge.ensureChecksum(nodeArchiveID, nodeArchiveURL);
//        
//        assertTrue("Result should be true", result);
//    }
    
//    @Test
//    public void ensureChecksumUnchangedNode() throws MalformedURLException {
//        
//        final int nodeArchiveID = 11;
//        final URL nodeArchiveURL = new URL("file:/archive/some/url/file.cmdi");
//        
//        final String oldFakeChecksum = "thisistheoldfakechecksum";
//        
//        
//        context.checking(new Expectations() {{
//            
//            //TODO is this necessary
//            oneOf(mockArchiveObjectsDBW).isOnSite(NodeIdUtils.TONODEID(nodeArchiveID)); will(returnValue(Boolean.TRUE));
//            
//            oneOf(mockFile).exists(); will(returnValue(Boolean.TRUE));
//            oneOf(mockFile).canRead(); will(returnValue(Boolean.TRUE));
//            oneOf(mockFile).isFile(); will(returnValue(Boolean.TRUE));
//            oneOf(mockFile).getPath(); will(returnValue(nodeArchiveURL.getPath()));
//            
//            oneOf(mockArchiveObjectsDBW).getObjectChecksum(NodeIdUtils.TONODEID(nodeArchiveID)); will(returnValue(oldFakeChecksum));
//            
//        }});
//        
//        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
//        stub(method(Checksum.class, "create", String.class)).toReturn(oldFakeChecksum);
//        
//        boolean result = corpusStructureBridge.ensureChecksum(nodeArchiveID, nodeArchiveURL);
//        
//        assertFalse("Result should be false", result);
//    }
    
//    @Test
//    public void ensureChecksumChangedNode() throws MalformedURLException {
//        
//        final int nodeArchiveID = 11;
//        final URL nodeArchiveURL = new URL("file:/archive/some/url/file.cmdi");
//        
//        final String oldFakeChecksum = "thisistheoldfakechecksum";
//        final String newFakeChecksum = "thisisthenewfakechecksum";
//        
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockArchiveObjectsDBW).isOnSite(NodeIdUtils.TONODEID(nodeArchiveID)); will(returnValue(Boolean.TRUE));
//            
//            oneOf(mockFile).exists(); will(returnValue(Boolean.TRUE));
//            oneOf(mockFile).canRead(); will(returnValue(Boolean.TRUE));
//            oneOf(mockFile).isFile(); will(returnValue(Boolean.TRUE));
//            
//            oneOf(mockFile).getPath(); will(returnValue(nodeArchiveURL.getPath()));
//            
//            //TODO TRY TO GET OLD CHECKSUM ???
//            oneOf(mockArchiveObjectsDBW).getObjectChecksum(NodeIdUtils.TONODEID(nodeArchiveID)); will(returnValue(oldFakeChecksum));
//            
//            //TODO large file?
//            oneOf(mockArchiveObjectsDBW).setObjectChecksum(NodeIdUtils.TONODEID(nodeArchiveID), newFakeChecksum);
//            
//        }});
//        
//        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
//        stub(method(Checksum.class, "create", String.class)).toReturn(newFakeChecksum);
//        
//        boolean result = corpusStructureBridge.ensureChecksum(nodeArchiveID, nodeArchiveURL);
//        
//        assertTrue("Result should be true", result);
//    }
    
//    @Test
//    public void updateArchiveObjectsNodePID() {
//        
//        final int nodeArchiveID = 100;
//        final String protocol = "hdl:";
//        final String handlePrefix = "SOMETHING/";
//        final String completeHandle = handlePrefix + ":0000-0001";
//        final String completeHandleWithProtocol = protocol + completeHandle;
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockArchiveObjectsDBW).setArchiveObjectPid(NodeIdUtils.TONODEID(nodeArchiveID), completeHandleWithProtocol);
//        }});
//        
//        corpusStructureBridge.updateArchiveObjectsNodePID(nodeArchiveID, completeHandleWithProtocol);
//        
//    }
}