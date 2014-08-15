/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.ams;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
import nl.mpi.lat.ams.IAmsRemoteService;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.junit.Assert.*;
import org.junit.*;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusAmsServiceBridgeTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock IAmsRemoteService mockAmsRemoteService;

    @Mock WorkspaceNodeReplacement mockNodeReplacement_1;
    @Mock WorkspaceNodeReplacement mockNodeReplacement_2;
    
    private LamusAmsServiceBridge amsServiceBridge;

    
    public LamusAmsServiceBridgeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        
        amsServiceBridge = new LamusAmsServiceBridge();
        ReflectionTestUtils.setField(amsServiceBridge, "amsRemoteService", mockAmsRemoteService);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getMailAddress() {
        
        final String userID = "someUser";
        final String emailAddress = "someUser@test.nl";
        
        context.checking(new Expectations() {{
            
            oneOf(mockAmsRemoteService).getUserEmailAddress(userID);
                will(returnValue(emailAddress));
        }});
        
        String retrievedEmailAddress = amsServiceBridge.getMailAddress(userID);
        
        assertEquals("Retrieved email address different from expected", emailAddress, retrievedEmailAddress);
    }
    
    @Test
    public void triggerAccessRightsRecalculation() throws URISyntaxException {
        
        final URI workspaceRootNodeURI = new URI(UUID.randomUUID().toString());
        
        final Collection<URI> recalculationTargetURIs = new ArrayList<>();
        recalculationTargetURIs.add(workspaceRootNodeURI);
        
        context.checking(new Expectations() {{
            
            oneOf(mockAmsRemoteService).triggerRightsRecalculation(recalculationTargetURIs, Boolean.TRUE, Boolean.TRUE);
        }});
        
        amsServiceBridge.triggerAccessRightsRecalculation(workspaceRootNodeURI);
    }
    
    @Test
    public void triggerAccessRightsRecalculationWithVersionedNodes() throws URISyntaxException {
        
        final URI topNode = new URI(UUID.randomUUID().toString());
        
        final Collection<WorkspaceNodeReplacement> nodeReplacementsList = new ArrayList<>();
        nodeReplacementsList.add(mockNodeReplacement_1);
        nodeReplacementsList.add(mockNodeReplacement_2);
        
        final URI oldNodeURI_1 = new URI(UUID.randomUUID().toString());
        final URI oldNodeURI_2 = new URI(UUID.randomUUID().toString());
        
        final Collection<URI> versionedNodes = new ArrayList<>();
        versionedNodes.add(oldNodeURI_1);
        versionedNodes.add(oldNodeURI_2);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeReplacement_1).getOldNodeURI(); will(returnValue(oldNodeURI_1));
            oneOf(mockNodeReplacement_2).getOldNodeURI(); will(returnValue(oldNodeURI_2));
            
            oneOf(mockAmsRemoteService).triggerRightsRecalculationWithVersionedNodes(topNode, versionedNodes);
        }});
        
        amsServiceBridge.triggerAccessRightsRecalculationWithVersionedNodes(topNode, nodeReplacementsList);
    }
    
    @Test
    public void triggerAccessRightsRecalculationForVersionedNodes() throws URISyntaxException {
        
        final URI topNode = new URI(UUID.randomUUID().toString());
        
        final Collection<URI> recalculationTargetURIs = new ArrayList<>();
        recalculationTargetURIs.add(topNode);
        
        final Collection<WorkspaceNodeReplacement> nodeReplacementsList = new ArrayList<>();
        nodeReplacementsList.add(mockNodeReplacement_1);
        nodeReplacementsList.add(mockNodeReplacement_2);
        
        final URI oldNodeURI_1 = new URI(UUID.randomUUID().toString());
        final URI oldNodeURI_2 = new URI(UUID.randomUUID().toString());
        
        final Collection<URI> versionedNodes = new ArrayList<>();
        versionedNodes.add(oldNodeURI_1);
        versionedNodes.add(oldNodeURI_2);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeReplacement_1).getOldNodeURI(); will(returnValue(oldNodeURI_1));
            oneOf(mockNodeReplacement_2).getOldNodeURI(); will(returnValue(oldNodeURI_2));
            
            oneOf(mockAmsRemoteService).triggerRightsRecalculationForVersionedNodes(versionedNodes, Boolean.TRUE, Boolean.FALSE);
            oneOf(mockAmsRemoteService).triggerRightsRecalculation(recalculationTargetURIs, Boolean.FALSE, Boolean.TRUE);
        }});
        
        amsServiceBridge.triggerAccessRightsRecalculationForVersionedNodes(nodeReplacementsList, topNode);
    }
    
    @Test
    public void triggerAmsNodeReplacements() throws URISyntaxException {
        
        final String userID = "someUser";
        
        final Collection<WorkspaceNodeReplacement> nodeReplacementsList = new ArrayList<>();
        nodeReplacementsList.add(mockNodeReplacement_1);
        nodeReplacementsList.add(mockNodeReplacement_2);
        
        final URI oldNodeURI_1 = new URI(UUID.randomUUID().toString());
        final URI newNodeURI_1 = new URI(UUID.randomUUID().toString());
        final URI oldNodeURI_2 = new URI(UUID.randomUUID().toString());
        final URI newNodeURI_2 = new URI(UUID.randomUUID().toString());
        
        final Map<URI, URI> nodeReplacementsMap = new HashMap<>();
        nodeReplacementsMap.put(oldNodeURI_1, newNodeURI_1);
        nodeReplacementsMap.put(oldNodeURI_2, newNodeURI_2);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeReplacement_1).getOldNodeURI(); will(returnValue(oldNodeURI_1));
            oneOf(mockNodeReplacement_1).getNewNodeURI(); will(returnValue(newNodeURI_1));
            
            oneOf(mockNodeReplacement_2).getOldNodeURI(); will(returnValue(oldNodeURI_2));
            oneOf(mockNodeReplacement_2).getNewNodeURI(); will(returnValue(newNodeURI_2));
            
            oneOf(mockAmsRemoteService).replaceNodesWithDefaultAccessRules(nodeReplacementsMap, userID);
        }});
        
        amsServiceBridge.triggerAmsNodeReplacements(nodeReplacementsList, userID);
    }
}
