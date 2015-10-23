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
package nl.mpi.lamus.workspace.model.implementation;

import nl.mpi.archiving.corpusstructure.core.CorpusNodeType;
import nl.mpi.lamus.cmdi.profile.CmdiProfile;
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
public class LamusNodeUtilTest {
    
        
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private NodeUtil nodeUtil;
    
    @Mock CmdiProfile mockCmdiProfile;
    @Mock WorkspaceNode mockWorkspaceNode;
    
    
    public LamusNodeUtilTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        nodeUtil = new LamusNodeUtil();
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void convertArchiveNodeType_Null() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.UNKNOWN;
        WorkspaceNodeType result = nodeUtil.convertArchiveNodeType(null);
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertArchiveNodeType_Metadata() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.METADATA;
        WorkspaceNodeType result = nodeUtil.convertArchiveNodeType(CorpusNodeType.METADATA);
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertArchiveNodeType_Collection() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.METADATA;
        WorkspaceNodeType result = nodeUtil.convertArchiveNodeType(CorpusNodeType.COLLECTION);
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertArchiveNodeType_ResourceImage() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_IMAGE;
        WorkspaceNodeType result = nodeUtil.convertArchiveNodeType(CorpusNodeType.RESOURCE_IMAGE);
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertArchiveNodeType_ResourceVideo() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_VIDEO;
        WorkspaceNodeType result = nodeUtil.convertArchiveNodeType(CorpusNodeType.RESOURCE_VIDEO);
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertArchiveNodeType_ResourceAudio() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_AUDIO;
        WorkspaceNodeType result = nodeUtil.convertArchiveNodeType(CorpusNodeType.RESOURCE_AUDIO);
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertArchiveNodeType_ResourceLexical() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_WRITTEN;
        WorkspaceNodeType result = nodeUtil.convertArchiveNodeType(CorpusNodeType.RESOURCE_LEXICAL);
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertArchiveNodeType_ResourceAnnotation() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_WRITTEN;
        WorkspaceNodeType result = nodeUtil.convertArchiveNodeType(CorpusNodeType.RESOURCE_ANNOTATION);
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertArchiveNodeType_ResourceOther() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_OTHER;
        WorkspaceNodeType result = nodeUtil.convertArchiveNodeType(CorpusNodeType.RESOURCE_OTHER);
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertArchiveNodeType_ImdiCatalogue() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.METADATA;
        WorkspaceNodeType result = nodeUtil.convertArchiveNodeType(CorpusNodeType.IMDICATALOGUE);
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertArchiveNodeType_ImdiInfo() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_OTHER;
        WorkspaceNodeType result = nodeUtil.convertArchiveNodeType(CorpusNodeType.IMDIINFO);
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertArchiveNodeType_Unknown() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.UNKNOWN;
        WorkspaceNodeType result = nodeUtil.convertArchiveNodeType(CorpusNodeType.UNKOWN);
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertMimetype_Null() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.UNKNOWN;
        WorkspaceNodeType result = nodeUtil.convertMimetype(null);
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertMimetype_Metadata() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.METADATA;
        WorkspaceNodeType result = nodeUtil.convertMimetype("text/x-cmdi+xml");
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertMimetype_ResourceWritten() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_WRITTEN;
        WorkspaceNodeType result = nodeUtil.convertMimetype("text/x-lmf-wrap+xml");
        assertEquals("Result different from expected (1)", expectedType, result);
        
        result = nodeUtil.convertMimetype("text/plain");
        assertEquals("Result different from expected (2)", expectedType, result);
        
        result = nodeUtil.convertMimetype("application/pdf");
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertMimetype_ResourceAudio() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_AUDIO;
        WorkspaceNodeType result = nodeUtil.convertMimetype("audio/wav");
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertMimetype_ResourceVideo() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_VIDEO;
        WorkspaceNodeType result = nodeUtil.convertMimetype("video/mpeg");
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertMimetype_ResourceImage() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_IMAGE;
        WorkspaceNodeType result = nodeUtil.convertMimetype("image/jpeg");
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void convertMimetype_ResourceOther() {
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_OTHER;
        WorkspaceNodeType result = nodeUtil.convertMimetype("something/else");
        assertEquals("Result different from expected", expectedType, result);
    }
    
    @Test
    public void isMetadata_Null() {
        boolean result = nodeUtil.isTypeMetadata(null);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void isMetadata_Metadata() {
        boolean result = nodeUtil.isTypeMetadata(WorkspaceNodeType.METADATA);
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void isMetadata_ResourceImage() {
        boolean result = nodeUtil.isTypeMetadata(WorkspaceNodeType.RESOURCE_IMAGE);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void isMetadata_ResourceVideo() {
        boolean result = nodeUtil.isTypeMetadata(WorkspaceNodeType.RESOURCE_VIDEO);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void isMetadata_ResourceAudio() {
        boolean result = nodeUtil.isTypeMetadata(WorkspaceNodeType.RESOURCE_AUDIO);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void isMetadata_ResourceWritten() {
        boolean result = nodeUtil.isTypeMetadata(WorkspaceNodeType.RESOURCE_WRITTEN);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void isMetadata_ResourceOther() {
        boolean result = nodeUtil.isTypeMetadata(WorkspaceNodeType.RESOURCE_OTHER);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void isMetadata_Unknown() {
        boolean result = nodeUtil.isTypeMetadata(WorkspaceNodeType.UNKNOWN);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void isNotInfoFile() {
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceNode).getType(); will(returnValue(WorkspaceNodeType.METADATA));
        }});
        
        assertFalse("Result should be false", nodeUtil.isNodeInfoFile(mockWorkspaceNode));
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceNode).getType(); will(returnValue(WorkspaceNodeType.RESOURCE_IMAGE));
        }});
        
        assertFalse("Result should be false", nodeUtil.isNodeInfoFile(mockWorkspaceNode));
    }
    
    @Test
    public void isInfoFile() {
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceNode).getType(); will(returnValue(WorkspaceNodeType.RESOURCE_INFO));
        }});
        
        assertTrue("Result should be true", nodeUtil.isNodeInfoFile(mockWorkspaceNode));
    }
    
    @Test
    public void isLatCorpus() {
        
        final String profileName = "lat-corpus";
        
        context.checking(new Expectations() {{
            allowing(mockCmdiProfile).getName(); will(returnValue(profileName));
        }});
        
        boolean result = nodeUtil.isProfileLatCorpusOrSession(mockCmdiProfile);
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void isLatSession() {
        
        final String profileName = "lat-session";
        
        context.checking(new Expectations() {{
            allowing(mockCmdiProfile).getName(); will(returnValue(profileName));
        }});
        
        boolean result = nodeUtil.isProfileLatCorpusOrSession(mockCmdiProfile);
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void isNeitherLatCorpusNorLatSession() {
        
        final String profileName = "something";
        
        context.checking(new Expectations() {{
            allowing(mockCmdiProfile).getName(); will(returnValue(profileName));
        }});
        
        boolean result = nodeUtil.isProfileLatCorpusOrSession(mockCmdiProfile);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void isNullProfile() {
        
        boolean result = nodeUtil.isProfileLatCorpusOrSession(null);
        assertFalse("Result should be false", result);
    }
}
