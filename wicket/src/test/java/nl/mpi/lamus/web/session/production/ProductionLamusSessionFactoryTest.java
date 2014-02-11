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
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.web.session.LamusSessionFactory;
import org.apache.wicket.Application;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
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

/**
 *
 * @author guisil
 */
public class ProductionLamusSessionFactoryTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private LamusSessionFactory lamusSessionFactory;
    
    @Mock Application mockApplication;
    @Mock Request mockRequest;
    @Mock Response mockResponse;
    
    private final Locale locale = Locale.ENGLISH;
    
    
    public ProductionLamusSessionFactoryTest() {
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
        
        lamusSessionFactory = new ProductionLamusSessionFactory();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of createSession method, of class ProductionLamusSessionFactory.
     */
    @Test
    public void createSession() {
        
        LamusSession retrievedSession = lamusSessionFactory.createSession(mockApplication, mockRequest, mockResponse);
        
        assertNotNull("Retrieved session should not be null", retrievedSession);
    }
}