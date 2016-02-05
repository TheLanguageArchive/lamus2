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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import javax.xml.transform.TransformerException;
import net.handle.hdllib.HandleException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.handle.util.HandleManager;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.lamus.archive.ArchiveHandleHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataException;
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
public class LamusArchiveHandleHelperTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    private ArchiveHandleHelper archiveHandleHelper;
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock HandleManager mockHandleManager;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock MetadataApiBridge mockMetadataApiBridge;
    @Mock NodeUtil mockNodeUtil;
    @Mock HandleParser mockHandleParser;
    
    @Mock CorpusNode mockCorpusNode;
    @Mock WorkspaceNode mockWorkspaceNode;
    
    
    public LamusArchiveHandleHelperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        archiveHandleHelper = new LamusArchiveHandleHelper(
                mockCorpusStructureProvider, mockNodeResolver,
                mockHandleManager, mockWorkspaceDao,
                mockMetadataApiBridge, mockNodeUtil, mockHandleParser);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getArchiveHandleForNullNode() {
        
        final URI nodeUri = URI.create("node:001");
        
        final String expectedMessage = "Node with URI '" + nodeUri + "' not found";
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(nodeUri); will(returnValue(null));
        }});
        
        try {
            archiveHandleHelper.getArchiveHandleForNode(nodeUri);
            fail("should have thrown an exception");
        } catch(NodeNotFoundException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception node URI different from expected", nodeUri, ex.getNode());
        }
    }
    
    @Test
    public void getArchiveHandleForNullPid() throws NodeNotFoundException {
        
        final URI nodeUri = URI.create("node:001");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(nodeUri); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getPID(mockCorpusNode); will(returnValue(null));
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(null); will(returnValue(null));
        }});
        
        URI retrievedPid = archiveHandleHelper.getArchiveHandleForNode(nodeUri);
        
        assertNull("Retrieved handle should be null", retrievedPid);
    }
    
    @Test
    public void getArchiveHandleForNode() throws NodeNotFoundException {
        
        final URI nodeUri = URI.create("node:001");
        final String baseHandle = UUID.randomUUID().toString();
        final URI someIntermediateHandle = URI.create(baseHandle);
        final URI expectedHandle = URI.create("hdl:" + baseHandle);
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(nodeUri); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getPID(mockCorpusNode); will(returnValue(someIntermediateHandle));
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(someIntermediateHandle); will(returnValue(expectedHandle));
        }});
        
        URI retrievedPid = archiveHandleHelper.getArchiveHandleForNode(nodeUri);
        
        assertEquals("Retrieved handle different from expected", expectedHandle, retrievedPid);
    }
    
    @Test
    public void deleteHandleResource()
            throws HandleException, IOException, TransformerException, MetadataException {
        
        final URL location = new URL("file:/location/file.txt");
        final URI archiveHandle = URI.create("hdl:" + UUID.randomUUID().toString());
        final URI archiveHandleWithoutHdl = URI.create(archiveHandle.getSchemeSpecificPart());
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(archiveHandle));
            oneOf(mockHandleManager).deleteHandle(archiveHandleWithoutHdl);
            oneOf(mockNodeUtil).isNodeMetadata(mockWorkspaceNode); will(returnValue(Boolean.FALSE));
        }});
        
        archiveHandleHelper.deleteArchiveHandleFromServerAndFile(mockWorkspaceNode, location);
    }
    
    @Test
    public void deleteHandleMetadata()
            throws HandleException, IOException, TransformerException, MetadataException {
        
        final URI archiveHandle = URI.create("hdl:" + UUID.randomUUID().toString());
        final URI archiveHandleWithoutHdl = URI.create(archiveHandle.getSchemeSpecificPart());
        final URL location = new URL("file:/workspace/location/r_node.cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(archiveHandle));
            oneOf(mockHandleManager).deleteHandle(archiveHandleWithoutHdl);
            oneOf(mockNodeUtil).isNodeMetadata(mockWorkspaceNode); will(returnValue(Boolean.TRUE));
            oneOf(mockMetadataApiBridge).removeSelfHandleAndSaveDocument(location);
        }});
        
        archiveHandleHelper.deleteArchiveHandleFromServerAndFile(mockWorkspaceNode, location);
    }
    
    @Test
    public void deleteHandle_throwsHandleException()
            throws HandleException, IOException, TransformerException, MetadataException {
        
        final URL location = new URL("file:/location/file.txt");
        final URI archiveHandle = URI.create("hdl:" + UUID.randomUUID().toString());
        final URI archiveHandleWithoutHdl = URI.create(archiveHandle.getSchemeSpecificPart());
        
        final HandleException expectedException = new HandleException(HandleException.CANNOT_CONNECT_TO_SERVER);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(archiveHandle));
            oneOf(mockHandleManager).deleteHandle(archiveHandleWithoutHdl); will(throwException(expectedException));
        }});
        
        try {
            archiveHandleHelper.deleteArchiveHandleFromServerAndFile(mockWorkspaceNode, location);
            fail("should have thrown an exception");
        } catch(HandleException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void deleteHandle_throwsIOException()
            throws HandleException, IOException, TransformerException, MetadataException {
        
        final URL location = new URL("file:/location/file.txt");
        final URI archiveHandle = URI.create("hdl:" + UUID.randomUUID().toString());
        final URI archiveHandleWithoutHdl = URI.create(archiveHandle.getSchemeSpecificPart());
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(archiveHandle));
            oneOf(mockHandleManager).deleteHandle(archiveHandleWithoutHdl); will(throwException(expectedException));
        }});
        
        try {
            archiveHandleHelper.deleteArchiveHandleFromServerAndFile(mockWorkspaceNode, location);
            fail("should have thrown an exception");
        } catch(IOException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void deleteHandle_throwsTransformerException()
            throws HandleException, IOException, TransformerException, MetadataException {
        
        final URI archiveHandle = URI.create("hdl:" + UUID.randomUUID().toString());
        final URI archiveHandleWithoutHdl = URI.create(archiveHandle.getSchemeSpecificPart());
        final URL location = new URL("file:/trash/location/r_node.cmdi");
        
        final TransformerException expectedException = new TransformerException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(archiveHandle));
            oneOf(mockHandleManager).deleteHandle(archiveHandleWithoutHdl);
            oneOf(mockNodeUtil).isNodeMetadata(mockWorkspaceNode); will(returnValue(Boolean.TRUE));
            oneOf(mockMetadataApiBridge).removeSelfHandleAndSaveDocument(location); will(throwException(expectedException));
        }});
        
        try {
            archiveHandleHelper.deleteArchiveHandleFromServerAndFile(mockWorkspaceNode, location);
            fail("should have thrown an exception");
        } catch(TransformerException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void deleteHandle_throwsMetadataException()
            throws HandleException, IOException, TransformerException, MetadataException {
        
        final URI archiveHandle = URI.create("hdl:" + UUID.randomUUID().toString());
        final URI archiveHandleWithoutHdl = URI.create(archiveHandle.getSchemeSpecificPart());
        final URL location = new URL("file:/trash/location/r_node.cmdi");
        
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(archiveHandle));
            oneOf(mockHandleManager).deleteHandle(archiveHandleWithoutHdl);
            oneOf(mockNodeUtil).isNodeMetadata(mockWorkspaceNode); will(returnValue(Boolean.TRUE));
            oneOf(mockMetadataApiBridge).removeSelfHandleAndSaveDocument(location); will(throwException(expectedException));
        }});
        
        try {
            archiveHandleHelper.deleteArchiveHandleFromServerAndFile(mockWorkspaceNode, location);
            fail("should have thrown an exception");
        } catch(MetadataException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
}