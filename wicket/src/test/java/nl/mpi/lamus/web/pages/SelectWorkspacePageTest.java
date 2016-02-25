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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.model.WorkspaceModelProvider;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.tree.implementation.WorkspaceTreeModelProviderFactory;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author guisil
 */
public class SelectWorkspacePageTest extends AbstractLamusWicketTest {
    
    private final int wsId1 = 1;
    private final int wsId2 = 2;
    
    private final Workspace mockWs1 = new MockWorkspace() {{
        setWorkspaceID(wsId1);
    }};
    private final Workspace mockWs2 = new MockWorkspace() {{
        setWorkspaceID(wsId2);
    }};
    private final Collection<Workspace> mockWsList = new ArrayList<Workspace>() {{
        add(mockWs1);
        add(mockWs2);
    }};

    private SelectWorkspacePage selectWsPage;
    
    
    @Mock private WorkspaceTreeService mockWorkspaceServiceBean;
    @Mock private WorkspaceTreeModelProviderFactory mockWorkspaceTreeModelProviderFactoryBean;
    @Mock private LamusWicketPagesProvider mockLamusWicketPagesProviderBean;
    @Mock private WorkspacePage mockWorkspacePage;
    @Mock private Collection<String> mockManagerUsers;
    @Mock private WorkspaceModelProvider mockWorkspaceModelProvider;
    
    private final IModel<Workspace> mockWsModel = new Model<Workspace>(mockWs1) {

        @Override
        public Workspace getObject() {
            return mockWs1;
        }
    };

    private final String mockRegisterUrl = "https://test.mpi.nl/registerUrl";
    private final String mockManualUrl = "http://test.mpi.nl/lamus/manusl";
    
    
    @Override
    protected void setUpTest() throws WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
        MockitoAnnotations.initMocks(this);
        
        when(mockWorkspaceServiceBean.getWorkspace(wsId1)).thenReturn(mockWs1);
        
        when(mockWorkspaceServiceBean.listUserWorkspaces(AbstractLamusWicketTest.MOCK_USER_ID)).thenReturn(mockWsList);
        when(mockWorkspaceServiceBean.openWorkspace(AbstractLamusWicketTest.MOCK_USER_ID, 1)).thenReturn(mockWs1);
        when(mockLamusWicketPagesProviderBean.getWorkspacePage(mockWs1)).thenReturn(mockWorkspacePage);
        
        when(mockWorkspaceModelProvider.getWorkspaceModel(wsId1)).thenReturn(mockWsModel);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_SERVICE, mockWorkspaceServiceBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_TREE_MODEL_PROVIDER_FACTORY, mockWorkspaceTreeModelProviderFactoryBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_PAGES_PROVIDER, mockLamusWicketPagesProviderBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_REGISTER_URL, mockRegisterUrl);
        addMock(AbstractLamusWicketTest.BEAN_NAME_MANUAL_URL, mockManualUrl);
        addMock(AbstractLamusWicketTest.BEAN_NAME_MANAGER_USERS, mockManagerUsers);
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_MODEL_PROVIDER, mockWorkspaceModelProvider);
        
        selectWsPage = new SelectWorkspacePage();
        getTester().startPage(selectWsPage);
    }

    @Override
    protected void tearDownTest() {
        
    }

    //TODO Tests are still very incomplete
    
    @Test
    @DirtiesContext
    public void pageRendered() {
        
        getTester().assertRenderedPage(SelectWorkspacePage.class);
    }
    
    @Test
    @DirtiesContext
    public void componentsRendered() {
        
        getTester().assertComponent("formContainer", WebMarkupContainer.class);
        getTester().assertEnabled("formContainer");
        
        getTester().assertComponent("formContainer:workspaceForm", Form.class);
        getTester().assertEnabled("formContainer:workspaceForm");
        
        getTester().assertComponent("formContainer:workspaceForm:workspaceSelection", ListChoice.class);
        getTester().assertEnabled("formContainer:workspaceForm:workspaceSelection");
        
        getTester().assertComponent("formContainer:workspaceForm:openWorkspace", Button.class);
        getTester().assertEnabled("formContainer:workspaceForm:openWorkspace");
    }
    
    @Test
    @DirtiesContext
    public void listOfWorkspaces() {
        
        verify(mockWorkspaceServiceBean).listUserWorkspaces(AbstractLamusWicketTest.MOCK_USER_ID);
        
        ListChoice<Workspace> listComponent =
                (ListChoice<Workspace>) getTester().getComponentFromLastRenderedPage("formContainer:workspaceForm:workspaceSelection");
        
        assertEquals("List of workspaces different from expected", mockWsList, listComponent.getChoices());
    }
    
    @Test
    @DirtiesContext
    public void formSubmitted() throws WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
        FormTester formTester = getTester().newFormTester("formContainer:workspaceForm", false);
        
        
        formTester.select("workspaceSelection", 0);
        formTester.submit("openWorkspace");
        
        verify(mockWorkspaceServiceBean).openWorkspace(AbstractLamusWicketTest.MOCK_USER_ID, mockWs1.getWorkspaceID());
        verify(mockLamusWicketPagesProviderBean).getWorkspacePage(mockWs1);
        
        verify(mockWorkspaceModelProvider).getWorkspaceModel(wsId1);
        
        
        getTester().assertRenderedPage(WorkspacePage.class);
    }
}