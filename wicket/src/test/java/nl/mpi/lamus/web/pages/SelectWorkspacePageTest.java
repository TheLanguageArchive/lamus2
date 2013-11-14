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

import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.tree.implementation.WorkspaceTreeModelProviderFactory;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
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
    
    private final Workspace mockWs1 = new MockWorkspace() {{
        setWorkspaceID(1);
    }};
    private final Workspace mockWs2 = new MockWorkspace() {{
        setWorkspaceID(2);
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

    
    @Override
    protected void setUpTest() {
        
        MockitoAnnotations.initMocks(this);
        
        when(mockWorkspaceServiceBean.listUserWorkspaces(AbstractLamusWicketTest.MOCK_USER_ID)).thenReturn(mockWsList);
        when(mockWorkspaceServiceBean.openWorkspace(AbstractLamusWicketTest.MOCK_USER_ID, 1)).thenReturn(mockWs1);
        when(mockLamusWicketPagesProviderBean.getWorkspacePage(mockWs1)).thenReturn(mockWorkspacePage);
        
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_SERVICE, mockWorkspaceServiceBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_TREE_MODEL_PROVIDER_FACTORY, mockWorkspaceTreeModelProviderFactoryBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_PAGES_PROVIDER, mockLamusWicketPagesProviderBean);
        
        selectWsPage = new SelectWorkspacePage();
        getTester().startPage(selectWsPage);
    }

    @Override
    protected void tearDownTest() {
        
    }

    
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
        
        getTester().assertComponent("formContainer:workspaceForm:workspace", ListChoice.class);
        getTester().assertEnabled("formContainer:workspaceForm:workspace");
        
        getTester().assertComponent("formContainer:workspaceForm:OpenWorkspace", Button.class);
        getTester().assertEnabled("formContainer:workspaceForm:OpenWorkspace");
    }
    
    @Test
    @DirtiesContext
    public void listOfWorkspaces() {
        
        verify(mockWorkspaceServiceBean).listUserWorkspaces(AbstractLamusWicketTest.MOCK_USER_ID);
        
        ListChoice<Workspace> listComponent =
                (ListChoice<Workspace>) getTester().getComponentFromLastRenderedPage("formContainer:workspaceForm:workspace");
        
        assertEquals("List of workspaces different from expected", mockWsList, listComponent.getChoices());
    }
    
    @Test
    @DirtiesContext
    public void formSubmitted() {
        
        FormTester formTester = getTester().newFormTester("formContainer:workspaceForm", false);
        
        formTester.select("workspace", 0);
        formTester.submit("OpenWorkspace");
        
        verify(mockWorkspaceServiceBean).openWorkspace(AbstractLamusWicketTest.MOCK_USER_ID, mockWs1.getWorkspaceID());
        verify(mockLamusWicketPagesProviderBean).getWorkspacePage(mockWs1);
        
        getTester().assertRenderedPage(WorkspacePage.class);
    }
}