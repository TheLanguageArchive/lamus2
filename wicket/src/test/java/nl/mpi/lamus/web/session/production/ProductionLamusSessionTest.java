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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author guisil
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({RequestCycle.class})
public class ProductionLamusSessionTest {
    
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
        
        MockitoAnnotations.initMocks(this);
        
        when(mockRequest.getLocale()).thenReturn(locale);
        
        session = new ProductionLamusSession(mockRequest);
    }
    
    @After
    public void tearDown() {
    }

    
    
    @Test
    public void getValidUserId() {
        
        final String expectedUserId = "someUser";
        
        PowerMockito.mockStatic(RequestCycle.class);
        when(RequestCycle.get()).thenReturn(mockRequestCycle);
        
        when(mockRequestCycle.getRequest()).thenReturn(mockRequest);
        when(mockRequest.getContainerRequest()).thenReturn(mockHttpServletRequest);
        when(mockHttpServletRequest.getRemoteUser()).thenReturn(expectedUserId);
        
        String retrievedUserId = session.getUserId();
        
        verify(mockRequestCycle).getRequest();
        verify(mockRequest).getContainerRequest();
        verify(mockHttpServletRequest).getRemoteUser();
        
        assertEquals("Retrieved userId different from expected", expectedUserId, retrievedUserId);
    }
    
    @Test
    public void getNullUserId() {
        
        final String expectedUserId = "anonymous";
        
        PowerMockito.mockStatic(RequestCycle.class);
        when(RequestCycle.get()).thenReturn(mockRequestCycle);
        
        when(mockRequestCycle.getRequest()).thenReturn(mockRequest);
        when(mockRequest.getContainerRequest()).thenReturn(mockHttpServletRequest);
        when(mockHttpServletRequest.getRemoteUser()).thenReturn(null);
        
        String retrievedUserId = session.getUserId();

        verify(mockRequestCycle).getRequest();
        verify(mockRequest).getContainerRequest();
        verify(mockHttpServletRequest).getRemoteUser();
        
        assertEquals("Retrieved userId different from expected", expectedUserId, retrievedUserId);
    }
    
    @Test
    public void getEmptyUserId() {
        
        final String expectedUserId = "anonymous";
        
        PowerMockito.mockStatic(RequestCycle.class);
        when(RequestCycle.get()).thenReturn(mockRequestCycle);
        
        when(mockRequestCycle.getRequest()).thenReturn(mockRequest);
        when(mockRequest.getContainerRequest()).thenReturn(mockHttpServletRequest);
        when(mockHttpServletRequest.getRemoteUser()).thenReturn("");
        
        String retrievedUserId = session.getUserId();
        
        verify(mockRequestCycle).getRequest();
        verify(mockRequest).getContainerRequest();
        verify(mockHttpServletRequest).getRemoteUser();
        
        assertEquals("Retrieved userId different from expected", expectedUserId, retrievedUserId);
    }

    @Test
    public void userIsAuthenticated() {
        
        final String userId = "someUser";
        
        PowerMockito.mockStatic(RequestCycle.class);
        when(RequestCycle.get()).thenReturn(mockRequestCycle);
        
        when(mockRequestCycle.getRequest()).thenReturn(mockRequest);
        when(mockRequest.getContainerRequest()).thenReturn(mockHttpServletRequest);
        when(mockHttpServletRequest.getRemoteUser()).thenReturn(userId);
        
        boolean result = session.isAuthenticated();
        
        verify(mockRequestCycle).getRequest();
        verify(mockRequest).getContainerRequest();
        verify(mockHttpServletRequest).getRemoteUser();
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void userIsNotAuthenticated() {
        
        final String userId = "";
        
        PowerMockito.mockStatic(RequestCycle.class);
        when(RequestCycle.get()).thenReturn(mockRequestCycle);
        
        when(mockRequestCycle.getRequest()).thenReturn(mockRequest);
        when(mockRequest.getContainerRequest()).thenReturn(mockHttpServletRequest);
        when(mockHttpServletRequest.getRemoteUser()).thenReturn(userId);
        
        boolean result = session.isAuthenticated();
        
        verify(mockRequestCycle).getRequest();
        verify(mockRequest).getContainerRequest();
        verify(mockHttpServletRequest).getRemoteUser();
        
        assertFalse("Result should be false", result);
    }
}