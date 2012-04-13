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

import nl.mpi.corpusstructure.UnknownNodeException;
import nl.mpi.lat.ams.model.NodeAuth;
import nl.mpi.lat.ams.model.NodePcplRule;
import nl.mpi.lat.ams.model.rule.DomainEditor;
import nl.mpi.lat.ams.service.LicenseService;
import nl.mpi.lat.ams.service.RuleService;
import nl.mpi.lat.auth.authentication.AuthenticationService;
import nl.mpi.lat.auth.authorization.AdvAuthorizationService;
import nl.mpi.lat.auth.principal.LatUser;
import nl.mpi.lat.auth.principal.PrincipalService;
import nl.mpi.lat.dao.DataSourceException;
import nl.mpi.lat.fabric.FabricService;
import nl.mpi.lat.fabric.NodeID;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"Ams2BridgeTest-context.xml", "Ams2BridgeTest_authentication-context.xml", "Ams2BridgeTest_core-context.xml"})
public class Ams2BridgeTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Autowired
    Ams2Bridge testAms2BridgeFromSpringContext;
    
    private Ams2Bridge testAms2BridgeWithMockServices;
    private Ams2Bridge testAms2BridgeWithNullFabricService;
    @Mock PrincipalService mockPrincipalSrv;
    @Mock AuthenticationService mockAuthenticationSrv;
    @Mock AdvAuthorizationService mockAuthorizationSrv;
    @Mock FabricService mockFabricSrv;
    @Mock LicenseService mockLicenseSrv;
    @Mock RuleService mockRuleSrv;
    
    @Mock LatUser mockUser;
    @Mock NodeID mockNodeID;
    
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
        testAms2BridgeWithMockServices = new Ams2Bridge(mockPrincipalSrv, mockAuthenticationSrv,
                mockAuthorizationSrv, mockFabricSrv, mockLicenseSrv, mockRuleSrv);
        testAms2BridgeWithNullFabricService = new Ams2Bridge(mockPrincipalSrv, mockAuthenticationSrv,
                mockAuthorizationSrv, null, mockLicenseSrv, mockRuleSrv);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test the constructor without parameters
     */
    @Test
    public void authorizationServiceInitialisedWhenCallingNoArgConstructor() {
        
//        Ams2Bridge testAmsBridge = new Ams2Bridge();
        AdvAuthorizationService authorizationSrv = testAms2BridgeFromSpringContext.getAuthorizationSrv();
        assertNotNull(authorizationSrv);
    }
    
    /**
     * Test the constructor without parameters
     */
    @Test
    public void principalServiceInitialisedWhenCallingNoArgConstructor() {
        
//        Ams2Bridge testAmsBridge = new Ams2Bridge();
        PrincipalService principalSrv = testAms2BridgeFromSpringContext.getPrincipalSrv();
        assertNotNull(principalSrv);
    }
    
    /**
     * Test the constructor without parameters
     */
    @Test
    public void authenticationServiceInitialisedWhenCallingNoArgConstructor() {
        
//        Ams2Bridge testAmsBridge = new Ams2Bridge();
        AuthenticationService authenticationSrv = testAms2BridgeFromSpringContext.getAuthenticationSrv();
        assertNotNull(authenticationSrv);
    }
    
    /**
     * Test the constructor without parameters
     */
    @Test
    public void fabricServiceInitialisedWhenCallingNoArgConstructor() {
        
//        Ams2Bridge testAmsBridge = new Ams2Bridge();
        FabricService fabricSrv = testAms2BridgeFromSpringContext.getFabricSrv();
        assertNotNull(fabricSrv);
    }
    
    /**
     * Test the constructor without parameters
     */
    @Test
    public void licenseServiceInitialisedWhenCallingNoArgConstructor() {
        
//        Ams2Bridge testAmsBridge = new Ams2Bridge();
        LicenseService licenseSrv = testAms2BridgeFromSpringContext.getLicenseSrv();
        assertNotNull(licenseSrv);
    }
    
    /**
     * Test the constructor without parameters
     */
    @Test
    public void ruleServiceInitialisedWhenCallingNoArgConstructor() {
        
//        Ams2Bridge testAmsBridge = new Ams2Bridge();
        RuleService ruleSrv = testAms2BridgeFromSpringContext.getRuleSrv();
        assertNotNull(ruleSrv);
    }
    
    @Test
    public void statusIsFalseWhenAuthenticationServiceIsNull() {
        
        Ams2Bridge ams2BridgeWithNullAuthorizationService = new Ams2Bridge(mockPrincipalSrv, null,
                mockAuthorizationSrv, mockFabricSrv, mockLicenseSrv, mockRuleSrv);
        assertFalse(ams2BridgeWithNullAuthorizationService.getStatus());
    }
    
    @Test
    public void statusIsFalseWhenAuthorizationServiceIsNull() {
        
        Ams2Bridge ams2BridgeWithNullAuthorizationService = new Ams2Bridge(mockPrincipalSrv, mockAuthenticationSrv,
                null, mockFabricSrv, mockLicenseSrv, mockRuleSrv);
        assertFalse(ams2BridgeWithNullAuthorizationService.getStatus());
    }
    
    @Test
    public void statusIsFalseWhenPrincipalServiceIsNull() {
        
        Ams2Bridge ams2BridgeWithNullAuthorizationService = new Ams2Bridge(null, mockAuthenticationSrv,
                mockAuthorizationSrv, mockFabricSrv, mockLicenseSrv, mockRuleSrv);
        assertFalse(ams2BridgeWithNullAuthorizationService.getStatus());
    }
    
    @Test
    public void statusIsTrueWhenAllServicesAreNotNull() {
        
        Ams2Bridge ams2BridgeWithNullAuthorizationService = new Ams2Bridge(mockPrincipalSrv, mockAuthenticationSrv,
                mockAuthorizationSrv, mockFabricSrv, mockLicenseSrv, mockRuleSrv);
        assertTrue(ams2BridgeWithNullAuthorizationService.getStatus());
    }
    
    @Test
    public void fabricServiceIsCalledWhenClosing() {
        
        context.checking(new Expectations() {{
            oneOf (mockFabricSrv).close();
        }});
        
        testAms2BridgeWithMockServices.close();
    }
    
    @Test
    public void doNothingWhenClosingAndFabricServiceIsNull() {
        
        context.checking(new Expectations() {{
            never (mockFabricSrv).close();
        }});
        
        testAms2BridgeWithNullFabricService.close();
    }
    
    @Test
    public void userHasWriteAccess() {
        
        final String userID = "someUser";
        final String nodeIDStr = "MPI10#";
        
        context.checking(new Expectations() {{
            oneOf (mockPrincipalSrv).getUser(userID); will(returnValue(mockUser));
            oneOf (mockFabricSrv).newNodeID(nodeIDStr); will(returnValue(mockNodeID));
            oneOf (mockAuthorizationSrv).isWriteable(mockUser, mockNodeID); will(returnValue(true));
        }});
        
        boolean hasAccess = testAms2BridgeWithMockServices.hasWriteAccess(userID, nodeIDStr);
        assertTrue(hasAccess);
    }
    
    @Test
    public void userDoesNotHaveWriteAccess() {
        
        final String userID = "someUser";
        final String nodeIDStr = "MPI10#";
        
        context.checking(new Expectations() {{
            oneOf (mockPrincipalSrv).getUser(userID); will(returnValue(mockUser));
            oneOf (mockFabricSrv).newNodeID(nodeIDStr); will(returnValue(mockNodeID));
            oneOf (mockAuthorizationSrv).isWriteable(mockUser, mockNodeID); will(returnValue(false));
        }});
        
        boolean hasAccess = testAms2BridgeWithMockServices.hasWriteAccess(userID, nodeIDStr);
        assertFalse(hasAccess);
    }
    
    @Test
    public void getUsedStorageSpaceUnknownNodeException() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        
        getsUnknownNodeExceptionDomEditorRuleOpts(nodeIDStr);
        
        long usedStorageSpace = testAms2BridgeWithMockServices.getUsedStorageSpace(userID, nodeIDStr);
        assertEquals(AmsBridge.ERROR_MB.longValue(), usedStorageSpace);
    }
    
    @Test
    public void getUsedStorageSpaceDataSourceException() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        
        getsDataSourceExceptionDomEditorRuleOpts(userID, nodeIDStr);
        
        long usedStorageSpace = testAms2BridgeWithMockServices.getUsedStorageSpace(userID, nodeIDStr);
        assertEquals(AmsBridge.ERROR_MB.longValue(), usedStorageSpace);
    }
    
    @Test
    public void getUsedStorageSpaceNullDomainRules() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        
        getsNullDomEditorRuleOpts(userID, nodeIDStr);
        
        long usedStorageSpace = testAms2BridgeWithMockServices.getUsedStorageSpace(userID, nodeIDStr);
        assertEquals(AmsBridge.DEFAULT_MB.longValue(), usedStorageSpace);
    }
    
    @Test
    public void getUsedStorageSpaceNullInteger() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        final NodePcplRule nodePcplRule = new NodePcplRule();
        nodePcplRule.setMaxStorageMB(null);
        
        getsDomEditorRuleOpts(userID, nodeIDStr, nodePcplRule);
        
        long usedStorageSpace = testAms2BridgeWithMockServices.getUsedStorageSpace(userID, nodeIDStr);
        assertEquals(AmsBridge.DEFAULT_MB.longValue(), usedStorageSpace);
    }
    
    @Test
    public void getUsedStorageSpaceExistingDomainRules() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        int expectedUsedStorageSpaceInMB = 10;
        long expectedUsedStorageSpaceInBytes = 10 * 1024 * 1024;
        final NodePcplRule nodePcplRule = new NodePcplRule();
        nodePcplRule.setUsedStorageMB(expectedUsedStorageSpaceInMB);
        
        getsDomEditorRuleOpts(userID, nodeIDStr, nodePcplRule);
        
        long usedStorageSpace = testAms2BridgeWithMockServices.getUsedStorageSpace(userID, nodeIDStr);
        assertEquals(expectedUsedStorageSpaceInBytes, usedStorageSpace);
    }
    
    @Test
    public void setUsedStorageSpaceUnknownNodeException() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        long expectedUsedStorageSpaceInBytes = 10 * 1024 * 1024;
        
        getsUnknownNodeExceptionDomEditorRuleOpts(nodeIDStr);
        
        testAms2BridgeWithMockServices.setUsedStorageSpace(userID, nodeIDStr, expectedUsedStorageSpaceInBytes);
    }
    
    @Test
    public void setUsedStorageSpaceDataSourceException() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        long expectedUsedStorageSpaceInBytes = 10 * 1024 * 1024;
        
        getsDataSourceExceptionDomEditorRuleOpts(userID, nodeIDStr);
        
        testAms2BridgeWithMockServices.setUsedStorageSpace(userID, nodeIDStr, expectedUsedStorageSpaceInBytes);
    }
    
    @Test
    public void setUsedStorageSpaceNullDomainRules() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        long expectedUsedStorageSpaceInBytes = 10 * 1024 * 1024;
        
        getsNullDomEditorRuleOpts(userID, nodeIDStr);
        
        testAms2BridgeWithMockServices.setUsedStorageSpace(userID, nodeIDStr, expectedUsedStorageSpaceInBytes);
    }
    
    @Test
    public void setUsedStorageSpaceExistingDomainRulesVirtual() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        long expectedUsedStorageSpaceInBytes = 10 * 1024 * 1024;
        final NodePcplRule nodePcplRule = new NodePcplRule();
        nodePcplRule.setRule(new DomainEditor());
        nodePcplRule.setParent(new NodeAuth());
        nodePcplRule.setVirtual(true);
        
        getsDomEditorRuleOpts(userID, nodeIDStr, nodePcplRule);
        
        context.checking(new Expectations() {{
            never (mockAuthorizationSrv).save(nodePcplRule.getParent());
        }});
        
        testAms2BridgeWithMockServices.setUsedStorageSpace(userID, nodeIDStr, expectedUsedStorageSpaceInBytes);
        assertEquals(null, nodePcplRule.getUsedStorageMB());
    }
    
    @Test
    public void setUsedStorageSpaceExistingDomainRulesNotVirtual() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        int expectedUsedStorageSpaceInMB = 10;
        long expectedUsedStorageSpaceInBytes = 10 * 1024 * 1024;
        final NodePcplRule nodePcplRule = new NodePcplRule();
        nodePcplRule.setRule(new DomainEditor());
        nodePcplRule.setParent(new NodeAuth());
        nodePcplRule.setVirtual(false);
        
        getsDomEditorRuleOpts(userID, nodeIDStr, nodePcplRule);
        
        context.checking(new Expectations() {{
            oneOf (mockAuthorizationSrv).save(nodePcplRule.getParent());
        }});
        
        testAms2BridgeWithMockServices.setUsedStorageSpace(userID, nodeIDStr, expectedUsedStorageSpaceInBytes);
        assertEquals(Integer.valueOf(expectedUsedStorageSpaceInMB), nodePcplRule.getUsedStorageMB());
    }

    @Test
    public void getMaxStorageSpaceUnknownNodeException() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        
        getsUnknownNodeExceptionDomEditorRuleOpts(nodeIDStr);
        
        long maxStorageSpace = testAms2BridgeWithMockServices.getMaxStorageSpace(userID, nodeIDStr);
        assertEquals(AmsBridge.ERROR_MB.longValue(), maxStorageSpace);
    }
    
    @Test
    public void getMaxStorageSpaceDataSourceException() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        
        getsDataSourceExceptionDomEditorRuleOpts(userID, nodeIDStr);
        
        long maxStorageSpace = testAms2BridgeWithMockServices.getMaxStorageSpace(userID, nodeIDStr);
        assertEquals(AmsBridge.ERROR_MB.longValue(), maxStorageSpace);
    }
    
    @Test
    public void getMaxStorageSpaceNullDomainRules() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        
        getsNullDomEditorRuleOpts(userID, nodeIDStr);
        
        long maxStorageSpace = testAms2BridgeWithMockServices.getMaxStorageSpace(userID, nodeIDStr);
        assertEquals(AmsBridge.DEFAULT_MB.longValue(), maxStorageSpace);
    }
    
    @Test
    public void getMaxStorageSpaceNullInteger() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        final NodePcplRule nodePcplRule = new NodePcplRule();
        nodePcplRule.setMaxStorageMB(null);
        
        getsDomEditorRuleOpts(userID, nodeIDStr, nodePcplRule);
        
        long maxStorageSpace = testAms2BridgeWithMockServices.getMaxStorageSpace(userID, nodeIDStr);
        assertEquals(AmsBridge.DEFAULT_MB.longValue(), maxStorageSpace);
    }
    
    @Test
    public void getMaxStorageSpaceExistingDomainRules() {
        
        String userID = "someUser";
        String nodeIDStr = "MPI10#";
        int expectedMaxStorageSpaceInMB = 10;
        long expectedMaxStorageSpaceInBytes = 10 * 1024 * 1024;
        final NodePcplRule nodePcplRule = new NodePcplRule();
        nodePcplRule.setMaxStorageMB(expectedMaxStorageSpaceInMB);
        
        getsDomEditorRuleOpts(userID, nodeIDStr, nodePcplRule);
        
        long usedStorageSpace = testAms2BridgeWithMockServices.getMaxStorageSpace(userID, nodeIDStr);
        assertEquals(expectedMaxStorageSpaceInBytes, usedStorageSpace);
    }

    
    private void getsUnknownNodeExceptionDomEditorRuleOpts(final String nodeIDStr) {
        
        context.checking(new Expectations() {{
            oneOf (mockFabricSrv).newNodeID(nodeIDStr); will(throwException(new UnknownNodeException("Some problem with the node ID")));
        }});
    }
    
    private void getsDataSourceExceptionDomEditorRuleOpts(final String userID, final String nodeIDStr) {
        
        context.checking(new Expectations() {{
            oneOf (mockFabricSrv).newNodeID(nodeIDStr); will(returnValue(null));
            oneOf (mockPrincipalSrv).getUser(userID); will(throwException(new DataSourceException("Some problem getting user")));
        }});
    }
    
    private void getsNullDomEditorRuleOpts(final String userID, final String nodeIDStr) {
        
        context.checking(new Expectations() {{
            oneOf (mockFabricSrv).newNodeID(nodeIDStr); will(returnValue(mockNodeID));
            oneOf (mockPrincipalSrv).getUser(userID); will(returnValue(mockUser));
            oneOf (mockAuthorizationSrv).getEffectiveDomainEditorRule(mockNodeID, mockUser); will(returnValue(null));
        }});
    }
    
    private void getsDomEditorRuleOpts(final String userID, final String nodeIDStr, final NodePcplRule nodePcplRule) {
        
        context.checking(new Expectations() {{
            oneOf (mockFabricSrv).newNodeID(nodeIDStr); will(returnValue(mockNodeID));
            oneOf (mockPrincipalSrv).getUser(userID); will(returnValue(mockUser));
            oneOf (mockAuthorizationSrv).getEffectiveDomainEditorRule(mockNodeID, mockUser); will(returnValue(nodePcplRule));
        }});
    }
}
