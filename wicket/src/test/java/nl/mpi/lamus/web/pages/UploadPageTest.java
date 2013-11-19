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

import java.io.File;
import java.io.InputStream;
import nl.mpi.archiving.tree.LinkedTreeModelProvider;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.components.UnlinkedNodesPanel;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.model.mock.MockWorkspaceTreeNode;
import nl.mpi.lamus.web.providers.LamusWicketPagesProvider;
import nl.mpi.lamus.workspace.actions.TreeNodeActionsProvider;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.tree.implementation.WorkspaceTreeModelProviderFactory;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author guisil
 */
public class UploadPageTest extends AbstractLamusWicketTest {
    
    private UploadPage uploadPage;
    
    @Mock private WorkspaceTreeService mockWorkspaceServiceBean;
    @Mock private WorkspaceTreeModelProviderFactory mockWorkspaceTreeModelProviderFactoryBean;
    
    @Mock private TreeNodeActionsProvider mockTreeNodeActionsProviderBean;
    
    @Mock private LinkedTreeModelProvider mockTreeModelProvider;
    
    @Mock private LamusWicketPagesProvider mockPagesProvider;
    
//    @Mock private FileUploadField mockFileUploadField;
//    @Mock private FileUpload mockFileUpload;
    @Mock private InputStream mockFileInputStream;
    
    private File mockFileToUpload;
    
    private String mockFilename = "someimage.jpg";
    
    
    private int mockWorkspaceID = 1;
    private int mockWorkspaceTopNodeID = 10;
    private MockWorkspace mockWorkspace = new MockWorkspace() {{
        setUserID(AbstractLamusWicketTest.MOCK_USER_ID);
        setWorkspaceID(mockWorkspaceID);
        setStatus(WorkspaceStatus.INITIALISED);
        setTopNodeID(mockWorkspaceTopNodeID);
    }};
    private MockWorkspaceTreeNode mockWorkspaceTopNode = new MockWorkspaceTreeNode() {{
        setWorkspaceID(mockWorkspaceID);
        setWorkspaceNodeID(mockWorkspaceTopNodeID);
        setName("topNode");
        setType(WorkspaceNodeType.METADATA);
    }};
    
    TemporaryFolder testFolder = new TemporaryFolder();
    private File mockUploadDirectory;
    
//    List<FileUpload> mockFileUploads = new ArrayList<FileUpload>();
    
    @Override
    protected void setUpTest() throws Exception {
        
        testFolder.create();
        mockUploadDirectory = testFolder.newFolder("workspace_" + mockWorkspaceID, "upload");
//        mockFileToUpload = testFolder.newFile(mockFilename);
//        mockFileToUpload.createNewFile();
        
        
        MockitoAnnotations.initMocks(this);
        
        when(mockWorkspaceServiceBean.getWorkspace(mockWorkspaceID)).thenReturn(mockWorkspace);
        when(mockWorkspaceServiceBean.getTreeNode(mockWorkspaceTopNodeID, null)).thenReturn(mockWorkspaceTopNode);
        when(mockWorkspaceServiceBean.getWorkspaceUploadDirectory(mockWorkspaceID)).thenReturn(mockUploadDirectory);
        when(mockWorkspaceTreeModelProviderFactoryBean.createTreeModelProvider(mockWorkspaceTopNode)).thenReturn(mockTreeModelProvider);
        when(mockTreeModelProvider.getRoot()).thenReturn(mockWorkspaceTopNode);
        
//        when(mockFileUpload.getClientFileName()).thenReturn(mockFilename);
//        when(mockFileUpload.getInputStream()).thenReturn(mockFileInputStream);
        
//        when(mockFileUploadField.getId()).thenReturn("fileInput");
//        when(mockFileUploadField.getFileUploads()).thenReturn(mockFileUploads);
        
        
//        when(mockFileToUpload.inputStream()).thenReturn(mockFileInputStream);
        
        
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_SERVICE, mockWorkspaceServiceBean);
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_TREE_MODEL_PROVIDER_FACTORY, mockWorkspaceTreeModelProviderFactoryBean);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_TREE_NODE_ACTIONS_PROVIDER, mockTreeNodeActionsProviderBean);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_PAGES_PROVIDER, mockPagesProvider);
        
        
//        mockFileUploads.add(mockFileUpload);
        
        
        uploadPage = new UploadPage(new WorkspaceModel(mockWorkspace));
        getTester().startPage(uploadPage);
    }

    @Override
    protected void tearDownTest() throws Exception {
        
    }
    
    
    @Test
    @DirtiesContext
    public void componentsRendered() {
        
        getTester().assertComponent("progressUpload", Form.class);
        getTester().assertEnabled("progressUpload");
        
        getTester().assertComponent("progressUpload:fileInput", FileUploadField.class);
        getTester().assertEnabled("progressUpload:fileInput");
        
        
        getTester().assertComponent("progressUpload:progress", UploadProgressBar.class);
        getTester().assertEnabled("progressUpload:progress");
        
        getTester().assertComponent("uploadFeedback", FeedbackPanel.class);
        getTester().assertEnabled("uploadFeedback");
        
        getTester().assertComponent("unlinkedNodesPanel", UnlinkedNodesPanel.class);
        getTester().assertEnabled("unlinkedNodesPanel");
    }
    
    
    //TODO couldn't test this; the "getFileUploads" method of the upload field always seems to return null and couldn't find a way to mock it...
    
//    @Test
//    @DirtiesContext
//    public void submitUpload() throws IOException, TypeCheckerException {
//        
//        FormTester formTester = getTester().newFormTester("progressUpload", false);
//        org.apache.wicket.util.file.File mockFileToSet = new org.apache.wicket.util.file.File(mockFileToUpload);
//        formTester.setFile("fileInput", mockFileToSet, "image/jpeg");
////        FileUploadField uploadField = (FileUploadField) getTester().getComponentFromLastRenderedPage("progressUpload:fileInput");
////        uploadField.setModel(new ListModel<FileUpload>(mockFileUploads));
////        Form uploadForm = (Form) getTester().getComponentFromLastRenderedPage("progressUpload");
////        uploadForm.addOrReplace(mockFileUploadField);
//        
//        formTester.submit();
//        
//        verify(mockWorkspaceServiceBean).uploadFileIntoWorkspace(AbstractLamusWicketTest.MOCK_USER_ID, mockWorkspaceID, mockFileInputStream, mockFilename);
//    }
    
    //TODO test exceptions
}