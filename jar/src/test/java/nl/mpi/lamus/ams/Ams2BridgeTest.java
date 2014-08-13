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
import java.util.UUID;
import nl.mpi.lat.ams.IAmsRemoteService;
//import nl.mpi.lat.ams.authentication.impl.IntegratedAuthenticationSrv;
//import nl.mpi.lat.ams.model.NodeAuth;
//import nl.mpi.lat.ams.model.NodePcplRule;
//import nl.mpi.lat.ams.model.rule.DomainEditor;
//import nl.mpi.lat.ams.service.LicenseService;
//import nl.mpi.lat.ams.service.RuleService;
//import nl.mpi.lat.ams.service.impl.AmsAuthorizationSrv;
//import nl.mpi.lat.ams.service.impl.LicenseSrv;
//import nl.mpi.lat.ams.service.impl.PrincipalSrv;
//import nl.mpi.lat.ams.service.impl.RuleSrv;
//import nl.mpi.lat.auth.authentication.AuthenticationService;
//import nl.mpi.lat.auth.authorization.AdvAuthorizationService;
//import nl.mpi.lat.auth.principal.LatUser;
//import nl.mpi.lat.auth.principal.PrincipalService;
//import nl.mpi.lat.dao.DataSourceException;
//import nl.mpi.lat.fabric.FabricService;
//import nl.mpi.lat.fabric.NodeID;
//import nl.mpi.latimpl.fabric.FabricSrv;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"Ams2BridgeTest-context.xml", "Ams2BridgeTest_authentication-context.xml", "Ams2BridgeTest_core-context.xml"})
//@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
//@ActiveProfiles("testing")
public class Ams2BridgeTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock IAmsRemoteService mockAmsRemoteService;
    
    private Ams2Bridge ams2Bridge;
    
//    @Mock LatUser mockUser;
//    @Mock NodeID mockNodeID;
    
    public Ams2BridgeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        
        ams2Bridge = new Ams2Bridge();
        ReflectionTestUtils.setField(ams2Bridge, "amsRemoteService", mockAmsRemoteService);
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
        
        String retrievedEmailAddress = ams2Bridge.getMailAddress(userID);
        
        assertEquals("Retrieved email address different from expected", emailAddress, retrievedEmailAddress);
    }
    
   
//    @Test
    public void getUsedStorageSpaceUnknownNodeException() {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        
//        getsUnknownNodeExceptionDomEditorRuleOpts(nodeIDStr);
//        
//        long usedStorageSpace = testAms2BridgeWithMockServices.getUsedStorageSpace(userID, nodeIDStr);
//        assertEquals(AmsBridge.ERROR_MB.longValue(), usedStorageSpace);
    }
    
//    @Test
    public void getUsedStorageSpaceDataSourceException() {
  
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        
//        getsDataSourceExceptionDomEditorRuleOpts(userID, nodeIDStr);
//        
//        long usedStorageSpace = testAms2BridgeWithMockServices.getUsedStorageSpace(userID, nodeIDStr);
//        assertEquals(AmsBridge.ERROR_MB.longValue(), usedStorageSpace);
    }
    
//    @Test
    public void getUsedStorageSpaceNullDomainRules() {
  
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        
//        getsNullDomEditorRuleOpts(userID, nodeIDStr);
//        
//        long usedStorageSpace = testAms2BridgeWithMockServices.getUsedStorageSpace(userID, nodeIDStr);
//        assertEquals(AmsBridge.DEFAULT_MB.longValue(), usedStorageSpace);
    }
    
//    @Test
    public void getUsedStorageSpaceNullInteger() {
      
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        final NodePcplRule nodePcplRule = new NodePcplRule();
//        nodePcplRule.setMaxStorageMB(null);
//        
//        getsDomEditorRuleOpts(userID, nodeIDStr, nodePcplRule);
//        
//        long usedStorageSpace = testAms2BridgeWithMockServices.getUsedStorageSpace(userID, nodeIDStr);
//        assertEquals(AmsBridge.DEFAULT_MB.longValue(), usedStorageSpace);
    }
    
