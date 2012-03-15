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

import nl.mpi.lat.ams.service.LicenseService;
import nl.mpi.lat.ams.service.RuleService;
import nl.mpi.lat.auth.authentication.AuthenticationService;
import nl.mpi.lat.auth.authorization.AdvAuthorizationService;
import nl.mpi.lat.auth.principal.LatUser;
import nl.mpi.lat.auth.principal.PrincipalService;
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
        
        testAms2BridgeWithMockServices.setFabricSrv(null);
        
        context.checking(new Expectations() {{
            never (mockFabricSrv).close();
        }});
        
        testAms2BridgeWithMockServices.close();
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

}
