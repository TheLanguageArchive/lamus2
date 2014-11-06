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
package nl.mpi.lamus.archive.permissions.implementation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.archive.permissions.PermissionAdjuster;
import nl.mpi.lamus.archive.permissions.PermissionAdjusterHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;

/**
 *
 * @author guisil
 */
public class LamusPermissionAdjusterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock PermissionAdjusterHelper mockPermissionAdjusterHelper;
    
    @Mock WorkspaceNode mockNode1;
    @Mock WorkspaceNode mockNode2;
    
    @Mock ApaPermission mockCurrentPermissions1;
    @Mock ApaPermission mockCurrentPermissions2;
    @Mock ApaPermission mockDesiredPermissions1;
    @Mock ApaPermission mockDesiredPermissions2;
    
    
    private PermissionAdjuster permissionAdjuster;
    
    
    public LamusPermissionAdjusterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        permissionAdjuster = new LamusPermissionAdjuster(mockWorkspaceDao, mockPermissionAdjusterHelper);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void adjustPermissions() throws URISyntaxException, FileNotFoundException, IOException {
        
        final int workspaceID = 11;
        final String localPath_Node1 = "/archive/path/descendant1.txt";
        final File localFile_Node1 = new File(localPath_Node1);
        final URL localURL_Node1 = localFile_Node1.toURI().toURL();
        final String localPath_Node2 = "/archive/path/descendant2.txt";
        final File localFile_Node2 = new File(localPath_Node2);
        final URL localURL_Node2 = localFile_Node2.toURI().toURL();
        
        final Collection<WorkspaceNode> allNodes = new ArrayList<>();
        allNodes.add(mockNode1);
        allNodes.add(mockNode2);
        
        context.checking(new Expectations() {{
            
            oneOf(mockPermissionAdjusterHelper).loadConfiguredPermissions();
            
            oneOf(mockWorkspaceDao).getNodesForWorkspace(workspaceID); will(returnValue(allNodes));
            
            //loop - first iteration
            oneOf(mockNode1).isExternal(); will(returnValue(Boolean.FALSE));
            oneOf(mockNode1).getArchiveURL(); will(returnValue(localURL_Node1));
            oneOf(mockPermissionAdjusterHelper).getCurrentPermissionsForPath(localPath_Node1); will(returnValue(mockCurrentPermissions1));
            oneOf(mockPermissionAdjusterHelper).getDesiredPermissionsForPath(localPath_Node1); will(returnValue(mockDesiredPermissions1));
            oneOf(mockPermissionAdjusterHelper).checkAndRepairFile(localPath_Node1, mockCurrentPermissions1, mockDesiredPermissions1); will(returnValue(Boolean.TRUE));
            
            //loop - second iteration
            oneOf(mockNode2).isExternal(); will(returnValue(Boolean.FALSE));
            oneOf(mockNode2).getArchiveURL(); will(returnValue(localURL_Node2));
            oneOf(mockPermissionAdjusterHelper).getCurrentPermissionsForPath(localPath_Node2); will(returnValue(mockCurrentPermissions2));
            oneOf(mockPermissionAdjusterHelper).getDesiredPermissionsForPath(localPath_Node2); will(returnValue(mockDesiredPermissions2));
            oneOf(mockPermissionAdjusterHelper).checkAndRepairFile(localPath_Node2, mockCurrentPermissions2, mockDesiredPermissions2);
        }});
        
        permissionAdjuster.adjustPermissions(workspaceID);
    }
    
    @Test
    public void adjustPermissions_OneExternal() throws URISyntaxException, FileNotFoundException, IOException {
        
        final int workspaceID = 11;
        final String localPath_Node1 = "/archive/path/descendant1.txt";
        final File localFile_Node1 = new File(localPath_Node1);
        final URL localURL_Node1 = localFile_Node1.toURI().toURL();
        final String localPath_Node2 = "/archive/path/descendant2.txt";
        final File localFile_Node2 = new File(localPath_Node2);
        final URL localURL_Node2 = localFile_Node2.toURI().toURL();
        
        final Collection<WorkspaceNode> allNodes = new ArrayList<>();
        allNodes.add(mockNode1);
        allNodes.add(mockNode2);
        
        context.checking(new Expectations() {{
            
            oneOf(mockPermissionAdjusterHelper).loadConfiguredPermissions();
            
            oneOf(mockWorkspaceDao).getNodesForWorkspace(workspaceID); will(returnValue(allNodes));
            
            //loop - first iteration
            oneOf(mockNode1).isExternal(); will(returnValue(Boolean.TRUE));
            
            //loop - second iteration
            oneOf(mockNode2).isExternal(); will(returnValue(Boolean.FALSE));
            oneOf(mockNode2).getArchiveURL(); will(returnValue(localURL_Node2));
            oneOf(mockPermissionAdjusterHelper).getCurrentPermissionsForPath(localPath_Node2); will(returnValue(mockCurrentPermissions2));
            oneOf(mockPermissionAdjusterHelper).getDesiredPermissionsForPath(localPath_Node2); will(returnValue(mockDesiredPermissions2));
            oneOf(mockPermissionAdjusterHelper).checkAndRepairFile(localPath_Node2, mockCurrentPermissions2, mockDesiredPermissions2);
        }});
        
        permissionAdjuster.adjustPermissions(workspaceID);
    }
    
    @Test
    public void adjustPermissions_FailFileLoad() throws URISyntaxException, FileNotFoundException, IOException {
        
        final int workspaceID = 11;
        final IOException exception = new IOException("failed loading the file");
        
        context.checking(new Expectations() {{
            
            oneOf(mockPermissionAdjusterHelper).loadConfiguredPermissions(); will(throwException(exception));
        }});
        
        permissionAdjuster.adjustPermissions(workspaceID);
    }
}