//    @Test
    public void getUsedStorageSpaceExistingDomainRules() {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        int expectedUsedStorageSpaceInMB = 10;
//        long expectedUsedStorageSpaceInBytes = 10 * 1024 * 1024;
//        final NodePcplRule nodePcplRule = new NodePcplRule();
//        nodePcplRule.setUsedStorageMB(expectedUsedStorageSpaceInMB);
//        
//        getsDomEditorRuleOpts(userID, nodeIDStr, nodePcplRule);
//        
//        long usedStorageSpace = testAms2BridgeWithMockServices.getUsedStorageSpace(userID, nodeIDStr);
//        assertEquals(expectedUsedStorageSpaceInBytes, usedStorageSpace);
    }
    
//    @Test
    public void setUsedStorageSpaceUnknownNodeException() {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        long expectedUsedStorageSpaceInBytes = 10 * 1024 * 1024;
//        
//        getsUnknownNodeExceptionDomEditorRuleOpts(nodeIDStr);
//        
//        testAms2BridgeWithMockServices.setUsedStorageSpace(userID, nodeIDStr, expectedUsedStorageSpaceInBytes);
    }
    
//    @Test
    public void setUsedStorageSpaceDataSourceException() {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        long expectedUsedStorageSpaceInBytes = 10 * 1024 * 1024;
//        
//        getsDataSourceExceptionDomEditorRuleOpts(userID, nodeIDStr);
//        
//        testAms2BridgeWithMockServices.setUsedStorageSpace(userID, nodeIDStr, expectedUsedStorageSpaceInBytes);
    }
    
//    @Test
    public void setUsedStorageSpaceNullDomainRules() {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        long expectedUsedStorageSpaceInBytes = 10 * 1024 * 1024;
//        
//        getsNullDomEditorRuleOpts(userID, nodeIDStr);
//        
//        testAms2BridgeWithMockServices.setUsedStorageSpace(userID, nodeIDStr, expectedUsedStorageSpaceInBytes);
    }
    
//    @Test
    public void setUsedStorageSpaceExistingDomainRulesVirtual() {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        long expectedUsedStorageSpaceInBytes = 10 * 1024 * 1024;
//        final NodePcplRule nodePcplRule = new NodePcplRule();
//        nodePcplRule.setRule(new DomainEditor());
//        nodePcplRule.setParent(new NodeAuth());
//        nodePcplRule.setVirtual(true);
//        
//        getsDomEditorRuleOpts(userID, nodeIDStr, nodePcplRule);
//        
//        context.checking(new Expectations() {{
//            never (mockAuthorizationSrv).save(nodePcplRule.getParent());
//        }});
//        
//        testAms2BridgeWithMockServices.setUsedStorageSpace(userID, nodeIDStr, expectedUsedStorageSpaceInBytes);
//        assertEquals(null, nodePcplRule.getUsedStorageMB());
    }
    
//    @Test
    public void setUsedStorageSpaceExistingDomainRulesNotVirtual() {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        int expectedUsedStorageSpaceInMB = 10;
//        long expectedUsedStorageSpaceInBytes = 10 * 1024 * 1024;
//        final NodePcplRule nodePcplRule = new NodePcplRule();
//        nodePcplRule.setRule(new DomainEditor());
//        nodePcplRule.setParent(new NodeAuth());
//        nodePcplRule.setVirtual(false);
//        
//        getsDomEditorRuleOpts(userID, nodeIDStr, nodePcplRule);
//        
//        context.checking(new Expectations() {{
//            oneOf (mockAuthorizationSrv).save(nodePcplRule.getParent());
//        }});
//        
//        testAms2BridgeWithMockServices.setUsedStorageSpace(userID, nodeIDStr, expectedUsedStorageSpaceInBytes);
//        assertEquals(Integer.valueOf(expectedUsedStorageSpaceInMB), nodePcplRule.getUsedStorageMB());
    }

//    @Test
    public void getMaxStorageSpaceUnknownNodeException() {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        
//        getsUnknownNodeExceptionDomEditorRuleOpts(nodeIDStr);
//        
//        long maxStorageSpace = testAms2BridgeWithMockServices.getMaxStorageSpace(userID, nodeIDStr);
//        assertEquals(AmsBridge.ERROR_MB.longValue(), maxStorageSpace);
    }
    
