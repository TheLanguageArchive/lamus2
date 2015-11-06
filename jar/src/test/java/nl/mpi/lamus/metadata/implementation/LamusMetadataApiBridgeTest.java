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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.handle.util.implementation.HandleConstants;
import nl.mpi.lamus.cmdi.profile.AllowedCmdiProfiles;
import nl.mpi.lamus.cmdi.profile.CmdiProfile;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataDocumentException;
import nl.mpi.metadata.api.MetadataElementException;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.HeaderInfo;
import nl.mpi.metadata.api.model.MetadataContainer;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.MetadataElement;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.type.MetadataElementType;
import nl.mpi.metadata.cmdi.api.CMDIApi;
import nl.mpi.metadata.cmdi.api.CMDIConstants;
import nl.mpi.metadata.cmdi.api.model.CMDIContainerMetadataElement;
import nl.mpi.metadata.cmdi.api.model.CMDIDocument;
import nl.mpi.metadata.cmdi.api.model.CMDIMetadataElement;
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
    @Mock HandleParser mockHandleParser;
    @Mock CMDIMetadataElementFactory mockMetadataElementFactory;
    
    @Mock AllowedCmdiProfiles mockAllowedCmdiProfiles;
    
    @Mock MetadataDocument mockMetadataDocument;
    @Mock CMDIDocument mockCMDIDocument;
    @Mock File mockFile;
    @Mock StreamResult mockStreamResult;
    @Mock ReferencingMetadataDocument mockReferencingMetadataDocument;
    @Mock Reference mockReference;
    @Mock ResourceProxy mockResourceProxy;
    @Mock ResourceProxy mockAnotherResourceProxy;
    @Mock Component mockCollectionComponent;
    @Mock Component mockMediaFileComponent;
    @Mock ComponentType mockComponentType;
    @Mock ComponentType mockAnotherComponentType;
    @Mock ComponentType mockYetAnotherComponentType;
    @Mock CMDIProfile mockCMDIProfile;
    @Mock CMDIProfileElement mockCMDIProfileElement;
    @Mock CMDIContainerMetadataElement mockCmdiContainerMetadataElement;
    @Mock CMDIContainerMetadataElement mockAnotherCmdiContainerMetadataElement;
    @Mock CMDIContainerMetadataElement mockYetAnotherCmdiContainerMetadataElement;
    @Mock MetadataContainer<CMDIMetadataElement> mockCmdiContainer;
    @Mock MetadataElement mockMetadataElement;
    @Mock MetadataElementType mockMetadataElementType;
    
    private final Map<String, String> collectionComponentsByMimetypeMap;
    private final Map<String, String> collectionComponentsByNodeTypeMap;
    private final Map<String, String> latCorpusComponentsByMimetypeMap;
    private final Map<String, String> latCorpusComponentsByNodeTypeMap;
    private final Map<String, String> latSessionComponentsByMimetypeMap;
    private final Map<String, String> latSessionComponentsByNodeTypeMap;
    private final List<CmdiProfile> aFewProfiles;
    
    @Factory
    public static Matcher<HeaderInfo> equivalentHeaderInfo(HeaderInfo headerInfo ) {
        return new HeaderInfoMatcher(headerInfo);
    }
    
    
    public LamusMetadataApiBridgeTest() {
        
        collectionComponentsByMimetypeMap = new HashMap<>();
        collectionComponentsByNodeTypeMap = new HashMap<>();
        List<String> allowedCollectionTypes = new ArrayList<>();
        allowedCollectionTypes.add("Metadata");
        allowedCollectionTypes.add("LandingPage");
        allowedCollectionTypes.add("SearchPage");
        allowedCollectionTypes.add("SearchService");
        CmdiProfile collectionProfile = new CmdiProfile();
        collectionProfile.setId("clarin.eu:cr1:p_1345561703620");
        collectionProfile.setLocation(URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1345561703620"));
        collectionProfile.setComponentsByMimetypeMap(collectionComponentsByMimetypeMap);
        collectionProfile.setComponentsByNodeTypeMap(collectionComponentsByNodeTypeMap);
        collectionProfile.setAllowedReferenceTypes(allowedCollectionTypes);
        collectionProfile.setAllowInfoLinks(Boolean.FALSE);
        
        latCorpusComponentsByMimetypeMap = new HashMap<>();
        latCorpusComponentsByMimetypeMap.put("^text/x-cmdi\\+xml$", "lat-corpus/CorpusLink");
        latCorpusComponentsByMimetypeMap.put("^info$", "lat-corpus/InfoLink");
        latCorpusComponentsByNodeTypeMap = new HashMap<>();
        latCorpusComponentsByNodeTypeMap.put("^METADATA$", "lat-corpus/CorpusLink");
        latCorpusComponentsByNodeTypeMap.put("^RESOURCE_INFO$", "lat-corpus/InfoLink");
        List<String> allowedCorpusTypes = new ArrayList<>();
        allowedCorpusTypes.add("Metadata");
        allowedCorpusTypes.add("Resource");
        allowedCorpusTypes.add("LandingPage");
        allowedCorpusTypes.add("SearchPage");
        allowedCorpusTypes.add("SearchService");
        CmdiProfile latCorpusProfile = new CmdiProfile();
        latCorpusProfile.setId("clarin.eu:cr1:p_1407745712064");
        latCorpusProfile.setLocation(URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1407745712064"));
        latCorpusProfile.setComponentsByMimetypeMap(latCorpusComponentsByMimetypeMap);
        latCorpusProfile.setComponentsByNodeTypeMap(latCorpusComponentsByNodeTypeMap);
        latCorpusProfile.setAllowedReferenceTypes(allowedCorpusTypes);
        latCorpusProfile.setAllowInfoLinks(Boolean.TRUE);
        latCorpusProfile.setDocumentNamePath("/cmd:CMD/cmd:Components/cmd:lat-corpus/cmd:Name");
        
        latSessionComponentsByMimetypeMap = new HashMap<>();
        latSessionComponentsByMimetypeMap.put("^(video|audio|image)/.*$", "lat-session/Resources/MediaFile");
        latSessionComponentsByMimetypeMap.put("^(?!.*text/x-cmdi\\+xml)(text|application)/.*$", "lat-session/Resources/WrittenResource");
        latSessionComponentsByMimetypeMap.put("^info$", "lat-session/InfoLink");
        latSessionComponentsByNodeTypeMap = new HashMap<>();
        latSessionComponentsByNodeTypeMap.put("^(RESOURCE_VIDEO|RESOURCE_AUDIO|RESOURCE_IMAGE)$", "lat-session/Resources/MediaFile");
        latSessionComponentsByNodeTypeMap.put("^RESOURCE_WRITTEN$", "lat-session/Resources/WrittenResource");
        latSessionComponentsByNodeTypeMap.put("^RESOURCE_INFO$", "lat-session/InfoLink");
        List<String> allowedSessionTypes = new ArrayList<>();
        allowedSessionTypes.add("Resource");
        allowedSessionTypes.add("LandingPage");
        allowedSessionTypes.add("SearchPage");
        allowedSessionTypes.add("SearchService");
        CmdiProfile latSessionProfile = new CmdiProfile();
        latSessionProfile.setId("clarin.eu:cr1:p_1407745712035");
        latSessionProfile.setLocation(URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1407745712035"));
        latSessionProfile.setComponentsByMimetypeMap(latSessionComponentsByMimetypeMap);
        latSessionProfile.setComponentsByNodeTypeMap(latSessionComponentsByNodeTypeMap);
        latSessionProfile.setAllowedReferenceTypes(allowedSessionTypes);
        latSessionProfile.setAllowInfoLinks(Boolean.TRUE);
        latSessionProfile.setDocumentNamePath("/cmd:CMD/cmd:Components/cmd:lat-session/cmd:Name");
        
        aFewProfiles = new ArrayList<>();
        aFewProfiles.add(collectionProfile);
        aFewProfiles.add(latCorpusProfile);
        aFewProfiles.add(latSessionProfile);
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
                mockWorkspaceFileHandler, mockHandleParser,
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
            
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(handle); will(returnValue(preparedHandle));
            oneOf(mockCMDIDocument).putHeaderInformation(with(equivalentHeaderInfo(headerInfo)));
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(mockFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockCMDIDocument, mockStreamResult);
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockFile);
        
        lamusMetadataApiBridge.addSelfHandleAndSaveDocument(mockCMDIDocument, handle, targetLocation);
    }
    
    @Test
    public void addSelfHandle_throwsMetadataException() throws MalformedURLException, URISyntaxException, MetadataException, IOException, TransformerException {
        
        final URI handle = URI.create("11142/" + UUID.randomUUID().toString());
        final URI preparedHandle = URI.create("hdl:" + handle.toString());
        final URL targetLocation = new URL("file:/workspace/folder/file.cmdi");
        final HeaderInfo headerInfo = new HeaderInfo(CMDIConstants.CMD_HEADER_MD_SELF_LINK, preparedHandle.toString());
        
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(handle); will(returnValue(preparedHandle));
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
            
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(handle); will(returnValue(preparedHandle));
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
        
        final String profileId = "clarin.eu:cr1:p_1407745712064";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
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
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        boolean result = lamusMetadataApiBridge.isMetadataReferenceAllowedInProfile(profileLocation);
        assertTrue("Result should be true for Metadata", result);
        
        result = lamusMetadataApiBridge.isResourceReferenceAllowedInProfile(profileLocation);
        assertFalse("Result should be false for Resource", result);
    }
    
    @Test
    public void onlyResourceReferenceAllowed() {

        final String profileId = "clarin.eu:cr1:p_1407745712035";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        boolean result = lamusMetadataApiBridge.isMetadataReferenceAllowedInProfile(profileLocation);
        assertFalse("Result should be false for Metadata", result);
        
        result = lamusMetadataApiBridge.isResourceReferenceAllowedInProfile(profileLocation);
        assertTrue("Result should be true for Resource", result);
    }
    
    @Test
    public void referenceTypeIsMetadata() {
        
        context.checking(new Expectations() {{
            allowing(mockReference).getType(); will(returnValue(MetadataReferenceType.REFERENCE_TYPE_METADATA));
        }});
        
        boolean result = lamusMetadataApiBridge.isReferenceTypeAPage(mockReference);
        
        assertFalse("Result should have been false for " + MetadataReferenceType.REFERENCE_TYPE_METADATA, result);
    }
    
    @Test
    public void referenceTypeIsResource() {
        
        context.checking(new Expectations() {{
            allowing(mockReference).getType(); will(returnValue(MetadataReferenceType.REFERENCE_TYPE_RESOURCE));
        }});
        
        boolean result = lamusMetadataApiBridge.isReferenceTypeAPage(mockReference);
        
        assertFalse("Result should have been false for " + MetadataReferenceType.REFERENCE_TYPE_RESOURCE, result);
    }
    
    @Test
    public void referenceTypeIsLandingPage() {
        
        context.checking(new Expectations() {{
            allowing(mockReference).getType(); will(returnValue(MetadataReferenceType.REFERENCE_TYPE_LANDING_PAGE));
        }});
        
        boolean result = lamusMetadataApiBridge.isReferenceTypeAPage(mockReference);
        
        assertTrue("Result should have been true for " + MetadataReferenceType.REFERENCE_TYPE_LANDING_PAGE, result);
    }
    
    @Test
    public void referenceTypeIsSearchPage() {
        
        context.checking(new Expectations() {{
            allowing(mockReference).getType(); will(returnValue(MetadataReferenceType.REFERENCE_TYPE_SEARCH_PAGE));
        }});
        
        boolean result = lamusMetadataApiBridge.isReferenceTypeAPage(mockReference);
        
        assertTrue("Result should have been true for " + MetadataReferenceType.REFERENCE_TYPE_SEARCH_PAGE, result);
    }
    
    @Test
    public void referenceTypeIsSearchService() {
        
        context.checking(new Expectations() {{
            allowing(mockReference).getType(); will(returnValue(MetadataReferenceType.REFERENCE_TYPE_SEARCH_SERVICE));
        }});
        
        boolean result = lamusMetadataApiBridge.isReferenceTypeAPage(mockReference);
        
        assertTrue("Result should have been true for " + MetadataReferenceType.REFERENCE_TYPE_SEARCH_SERVICE, result);
    }
    
    @Test
    public void getComponentPathForReferenceType_Metadata_LatCorpus() {
        
        final String expectedComponentPath = "lat-corpus/CorpusLink";
        
        final String profileId = "clarin.eu:cr1:p_1407745712064";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        
        final String referenceMimetype = "text/x-cmdi+xml";
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        String retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, referenceMimetype, null, Boolean.FALSE);
        
        assertEquals("Retrieved component different from expected", expectedComponentPath, retrievedComponentPath);
        
        // using the node type
        
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, null, nodeType, Boolean.FALSE);
        
        assertEquals("Retrieved component different from expected", expectedComponentPath, retrievedComponentPath);
    }
    
    @Test
    public void getComponentPathForReferenceType_Info_LatCorpus() {
        
        final String expectedComponentPath = "lat-corpus/InfoLink";
        
        final String profileId = "clarin.eu:cr1:p_1407745712064";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        
        final String referenceMimetype = "text/plain";
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        String retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, referenceMimetype, null, Boolean.TRUE);
        
        assertEquals("Retrieved component different from expected", expectedComponentPath, retrievedComponentPath);
        
        // using the node type
        
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_INFO;
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, null, nodeType, Boolean.FALSE);
        
        assertEquals("Retrieved component different from expected", expectedComponentPath, retrievedComponentPath);
    }
    
    @Test
    public void getComponentPathForReferenceType_Media_LatSession() {
        
        final String expectedComponentPath = "lat-session/Resources/MediaFile";
        
        final String profileId = "clarin.eu:cr1:p_1407745712035";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        
        final String referenceMimetype = "image/jpg";
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        String retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, referenceMimetype, null, Boolean.FALSE);
        
        assertEquals("Retrieved component different from expected", expectedComponentPath, retrievedComponentPath);
        
        // using the node type
        
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_IMAGE;
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, null, nodeType, Boolean.FALSE);
        
        assertEquals("Retrieved component different from expected", expectedComponentPath, retrievedComponentPath);
    }
    
    @Test
    public void getComponentPathForReferenceType_Written_LatSession() {
        
        final String expectedComponentPath = "lat-session/Resources/WrittenResource";
        
        final String profileId = "clarin.eu:cr1:p_1407745712035";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        
        final String referenceMimetype = "text/plain";
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        String retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, referenceMimetype, null, Boolean.FALSE);
        
        assertEquals("Retrieved component different from expected", expectedComponentPath, retrievedComponentPath);
        
        // using the node type
        
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, null, nodeType, Boolean.FALSE);
        
        assertEquals("Retrieved component different from expected", expectedComponentPath, retrievedComponentPath);
    }
    
    @Test
    public void getComponentPathForReferenceType_Info_LatSession() {
        
        final String expectedComponentPath = "lat-session/InfoLink";
        
        final String profileId = "clarin.eu:cr1:p_1407745712035";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        
        final String referenceMimetype = "text/plain";
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        String retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, referenceMimetype, null, Boolean.TRUE);
        
        assertEquals("Retrieved component different from expected", expectedComponentPath, retrievedComponentPath);
        
        // using the node type
        
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_INFO;
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, null, nodeType, Boolean.FALSE);
        
        assertEquals("Retrieved component different from expected", expectedComponentPath, retrievedComponentPath);
    }
    
    @Test
    public void getComponentPathForReferenceType_ReferenceNotMatched() {
        
        final String profileId = "clarin.eu:cr1:p_1345561703620";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        Map<String, String> componentsByMimetypeMap = new HashMap<>();
        componentsByMimetypeMap.put("^text/x-cmdi\\+xml$", "Collection");
        Map<String, String> componentsByNodeTypeMap = new HashMap<>();
        componentsByNodeTypeMap.put("^METADATA$", "Collection");
        CmdiProfile profile = new CmdiProfile();
        profile.setId(profileId);
        profile.setLocation(profileLocation);
        profile.setComponentsByMimetypeMap(componentsByMimetypeMap);
        profile.setComponentsByNodeTypeMap(componentsByNodeTypeMap);
        final List<CmdiProfile> profiles = new ArrayList<>();
        profiles.add(profile);
        
        final String referenceMimetype = "image/jpg";
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(profiles));
        }});
        
        String retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, referenceMimetype, null, Boolean.FALSE);
        
        assertNull("Retrieved component should be null", retrievedComponentPath);
        
        // using the node type
        
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_IMAGE;
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(profiles));
        }});
        
        retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, null, nodeType, Boolean.FALSE);
        
        assertNull("Retrieved component should be null", retrievedComponentPath);
    }
    
    @Test
    public void getComponentPathForReferenceType_EmptyComponentMap() {
        
        final String profileId = "clarin.eu:cr1:p_1345561703620";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        Map<String, String> componentsByMimetypeMap = new HashMap<>();
        Map<String, String> componentsByNodeTypeMap = new HashMap<>();
        CmdiProfile profile = new CmdiProfile();
        profile.setId(profileId);
        profile.setLocation(profileLocation);
        profile.setComponentsByMimetypeMap(componentsByMimetypeMap);
        profile.setComponentsByNodeTypeMap(componentsByNodeTypeMap);
        final List<CmdiProfile> profiles = new ArrayList<>();
        profiles.add(profile);
        
        final String referenceMimetype = "text/x-cmdi+xml";
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(profiles));
        }});
        
        String retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, referenceMimetype, null, Boolean.FALSE);
        
        assertNull("Retrieved component should be null", retrievedComponentPath);
        
        // using the node type
        
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(profiles));
        }});
        
        retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, null, nodeType, Boolean.FALSE);
        
        assertNull("Retrieved component should be null", retrievedComponentPath);
    }
    
    @Test
    public void getComponentPathForReferenceType_NullType() {
        
        final String profileId = "clarin.eu:cr1:p_1407745712064";
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + profileId);
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        String retrievedComponentPath = lamusMetadataApiBridge.getComponentPathForProfileAndReferenceType(profileLocation, null, null, Boolean.FALSE);
        
        assertNull("Retrieved component should be null", retrievedComponentPath);
    }
    
    @Test
    public void getComponent_Found() {
        
        final String rootName = "lat-session";
        final String elementName = "MediaFile";
        final String elementPath = "/" + rootName + "/" + elementName;
        final String firstSuffix = "[1]";
        final String elementPathWithFirstSuffix = elementPath + firstSuffix;
        final String refId = "ref_12345676543234";
        
        final Collection<ResourceProxy> componentReferences = new ArrayList<>();
        componentReferences.add(mockResourceProxy);
        
        context.checking(new Expectations() {{
            allowing(mockCmdiContainerMetadataElement).getName(); will(returnValue(rootName));
            oneOf(mockCmdiContainerMetadataElement).getChildElement(elementPathWithFirstSuffix); will(returnValue(mockMediaFileComponent));
            oneOf(mockMediaFileComponent).getReferences(); will(returnValue(componentReferences));
            oneOf(mockResourceProxy).getId(); will(returnValue(refId));
        }});
        
        Component retrievedComponent = lamusMetadataApiBridge.getComponent(mockCmdiContainerMetadataElement, elementPath, refId);
        
        assertEquals("Retrieved element different from expected", mockMediaFileComponent, retrievedComponent);
    }
    
    @Test
    public void getComponent_ChildForPathNotFound() {
        
        final String rootName = "lat-session";
        final String elementName = "MediaFile";
        final String elementPath = "/" + rootName + "/" + elementName;
        final String firstSuffix = "[1]";
        final String elementPathWithFirstSuffix = elementPath + firstSuffix;
        final String refId = "ref_12345676543234";
        
        context.checking(new Expectations() {{
            allowing(mockCmdiContainerMetadataElement).getName(); will(returnValue(rootName));
            oneOf(mockCmdiContainerMetadataElement).getChildElement(elementPathWithFirstSuffix); will(returnValue(null));
        }});
        
        Component retrievedComponent = lamusMetadataApiBridge.getComponent(mockCmdiContainerMetadataElement, elementPath, refId);
        
        assertNull("Retrieved element should be null", retrievedComponent);
    }
    
    @Test
    public void getComponent_RefNotFound() {
        
        final String rootName = "lat-session";
        final String elementName = "MediaFile";
        final String elementPath = "/" + rootName + "/" + elementName;
        final String firstSuffix = "[1]";
        final String secondSuffix = "[2]";
        final String elementPathWithFirstSuffix = elementPath + firstSuffix;
        final String elementPathWithSecondSuffix = elementPath + secondSuffix;
        final String refId = "ref_12345676543234";
        final String otherRefId = "ref_9876523442";
        
        final Collection<ResourceProxy> componentReferences = new ArrayList<>();
        componentReferences.add(mockResourceProxy);
        
        context.checking(new Expectations() {{
            allowing(mockCmdiContainerMetadataElement).getName(); will(returnValue(rootName));
            oneOf(mockCmdiContainerMetadataElement).getChildElement(elementPathWithFirstSuffix); will(returnValue(mockMediaFileComponent));
            oneOf(mockMediaFileComponent).getReferences(); will(returnValue(componentReferences));
            oneOf(mockResourceProxy).getId(); will(returnValue(otherRefId));
            
            oneOf(mockCmdiContainerMetadataElement).getChildElement(elementPathWithSecondSuffix); will(returnValue(null));
        }});
        
        Component retrievedComponent = lamusMetadataApiBridge.getComponent(mockCmdiContainerMetadataElement, elementPath, refId);
        
        assertNull("Retrieved element should be null", retrievedComponent);
    }
    
    @Test
    public void createComponent_PathExists_IsRoot() throws MetadataException {
        
        final String rootName = "collection";
        final String elementPath = "/" + rootName;
        
        context.checking(new Expectations() {{
            oneOf(mockCmdiContainerMetadataElement).getType(); will(returnValue(mockComponentType));
            oneOf(mockCmdiContainerMetadataElement).getName(); will(returnValue(rootName));
        }});
        
        CMDIContainerMetadataElement retrievedElement = lamusMetadataApiBridge.createComponentPathWithin(mockCmdiContainerMetadataElement, elementPath);
        
        assertEquals("Retrieved element different from expected", mockCmdiContainerMetadataElement, retrievedElement);
    }
    
    @Test
    public void createComponent_PathExists_IsNotRoot() throws MetadataException {
        
        final String rootName = "lat-session";
        final String elementName = "MediaFile";
        final String elementPath = "/" + rootName + "/" + elementName;
        
        context.checking(new Expectations() {{
            
            oneOf(mockCmdiContainerMetadataElement).getType(); will(returnValue(mockComponentType));
            allowing(mockCmdiContainerMetadataElement).getName(); will(returnValue(rootName));
            
            oneOf(mockComponentType).getType(elementName); will(returnValue(mockAnotherComponentType));
            oneOf(mockMetadataElementFactory).createNewMetadataElement(mockCmdiContainerMetadataElement, mockAnotherComponentType); will(returnValue(mockAnotherCmdiContainerMetadataElement));
            oneOf(mockCmdiContainerMetadataElement).addChildElement(mockAnotherCmdiContainerMetadataElement);
        }});
        
        CMDIContainerMetadataElement retrievedElement = lamusMetadataApiBridge.createComponentPathWithin(mockCmdiContainerMetadataElement, elementPath);
        
        assertEquals("Retrieved element different from expected", mockAnotherCmdiContainerMetadataElement, retrievedElement);
    }
    
    @Test
    public void createComponent_PathDoesNotExist() throws MetadataException {
        
        final String rootName = "someRoot";
        final String intermediateName = "someIntermediate";
        final String elementName = "MediaFile";
        final String elementPath = "/" + rootName + "/" + intermediateName + "/" + elementName;
        
        context.checking(new Expectations() {{
            
            oneOf(mockCmdiContainerMetadataElement).getType(); will(returnValue(mockComponentType));
            allowing(mockCmdiContainerMetadataElement).getName(); will(returnValue(rootName));
            
            oneOf(mockComponentType).getType(intermediateName); will(returnValue(mockAnotherComponentType));
            oneOf(mockCmdiContainerMetadataElement).getChildElement(intermediateName); will(returnValue(null));
            oneOf(mockMetadataElementFactory).createNewMetadataElement(mockCmdiContainerMetadataElement, mockAnotherComponentType); will(returnValue(mockAnotherCmdiContainerMetadataElement));
            oneOf(mockCmdiContainerMetadataElement).addChildElement(mockAnotherCmdiContainerMetadataElement);
            
            oneOf(mockAnotherComponentType).getType(elementName); will(returnValue(mockYetAnotherComponentType));
            oneOf(mockMetadataElementFactory).createNewMetadataElement(mockAnotherCmdiContainerMetadataElement, mockYetAnotherComponentType); will(returnValue(mockYetAnotherCmdiContainerMetadataElement));
            oneOf(mockAnotherCmdiContainerMetadataElement).addChildElement(mockYetAnotherCmdiContainerMetadataElement);
        }});
        
        CMDIContainerMetadataElement retrievedElement = lamusMetadataApiBridge.createComponentPathWithin(mockCmdiContainerMetadataElement, elementPath);
        
        assertEquals("Retrieved element different from expected", mockYetAnotherCmdiContainerMetadataElement, retrievedElement);
    }
    
    @Test
    public void addReferenceInComponent() {
        
        final String resourceProxyId = "res_12345667764785646342";
        
        context.checking(new Expectations() {{
            oneOf(mockResourceProxy).getId(); will(returnValue(resourceProxyId));
            oneOf(mockMediaFileComponent).addDocumentResourceProxyReference(resourceProxyId); will(returnValue(mockAnotherResourceProxy));
        }});
        
        ResourceProxy result = lamusMetadataApiBridge.addReferenceInComponent(mockMediaFileComponent, mockResourceProxy);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Retrieved resource proxy different from expected", mockAnotherResourceProxy, result);
    }
    
    @Test
    public void removeComponent() throws MetadataException {
        
        context.checking(new Expectations() {{
            oneOf(mockMediaFileComponent).getParent(); will(returnValue(mockCmdiContainer));
            oneOf(mockCmdiContainer).removeChildElement(mockMediaFileComponent); will(returnValue(Boolean.TRUE));
        }});
        
        boolean result = lamusMetadataApiBridge.removeComponent(mockMediaFileComponent);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void removeComponent_ThrowsException() throws MetadataException {

        context.checking(new Expectations() {{
            ignoring(mockMetadataElement);
        }});
        
        final MetadataElementException expectedException = new MetadataElementException(mockMetadataElement, "something went wrong");
        
        context.checking(new Expectations() {{
            oneOf(mockMediaFileComponent).getParent(); will(returnValue(mockCmdiContainer));
            oneOf(mockCmdiContainer).removeChildElement(mockMediaFileComponent); will(throwException(expectedException));
        }});
        
        try {
            lamusMetadataApiBridge.removeComponent(mockMediaFileComponent);
            fail("should have thrown exception");
        } catch(MetadataException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void referenceIsInfoLink() {
        
        final String componentTypeName = MetadataComponentType.COMPONENT_TYPE_INFO_LINK;
        
        final Collection<MetadataElement> elements = new ArrayList<>();
        elements.add(mockMetadataElement);
        
        context.checking(new Expectations() {{
            oneOf(mockReferencingMetadataDocument).getResourceProxyReferences(mockReference); will(returnValue(elements));
            allowing(mockMetadataElement).getType(); will(returnValue(mockMetadataElementType));
            oneOf(mockMetadataElementType).getName(); will(returnValue(componentTypeName));
        }});
        
        boolean result = lamusMetadataApiBridge.isReferenceAnInfoLink(mockReferencingMetadataDocument, mockReference);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void referenceIsNotInfoLink() {
        
        final String componentTypeName = "SomethingElse";
        
        final Collection<MetadataElement> elements = new ArrayList<>();
        elements.add(mockMetadataElement);
        
        context.checking(new Expectations() {{
            oneOf(mockReferencingMetadataDocument).getResourceProxyReferences(mockReference); will(returnValue(elements));
            allowing(mockMetadataElement).getType(); will(returnValue(mockMetadataElementType));
            oneOf(mockMetadataElementType).getName(); will(returnValue(componentTypeName));
        }});
        
        boolean result = lamusMetadataApiBridge.isReferenceAnInfoLink(mockReferencingMetadataDocument, mockReference);
        
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void infoLinkIsAllowedInProfile() {
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        boolean result = lamusMetadataApiBridge.isInfoLinkAllowedInProfile(URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1407745712064"));
        
        assertTrue("Result should be true (lat-corpus)", result);
        
        result = lamusMetadataApiBridge.isInfoLinkAllowedInProfile(URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1407745712035"));
        
        assertTrue("Result should be true (lat-session)", result);
    }
    
    @Test
    public void infoLinkIsNotAllowedInProfile() {
        
        context.checking(new Expectations() {{
            allowing(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
                
        boolean result = lamusMetadataApiBridge.isInfoLinkAllowedInProfile(URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1345561703620"));
        
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void getDocumentReferenceWithShortHandle() {
        
        final String baseHandle = "11142/" + UUID.randomUUID().toString();
        final URI handleWithShortProxy = URI.create(HandleConstants.HDL_SHORT_PROXY + ":" + baseHandle);
        final URI handleWithLongProxy = URI.create(HandleConstants.HDL_LONG_PROXY + baseHandle);
        
        // handle in reference has short proxy, so the first check is successful
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(handleWithShortProxy); will(returnValue(handleWithShortProxy));
            oneOf(mockCMDIDocument).getDocumentReferenceByURI(handleWithShortProxy); will(returnValue(mockResourceProxy));
        }});
        
        ResourceProxy retrievedReference = lamusMetadataApiBridge.getDocumentReferenceByDoubleCheckingURI(mockCMDIDocument, handleWithShortProxy);
        
        assertEquals("Retrieved reference different from expected", mockResourceProxy, retrievedReference);
        
        retrievedReference = null;
        
        // handle in reference has long proxy, so the first check is not successful, but the second one is
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(handleWithShortProxy); will(returnValue(handleWithShortProxy));
            oneOf(mockCMDIDocument).getDocumentReferenceByURI(handleWithShortProxy); will(returnValue(null));
            
            oneOf(mockHandleParser).prepareAndValidateHandleWithLongHdlPrefix(handleWithShortProxy); will(returnValue(handleWithLongProxy));
            oneOf(mockCMDIDocument).getDocumentReferenceByURI(handleWithLongProxy); will(returnValue(mockResourceProxy));
        }});
        
        retrievedReference = lamusMetadataApiBridge.getDocumentReferenceByDoubleCheckingURI(mockCMDIDocument, handleWithShortProxy);
        
        assertEquals("Retrieved reference different from expected", mockResourceProxy, retrievedReference);
    }
    
    @Test
    public void getDocumentReferenceWithLongHandle() {
        
        final String baseHandle = "11142/" + UUID.randomUUID().toString();
        final URI handleWithShortProxy = URI.create(HandleConstants.HDL_SHORT_PROXY + ":" + baseHandle);
        final URI handleWithLongProxy = URI.create(HandleConstants.HDL_LONG_PROXY + baseHandle);
        
        // handle in reference has short proxy, so the first check is successful
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(handleWithLongProxy); will(returnValue(handleWithShortProxy));
            oneOf(mockCMDIDocument).getDocumentReferenceByURI(handleWithShortProxy); will(returnValue(mockResourceProxy));
        }});
        
        ResourceProxy retrievedReference = lamusMetadataApiBridge.getDocumentReferenceByDoubleCheckingURI(mockCMDIDocument, handleWithLongProxy);
        
        assertEquals("Retrieved reference different from expected", mockResourceProxy, retrievedReference);
        
        retrievedReference = null;
        
        // handle in reference has long proxy, so the first check is not successful, but the second one is
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(handleWithLongProxy); will(returnValue(handleWithShortProxy));
            oneOf(mockCMDIDocument).getDocumentReferenceByURI(handleWithShortProxy); will(returnValue(null));
            
            oneOf(mockHandleParser).prepareAndValidateHandleWithLongHdlPrefix(handleWithLongProxy); will(returnValue(handleWithLongProxy));
            oneOf(mockCMDIDocument).getDocumentReferenceByURI(handleWithLongProxy); will(returnValue(mockResourceProxy));
        }});
        
        retrievedReference = lamusMetadataApiBridge.getDocumentReferenceByDoubleCheckingURI(mockCMDIDocument, handleWithLongProxy);
        
        assertEquals("Retrieved reference different from expected", mockResourceProxy, retrievedReference);
    }
    
    @Test
    public void getDocumentReferenceWithHandleMissingProxy() {
        
        final String baseHandle = "11142/" + UUID.randomUUID().toString();
        final URI handleWithoutProxy = URI.create(baseHandle);
        final URI handleWithShortProxy = URI.create(HandleConstants.HDL_SHORT_PROXY + ":" + baseHandle);
        final URI handleWithLongProxy = URI.create(HandleConstants.HDL_LONG_PROXY + baseHandle);
        
        // handle in reference has short proxy, so the first check is successful
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(handleWithoutProxy); will(returnValue(handleWithShortProxy));
            oneOf(mockCMDIDocument).getDocumentReferenceByURI(handleWithShortProxy); will(returnValue(mockResourceProxy));
        }});
        
        ResourceProxy retrievedReference = lamusMetadataApiBridge.getDocumentReferenceByDoubleCheckingURI(mockCMDIDocument, handleWithoutProxy);
        
        assertEquals("Retrieved reference different from expected", mockResourceProxy, retrievedReference);
        
        retrievedReference = null;
        
        // handle in reference has long proxy, so the first check is not successful, but the second one is
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(handleWithoutProxy); will(returnValue(handleWithShortProxy));
            oneOf(mockCMDIDocument).getDocumentReferenceByURI(handleWithShortProxy); will(returnValue(null));
            
            oneOf(mockHandleParser).prepareAndValidateHandleWithLongHdlPrefix(handleWithoutProxy); will(returnValue(handleWithLongProxy));
            oneOf(mockCMDIDocument).getDocumentReferenceByURI(handleWithLongProxy); will(returnValue(mockResourceProxy));
        }});
        
        retrievedReference = lamusMetadataApiBridge.getDocumentReferenceByDoubleCheckingURI(mockCMDIDocument, handleWithoutProxy);
        
        assertEquals("Retrieved reference different from expected", mockResourceProxy, retrievedReference);
    }
    
    @Test
    public void getDocumentReferenceWithNoHandle() {
        
        final URI uri = URI.create("http://some/url/to/the/file.txt");
        
        // not a handle, so it doesn't add any proxy - in this case the reference is found anyway (could be an external node)
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(uri); will(throwException(new IllegalArgumentException()));
            oneOf(mockCMDIDocument).getDocumentReferenceByURI(uri); will(returnValue(mockResourceProxy));
        }});
        
        ResourceProxy retrievedReference = lamusMetadataApiBridge.getDocumentReferenceByDoubleCheckingURI(mockCMDIDocument, uri);
        
        assertEquals("Retrieved reference different from expected", mockResourceProxy, retrievedReference);
        
        // in this case the reference is not found
        
        context.checking(new Expectations() {{
            oneOf(mockHandleParser).prepareAndValidateHandleWithHdlPrefix(uri); will(throwException(new IllegalArgumentException()));
            oneOf(mockCMDIDocument).getDocumentReferenceByURI(uri); will(returnValue(null));
        }});
        
        retrievedReference = lamusMetadataApiBridge.getDocumentReferenceByDoubleCheckingURI(mockCMDIDocument, uri);
        
        assertNull("Retrieved reference should be null", retrievedReference);
    }
    
    @Test
    public void getDocumentNameForProfile_Mapped() {
        
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1407745712064");
        final String profileNamePath = "/cmd:CMD/cmd:Components/cmd:lat-corpus/cmd:Name";
        final String expectedName = "ThisNodeName";
        
        context.checking(new Expectations() {{
            oneOf(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
            oneOf(mockMetadataDocument).getChildElement(profileNamePath); will(returnValue(mockMetadataElement));
            oneOf(mockMetadataElement).getDisplayValue(); will(returnValue(expectedName));
        }});
        
        String retrievedName = lamusMetadataApiBridge.getDocumentNameForProfile(mockMetadataDocument, profileLocation);
        
        assertEquals("Retrieved name different from expected", expectedName, retrievedName);
    }
    
    @Test
    public void getDocumentNameForProfile_NotMapped() {
        
        final URI profileLocation = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1345561703620");
        
        context.checking(new Expectations() {{
            oneOf(mockAllowedCmdiProfiles).getProfiles(); will(returnValue(aFewProfiles));
        }});
        
        String retrievedName = lamusMetadataApiBridge.getDocumentNameForProfile(mockMetadataDocument, profileLocation);
        
        assertNull("Retrieved name should be null", retrievedName);
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