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
package nl.mpi.lamus.ams.implementation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.util.implementation.MockableURL;
import nl.mpi.lat.ams.AmsLicense;
import nl.mpi.lat.ams.AmsLicenseFactory;
import nl.mpi.lat.ams.IAmsRemoteService;
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
import static org.powermock.api.support.membermodification.MemberModifier.suppress;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AmsLicenseFactory.class, BeanUtils.class})
public class AmsFakeRemoteServiceTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock LicenseService mockLicenseService;
    @Mock PrincipalService mockPrincipalService;
    @Mock FabricService mockFabricService;
    @Mock AdvAuthorizationService mockAuthorizationService;
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock AmsFakeRemoteServiceHelper mockRemoteServiceHelper;
    
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
    
    @Mock AmsLicense mockAmsLicense;
    
    
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
        
        ReflectionTestUtils.setField(amsFakeRemoteService, "corpusStructureProvider", mockCorpusStructureProvider);
        ReflectionTestUtils.setField(amsFakeRemoteService, "nodeResolver", mockNodeResolver);
        ReflectionTestUtils.setField(amsFakeRemoteService, "remoteServiceHelper", mockRemoteServiceHelper);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void getLicenseAcceptance() {
        
        final String nodeID = "11";
        final String nodeMpiID = "MPI11#";
        
        final String license1_link = "http://link/to/license1";
        final String license2_link = "https://link/to/license2";
        
        final List<License> nodeLicenses = new ArrayList<>();
        nodeLicenses.add(mockLicense_1);
        
        final List<AmsLicense> expectedAmsLicenses = new ArrayList<>();
        expectedAmsLicenses.add(mockAmsLicense);
    
        context.checking(new Expectations() {{
            
            oneOf(mockFabricService).newNodeID(nodeMpiID); will(returnValue(mockNodeID_1));
            oneOf(mockLicenseService).getLicenses(mockNodeID_1); will(returnValue(nodeLicenses));
            
            //loop - first iteration
            oneOf(mockLicenseService).getLicenseLink(mockLicense_1); will(returnValue(license1_link));
            oneOf(mockAmsLicense).setLinkToLicense(license1_link);
            
        }});
        
        stub(method(AmsLicenseFactory.class, "getNewAmsLicense")).toReturn(mockAmsLicense);
        suppress(method(BeanUtils.class, "copyProperties", AmsLicense.class, License.class));
        
        List<AmsLicense> retrievedAmsLicenses = amsFakeRemoteService.getLicenseAcceptance(nodeID);
        
        assertEquals("List of AmsLicenses different from expected", expectedAmsLicenses, retrievedAmsLicenses);
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
    public void triggerRightsRecalculation() throws UnsupportedEncodingException, MalformedURLException, IOException {

        final Collection<URI> targetURIs = new ArrayList<>();
        final URI nodeURI_1 = URI.create(UUID.randomUUID().toString());
        final String nodeMpiID_1 = "MPI11#";
        targetURIs.add(nodeURI_1);
        final URI nodeURI_2 = URI.create(UUID.randomUUID().toString());
        final String nodeMpiID_2 = "MPI12#";
        targetURIs.add(nodeURI_2);
        
        final StringBuilder targetNodeIDs = new StringBuilder();
        targetNodeIDs.append(nodeMpiID_1);
        targetNodeIDs.append(nodeMpiID_2);
        
        final MockableURL recalcUrl = new MockableURL(new URL("http://some/url/recalc/bla/bla"));
        
        final boolean triggerCsTranscription = Boolean.TRUE;
        final boolean triggerWsTranscription = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            oneOf(mockRemoteServiceHelper).getTargetNodeIDsAsString(targetURIs);
                will(returnValue(targetNodeIDs.toString()));
            oneOf(mockRemoteServiceHelper).getRecalcUrl(triggerCsTranscription, triggerWsTranscription, targetNodeIDs.toString());
                will(returnValue(recalcUrl));
            oneOf(mockRemoteServiceHelper).sendCallToAccessRightsManagementSystem(recalcUrl);
            
        }});
        
        amsFakeRemoteService.triggerRightsRecalculation(targetURIs, triggerCsTranscription, triggerWsTranscription);
    }
    
    @Test
    public void triggerRightsRecalculationUnsupportedEncodingException() throws UnsupportedEncodingException, MalformedURLException {
        
        final Collection<URI> targetURIs = new ArrayList<>();
        final URI nodeURI_1 = URI.create(UUID.randomUUID().toString());
        final String nodeMpiID_1 = "MPI11#";
        targetURIs.add(nodeURI_1);
        final URI nodeURI_2 = URI.create(UUID.randomUUID().toString());
        final String nodeMpiID_2 = "MPI12#";
        targetURIs.add(nodeURI_2);
        
        final StringBuilder targetNodeIDs = new StringBuilder();
        targetNodeIDs.append(nodeMpiID_1);
        targetNodeIDs.append(nodeMpiID_2);
        
        final UnsupportedEncodingException expectedCause = new UnsupportedEncodingException();
        final String expectedMessage = "Error constructing AMS recalculation URL";
        
        final boolean triggerCsTranscription = Boolean.TRUE;
        final boolean triggerWsTranscription = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            oneOf(mockRemoteServiceHelper).getTargetNodeIDsAsString(targetURIs);
                will(returnValue(targetNodeIDs.toString()));
            oneOf(mockRemoteServiceHelper).getRecalcUrl(triggerCsTranscription, triggerWsTranscription, targetNodeIDs.toString());
                will(throwException(expectedCause));
        }});
        
        try {
            amsFakeRemoteService.triggerRightsRecalculation(targetURIs, triggerCsTranscription, triggerWsTranscription);
            fail("should have thrown exception");
        } catch(RuntimeException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedCause, ex.getCause());
        }
    }
    
    @Test
    public void triggerRightsRecalculationMalformedURLException() throws UnsupportedEncodingException, MalformedURLException {
        
        final Collection<URI> targetURIs = new ArrayList<>();
        final URI nodeURI_1 = URI.create(UUID.randomUUID().toString());
        final String nodeMpiID_1 = "MPI11#";
        targetURIs.add(nodeURI_1);
        final URI nodeURI_2 = URI.create(UUID.randomUUID().toString());
        final String nodeMpiID_2 = "MPI12#";
        targetURIs.add(nodeURI_2);
        
        final StringBuilder targetNodeIDs = new StringBuilder();
        targetNodeIDs.append(nodeMpiID_1);
        targetNodeIDs.append(nodeMpiID_2);
        
        final MalformedURLException expectedCause = new MalformedURLException();
        final String expectedMessage = "Error constructing AMS recalculation URL";
        
        final boolean triggerCsTranscription = Boolean.TRUE;
        final boolean triggerWsTranscription = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            oneOf(mockRemoteServiceHelper).getTargetNodeIDsAsString(targetURIs);
                will(returnValue(targetNodeIDs.toString()));
            oneOf(mockRemoteServiceHelper).getRecalcUrl(triggerCsTranscription, triggerWsTranscription, targetNodeIDs.toString());
                will(throwException(expectedCause));
        }});
        
        try {
            amsFakeRemoteService.triggerRightsRecalculation(targetURIs, triggerCsTranscription, triggerWsTranscription);
            fail("should have thrown exception");
        } catch(RuntimeException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedCause, ex.getCause());
        }
    }
    
    @Test
    public void triggerRightsRecalculationIOException() throws UnsupportedEncodingException, MalformedURLException, IOException {
        
        final Collection<URI> targetURIs = new ArrayList<>();
        final URI nodeURI_1 = URI.create(UUID.randomUUID().toString());
        final String nodeMpiID_1 = "MPI11#";
        targetURIs.add(nodeURI_1);
        final URI nodeURI_2 = URI.create(UUID.randomUUID().toString());
        final String nodeMpiID_2 = "MPI12#";
        targetURIs.add(nodeURI_2);
        
        final StringBuilder targetNodeIDs = new StringBuilder();
        targetNodeIDs.append(nodeMpiID_1);
        targetNodeIDs.append(nodeMpiID_2);
        
        final MockableURL recalcUrl = new MockableURL(new URL("http://some/url/recalc/bla/bla"));
        
        final IOException expectedCause = new IOException();
        final String expectedMessage = "Error invoking AMS rights recalculation";
        
        final boolean triggerCsTranscription = Boolean.TRUE;
        final boolean triggerWsTranscription = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            oneOf(mockRemoteServiceHelper).getTargetNodeIDsAsString(targetURIs);
                will(returnValue(targetNodeIDs.toString()));
            oneOf(mockRemoteServiceHelper).getRecalcUrl(triggerCsTranscription, triggerWsTranscription, targetNodeIDs.toString());
                will(returnValue(recalcUrl));
            oneOf(mockRemoteServiceHelper).sendCallToAccessRightsManagementSystem(recalcUrl);
                will(throwException(expectedCause));
        }});
        
        try {
            amsFakeRemoteService.triggerRightsRecalculation(targetURIs, triggerCsTranscription, triggerWsTranscription);
            fail("should have thrown exception");
        } catch(RuntimeException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedCause, ex.getCause());
        }
    }
}