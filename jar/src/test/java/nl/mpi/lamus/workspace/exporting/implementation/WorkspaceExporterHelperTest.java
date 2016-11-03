/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.archive.CorpusStructureBridge;
import nl.mpi.lamus.cmdi.profile.AllowedCmdiProfiles;
import nl.mpi.lamus.cmdi.profile.CmdiProfile;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.exporting.ExporterHelper;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
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
public class WorkspaceExporterHelperTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock NodeUtil mockNodeUtil;
    @Mock CorpusStructureBridge mockCorpusStructureBridge;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock ArchiveFileLocationProvider mockArchiveFileLocationProvider;
    @Mock AllowedCmdiProfiles mockAllowedCmdiProfiles;
    @Mock  WorkspaceDao mockWorkspaceDao;
    
    @Mock WorkspaceNode mockCurrentNode;
    @Mock WorkspaceNode mockParentNode;
    @Mock CmdiProfile mockCmdiProfile;
    @Mock MetadataApiBridge metadataApiBridge;

    
    private ExporterHelper exporterHelper;
    
    
    public WorkspaceExporterHelperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        exporterHelper = new WorkspaceExporterHelper(mockNodeUtil, mockCorpusStructureBridge, mockArchiveFileHelper, mockAllowedCmdiProfiles, mockArchiveFileLocationProvider, mockWorkspaceDao);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getNamePathToUseForThisExporter_NullParentPath() throws MalformedURLException {
        
        // don't accept null
        
        String expectedExceptionMessage = "The name path closest top node should be provided to this exporter (" + AddedNodeExporter.class.toString() + ").";
        
        addLogExpectationsGenericNode();
        
        try {
            exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, null, Boolean.FALSE, AddedNodeExporter.class);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
        
        // null, but not metadata
        
        final String currentNodeName = "CurrentNode";
        expectedExceptionMessage = "The name path closest top node should have been bootstrapped before the current node (" + currentNodeName + ").";
        
        addLogExpectationsGenericNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.FALSE));
            oneOf(mockCurrentNode).getName(); will(returnValue(currentNodeName));
        }});
        
        try {
            exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, null, Boolean.TRUE, AddedNodeExporter.class);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
        
        // metadata, accept null
        
        final String currentNodePath = "TopNode/ParentNode";
        
        addLogExpectationsMetadataNode();
        
        context.checking(new Expectations() {{
            exactly(2).of(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(URI.create("hdl:11142/" + UUID.randomUUID().toString())));
            oneOf(mockCorpusStructureBridge).getCorpusNamePathToClosestTopNode(mockCurrentNode); will(returnValue(currentNodePath));
        }});
        
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, null, Boolean.TRUE, GeneralNodeExporter.class);
        
        assertEquals("Result different from expected", currentNodePath, result);
    }
    
    @Test
    public void getNamePathToUseForThisExporter_IgnoreCorpusPath() throws MalformedURLException {
        final String parentProfileSchemaStr = "uri:schema";
        final URI parentProfileSchema = URI.create(parentProfileSchemaStr);
        final String parentTranslatedType = "session";
    	
        String expectedPath = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        addLogExpectationsMetadataNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(null));
        }});
        
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, CorpusStructureBridge.IGNORE_CORPUS_PATH, Boolean.TRUE, UnlinkedNodeExporter.class);

        assertEquals("Result different from expected", expectedPath, result);
        
        final String sessionName = "session test name (mock)";
        final String sessionFolderName = "session_test_name_mock_";
        
        addLogExpectationsMetadataNode();

        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(URI.create("hdl:11142/" + UUID.randomUUID().toString())));
            oneOf(mockCurrentNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockCurrentNode).getProfileSchemaURI(); will(returnValue(parentProfileSchema));
            oneOf(mockAllowedCmdiProfiles).getProfile(parentProfileSchemaStr); will(returnValue(mockCmdiProfile));
            oneOf(mockCmdiProfile).getTranslateType(); will(returnValue(parentTranslatedType));
            oneOf(mockParentNode).getWorkspaceNodeID();
            oneOf(mockWorkspaceDao).getDescendantWorkspaceNodesByType(0, WorkspaceNodeType.METADATA);
            oneOf(mockParentNode).getName(); will(returnValue(sessionName));
            oneOf(mockArchiveFileHelper).correctPathElement(sessionName, "getNamePathToUseForThisExporter"); will(returnValue(sessionFolderName));
        }});
        
        result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, CorpusStructureBridge.IGNORE_CORPUS_PATH, Boolean.TRUE, UnlinkedNodeExporter.class);

        assertEquals("Result different from expected", expectedPath, result);
        
        addLogExpectationsMetadataNode();

        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(URI.create("hdl:11142/" + UUID.randomUUID().toString())));
            oneOf(mockCurrentNode).getArchiveURI(); will(returnValue(URI.create("hdl:11142/" + UUID.randomUUID().toString())));
            oneOf(mockCurrentNode).getProfileSchemaURI(); will(returnValue(parentProfileSchema));
            oneOf(mockAllowedCmdiProfiles).getProfile(parentProfileSchemaStr); will(returnValue(mockCmdiProfile));
            oneOf(mockCorpusStructureBridge).getCorpusNamePathToClosestTopNode(mockCurrentNode); will(returnValue("parent"));
        }});
        
        result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, CorpusStructureBridge.IGNORE_CORPUS_PATH, Boolean.TRUE, UnlinkedNodeExporter.class);
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getNamePathToUseForThisExporter_WrittenResource() {
        
        final String parentNamePathToClosestTopNode = "TopNode/GrandParentNode";
        final String expectedPath = parentNamePathToClosestTopNode;
        
        addLogExpectationsGenericNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.FALSE));
            oneOf(mockNodeUtil).isNodeInfoFile(mockCurrentNode); will(returnValue(Boolean.FALSE));
        }});
        
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, parentNamePathToClosestTopNode, Boolean.FALSE, AddedNodeExporter.class);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getNamePathToUseForThisExporter_MediaFile() {
        
        final String parentNamePathToClosestTopNode = "TopNode/GrandParentNode";
        final String expectedPath = parentNamePathToClosestTopNode;
        
        addLogExpectationsGenericNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.FALSE));
            oneOf(mockNodeUtil).isNodeInfoFile(mockCurrentNode); will(returnValue(Boolean.FALSE));
        }});
        
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, parentNamePathToClosestTopNode, Boolean.FALSE, AddedNodeExporter.class);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getNamePathToUseForThisExporter_InfoFileUnderCorpus() {
        
        final String parentNamePathToClosestTopNode = "TopNode/GrandParentNode";
        final String parentName = "ParentNode";
        final String expectedPath = parentNamePathToClosestTopNode + File.separator + parentName;
        
        final String parentProfileSchemaStr = "uri:schema";
        final URI parentProfileSchema = URI.create(parentProfileSchemaStr);
        final String parentTranslatedType = "corpus";
        
        addLogExpectationsGenericNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.FALSE));
            oneOf(mockNodeUtil).isNodeInfoFile(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileSchema));
            oneOf(mockAllowedCmdiProfiles).getProfile(parentProfileSchemaStr); will(returnValue(mockCmdiProfile));
            allowing(mockCmdiProfile).getTranslateType(); will(returnValue(parentTranslatedType));
            oneOf(mockParentNode).getName(); will(returnValue(parentName));
            oneOf(mockArchiveFileHelper).correctPathElement(parentName, "getNamePathToUseForThisExporter"); will(returnValue(parentName));
        }});
        
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, parentNamePathToClosestTopNode, Boolean.FALSE, AddedNodeExporter.class);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getNamePathToUseForThisExporter_InfoFileUnderSession() {
        
        final String parentNamePathToClosestTopNode = "TopNode/GrandParentNode";
        final String expectedPath = parentNamePathToClosestTopNode;
        
        final String parentProfileSchemaStr = "uri:schema";
        final URI parentProfileSchema = URI.create(parentProfileSchemaStr);
        final String parentTranslatedType = "session";
        
        addLogExpectationsGenericNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.FALSE));
            oneOf(mockNodeUtil).isNodeInfoFile(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getProfileSchemaURI(); will(returnValue(parentProfileSchema));
            oneOf(mockAllowedCmdiProfiles).getProfile(parentProfileSchemaStr); will(returnValue(mockCmdiProfile));
            allowing(mockCmdiProfile).getTranslateType(); will(returnValue(parentTranslatedType));
        }});
        
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, parentNamePathToClosestTopNode, Boolean.FALSE, AddedNodeExporter.class);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getNamePathToUseForThisExporter_Session() {
        
        final String parentNamePathToClosestTopNode = "TopNode/GrandParentNode";
        final String parentName = "ParentNode";
        final String expectedPath = parentNamePathToClosestTopNode + File.separator + parentName;
        
        addLogExpectationsMetadataNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockParentNode).getName(); will(returnValue(parentName));
            oneOf(mockArchiveFileHelper).correctPathElement(parentName, "getNamePathToUseForThisExporter"); will(returnValue(parentName));
        }});
        
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, parentNamePathToClosestTopNode, Boolean.FALSE, AddedNodeExporter.class);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getNamePathToUseForThisExporter_Corpus_ParentInArchive() throws URISyntaxException, MalformedURLException {
        
        final String parentNamePathToClosestTopNode = "";
        final URL parentNameURL = new URL("http://ParentNode/Corpusstructure/node.cmdi");
        final String parentName = "Parent Node";
        final String expectedPath = "ParentNode/";
        final URI parentProfileUri = new URI("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.1/profiles/clarin.eu:cr1:p_1456409483202/xsd");  
        
        addLogExpectationsMetadataNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(URI.create("hdl:11142/" + UUID.randomUUID().toString())));
            oneOf(mockCurrentNode).getProfileSchemaURI(); will(returnValue(parentProfileUri));
            oneOf(mockAllowedCmdiProfiles).getProfile(parentProfileUri.toString()); will(returnValue(mockCmdiProfile));
            oneOf(mockCurrentNode).getArchiveURI(); will(returnValue(null));
            allowing(mockCmdiProfile).getTranslateType(); will(returnValue("corpus"));
            oneOf(mockParentNode).getArchiveURL(); will(returnValue(parentNameURL));
            oneOf(mockArchiveFileLocationProvider).getFolderNameBeforeCorpusstructure(parentNameURL.toString());  will(returnValue(expectedPath));
        }});
        
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, parentNamePathToClosestTopNode, Boolean.FALSE, AddedNodeExporter.class);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getNamePathToUseForThisExporter_Session_SpecialCharacters() {
        
        final String parentNamePathToClosestTopNode = "TopNode/GrandParentNode";
        final String parentName = "NóPai";
        final String parentPathName = "N_Pai";
        final String expectedPath = parentNamePathToClosestTopNode + File.separator + parentPathName;
        
        addLogExpectationsMetadataNode();
        
        context.checking(new Expectations() {{
        	oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockParentNode).getName(); will(returnValue(parentName));
            oneOf(mockArchiveFileHelper).correctPathElement(parentName, "getNamePathToUseForThisExporter"); will(returnValue(parentPathName));
        }});
        
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, parentNamePathToClosestTopNode, Boolean.FALSE, AddedNodeExporter.class);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getNamePathToUseForThisExporter_Session_ParentIsTopNode() {
        
        final String parentNamePathToClosestTopNode = "";
        final String parentName = "ParentNode";
        final String expectedPath = parentName;
        
        addLogExpectationsMetadataNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockParentNode).getName(); will(returnValue(parentName));
            oneOf(mockArchiveFileHelper).correctPathElement(parentName, "getNamePathToUseForThisExporter"); will(returnValue(parentName));
        }});
        
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, parentNamePathToClosestTopNode, Boolean.FALSE, AddedNodeExporter.class);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getNamePathToUseForThisExporter_Corpus() {
        
        final String parentNamePathToClosestTopNode = "TopNode/GrandParentNode";
        final String parentName = "ParentNode";
        final String expectedPath = parentNamePathToClosestTopNode + File.separator + parentName;
        
        addLogExpectationsMetadataNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockParentNode).getName(); will(returnValue(parentName));
            oneOf(mockArchiveFileHelper).correctPathElement(parentName, "getNamePathToUseForThisExporter"); will(returnValue(parentName));
        }});
        
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, parentNamePathToClosestTopNode, Boolean.FALSE, AddedNodeExporter.class);
        
        assertEquals("Result different from expected", expectedPath, result);
    }

    @Test
    public void getNamePathToUseForThisExporter_Corpus_ParentIsTopNode() {
        
        final String parentNamePathToClosestTopNode = "";
        final String parentName = "ParentNode";
        final String expectedPath = parentName;
        
        addLogExpectationsMetadataNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockParentNode).getName(); will(returnValue(parentName));
            oneOf(mockArchiveFileHelper).correctPathElement(parentName, "getNamePathToUseForThisExporter"); will(returnValue(parentName));
        }});
        
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, parentNamePathToClosestTopNode, Boolean.FALSE, AddedNodeExporter.class);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getNamePathToUseForThisExporter_Corpus_ParentIsTopNode_SpecialCharacters() {
        
        final String parentNamePathToClosestTopNode = "";
        final String parentName = "NóPai";
        final String parentPathName = "N_Pai";
        final String expectedPath = parentPathName;
        
        addLogExpectationsMetadataNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockParentNode).getName(); will(returnValue(parentName));
            oneOf(mockArchiveFileHelper).correctPathElement(parentName, "getNamePathToUseForThisExporter"); will(returnValue(parentPathName));
        }});
        
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, parentNamePathToClosestTopNode, Boolean.FALSE, AddedNodeExporter.class);
        
        assertEquals("Result different from expected", expectedPath, result);
    }

    @Test
    public void getNamePathToUseForThisExporter_ExistingCorpus() throws MalformedURLException {
        
        final String parentPath = "TopNode/GrandParentNode";
        final String sessionName = "session test name (mock)";
        final String sessionFolderName = "session_test_name_mock_";
        
        final String parentProfileSchemaStr = "uri:schema";
        final URI parentProfileSchema = URI.create(parentProfileSchemaStr);
        final String parentTranslatedType = "session";
        
        addLogExpectationsMetadataNode();
        
        //Parent already in DB current node not
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(URI.create("hdl:11142/" + UUID.randomUUID().toString())));
            oneOf(mockCurrentNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockCurrentNode).getProfileSchemaURI(); will(returnValue(parentProfileSchema));
            oneOf(mockAllowedCmdiProfiles).getProfile(parentProfileSchemaStr); will(returnValue(mockCmdiProfile));
            oneOf(mockCmdiProfile).getTranslateType(); will(returnValue(parentTranslatedType));
            oneOf(mockParentNode).getWorkspaceNodeID();
            oneOf(mockWorkspaceDao).getDescendantWorkspaceNodesByType(0, WorkspaceNodeType.METADATA);
            oneOf(mockParentNode).getName(); will(returnValue(sessionName));
            oneOf(mockArchiveFileHelper).correctPathElement(sessionName, "getNamePathToUseForThisExporter"); will(returnValue(sessionFolderName));
        }});
        
        //Parent and current nodes already in DB
        String result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, parentPath, Boolean.FALSE, AddedNodeExporter.class);
        assertEquals("Result different from expected",  parentPath + File.separator + sessionFolderName, result);
        
        addLogExpectationsMetadataNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(URI.create("hdl:11142/" + UUID.randomUUID().toString())));
            oneOf(mockCurrentNode).getProfileSchemaURI(); will(returnValue(parentProfileSchema));
            oneOf(mockAllowedCmdiProfiles).getProfile(parentProfileSchemaStr); will(returnValue(mockCmdiProfile));
            oneOf(mockCurrentNode).getArchiveURI(); will(returnValue(URI.create("hdl:11142/" + UUID.randomUUID().toString())));
            oneOf(mockCorpusStructureBridge).getCorpusNamePathToClosestTopNode(mockCurrentNode); will(returnValue(sessionFolderName));
        }});
        
        result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, parentPath, Boolean.FALSE, AddedNodeExporter.class);
        assertEquals("Result different from expected",  parentPath + File.separator + sessionFolderName, result);
        
        //Parent is top node
        addLogExpectationsMetadataNode();
        
        context.checking(new Expectations() {{
            oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(URI.create("hdl:11142/" + UUID.randomUUID().toString())));
            oneOf(mockCurrentNode).getProfileSchemaURI(); will(returnValue(parentProfileSchema));
            oneOf(mockAllowedCmdiProfiles).getProfile(parentProfileSchemaStr); will(returnValue(mockCmdiProfile));
            oneOf(mockCurrentNode).getArchiveURI(); will(returnValue(URI.create("hdl:11142/" + UUID.randomUUID().toString())));
            oneOf(mockCorpusStructureBridge).getCorpusNamePathToClosestTopNode(mockCurrentNode); will(returnValue(sessionFolderName));
        }});
        
        //null parent path
        result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, "", Boolean.FALSE, AddedNodeExporter.class);
        assertEquals("Result different from expected", sessionFolderName, result);
        
        addLogExpectationsMetadataNode();

        context.checking(new Expectations() {{
        	exactly(2).of(mockNodeUtil).isNodeMetadata(mockCurrentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentNode).getArchiveURI(); will(returnValue(URI.create("hdl:11142/" + UUID.randomUUID().toString())));
        	oneOf(mockCorpusStructureBridge).getCorpusNamePathToClosestTopNode(mockCurrentNode); will(returnValue(parentPath));
        }});
        
        result = exporterHelper.getNamePathToUseForThisExporter(mockCurrentNode, mockParentNode, null, Boolean.TRUE, GeneralNodeExporter.class);
        assertEquals("Result different from expected", parentPath, result);
    }
    
    private void addLogExpectationsMetadataNode () {
    	context.checking(new Expectations() {{
    		addLogExpectationsGenericNode();
	    	oneOf(mockParentNode).getArchiveURL();
	    	oneOf(mockParentNode).getArchiveURI();
    	}});
    }
    
    private void addLogExpectationsGenericNode () {
    	context.checking(new Expectations() {{
	    	oneOf(mockCurrentNode).getWorkspaceURL();
	    	oneOf(mockCurrentNode).getArchiveURL();
	    	oneOf(mockCurrentNode).getArchiveURI();
	    	oneOf(mockNodeUtil).isNodeMetadata(mockCurrentNode);
    	}});
    }
    
}
