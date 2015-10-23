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
package nl.mpi.lamus.web.components;

import java.io.File;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.web.AbstractLamusWicketTest;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.model.mock.MockWorkspace;
import nl.mpi.lamus.web.model.mock.MockWorkspaceTreeNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
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
public class UploadPanelTest extends AbstractLamusWicketTest {
    
    private UploadPanel uploadPanel;
    
    @Mock private WorkspaceTreeService mockWorkspaceServiceBean;
    @Mock private FeedbackPanel mockFeedbackPanel;
    
    
    private int mockWorkspaceID = 1;
    private int mockWorkspaceTopNodeID = 10;
    private final MockWorkspace mockWorkspace = new MockWorkspace() {{
        setUserID(AbstractLamusWicketTest.MOCK_USER_ID);
        setWorkspaceID(mockWorkspaceID);
        setStatus(WorkspaceStatus.INITIALISED);
        setTopNodeID(mockWorkspaceTopNodeID);
    }};
    private final MockWorkspaceTreeNode mockWorkspaceTopNode = new MockWorkspaceTreeNode() {{
        setWorkspaceID(mockWorkspaceID);
        setWorkspaceNodeID(mockWorkspaceTopNodeID);
        setName("topNode");
        setType(WorkspaceNodeType.METADATA);
    }};
    
    TemporaryFolder testFolder = new TemporaryFolder();
    private File mockUploadDirectory;
    
    
    @Override
    protected void setUpTest() throws Exception {
        
        testFolder.create();
        mockUploadDirectory = testFolder.newFolder("workspace_" + mockWorkspaceID, "upload");
        
        MockitoAnnotations.initMocks(this);
        
        when(mockWorkspaceServiceBean.getWorkspace(mockWorkspaceID)).thenReturn(mockWorkspace);
        when(mockWorkspaceServiceBean.getWorkspaceUploadDirectory(mockWorkspaceID)).thenReturn(mockUploadDirectory);
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_WORKSPACE_SERVICE, mockWorkspaceServiceBean);
        
        uploadPanel = new UploadPanel("uploadPanel", new WorkspaceModel(mockWorkspaceID), mockFeedbackPanel);
        getTester().startComponentInPage(uploadPanel);
    }

    @Override
    protected void tearDownTest() throws Exception {
        
    }
    
    
    @Test
    @DirtiesContext
    public void componentsRendered() {
        
        getTester().assertComponent("uploadPanel:progressUpload", Form.class);
        getTester().assertEnabled("uploadPanel:progressUpload");
        
        getTester().assertComponent("uploadPanel:progressUpload:fileInput", FileUploadField.class);
        getTester().assertEnabled("uploadPanel:progressUpload:fileInput");
        
        
        getTester().assertComponent("uploadPanel:progressUpload:progress", UploadProgressBar.class);
        getTester().assertEnabled("uploadPanel:progressUpload:progress");
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