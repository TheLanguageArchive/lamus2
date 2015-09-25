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
package nl.mpi.lamus.archive.implementation;

import java.io.File;
import java.net.URI;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.CorpusStructureBridge;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
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
public class LamusCorpusStructureBridgeTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    
    @Mock WorkspaceNode mockNode;
    @Mock CorpusNode mockCorpusNode;
    @Mock CorpusNode mockParentCorpusNode;
    @Mock CorpusNode mockGrandParentCorpusNode;
    @Mock CorpusNode mockGreatGrandParentCorpusNode;
    @Mock CorpusNode mockGreatestGrandParentCorpusNode;
    
    private final String corpusstructureDirectoryName = "Corpusstructure";
    private final String metadataDirectoryName = "Metadata";
    
    
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
        corpusStructureBridge = new LamusCorpusStructureBridge(
                mockCorpusStructureProvider, mockNodeResolver, mockArchiveFileHelper,
                corpusstructureDirectoryName, metadataDirectoryName);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getCorpusNamePathToClosestTopNode_TopNode() {
        
        final URI nodeArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String localPath = "/archive/root/TopNode/Corpusstructure/topnode.cmdi";
        final File localFile = new File(localPath);
        
        final URI parentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String parentLocalPath = "/archive/root/Corpusstructure/root.cmdi";
        final File parentLocalFile = new File(parentLocalPath);
        
        final String expectedPath = "";
        
        context.checking(new Expectations() {{
            allowing(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(localFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(nodeArchiveURI); will(returnValue(parentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(parentArchiveURI); will(returnValue(mockParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockParentCorpusNode); will(returnValue(parentLocalFile));
        }});
        
        String result = corpusStructureBridge.getCorpusNamePathToClosestTopNode(mockNode);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getCorpusNamePathToClosestTopNode_TopNodeChild() {
        
        final URI nodeArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String localPath = "/archive/root/TopNode/Corpusstructure/othernode.cmdi";
        final File localFile = new File(localPath);
        
        final URI parentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String parentLocalPath = "/archive/root/TopNode/Corpusstructure/topnode.cmdi";
        final File parentLocalFile = new File(parentLocalPath);
        final String parentNodeName = "TopNode";
        
        final URI grandParentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String grandParentLocalPath = "/archive/root/Corpusstructure/root.cmdi";
        final File grandParentLocalFile = new File(grandParentLocalPath);
        
        final String expectedPath = "TopNode";
        
        context.checking(new Expectations() {{
            allowing(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            allowing(mockParentCorpusNode).getNodeURI(); will(returnValue(parentArchiveURI));
            allowing(mockParentCorpusNode).getName(); will(returnValue(parentNodeName));
            allowing(mockArchiveFileHelper).correctPathElement(parentNodeName, "getCorpusNamePathToClosestTopNode"); will(returnValue(parentNodeName));
            
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(localFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(nodeArchiveURI); will(returnValue(parentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(parentArchiveURI); will(returnValue(mockParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockParentCorpusNode); will(returnValue(parentLocalFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(parentArchiveURI); will(returnValue(grandParentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(grandParentArchiveURI); will(returnValue(mockGrandParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockGrandParentCorpusNode); will(returnValue(grandParentLocalFile));
        }});
        
        String result = corpusStructureBridge.getCorpusNamePathToClosestTopNode(mockNode);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getCorpusNamePathToClosestTopNode_TopNodeChild_TopNodeFolderNotTheSameAsNodeName() {
        
        final URI nodeArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String localPath = "/archive/root/topenode/Corpusstructure/othernode.cmdi";
        final File localFile = new File(localPath);
        
        final URI parentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String parentLocalPath = "/archive/root/topenode/Corpusstructure/topnode.cmdi";
        final File parentLocalFile = new File(parentLocalPath);
        final String parentNodeName = "TopNode";
        
        final URI grandParentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String grandParentLocalPath = "/archive/root/Corpusstructure/root.cmdi";
        final File grandParentLocalFile = new File(grandParentLocalPath);
        
        final String expectedPath = "topenode";
        
        context.checking(new Expectations() {{
            allowing(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            allowing(mockParentCorpusNode).getNodeURI(); will(returnValue(parentArchiveURI));
            allowing(mockParentCorpusNode).getName(); will(returnValue(parentNodeName));
            allowing(mockArchiveFileHelper).correctPathElement(parentNodeName, "getCorpusNamePathToClosestTopNode"); will(returnValue(parentNodeName));
            
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(localFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(nodeArchiveURI); will(returnValue(parentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(parentArchiveURI); will(returnValue(mockParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockParentCorpusNode); will(returnValue(parentLocalFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(parentArchiveURI); will(returnValue(grandParentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(grandParentArchiveURI); will(returnValue(mockGrandParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockGrandParentCorpusNode); will(returnValue(grandParentLocalFile));
        }});
        
        String result = corpusStructureBridge.getCorpusNamePathToClosestTopNode(mockNode);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getCorpusNamePathToClosestTopNode_OtherDescendant() {
        
        final URI nodeArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String localPath = "/archive/root/TopNode/Corpusstructure/someothernode.cmdi";
        final File localFile = new File(localPath);
        
        final URI parentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String parentLocalPath = "/archive/root/TopNode/Corpusstructure/othernode.cmdi";
        final File parentLocalFile = new File(parentLocalPath);
        final String parentNodeName = "OtherNode";
        
        final URI grandParentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String grandParentLocalPath = "/archive/root/TopNode/Corpusstructure/topnode.cmdi";
        final File grandParentLocalFile = new File(grandParentLocalPath);
        final String grandParentNodeName = "TopNode";
        
        final URI greatGrandParentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String greatGrandParentLocalPath = "/archive/root/Corpusstructure/root.cmdi";
        final File greatGrandParentLocalFile = new File(greatGrandParentLocalPath);
        
        final String expectedPath = "TopNode/OtherNode";
        
        context.checking(new Expectations() {{
            allowing(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            allowing(mockParentCorpusNode).getNodeURI(); will(returnValue(parentArchiveURI));
            allowing(mockGrandParentCorpusNode).getNodeURI(); will(returnValue(grandParentArchiveURI));
            allowing(mockParentCorpusNode).getName(); will(returnValue(parentNodeName));
            allowing(mockArchiveFileHelper).correctPathElement(parentNodeName, "getCorpusNamePathToClosestTopNode"); will(returnValue(parentNodeName));
            allowing(mockGrandParentCorpusNode).getName(); will(returnValue(grandParentNodeName));
            allowing(mockArchiveFileHelper).correctPathElement(grandParentNodeName, "getCorpusNamePathToClosestTopNode"); will(returnValue(grandParentNodeName));
            
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(localFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(nodeArchiveURI); will(returnValue(parentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(parentArchiveURI); will(returnValue(mockParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockParentCorpusNode); will(returnValue(parentLocalFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(parentArchiveURI); will(returnValue(grandParentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(grandParentArchiveURI); will(returnValue(mockGrandParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockGrandParentCorpusNode); will(returnValue(grandParentLocalFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(grandParentArchiveURI); will(returnValue(greatGrandParentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(greatGrandParentArchiveURI); will(returnValue(mockGreatGrandParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockGreatGrandParentCorpusNode); will(returnValue(greatGrandParentLocalFile));
        }});
        
        String result = corpusStructureBridge.getCorpusNamePathToClosestTopNode(mockNode);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getCorpusNamePathToClosestTopNode_OtherDescendant_SpecialCharacters() {
        
        final URI nodeArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String localPath = "/archive/root/TopNode/Corpusstructure/someothernode.cmdi";
        final File localFile = new File(localPath);
        
        final URI parentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String parentLocalPath = "/archive/root/TopNode/Corpusstructure/outrono.cmdi";
        final File parentLocalFile = new File(parentLocalPath);
        final String parentNodeName = "OutroNó";
        final String parentNodeName_corrected = "OutroN_";
        
        final URI grandParentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String grandParentLocalPath = "/archive/root/TopNode/Corpusstructure/topnode.cmdi";
        final File grandParentLocalFile = new File(grandParentLocalPath);
        final String grandParentNodeName = "TopNode";
        
        final URI greatGrandParentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String greatGrandParentLocalPath = "/archive/root/Corpusstructure/root.cmdi";
        final File greatGrandParentLocalFile = new File(greatGrandParentLocalPath);
        
        final String expectedPath = "TopNode/OutroN_";
        
        context.checking(new Expectations() {{
            allowing(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            allowing(mockParentCorpusNode).getNodeURI(); will(returnValue(parentArchiveURI));
            allowing(mockGrandParentCorpusNode).getNodeURI(); will(returnValue(grandParentArchiveURI));
            allowing(mockParentCorpusNode).getName(); will(returnValue(parentNodeName));
            allowing(mockArchiveFileHelper).correctPathElement(parentNodeName, "getCorpusNamePathToClosestTopNode"); will(returnValue(parentNodeName_corrected));
            allowing(mockGrandParentCorpusNode).getName(); will(returnValue(grandParentNodeName));
            allowing(mockArchiveFileHelper).correctPathElement(grandParentNodeName, "getCorpusNamePathToClosestTopNode"); will(returnValue(grandParentNodeName));
            
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(localFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(nodeArchiveURI); will(returnValue(parentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(parentArchiveURI); will(returnValue(mockParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockParentCorpusNode); will(returnValue(parentLocalFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(parentArchiveURI); will(returnValue(grandParentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(grandParentArchiveURI); will(returnValue(mockGrandParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockGrandParentCorpusNode); will(returnValue(grandParentLocalFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(grandParentArchiveURI); will(returnValue(greatGrandParentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(greatGrandParentArchiveURI); will(returnValue(mockGreatGrandParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockGreatGrandParentCorpusNode); will(returnValue(greatGrandParentLocalFile));
        }});
        
        String result = corpusStructureBridge.getCorpusNamePathToClosestTopNode(mockNode);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getCorpusNamePathToClosestTopNode_TopNodeChild_Session() {
        
        final URI nodeArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String localPath = "/archive/root/TopNode/Metadata/session.cmdi";
        final File localFile = new File(localPath);
        
        final URI parentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String parentLocalPath = "/archive/root/TopNode/Corpusstructure/topnode.cmdi";
        final File parentLocalFile = new File(parentLocalPath);
        final String parentNodeName = "TopNode";
        
        final URI grandParentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String grandParentLocalPath = "/archive/root/Corpusstructure/root.cmdi";
        final File grandParentLocalFile = new File(grandParentLocalPath);
        
        final String expectedPath = "TopNode";
        
        context.checking(new Expectations() {{
            allowing(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            allowing(mockParentCorpusNode).getNodeURI(); will(returnValue(parentArchiveURI));
            allowing(mockParentCorpusNode).getName(); will(returnValue(parentNodeName));
            allowing(mockArchiveFileHelper).correctPathElement(parentNodeName, "getCorpusNamePathToClosestTopNode"); will(returnValue(parentNodeName));
            
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(localFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(nodeArchiveURI); will(returnValue(parentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(parentArchiveURI); will(returnValue(mockParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockParentCorpusNode); will(returnValue(parentLocalFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(parentArchiveURI); will(returnValue(grandParentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(grandParentArchiveURI); will(returnValue(mockGrandParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockGrandParentCorpusNode); will(returnValue(grandParentLocalFile));
        }});
        
        String result = corpusStructureBridge.getCorpusNamePathToClosestTopNode(mockNode);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getCorpusNamePathToClosestTopNode_OtherDescendant_Session() {
        
        final URI nodeArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String localPath = "/archive/root/TopNode/OtherNode/Metadata/session.cmdi";
        final File localFile = new File(localPath);
        
        final URI parentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String parentLocalPath = "/archive/root/TopNode/Corpusstructure/othernode.cmdi";
        final File parentLocalFile = new File(parentLocalPath);
        final String parentNodeName = "OtherNode";
        
        final URI grandParentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String grandParentLocalPath = "/archive/root/TopNode/Corpusstructure/topnode.cmdi";
        final File grandParentLocalFile = new File(grandParentLocalPath);
        final String grandParentNodeName = "TopNode";
        
        final URI greatGrandParentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String greatGrandParentLocalPath = "/archive/root/Corpusstructure/root.cmdi";
        final File greatGrandParentLocalFile = new File(greatGrandParentLocalPath);
        
        final String expectedPath = "TopNode/OtherNode";
        
        context.checking(new Expectations() {{
            allowing(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            allowing(mockParentCorpusNode).getNodeURI(); will(returnValue(parentArchiveURI));
            allowing(mockParentCorpusNode).getName(); will(returnValue(parentNodeName));
            allowing(mockArchiveFileHelper).correctPathElement(parentNodeName, "getCorpusNamePathToClosestTopNode"); will(returnValue(parentNodeName));
            allowing(mockGrandParentCorpusNode).getName(); will(returnValue(grandParentNodeName));
            allowing(mockArchiveFileHelper).correctPathElement(grandParentNodeName, "getCorpusNamePathToClosestTopNode"); will(returnValue(grandParentNodeName));
            allowing(mockGrandParentCorpusNode).getNodeURI(); will(returnValue(grandParentArchiveURI));
            
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(localFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(nodeArchiveURI); will(returnValue(parentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(parentArchiveURI);  will(returnValue(mockParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockParentCorpusNode); will(returnValue(parentLocalFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(parentArchiveURI); will(returnValue(grandParentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(grandParentArchiveURI); will(returnValue(mockGrandParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockGrandParentCorpusNode); will(returnValue(grandParentLocalFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(grandParentArchiveURI); will(returnValue(greatGrandParentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(greatGrandParentArchiveURI); will(returnValue(mockGreatGrandParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockGreatGrandParentCorpusNode); will(returnValue(greatGrandParentLocalFile));
        }});
        
        String result = corpusStructureBridge.getCorpusNamePathToClosestTopNode(mockNode);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getCorpusNamePathToClosestTopNode_Resource() {
        
        final URI nodeArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String localPath = "/archive/root/TopNode/OtherNode/Media/stuff.jpg";
        final File localFile = new File(localPath);
        
        final URI parentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String parentLocalPath = "/archive/root/TopNode/OtherNode/Metadata/session.cmdi";
        final File parentLocalFile = new File(parentLocalPath);
        
        final URI grandParentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String grandParentLocalPath = "/archive/root/TopNode/Corpusstructure/othernode.cmdi";
        final File grandParentLocalFile = new File(grandParentLocalPath);
        final String grandParentNodeName = "OtherNode";
        
        final URI greatGrandParentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String greatGrandParentLocalPath = "/archive/root/TopNode/Corpusstructure/topnode.cmdi";
        final File greatGrandParentLocalFile = new File(greatGrandParentLocalPath);
        final String greatGrandParentNodeName = "TopNode";
        
        final URI greatestGrandParentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String greatestGrandParentLocalPath = "/archive/root/Corpusstructure/root.cmdi";
        final File greatestGrandParentLocalFile = new File(greatestGrandParentLocalPath);
        
        final String expectedPath = "TopNode/OtherNode";
        
        context.checking(new Expectations() {{
            allowing(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            allowing(mockParentCorpusNode).getNodeURI(); will(returnValue(parentArchiveURI));
            allowing(mockGrandParentCorpusNode).getNodeURI(); will(returnValue(grandParentArchiveURI));
            allowing(mockGreatGrandParentCorpusNode).getNodeURI(); will(returnValue(greatGrandParentArchiveURI));
            allowing(mockGrandParentCorpusNode).getName(); will(returnValue(grandParentNodeName));
            allowing(mockArchiveFileHelper).correctPathElement(grandParentNodeName, "getCorpusNamePathToClosestTopNode"); will(returnValue(grandParentNodeName));
            allowing(mockGreatGrandParentCorpusNode).getName(); will(returnValue(greatGrandParentNodeName));
            allowing(mockArchiveFileHelper).correctPathElement(greatGrandParentNodeName, "getCorpusNamePathToClosestTopNode"); will(returnValue(greatGrandParentNodeName));
            
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(localFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(nodeArchiveURI); will(returnValue(parentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(parentArchiveURI); will(returnValue(mockParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockParentCorpusNode); will(returnValue(parentLocalFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(parentArchiveURI); will(returnValue(grandParentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(grandParentArchiveURI); will(returnValue(mockGrandParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockGrandParentCorpusNode); will(returnValue(grandParentLocalFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(grandParentArchiveURI); will(returnValue(greatGrandParentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(greatGrandParentArchiveURI); will(returnValue(mockGreatGrandParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockGreatGrandParentCorpusNode); will(returnValue(greatGrandParentLocalFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(greatGrandParentArchiveURI); will(returnValue(greatestGrandParentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(greatestGrandParentArchiveURI); will(returnValue(mockGreatestGrandParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockGreatestGrandParentCorpusNode); will(returnValue(greatestGrandParentLocalFile));
        }});
        
        String result = corpusStructureBridge.getCorpusNamePathToClosestTopNode(mockNode);
        
        assertEquals("Result different from expected", expectedPath, result);
    }
    
    @Test
    public void getCorpusNamePathToClosestTopNode_RetrievedNodeNull() {
        
        final URI nodeArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
            allowing(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(null));
        }});
        
        String result = corpusStructureBridge.getCorpusNamePathToClosestTopNode(mockNode);
        
        assertNull("Result should be null", result);
    }
    
    @Test
    public void getCorpusNamePathToClosestTopNode_RetrievedCanonicalParentNull() {
        
        final URI nodeArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String localPath = "/archive/root/TopNode/Corpusstructure/someothernode.cmdi";
        final File localFile = new File(localPath);
        
        context.checking(new Expectations() {{
            allowing(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(localFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(nodeArchiveURI); will(returnValue(null));
        }});
        
        String result = corpusStructureBridge.getCorpusNamePathToClosestTopNode(mockNode);
        
        assertNull("Result should be null", result);
    }
    
    @Test
    public void getCorpusNamePathToClosestTopNode_RetrievedParentNodeNull() {
        
        final URI nodeArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        final String localPath = "/archive/root/TopNode/Corpusstructure/someothernode.cmdi";
        final File localFile = new File(localPath);
        
        final URI parentArchiveURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        
        context.checking(new Expectations() {{
            allowing(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(localFile));
            
            oneOf(mockCorpusStructureProvider).getCanonicalParent(nodeArchiveURI); will(returnValue(parentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(parentArchiveURI); will(returnValue(null));
        }});
        
        String result = corpusStructureBridge.getCorpusNamePathToClosestTopNode(mockNode);
        
        assertNull("Result should be null", result);
    }
}
