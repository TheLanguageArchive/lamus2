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
package nl.mpi.lamus.web.session.production;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import nl.mpi.lamus.web.session.LamusSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
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
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

/**
 *
 * @author guisil
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({RequestCycle.class})
public class ProductionLamusSessionTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private LamusSession session;
    
    @Mock Request mockRequest;
    @Mock HttpServletRequest mockHttpServletRequest;
    @Mock RequestCycle mockRequestCycle;
    
    private final Locale locale = Locale.ENGLISH;
    
    public ProductionLamusSessionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        context.checking(new Expectations() {{
            oneOf(mockRequest).getLocale(); will(returnValue(locale));
        }});
        
        session = new ProductionLamusSession(mockRequest);
    }
    
    @After
    public void tearDown() {
    }

    
    
    @Test
    public void getValidUserId() {
        
        final String expectedUserId = "someUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockRequestCycle).getRequest(); will(returnValue(mockRequest));
            oneOf(mockRequest).getContainerRequest(); will(returnValue(mockHttpServletRequest));
            oneOf(mockHttpServletRequest).getRemoteUser(); will(returnValue(expectedUserId));
        }});
   
        stub(method(RequestCycle.class, "get")).toReturn(mockRequestCycle);
        
        String retrievedUserId = session.getUserId();
        
        assertEquals("Retrieved userId different from expected", expectedUserId, retrievedUserId);
    }
    
    @Test
    public void getNullUserId() {
        
        final String expectedUserId = "anonymous";
        
        context.checking(new Expectations() {{
            
            oneOf(mockRequestCycle).getRequest(); will(returnValue(mockRequest));
            oneOf(mockRequest).getContainerRequest(); will(returnValue(mockHttpServletRequest));
            oneOf(mockHttpServletRequest).getRemoteUser(); will(returnValue(null));
        }});

        stub(method(RequestCycle.class, "get")).toReturn(mockRequestCycle);
        
        String retrievedUserId = session.getUserId();
        
        assertEquals("Retrieved userId different from expected", expectedUserId, retrievedUserId);
    }
    
    @Test
    public void getEmptyUserId() {
        
        final String expectedUserId = "anonymous";
        
        context.checking(new Expectations() {{
            
            oneOf(mockRequestCycle).getRequest(); will(returnValue(mockRequest));
            oneOf(mockRequest).getContainerRequest(); will(returnValue(mockHttpServletRequest));
            oneOf(mockHttpServletRequest).getRemoteUser(); will(returnValue(""));
        }});

        stub(method(RequestCycle.class, "get")).toReturn(mockRequestCycle);
        
        String retrievedUserId = session.getUserId();
        
        assertEquals("Retrieved userId different from expected", expectedUserId, retrievedUserId);
    }

    @Test
    public void userIsAuthenticated() {
        
        final String userId = "someUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockRequestCycle).getRequest(); will(returnValue(mockRequest));
            oneOf(mockRequest).getContainerRequest(); will(returnValue(mockHttpServletRequest));
            oneOf(mockHttpServletRequest).getRemoteUser(); will(returnValue(userId));
        }});

        stub(method(RequestCycle.class, "get")).toReturn(mockRequestCycle);
        
        boolean result = session.isAuthenticated();
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void userIsNotAuthenticated() {
        
        final String userId = "";
        
        context.checking(new Expectations() {{
            
            oneOf(mockRequestCycle).getRequest(); will(returnValue(mockRequest));
            oneOf(mockRequest).getContainerRequest(); will(returnValue(mockHttpServletRequest));
            oneOf(mockHttpServletRequest).getRemoteUser(); will(returnValue(userId));
        }});

        stub(method(RequestCycle.class, "get")).toReturn(mockRequestCycle);
        
        boolean result = session.isAuthenticated();
        
        assertFalse("Result should be false", result);
    }
}