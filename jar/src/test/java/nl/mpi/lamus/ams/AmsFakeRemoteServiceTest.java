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
package nl.mpi.lamus.ams;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lat.ams.IAmsRemoteService;
import nl.mpi.lat.ams.export.RecalcTriggerService;
import nl.mpi.lat.ams.export.impl.ApacheAclExportSrv;
import nl.mpi.lat.ams.export.impl.CachedCorpusDbExportSrv;
import nl.mpi.lat.ams.export.impl.IntegratedAuthExportSrv;
import nl.mpi.lat.ams.model.License;
import nl.mpi.lat.ams.service.LicenseService;
import nl.mpi.lat.auth.authorization.AdvAuthorizationService;
import nl.mpi.lat.auth.principal.LatUser;
import nl.mpi.lat.auth.principal.PrincipalService;
import nl.mpi.lat.fabric.FabricService;
import nl.mpi.lat.fabric.NodeID;
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
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class AmsFakeRemoteServiceTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock LicenseService mockLicenseService;
    @Mock PrincipalService mockPrincipalService;
    @Mock FabricService mockFabricService;
    @Mock AdvAuthorizationService mockAuthorizationService;
    @Mock IntegratedAuthExportSrv mockIntegratedExportService;
    @Mock CachedCorpusDbExportSrv mockCachedCorpusDbExportService;
    @Mock ApacheAclExportSrv mockWebserverExportService;
    @Mock RecalcTriggerService mockRecalcTriggerService;
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    
    @Mock LatUser mockLatUser;
    @Mock CorpusNode mockOldNode_1;
    @Mock CorpusNode mockNewNode_1;
    @Mock CorpusNode mockOldNode_2;
    @Mock CorpusNode mockNewNode_2;
    @Mock NodeID mockOldNodeID_1;
    @Mock NodeID mockNewNodeID_1;
    @Mock NodeID mockOldNodeID_2;
    @Mock NodeID mockNewNodeID_2;
    
    @Mock CorpusNode mockNode_1;
    @Mock CorpusNode mockNode_2;
    @Mock NodeID mockNodeID_1;
    @Mock NodeID mockNodeID_2;
    
    @Mock CorpusNode mockVersionedNode_1;
    @Mock CorpusNode mockVersionedNode_2;
    @Mock NodeID mockVersionedNodeID_1;
    @Mock NodeID mockVersionedNodeID_2;
    
    @Mock License mockLicense_1;
    @Mock License mockLicense_2;
    
    
    private IAmsRemoteService amsFakeRemoteService;
    
    public AmsFakeRemoteServiceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        amsFakeRemoteService = new AmsFakeRemoteService();
        
        ReflectionTestUtils.setField(amsFakeRemoteService, "licenseService", mockLicenseService);
        ReflectionTestUtils.setField(amsFakeRemoteService, "principalService", mockPrincipalService);
        ReflectionTestUtils.setField(amsFakeRemoteService, "fabricService", mockFabricService);
        ReflectionTestUtils.setField(amsFakeRemoteService, "authorizationService", mockAuthorizationService);
        ReflectionTestUtils.setField(amsFakeRemoteService, "integratedExportService", mockIntegratedExportService);
        ReflectionTestUtils.setField(amsFakeRemoteService, "cachedCorpusDbExportService", mockCachedCorpusDbExportService);
        ReflectionTestUtils.setField(amsFakeRemoteService, "webserverExportService", mockWebserverExportService);
        ReflectionTestUtils.setField(amsFakeRemoteService, "recalcTriggerService", mockRecalcTriggerService);
        
        ReflectionTestUtils.setField(amsFakeRemoteService, "corpusStructureProvider", mockCorpusStructureProvider);
        ReflectionTestUtils.setField(amsFakeRemoteService, "nodeResolver", mockNodeResolver);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void getLicenseAcceptance() {
        
//        final String nodeID = "11";
//        final String nodeMpiID = "MPI11#";
//        
//        final List<License> nodeLicenses = new ArrayList<>();
//        nodeLicenses.add(mockLicense_1);
//        nodeLicenses.add(mockLicense_2);
//    
//        context.checking(new Expectations() {{
//            
//            oneOf(mockFabricService).newNodeID(nodeMpiID); will(returnValue(mockNodeID_1));
//            oneOf(mockLicenseService).getLicenses(mockNodeID_1); will(returnValue(nodeLicenses));
//            
//            //loop - first iteration
//            
//            
//            //loop - second iteration
//        }});
//        
//        amsFakeRemoteService.getLicenseAcceptance(nodeID);
        
        
        fail("not tested yet");
    }


    @Test
    public void getUserEmailAddress() {

        final String userID = "someUser";
        final String expectedEmailAdress = "someUser@mpi.nl";
        
        context.checking(new Expectations() {{
            
            oneOf(mockPrincipalService).getUser(userID); will(returnValue(mockLatUser));
            oneOf(mockLatUser).getEmail(); will(returnValue(expectedEmailAdress));
        }});
        
        String retrievedEmailAddress = amsFakeRemoteService.getUserEmailAddress(userID);
        
        assertEquals("Retrieved email address different from expected", expectedEmailAdress, retrievedEmailAddress);
    }

    @Test
    public void replaceNodesWithDefaultAccessRules() {
        
        final Map<URI, URI> replacementURIs = new HashMap<>();
        final URI oldURI_1 = URI.create(UUID.randomUUID().toString());
        final String oldID_1 = "11";
        final String oldMpiID_1 = "MPI11#";
        final URI newURI_1 = URI.create(UUID.randomUUID().toString());
        final String newID_1 = "12";
        final String newMpiID_1 = "MPI12#";
        replacementURIs.put(oldURI_1, newURI_1);
        final URI oldURI_2 = URI.create(UUID.randomUUID().toString());
        final String oldID_2 = "13";
        final String oldMpiID_2 = "MPI13#";
        final URI newURI_2 = URI.create(UUID.randomUUID().toString());
        final String newID_2 = "14";
        final String newMpiID_2 = "MPI14#";
        replacementURIs.put(oldURI_2, newURI_2);
        
        final Set<Map.Entry<URI,URI>> entries = replacementURIs.entrySet();
        
        final String userID = "someUser";
        
        context.checking(new Expectations() {{
            
            //loop - first iteration
            oneOf(mockCorpusStructureProvider).getNode(oldURI_1); will(returnValue(mockOldNode_1));
            oneOf(mockNodeResolver).getId(mockOldNode_1); will(returnValue(oldID_1));
            oneOf(mockCorpusStructureProvider).getNode(newURI_1); will(returnValue(mockNewNode_1));
            oneOf(mockNodeResolver).getId(mockNewNode_1); will(returnValue(newID_1));
            oneOf(mockFabricService).newNodeID(oldMpiID_1); will(returnValue(mockOldNodeID_1));
            oneOf(mockFabricService).newNodeID(newMpiID_1); will(returnValue(mockNewNodeID_1));
            
            oneOf(mockPrincipalService).getUser(userID); will(returnValue(mockLatUser));
            oneOf(mockAuthorizationService).performReplaceActionsOnNode(mockOldNodeID_1, mockNewNodeID_1, mockLatUser);
            
            //loop - second iteration
            oneOf(mockCorpusStructureProvider).getNode(oldURI_2); will(returnValue(mockOldNode_2));
            oneOf(mockNodeResolver).getId(mockOldNode_2); will(returnValue(oldID_2));
            oneOf(mockCorpusStructureProvider).getNode(newURI_2); will(returnValue(mockNewNode_2));
            oneOf(mockNodeResolver).getId(mockNewNode_2); will(returnValue(newID_2));
            oneOf(mockFabricService).newNodeID(oldMpiID_2); will(returnValue(mockOldNodeID_2));
            oneOf(mockFabricService).newNodeID(newMpiID_2); will(returnValue(mockNewNodeID_2));
            
            oneOf(mockPrincipalService).getUser(userID); will(returnValue(mockLatUser));
            oneOf(mockAuthorizationService).performReplaceActionsOnNode(mockOldNodeID_2, mockNewNodeID_2, mockLatUser);
        }});
        
        
        amsFakeRemoteService.replaceNodesWithDefaultAccessRules(replacementURIs, userID);
    }

    @Test
    public void triggerRightsRecalculationIntegratedService() {

        final Collection<URI> targetURIs = new ArrayList<>();
        final URI nodeURI_1 = URI.create(UUID.randomUUID().toString());
        final String nodeID_1 = "11";
        final String nodeMpiID_1 = "MPI11#";
        targetURIs.add(nodeURI_1);
        final URI nodeURI_2 = URI.create(UUID.randomUUID().toString());
        final String nodeID_2 = "12";
        final String nodeMpiID_2 = "MPI12#";
        targetURIs.add(nodeURI_2);
        
        final Set<NodeID> targetNodeIDs = new HashSet<>();
        targetNodeIDs.add(mockNodeID_1);
        targetNodeIDs.add(mockNodeID_2);
        
        context.checking(new Expectations() {{
            
            //loop - first iteration
            oneOf(mockCorpusStructureProvider).getNode(nodeURI_1); will(returnValue(mockNode_1));
            oneOf(mockNodeResolver).getId(mockNode_1); will(returnValue(nodeID_1));
            oneOf(mockFabricService).newNodeID(nodeMpiID_1); will(returnValue(mockNodeID_1));
            
            //loop - second iteration
            oneOf(mockCorpusStructureProvider).getNode(nodeURI_2); will(returnValue(mockNode_2));
            oneOf(mockNodeResolver).getId(mockNode_2); will(returnValue(nodeID_2));
            oneOf(mockFabricService).newNodeID(nodeMpiID_2); will(returnValue(mockNodeID_2));
            
            oneOf(mockRecalcTriggerService).triggerRecalculation(mockIntegratedExportService, targetNodeIDs, false);
        }});
        
        amsFakeRemoteService.triggerRightsRecalculation(targetURIs, true, true);
    }
    
    @Test
    public void triggerRightsRecalculationCachedService() {
        
        final Collection<URI> targetURIs = new ArrayList<>();
        final URI nodeURI_1 = URI.create(UUID.randomUUID().toString());
        final String nodeID_1 = "11";
        final String nodeMpiID_1 = "MPI11#";
        targetURIs.add(nodeURI_1);
        final URI nodeURI_2 = URI.create(UUID.randomUUID().toString());
        final String nodeID_2 = "12";
        final String nodeMpiID_2 = "MPI12#";
        targetURIs.add(nodeURI_2);
        
        final Set<NodeID> targetNodeIDs = new HashSet<>();
        targetNodeIDs.add(mockNodeID_1);
        targetNodeIDs.add(mockNodeID_2);
        
        context.checking(new Expectations() {{
            
            //loop - first iteration
            oneOf(mockCorpusStructureProvider).getNode(nodeURI_1); will(returnValue(mockNode_1));
            oneOf(mockNodeResolver).getId(mockNode_1); will(returnValue(nodeID_1));
            oneOf(mockFabricService).newNodeID(nodeMpiID_1); will(returnValue(mockNodeID_1));
            
            //loop - second iteration
            oneOf(mockCorpusStructureProvider).getNode(nodeURI_2); will(returnValue(mockNode_2));
            oneOf(mockNodeResolver).getId(mockNode_2); will(returnValue(nodeID_2));
            oneOf(mockFabricService).newNodeID(nodeMpiID_2); will(returnValue(mockNodeID_2));
            
            oneOf(mockRecalcTriggerService).triggerRecalculation(mockCachedCorpusDbExportService, targetNodeIDs, false);
        }});
        
        amsFakeRemoteService.triggerRightsRecalculation(targetURIs, true, false);
    }
    
    @Test
    public void triggerRightsRecalculationApacheService() {
        
        final Collection<URI> targetURIs = new ArrayList<>();
        final URI nodeURI_1 = URI.create(UUID.randomUUID().toString());
        final String nodeID_1 = "11";
        final String nodeMpiID_1 = "MPI11#";
        targetURIs.add(nodeURI_1);
        final URI nodeURI_2 = URI.create(UUID.randomUUID().toString());
        final String nodeID_2 = "12";
        final String nodeMpiID_2 = "MPI12#";
        targetURIs.add(nodeURI_2);
        
        final Set<NodeID> targetNodeIDs = new HashSet<>();
        targetNodeIDs.add(mockNodeID_1);
        targetNodeIDs.add(mockNodeID_2);
        
        context.checking(new Expectations() {{
            
            //loop - first iteration
            oneOf(mockCorpusStructureProvider).getNode(nodeURI_1); will(returnValue(mockNode_1));
            oneOf(mockNodeResolver).getId(mockNode_1); will(returnValue(nodeID_1));
            oneOf(mockFabricService).newNodeID(nodeMpiID_1); will(returnValue(mockNodeID_1));
            
            //loop - second iteration
            oneOf(mockCorpusStructureProvider).getNode(nodeURI_2); will(returnValue(mockNode_2));
            oneOf(mockNodeResolver).getId(mockNode_2); will(returnValue(nodeID_2));
            oneOf(mockFabricService).newNodeID(nodeMpiID_2); will(returnValue(mockNodeID_2));
            
            oneOf(mockRecalcTriggerService).triggerRecalculation(mockWebserverExportService, targetNodeIDs, false);
        }});
        
        amsFakeRemoteService.triggerRightsRecalculation(targetURIs, false, true);
    }

    @Test
    public void triggerRightsRecalculationWithVersionedNodes() {

        final URI targetURI = URI.create(UUID.randomUUID().toString());
        final String targetID = "11";
        final String targetMpiID = "MPI11#";
        final Collection<URI> versionedNodeURIs = new ArrayList<>();
        final URI versionedURI_1 = URI.create(UUID.randomUUID().toString());
        final String versionedID_1 = "12";
        final String versionedMpiID_1 = "MPI12#";
        final URI versionedURI_2 = URI.create(UUID.randomUUID().toString());
        final String versionedID_2 = "13";
        final String versionedMpiID_2 = "MPI13#";
        versionedNodeURIs.add(versionedURI_1);
        versionedNodeURIs.add(versionedURI_2);
        
        final Set<NodeID> targetNodeIDs = new HashSet<>();
        targetNodeIDs.add(mockNodeID_1);
        
        final Set<NodeID> versionedTargetNodeIDs = new HashSet<>();
        versionedTargetNodeIDs.add(mockVersionedNodeID_1);
        versionedTargetNodeIDs.add(mockVersionedNodeID_2);
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(targetURI); will(returnValue(mockNode_1));
            oneOf(mockNodeResolver).getId(mockNode_1); will(returnValue(targetID));
            oneOf(mockFabricService).newNodeID(targetMpiID); will(returnValue(mockNodeID_1));
            
            oneOf(mockRecalcTriggerService).triggerRecalculation(mockIntegratedExportService, targetNodeIDs, false);
            
            //loop - first iteration
            oneOf(mockCorpusStructureProvider).getNode(versionedURI_1); will(returnValue(mockVersionedNode_1));
            oneOf(mockNodeResolver).getId(mockVersionedNode_1); will(returnValue(versionedID_1));
            oneOf(mockFabricService).newNodeID(versionedMpiID_1); will(returnValue(mockVersionedNodeID_1));
            
            //loop - second iteration
            oneOf(mockCorpusStructureProvider).getNode(versionedURI_2); will(returnValue(mockVersionedNode_2));
            oneOf(mockNodeResolver).getId(mockVersionedNode_2); will(returnValue(versionedID_2));
            oneOf(mockFabricService).newNodeID(versionedMpiID_2); will(returnValue(mockVersionedNodeID_2));
            
            oneOf(mockRecalcTriggerService).triggerRecalculation(mockIntegratedExportService, versionedTargetNodeIDs, false);
        }});
        
        
        amsFakeRemoteService.triggerRightsRecalculationWithVersionedNodes(targetURI, versionedNodeURIs);
    }

    @Test
    public void testTriggerRightsRecalculationForVersionedNodes() {
        
        final Collection<URI> versionedNodeURIs = new ArrayList<>();
        final URI versionedURI_1 = URI.create(UUID.randomUUID().toString());
        final String versionedID_1 = "12";
        final String versionedMpiID_1 = "MPI12#";
        final URI versionedURI_2 = URI.create(UUID.randomUUID().toString());
        final String versionedID_2 = "13";
        final String versionedMpiID_2 = "MPI13#";
        versionedNodeURIs.add(versionedURI_1);
        versionedNodeURIs.add(versionedURI_2);
        
        final Set<NodeID> versionedTargetNodeIDs = new HashSet<>();
        versionedTargetNodeIDs.add(mockVersionedNodeID_1);
        versionedTargetNodeIDs.add(mockVersionedNodeID_2);
        
        context.checking(new Expectations() {{
            
            //loop - first iteration
            oneOf(mockCorpusStructureProvider).getNode(versionedURI_1); will(returnValue(mockVersionedNode_1));
            oneOf(mockNodeResolver).getId(mockVersionedNode_1); will(returnValue(versionedID_1));
            oneOf(mockFabricService).newNodeID(versionedMpiID_1); will(returnValue(mockVersionedNodeID_1));
            
            //loop - second iteration
            oneOf(mockCorpusStructureProvider).getNode(versionedURI_2); will(returnValue(mockVersionedNode_2));
            oneOf(mockNodeResolver).getId(mockVersionedNode_2); will(returnValue(versionedID_2));
            oneOf(mockFabricService).newNodeID(versionedMpiID_2); will(returnValue(mockVersionedNodeID_2));
            
            oneOf(mockRecalcTriggerService).triggerRecalculation(mockIntegratedExportService, versionedTargetNodeIDs, false);
        }});
        
        amsFakeRemoteService.triggerRightsRecalculationForVersionedNodes(versionedNodeURIs, true, true);
    }
}