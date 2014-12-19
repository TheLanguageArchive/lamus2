/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web;

import nl.mpi.lamus.web.session.LamusSessionFactory;
import nl.mpi.lamus.web.session.mock.MockLamusSessionFactory;
import org.apache.wicket.Session;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;

/**
 * Abstract test class which prepares the wicket mock environment
 * to be used by the implementing test classes.
 * @author guisil
 */
public abstract class AbstractLamusWicketTest {
    
    protected static final String BEAN_NAME_WORKSPACE_SERVICE = "workspaceService";
    protected static final String BEAN_NAME_WORKSPACE_TREE_SERVICE = "workspaceTreeService";
    protected static final String BEAN_NAME_SESSION_FACTORY = "sessionFactory";
    protected static final String BEAN_NAME_WORKSPACE_TREE_MODEL_PROVIDER_FACTORY = "workspaceTreeProviderFactory";
    protected static final String BEAN_NAME_UNLINKED_NODES_MODEL_PROVIDER_FACTORY = "unlinkedNodesProviderFactory";
    protected static final String BEAN_NAME_CREATE_WORKSPACE_TREE_PROVIDER = "createWorkspaceTreeProvider";
    protected static final String BEAN_NAME_TREE_NODE_ACTIONS_PROVIDER = "nodeActionsProvider";
    protected static final String BEAN_NAME_PAGES_PROVIDER = "pagesProvider";
    protected static final String BEAN_NAME_REGISTER_URL = "registerUrl";
    protected static final String BEAN_NAME_MANUAL_URL = "manualUrl";
    protected static final String BEAN_NAME_MANAGER_USERS = "managerUsers";
    
    protected static final String MOCK_USER_ID = "testUser";
    protected static final String MOCK_LAMUS_PAGE_TITLE = "The Language Archive - LAMUS 2";
    
    private ApplicationContextMock applicationContextMock;
    
    private WicketTester tester = null;
    
    private MockLamusSessionFactory lamusSessionFactory;
    
    
    @Before
    public void setUp() throws Exception {
        
        applicationContextMock = new ApplicationContextMock();
        
        tester = new WicketTester(new MockApplication() {

            @Override
            public Session newSession(Request request, Response response) {
                return AbstractLamusWicketTest.this.getSessionFactory().createSession(this, request, response);
            }
            
        });
        tester.getApplication().getComponentInstantiationListeners().add(new SpringComponentInjector(tester.getApplication(), applicationContextMock));
        setUpTest();
    }
    
    @After
    public void tearDown() throws Exception {
        
        tester.destroy();
        tearDownTest();
    }
    
    protected abstract void setUpTest() throws Exception;
    
    protected abstract void tearDownTest() throws Exception;
    
    protected void addMock(String beanName, Object mock) {
        applicationContextMock.putBean(beanName, mock);
    }
    
    protected ApplicationContextMock getApplicationContextMock() {
        return applicationContextMock;
    }
    
    protected WicketTester getTester() {
        return tester;
    }
    
    private LamusSessionFactory getSessionFactory() {
        if(lamusSessionFactory == null) {
            lamusSessionFactory = new MockLamusSessionFactory();
            lamusSessionFactory.setAuthenticated(true);
            lamusSessionFactory.setUserId(MOCK_USER_ID);
        }
        return lamusSessionFactory;
    }
}
