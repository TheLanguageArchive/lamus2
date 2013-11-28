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
package nl.mpi.lamus.web.pages;

import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.LamusWicketApplication;
import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.web.spring.LamusWicketApplicationTestBeans;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author guisil
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(
//        classes = LamusWicketApplicationTestBeans.class,
//        loader = AnnotationConfigContextLoader.class)
//@ActiveProfiles("testing")
public class IndexPageTest extends AbstractLamusWicketTest {
    
//    private WicketTester tester;
//    
//    @Autowired
//    private LamusWicketApplication lamusWicketApplication;
    
    
    private IndexPage indexPage;
    
    @Mock private LamusWicketPagesProvider mockPagesProviderBean;
    
    @Mock private CreateWorkspacePage mockCreateWorkspacePage;
    @Mock private SelectWorkspacePage mockSelectWorkspacePage;
    
    
    @Override
    protected void setUpTest() {
        
        MockitoAnnotations.initMocks(this);
        
        when(mockPagesProviderBean.getCreateWorkspacePage()).thenReturn(mockCreateWorkspacePage);
        when(mockPagesProviderBean.getSelectWorkspacePage()).thenReturn(mockSelectWorkspacePage);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_PAGES_PROVIDER, mockPagesProviderBean);
        
        indexPage = new IndexPage();
        getTester().startPage(indexPage);
    }

    @Override
    protected void tearDownTest() {
        
    }
    

    @Test
    @DirtiesContext
    public void pageRendersSuccessfully() {
        
        getTester().assertRenderedPage(IndexPage.class);
    }
    
    @Test
    @DirtiesContext
    public void linksRenderSuccessfully() {
        
        getTester().assertComponent("createWorkspaceLink", Link.class);
        getTester().assertEnabled("createWorkspaceLink");
        
        getTester().assertComponent("selectWorkspaceLink", Link.class);
        getTester().assertEnabled("selectWorkspaceLink");
        
        getTester().assertComponent("requestStorageSpaceLink", Link.class);
        getTester().assertEnabled("requestStorageSpaceLink");
    }
    
    @Test
    @DirtiesContext
    public void createWorkspaceLinkClick() {
        
        getTester().clickLink("createWorkspaceLink");
        
        verify(mockPagesProviderBean).getCreateWorkspacePage();
        
        getTester().assertRenderedPage(CreateWorkspacePage.class);
    }
    
    @Test
    @DirtiesContext
    public void selectWorkspaceLinkClick() {
        
        getTester().clickLink("selectWorkspaceLink");
        
        verify(mockPagesProviderBean).getSelectWorkspacePage();
        
        getTester().assertRenderedPage(SelectWorkspacePage.class);
    }
    
    //TODO test other links


}