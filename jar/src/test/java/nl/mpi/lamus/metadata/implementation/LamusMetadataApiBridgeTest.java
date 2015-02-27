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
package nl.mpi.lamus.metadata.implementation;

import com.sun.org.apache.xml.internal.utils.DefaultErrorHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataDocumentException;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HeaderInfo;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.cmdi.api.CMDIApi;
import nl.mpi.metadata.cmdi.api.CMDIConstants;
import nl.mpi.metadata.cmdi.api.model.CMDIDocument;
import org.apache.commons.io.FileUtils;
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
import org.junit.runner.RunWith;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xml.sax.SAXException;

/**
 *
 * @author guisil
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({FileUtils.class})
public class LamusMetadataApiBridgeTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private LamusMetadataApiBridge lamusMetadataApiBridge;
    
    @Mock MetadataAPI mockMetadataAPI;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    
    @Mock MetadataDocument mockMetadataDocument;
    @Mock CMDIDocument mockCmdiDocument;
    @Mock File mockFile;
    @Mock StreamResult mockStreamResult;
    
    
    public LamusMetadataApiBridgeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        lamusMetadataApiBridge = new LamusMetadataApiBridge(mockMetadataAPI, mockWorkspaceFileHandler);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getSelfHandleFromFile() throws MalformedURLException, IOException, MetadataException, URISyntaxException {
        
        final URL fileURL = new URL("file:/workspaces/upload/file.cmdi");
        final URI expectedHandle = new URI(UUID.randomUUID().toString());
        final HeaderInfo selfLink = new HeaderInfo(CMDIConstants.CMD_HEADER_MD_SELF_LINK, expectedHandle.toString());
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL);
                will(returnValue(mockMetadataDocument));
            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(fileURL.toURI()));
            oneOf(mockMetadataDocument).getHeaderInformation(CMDIConstants.CMD_HEADER_MD_SELF_LINK);
                will(returnValue(selfLink));
        }});
        
        
        URI retrievedHandle = lamusMetadataApiBridge.getSelfHandleFromFile(fileURL);
        
        assertNotNull("Retrieved handle should not be null", retrievedHandle);
        assertEquals("Retrieved handle different from expected", expectedHandle, retrievedHandle);
    }
    
    @Test
    public void getNullSelfHandleFromFile() throws MalformedURLException, IOException, MetadataException, URISyntaxException {
        
        final URL fileURL = new URL("file:/workspaces/upload/file.cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL);
                will(returnValue(mockMetadataDocument));
            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(fileURL.toURI()));
            oneOf(mockMetadataDocument).getHeaderInformation(CMDIConstants.CMD_HEADER_MD_SELF_LINK);
                will(returnValue(null));
        }});
        
        
        URI retrievedHandle = lamusMetadataApiBridge.getSelfHandleFromFile(fileURL);
        
        assertNull("Retrieved handle should be null", retrievedHandle);
    }
    
    @Test
    public void getSelfHandleFromFileThrowsIOException() throws MalformedURLException, IOException, MetadataException, MalformedURLException {
        
        final URL fileURL = new URL("file:/workspaces/upload/file.cmdi");
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL);
                will(throwException(expectedException));
        }});
        
        URI retrievedHandle = lamusMetadataApiBridge.getSelfHandleFromFile(fileURL);
        
        assertNull("Retrieved handle should be null", retrievedHandle);
    }
    
    @Test
    public void getSelfHandleFromFileThrowsMetadataException() throws MalformedURLException, IOException, MetadataException {
        
        final URL fileURL = new URL("file:/workspaces/upload/file.cmdi");
        
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL);
                will(throwException(expectedException));
        }});
        
        URI retrievedHandle = lamusMetadataApiBridge.getSelfHandleFromFile(fileURL);
        
        assertNull("Retrieved handle should be null", retrievedHandle);
    }

    @Test
    public void getSelfHandleFromFileThrowsUriException() throws MalformedURLException, IOException, MetadataException, URISyntaxException {
        
        final URL fileURL = new URL("file:/workspaces/upload/file.cmdi");
        final String invalidUriHandle = " ";
        final HeaderInfo selfLink = new HeaderInfo(CMDIConstants.CMD_HEADER_MD_SELF_LINK, invalidUriHandle);
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL);
                will(returnValue(mockMetadataDocument));
            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(fileURL.toURI()));
            oneOf(mockMetadataDocument).getHeaderInformation(CMDIConstants.CMD_HEADER_MD_SELF_LINK);
                will(returnValue(selfLink));
        }});
        
        URI retrievedHandle = lamusMetadataApiBridge.getSelfHandleFromFile(fileURL);
        
        assertNull("Retrieved handle should be null", retrievedHandle);
    }
    
    @Test
    public void getSelfHandleFromDocument() throws MalformedURLException, IOException, MetadataException, URISyntaxException {
        
        final URL fileURL = new URL("file:/workspaces/upload/file.cmdi");
        final URI expectedHandle = new URI(UUID.randomUUID().toString());
        final HeaderInfo selfLink = new HeaderInfo(CMDIConstants.CMD_HEADER_MD_SELF_LINK, expectedHandle.toString());
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(fileURL.toURI()));
            oneOf(mockMetadataDocument).getHeaderInformation(CMDIConstants.CMD_HEADER_MD_SELF_LINK);
                will(returnValue(selfLink));
        }});
        
        
        URI retrievedHandle = lamusMetadataApiBridge.getSelfHandleFromDocument(mockMetadataDocument);
        
        assertNotNull("Retrieved handle should not be null", retrievedHandle);
        assertEquals("Retrieved handle different from expected", expectedHandle, retrievedHandle);
    }
    
    @Test
    public void getNullSelfHandleFromDocument() throws MalformedURLException, IOException, MetadataException, URISyntaxException {
        
        final URL fileURL = new URL("file:/workspaces/upload/file.cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataDocument).getFileLocation(); will(returnValue(fileURL.toURI()));
            oneOf(mockMetadataDocument).getHeaderInformation(CMDIConstants.CMD_HEADER_MD_SELF_LINK);
                will(returnValue(null));
        }});
        
        
        URI retrievedHandle = lamusMetadataApiBridge.getSelfHandleFromDocument(mockMetadataDocument);
        
        assertNull("Retrieved handle should be null", retrievedHandle);
    }
    
    @Test
    public void removeSelfHandleFromFile() throws MalformedURLException, IOException, MetadataException, TransformerException {
        
        final URL fileURL = new URL("file:/workspace/folder/file.cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL);
                will(returnValue(mockCmdiDocument));
            oneOf(mockCmdiDocument).setHandle(null);
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockCmdiDocument, mockStreamResult);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
        
        lamusMetadataApiBridge.removeSelfHandleAndSaveDocument(fileURL);
    }
    
    @Test
    public void removeSelfHandleFromFile_NotHandleCarrier() throws MalformedURLException, IOException, MetadataException, TransformerException {
        
        final URL fileURL = new URL("file:/workspace/folder/file.cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL);
                will(returnValue(mockMetadataDocument));
        }});
        
        lamusMetadataApiBridge.removeSelfHandleAndSaveDocument(fileURL);
    }
    
    @Test
    public void removeSelfHandleFromFileThrowsIOException() throws MalformedURLException, IOException, MetadataException, TransformerException {
        
        final URL fileURL = new URL("file:/workspace/folder/file.cmdi");
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL);
                will(throwException(expectedException));
        }});
        
        try {
            lamusMetadataApiBridge.removeSelfHandleAndSaveDocument(fileURL);
            fail("should have thrown exception");
        } catch(IOException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void removeSelfHandleFromFileThrowsTransformerException() throws MalformedURLException, IOException, MetadataException, TransformerException {
        
        final URL fileURL = new URL("file:/workspace/folder/file.cmdi");
        
        final TransformerException expectedException = new TransformerException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL);
                    will(returnValue(mockCmdiDocument));
            oneOf(mockCmdiDocument).setHandle(null);
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockCmdiDocument, mockStreamResult);
                will(throwException(expectedException));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
        
        try {
            lamusMetadataApiBridge.removeSelfHandleAndSaveDocument(fileURL);
            fail("should have thrown exception");
        } catch(TransformerException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void removeSelfHandleFromFileThrowsMetadataException() throws MalformedURLException, IOException, MetadataException, TransformerException {
        
        final URL fileURL = new URL("file:/workspace/folder/file.cmdi");
        
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL);
                will(throwException(expectedException));
        }});
        
        try {
            lamusMetadataApiBridge.removeSelfHandleAndSaveDocument(fileURL);
            fail("should have thrown exception");
        } catch(MetadataException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void removeSelfHandleFromDocument() throws MalformedURLException, IOException, MetadataException, TransformerException {
        
        final URL fileURL = new URL("file:/workspace/folder/file.cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCmdiDocument).setHandle(null);
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockCmdiDocument, mockStreamResult);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
        
        lamusMetadataApiBridge.removeSelfHandleAndSaveDocument(mockCmdiDocument, fileURL);
    }
    
    @Test
    public void getNewSelfHandleHeaderInfo() throws URISyntaxException {
        
        final URI handle = new URI(UUID.randomUUID().toString().toUpperCase());
        
        HeaderInfo retrievedHeaderInfo = lamusMetadataApiBridge.getNewSelfHandleHeaderInfo(handle);
        
        assertNotNull("Retrieved header info should not be null", retrievedHeaderInfo);
        assertEquals("Header info name different from expected", CMDIConstants.CMD_HEADER_MD_SELF_LINK, retrievedHeaderInfo.getName());
        assertEquals("Header info value different from expected", handle.toString(), retrievedHeaderInfo.getValue());
    }
    
    @Test
    public void saveMetadataDocument() throws MalformedURLException, IOException, TransformerException, MetadataException {
        
        final URL documentURL = new URL("file:/some/location/file.cmdi");
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
        
        lamusMetadataApiBridge.saveMetadataDocument(mockMetadataDocument, documentURL);
    }
    
    @Test
    public void saveMetadataDocumentThrowsException() throws IOException, TransformerException, MetadataException {
        
        final URL documentURL = new URL("file:/some/location/file.cmdi");
        final MetadataException expectedException = new MetadataDocumentException(mockMetadataDocument, "some exception message");
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult); will(throwException(expectedException));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
        
        try {
            lamusMetadataApiBridge.saveMetadataDocument(mockMetadataDocument, documentURL);
            fail("shourd have thrown exception");
        } catch(MetadataException ex) {
            assertEquals("exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void metadataFileIsValid() throws MalformedURLException, IOException, MetadataException, SAXException {
        
        final URL fileURL = new URL("file:/some/location/file.cmdi");
        
        context.checking(new Expectations() {{
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL); will(returnValue(mockMetadataDocument));
            oneOf(mockMetadataAPI).validateMetadataDocument(with(same(mockMetadataDocument)), with(any(DefaultErrorHandler.class)));
        }});
        
        boolean result = lamusMetadataApiBridge.isMetadataFileValid(fileURL);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void metadataFileIsNotValid() throws MalformedURLException, IOException, MetadataException, SAXException {
        
        final URL fileURL = new URL("file:/some/location/file.cmdi");
        
        final SAXException expectedException = new SAXException("some exception message");
        
        context.checking(new Expectations() {{
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL); will(returnValue(mockMetadataDocument));
            oneOf(mockMetadataAPI).validateMetadataDocument(with(same(mockMetadataDocument)), with(any(DefaultErrorHandler.class))); will(throwException(expectedException));
        }});
        
        boolean result = lamusMetadataApiBridge.isMetadataFileValid(fileURL);
        
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void metadataValidationThrowsException() throws MalformedURLException, IOException, MetadataException {
        
        final URL fileURL = new URL("file:/some/location/file.cmdi");
        
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL); will(throwException(expectedException));
        }});
        
        boolean result = lamusMetadataApiBridge.isMetadataFileValid(fileURL);
        
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void metadataValid_ActuallyReadFile() throws MalformedURLException {
        
        final URL metadataFileToCheck = LamusMetadataApiBridgeTest.class.getResource("/orphanCollection.cmdi");
        
        MetadataApiBridge testMdApiBridge = new LamusMetadataApiBridge(new CMDIApi(), null);
        
        boolean result = testMdApiBridge.isMetadataFileValid(metadataFileToCheck);
        
        assertTrue("Metadata file should be valid", result);
    }
}