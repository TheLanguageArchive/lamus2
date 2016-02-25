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

import java.util.Collection;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author guisil
 */
public class IndexPageTest extends AbstractLamusWicketTest {
    
    private IndexPage indexPage;
    
    @Mock private LamusWicketPagesProvider mockPagesProviderBean;
    
    @Mock private CreateWorkspacePage mockCreateWorkspacePage;
    @Mock private SelectWorkspacePage mockSelectWorkspacePage;
    @Mock private Collection<String> mockManagerUsers;
    private final String mockRegisterUrl = "https://test.mpi.nl/registerUrl";
    private final String mockManualUrl = "http://test.mpi.nl/lamus/manual";
    
    
    @Override
    protected void setUpTest() {
        
        MockitoAnnotations.initMocks(this);
        
        when(mockPagesProviderBean.getCreateWorkspacePage()).thenReturn(mockCreateWorkspacePage);
        when(mockPagesProviderBean.getSelectWorkspacePage()).thenReturn(mockSelectWorkspacePage);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_PAGES_PROVIDER, mockPagesProviderBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_REGISTER_URL, mockRegisterUrl);
        addMock(AbstractLamusWicketTest.BEAN_NAME_MANUAL_URL, mockManualUrl);
        addMock(AbstractLamusWicketTest.BEAN_NAME_MANAGER_USERS, mockManagerUsers);
        
        indexPage = new IndexPage();
        getTester().startPage(indexPage);
    }

    @Override
    protected void tearDownTest() {
        
    }
    
    //TODO Tests are still very incomplete

    @Test
    @DirtiesContext
    public void pageRendersSuccessfully() {
        
        getTester().assertRenderedPage(IndexPage.class);
    }
}