//    @Test
    public void getMaxStorageSpaceDataSourceException() {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        
//        getsDataSourceExceptionDomEditorRuleOpts(userID, nodeIDStr);
//        
//        long maxStorageSpace = testAms2BridgeWithMockServices.getMaxStorageSpace(userID, nodeIDStr);
//        assertEquals(AmsBridge.ERROR_MB.longValue(), maxStorageSpace);
    }
    
//    @Test
    public void getMaxStorageSpaceNullDomainRules() {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        
//        getsNullDomEditorRuleOpts(userID, nodeIDStr);
//        
//        long maxStorageSpace = testAms2BridgeWithMockServices.getMaxStorageSpace(userID, nodeIDStr);
//        assertEquals(AmsBridge.DEFAULT_MB.longValue(), maxStorageSpace);
    }
    
//    @Test
    public void getMaxStorageSpaceNullInteger() {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        final NodePcplRule nodePcplRule = new NodePcplRule();
//        nodePcplRule.setMaxStorageMB(null);
//        
//        getsDomEditorRuleOpts(userID, nodeIDStr, nodePcplRule);
//        
//        long maxStorageSpace = testAms2BridgeWithMockServices.getMaxStorageSpace(userID, nodeIDStr);
//        assertEquals(AmsBridge.DEFAULT_MB.longValue(), maxStorageSpace);
    }
    
//    @Test
    public void getMaxStorageSpaceExistingDomainRules() {
        
//TODO Temporarily commented out, until AMS is changed in order to support URIs instead of NodeIDs
        
//        String userID = "someUser";
//        String nodeIDStr = "MPI10#";
//        int expectedMaxStorageSpaceInMB = 10;
//        long expectedMaxStorageSpaceInBytes = 10 * 1024 * 1024;
//        final NodePcplRule nodePcplRule = new NodePcplRule();
//        nodePcplRule.setMaxStorageMB(expectedMaxStorageSpaceInMB);
//        
//        getsDomEditorRuleOpts(userID, nodeIDStr, nodePcplRule);
//        
//        long usedStorageSpace = testAms2BridgeWithMockServices.getMaxStorageSpace(userID, nodeIDStr);
//        assertEquals(expectedMaxStorageSpaceInBytes, usedStorageSpace);
    }
    
//    private void getsDataSourceExceptionDomEditorRuleOpts(final String userID, final String nodeIDStr) {
//        
//        context.checking(new Expectations() {{
//            oneOf (mockFabricSrv).newNodeID(nodeIDStr); will(returnValue(null));
//            oneOf (mockPrincipalSrv).getUser(userID); will(throwException(new DataSourceException("Some problem getting user")));
//        }});
//    }
    
//    private void getsNullDomEditorRuleOpts(final String userID, final String nodeIDStr) {
//        
//        context.checking(new Expectations() {{
//            oneOf (mockFabricSrv).newNodeID(nodeIDStr); will(returnValue(mockNodeID));
//            oneOf (mockPrincipalSrv).getUser(userID); will(returnValue(mockUser));
//            oneOf (mockAuthorizationSrv).getEffectiveDomainEditorRule(mockNodeID, mockUser); will(returnValue(null));
//        }});
//    }
    
//    private void getsDomEditorRuleOpts(final String userID, final String nodeIDStr, final NodePcplRule nodePcplRule) {
//        
//        context.checking(new Expectations() {{
//            oneOf (mockFabricSrv).newNodeID(nodeIDStr); will(returnValue(mockNodeID));
//            oneOf (mockPrincipalSrv).getUser(userID); will(returnValue(mockUser));
//            oneOf (mockAuthorizationSrv).getEffectiveDomainEditorRule(mockNodeID, mockUser); will(returnValue(nodePcplRule));
//        }});
//    }
    
    @Test
    public void triggerAccessRightsRecalculation() throws URISyntaxException {
        
        final URI workspaceRootNodeURI = new URI(UUID.randomUUID().toString());
        
        final Collection<URI> recalculationTargetURIs = new ArrayList<>();
        recalculationTargetURIs.add(workspaceRootNodeURI);
        
        context.checking(new Expectations() {{
            
            oneOf(mockAmsRemoteService).triggerRightsRecalculation(recalculationTargetURIs, Boolean.TRUE, Boolean.TRUE);
        }});
        
        ams2Bridge.triggerAccessRightsRecalculation(workspaceRootNodeURI);
    }
}
