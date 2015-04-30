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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.handle.util.HandleManager;
import nl.mpi.lamus.cmdi.profile.AllowedCmdiProfiles;
import nl.mpi.lamus.cmdi.profile.CmdiProfile;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataDocumentException;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HeaderInfo;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.cmdi.api.CMDIApi;
import nl.mpi.metadata.cmdi.api.CMDIConstants;
import nl.mpi.metadata.cmdi.api.model.CMDIContainerMetadataElement;
import nl.mpi.metadata.cmdi.api.model.CMDIDocument;
import nl.mpi.metadata.cmdi.api.model.CMDIMetadataElementFactory;
import nl.mpi.metadata.cmdi.api.model.Component;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import nl.mpi.metadata.cmdi.api.type.CMDIProfile;
import nl.mpi.metadata.cmdi.api.type.CMDIProfileElement;
import nl.mpi.metadata.cmdi.api.type.ComponentType;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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
import org.xml.sax.helpers.DefaultHandler;

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
    @Mock HandleManager mockHandleManager;
    @Mock CMDIMetadataElementFactory mockMetadataElementFactory;
    
    @Mock AllowedCmdiProfiles mockAllowedCmdiProfiles;
    
    
    @Mock MetadataDocument mockMetadataDocument;
    @Mock CMDIDocument mockCMDIDocument;
    @Mock File mockFile;
    @Mock StreamResult mockStreamResult;
    
    @Mock ResourceProxy mockResourceProxy;
    @Mock ResourceProxy mockAnotherResourceProxy;
    @Mock Component mockCollectionComponent;
    @Mock ComponentType mockComponentType;
    @Mock CMDIProfile mockCMDIProfile;
    @Mock CMDIProfileElement mockCMDIProfileElement;
    @Mock CMDIContainerMetadataElement mockCmdiContainerMetadataElement;
    
    
    @Factory
    public static Matcher<HeaderInfo> equivalentHeaderInfo(HeaderInfo headerInfo ) {
        return new HeaderInfoMatcher(headerInfo);
    }
    
    
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
        lamusMetadataApiBridge = new LamusMetadataApiBridge(mockMetadataAPI,
                mockWorkspaceFileHandler, mockHandleManager,
                mockMetadataElementFactory, mockAllowedCmdiProfiles);
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
    public void addSelfHandle() throws MalformedURLException, URISyntaxException, MetadataException, IOException, TransformerException {
        
        final URI handle = URI.create("11142/" + UUID.randomUUID().toString());
        final URI preparedHandle = URI.create("hdl:" + handle.toString());
        final URL targetLocation = new URL("file:/workspace/folder/file.cmdi");
        final HeaderInfo headerInfo = new HeaderInfo(CMDIConstants.CMD_HEADER_MD_SELF_LINK, preparedHandle.toString());
        
        context.checking(new Expectations() {{
            
            oneOf(mockHandleManager).prepareHandleWithHdlPrefix(handle); will(returnValue(preparedHandle));
            oneOf(mockCMDIDocument).putHeaderInformation(with(equivalentHeaderInfo(headerInfo)));
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockCMDIDocument, mockStreamResult);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
        
        lamusMetadataApiBridge.addSelfHandleAndSaveDocument(mockCMDIDocument, handle, targetLocation);
    }
    
    @Test
    public void addSelfHandle_throwsURISyntaxException() throws MalformedURLException, URISyntaxException, MetadataException, IOException, TransformerException {
        
        final URI handle = URI.create("11142/" + UUID.randomUUID().toString());
        final URI preparedHandle = URI.create("hdl:" + handle.toString());
        final URL targetLocation = new URL("file:/workspace/folder/file.cmdi");
        final HeaderInfo headerInfo = new HeaderInfo(CMDIConstants.CMD_HEADER_MD_SELF_LINK, preparedHandle.toString());
        
        final URISyntaxException expectedException = new URISyntaxException(handle.toString(), "some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockHandleManager).prepareHandleWithHdlPrefix(handle); will(throwException(expectedException));
        }});

        try {
            lamusMetadataApiBridge.addSelfHandleAndSaveDocument(mockCMDIDocument, handle, targetLocation);
            fail("should have thrown exception");
        } catch(URISyntaxException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void addSelfHandle_throwsMetadataException() throws MalformedURLException, URISyntaxException, MetadataException, IOException, TransformerException {
        
        final URI handle = URI.create("11142/" + UUID.randomUUID().toString());
        final URI preparedHandle = URI.create("hdl:" + handle.toString());
        final URL targetLocation = new URL("file:/workspace/folder/file.cmdi");
        final HeaderInfo headerInfo = new HeaderInfo(CMDIConstants.CMD_HEADER_MD_SELF_LINK, preparedHandle.toString());
        
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockHandleManager).prepareHandleWithHdlPrefix(handle); will(returnValue(preparedHandle));
            oneOf(mockCMDIDocument).putHeaderInformation(with(equivalentHeaderInfo(headerInfo))); will(throwException(expectedException));
        }});

        try {
            lamusMetadataApiBridge.addSelfHandleAndSaveDocument(mockCMDIDocument, handle, targetLocation);
            fail("should have thrown exception");
        } catch(MetadataException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void addSelfHandle_throwsIOException() throws MalformedURLException, URISyntaxException, MetadataException, IOException, TransformerException {
        
        final URI handle = URI.create("11142/" + UUID.randomUUID().toString());
        final URI preparedHandle = URI.create("hdl:" + handle.toString());
        final URL targetLocation = new URL("file:/workspace/folder/file.cmdi");
        final HeaderInfo headerInfo = new HeaderInfo(CMDIConstants.CMD_HEADER_MD_SELF_LINK, preparedHandle.toString());
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockHandleManager).prepareHandleWithHdlPrefix(handle); will(returnValue(preparedHandle));
            oneOf(mockCMDIDocument).putHeaderInformation(with(equivalentHeaderInfo(headerInfo)));
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockCMDIDocument, mockStreamResult); will(throwException(expectedException));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
        
        try {
            lamusMetadataApiBridge.addSelfHandleAndSaveDocument(mockCMDIDocument, handle, targetLocation);
            fail("should have thrown exception");
        } catch(IOException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void removeSelfHandleFromFile() throws MalformedURLException, IOException, MetadataException, TransformerException {
        
        final URL fileURL = new URL("file:/workspace/folder/file.cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL);
                will(returnValue(mockCMDIDocument));
            oneOf(mockCMDIDocument).setHandle(null);
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockCMDIDocument, mockStreamResult);
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
                    will(returnValue(mockCMDIDocument));
            oneOf(mockCMDIDocument).setHandle(null);
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockCMDIDocument, mockStreamResult);
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
            
            oneOf(mockCMDIDocument).setHandle(null);
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockCMDIDocument, mockStreamResult);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
        
        lamusMetadataApiBridge.removeSelfHandleAndSaveDocument(mockCMDIDocument, fileURL);
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
    public void metadataFileIsValid() throws MalformedURLException, IOException, MetadataException, SAXException, URISyntaxException {
        
        final URL fileURL = new URL("file:/some/location/file.cmdi");
        
        context.checking(new Expectations() {{
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL); will(returnValue(mockMetadataDocument));
            allowing(mockMetadataDocument).getFileLocation(); will(returnValue(fileURL.toURI()));
            oneOf(mockMetadataAPI).validateMetadataDocument(with(same(mockMetadataDocument)), with(any(DefaultHandler.class)));
        }});
        
        boolean result = lamusMetadataApiBridge.isMetadataFileValid(fileURL);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void metadataFileIsNotValid() throws MalformedURLException, IOException, MetadataException, SAXException, URISyntaxException {
        
        final URL fileURL = new URL("file:/some/location/file.cmdi");
        
        final SAXException expectedException = new SAXException("some exception message");
        
        context.checking(new Expectations() {{
            oneOf(mockMetadataAPI).getMetadataDocument(fileURL); will(returnValue(mockMetadataDocument));
            allowing(mockMetadataDocument).getFileLocation(); will(returnValue(fileURL.toURI()));
            oneOf(mockMetadataAPI).validateMetadataDocument(with(same(mockMetadataDocument)), with(any(DefaultHandler.class))); will(throwException(expectedException));
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
    public void metadataValid_ActuallyReadFile() throws MalformedURLException, UnsupportedEncodingException {
        
        final URL metadataFileToCheck = LamusMetadataApiBridgeTest.class.getResource("/folder with spaces/orphanCollection.cmdi");
////        final String encodedUrl = URLEncoder.encode("file:/Users/guisil/Workspaces/with spaces/orphanCollection.cmdi", "UTF-8");
//        final String fileLocation = "file:/Users/guisil/Workspaces/with spaces/orphanCollection.cmdi";
//        final String noSpaceString = fileLocation.replace(" ", "%20");
//        final URL metadataFileToCheck = new URL(noSpaceString);
        
        MetadataApiBridge testMdApiBridge = new LamusMetadataApiBridge(new CMDIApi(), null, null, null, null);
        
        boolean result = testMdApiBridge.isMetadataFileValid(metadataFileToCheck);
        
        assertTrue("Metadata file should be valid", result);
    }
    
    @Test
    public void metadataDocumentIsValid() throws MalformedURLException, IOException, MetadataException, SAXException, URISyntaxException {
        
        final URL fileURL = new URL("file:/some/location/file.cmdi");
        
        context.checking(new Expectations() {{
            allowing(mockMetadataDocument).getFileLocation(); will(returnValue(fileURL.toURI()));
            oneOf(mockMetadataAPI).validateMetadataDocument(with(same(mockMetadataDocument)), with(any(DefaultHandler.class)));
        }});
        
        boolean result = lamusMetadataApiBridge.isMetadataDocumentValid(mockMetadataDocument);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void metadataDocumentIsNotValid() throws MalformedURLException, IOException, MetadataException, SAXException, URISyntaxException {
        
        final URL fileURL = new URL("file:/some/location/file.cmdi");
        
        final SAXException expectedException = new SAXException("some exception message");
        
        context.checking(new Expectations() {{
            allowing(mockMetadataDocument).getFileLocation(); will(returnValue(fileURL.toURI()));
            oneOf(mockMetadataAPI).validateMetadataDocument(with(same(mockMetadataDocument)), with(any(DefaultHandler.class))); will(throwException(expectedException));
        }});
        
        boolean result = lamusMetadataApiBridge.isMetadataDocumentValid(mockMetadataDocument);
        
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void bothReferenceTypesAllowed() {
        
        final String profileId = "clarin.eu:cr1:p_1345561703620";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        List<String> allowedReferenceType = new ArrayList<>();
        allowedReferenceType.add("Metadata");
        allowedReferenceType.add("Resource");
        CmdiProfile profile = new CmdiProfile();
        profile.setId(profileId);
        profile.setLocation(profileLocation);
        profile.setAllowedReferenceTypes(allowedReferenceType);
        final List<CmdiProfile> profiles = new ArrayList<>();
        profiles.add(profile);
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(profiles));
        }});
        
        boolean result = lamusMetadataApiBridge.isMetadataReferenceAllowedInProfile(profileLocation);
        assertTrue("Result should be true for Metadata", result);
        
        result = lamusMetadataApiBridge.isResourceReferenceAllowedInProfile(profileLocation);
        assertTrue("Result should be true for Resource", result);
    }
    
    @Test
    public void onlyMetadataReferenceAllowed() {
        
        final String profileId = "clarin.eu:cr1:p_1345561703620";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        List<String> allowedReferenceType = new ArrayList<>();
        allowedReferenceType.add("Metadata");
        CmdiProfile profile = new CmdiProfile();
        profile.setId(profileId);
        profile.setLocation(profileLocation);
        profile.setAllowedReferenceTypes(allowedReferenceType);
        final List<CmdiProfile> profiles = new ArrayList<>();
        profiles.add(profile);
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(profiles));
        }});
        
        boolean result = lamusMetadataApiBridge.isMetadataReferenceAllowedInProfile(profileLocation);
        assertTrue("Result should be true for Metadata", result);
        
        result = lamusMetadataApiBridge.isResourceReferenceAllowedInProfile(profileLocation);
        assertFalse("Result should be false for Resource", result);
    }
    
    @Test
    public void onlyResourceReferenceAllowed() {

        final String profileId = "clarin.eu:cr1:p_1345561703620";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        List<String> allowedReferenceType = new ArrayList<>();
        allowedReferenceType.add("Resource");
        CmdiProfile profile = new CmdiProfile();
        profile.setId(profileId);
        profile.setLocation(profileLocation);
        profile.setAllowedReferenceTypes(allowedReferenceType);
        final List<CmdiProfile> profiles = new ArrayList<>();
        profiles.add(profile);
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(profiles));
        }});
        
        boolean result = lamusMetadataApiBridge.isMetadataReferenceAllowedInProfile(profileLocation);
        assertFalse("Result should be false for Metadata", result);
        
        result = lamusMetadataApiBridge.isResourceReferenceAllowedInProfile(profileLocation);
        assertTrue("Result should be true for Resource", result);
    }
    
    @Test
    public void getComponentForReferenceType_AnyReference() {
        
        final String expectedComponentPath = "/collection";
        
        final String profileId = "clarin.eu:cr1:p_1345561703620";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        Map<String, String> componentMap = new HashMap<>();
        componentMap.put("^.+$", expectedComponentPath);
        CmdiProfile profile = new CmdiProfile();
        profile.setId(profileId);
        profile.setLocation(profileLocation);
        profile.setComponentMap(componentMap);
        final List<CmdiProfile> profiles = new ArrayList<>();
        profiles.add(profile);
        
        final String referenceMimetype = "text/x-cmdi+xml";
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(profiles));
        }});
        
        String retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, referenceMimetype);
        
        assertEquals("Retrieved component different from expected", expectedComponentPath, retrievedComponentPath);
    }
    
    @Test
    public void getComponentForReferenceType_SpecificReference() {
        
        final String expectedComponentPath = "/collection";
        
        final String profileId = "clarin.eu:cr1:p_1345561703620";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        Map<String, String> componentMap = new HashMap<>();
        componentMap.put("^text/x-cmdi\\+xml$", expectedComponentPath);
        CmdiProfile profile = new CmdiProfile();
        profile.setId(profileId);
        profile.setLocation(profileLocation);
        profile.setComponentMap(componentMap);
        final List<CmdiProfile> profiles = new ArrayList<>();
        profiles.add(profile);
        
        final String referenceMimetype = "text/x-cmdi+xml";
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(profiles));
        }});
        
        String retrievedComponent = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, referenceMimetype);
        
        assertEquals("Retrieved component type different from expected", expectedComponentPath, retrievedComponent);
    }
    
    @Test
    public void getComponentForReferenceType_ReferenceNotMatched() {
        
        final String profileId = "clarin.eu:cr1:p_1345561703620";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        Map<String, String> componentMap = new HashMap<>();
        componentMap.put("^text/x-cmdi\\+xml$", "Collection");
        CmdiProfile profile = new CmdiProfile();
        profile.setId(profileId);
        profile.setLocation(profileLocation);
        profile.setComponentMap(componentMap);
        final List<CmdiProfile> profiles = new ArrayList<>();
        profiles.add(profile);
        
        final String referenceMimetype = "text/x-imdi+xml";
        
        final String expectedExceptionMessage = "No matching component type could be found for type [" + referenceMimetype + "]. Reference cannot be added to parent.";
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(profiles));
        }});
        
        try {
            lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, referenceMimetype);
        } catch(IllegalStateException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void getComponentForReferenceType_EmptyComponentMap() {
        
        final String profileId = "clarin.eu:cr1:p_1345561703620";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        Map<String, String> componentMap = new HashMap<>();
        CmdiProfile profile = new CmdiProfile();
        profile.setId(profileId);
        profile.setLocation(profileLocation);
        profile.setComponentMap(componentMap);
        final List<CmdiProfile> profiles = new ArrayList<>();
        profiles.add(profile);
        
        final String referenceMimetype = "text/x-cmdi+xml";
        
        final String expectedExceptionMessage = "CMDI Profile [" + profileId + "] has no component types configured. Reference cannot be added to parent.";
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(profiles));
        }});
        
        try {
            lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, referenceMimetype);
            fail("should have thrown exception");
        } catch(IllegalStateException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void assure_elementPathExists_IsRoot() {
        
        final String elementName = "collection";
        final String elementPath = "/" + elementName;
        
        context.checking(new Expectations() {{
            oneOf(mockCmdiContainerMetadataElement).getChildElement(elementPath); will(returnValue(null));
            oneOf(mockCmdiContainerMetadataElement).getType(); will(returnValue(mockComponentType));
            oneOf(mockCmdiContainerMetadataElement).getName(); will(returnValue(elementName));
        }});
        
        CMDIContainerMetadataElement retrievedElement = lamusMetadataApiBridge.assureElementPathExistsWithin(mockCmdiContainerMetadataElement, elementPath);
        
        assertEquals("Retrieved element different from expected", mockCmdiContainerMetadataElement, retrievedElement);
    }
    
    @Test
    public void assure_elementPathExists_IsNotRoot() {
        
        final String elementName = "MediaFile";
        final String elementPath = "/lat-session/" + elementName;
        
        context.checking(new Expectations() {{
            oneOf(mockCmdiContainerMetadataElement).getChildElement(elementPath); will(returnValue(null));
            oneOf(mockCmdiContainerMetadataElement).getType(); will(returnValue(mockComponentType));
            oneOf(mockCmdiContainerMetadataElement).getName(); will(returnValue(elementName));
        }});
        
        CMDIContainerMetadataElement retrievedElement = lamusMetadataApiBridge.assureElementPathExistsWithin(mockCmdiContainerMetadataElement, elementPath);
        
        assertEquals("Retrieved element different from expected", mockCmdiContainerMetadataElement, retrievedElement);
    }
    
    @Test
    public void assure_elementPathExistsPartially() {
        fail("not tested yet");
    }
    
    @Test
    public void assure_elementPathDoesNotExist() {
        fail("not tested yet");
    }
    
    @Test
    public void addReferenceInComponent() {
        
        final String resourceProxyId = "res_12345667764785646342";
        
        context.checking(new Expectations() {{
            oneOf(mockResourceProxy).getId(); will(returnValue(resourceProxyId));
            oneOf(mockCollectionComponent).addDocumentResourceProxyReference(resourceProxyId); will(returnValue(mockAnotherResourceProxy));
        }});
        
        ResourceProxy result = lamusMetadataApiBridge.addReferenceInComponent(mockCollectionComponent, mockResourceProxy);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Retrieved resource proxy different from expected", mockAnotherResourceProxy, result);
    }
}


class HeaderInfoMatcher extends TypeSafeMatcher<HeaderInfo> {
    
    private final String headerInfoName;
    private final String headerInfoValue;

    public HeaderInfoMatcher(HeaderInfo hInfo) {
        headerInfoName = hInfo.getName();
        headerInfoValue = hInfo.getValue();
    }

    @Override
    public boolean matchesSafely(HeaderInfo hi) {
        if(hi.getName() == null) {
            return Boolean.FALSE;
        }
        if(!hi.getName().equals(headerInfoName)) {
            return Boolean.FALSE;
        }
        
        if(hi.getValue() == null) {
            return Boolean.FALSE;
        }
        if(!hi.getValue().equals(headerInfoValue)) {
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a HeaderInfo with name ").appendValue(headerInfoName).appendText(" and value ").appendValue(headerInfoValue);
    }
}