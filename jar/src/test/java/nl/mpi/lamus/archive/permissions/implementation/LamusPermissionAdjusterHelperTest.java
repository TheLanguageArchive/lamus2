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
import java.net.URLDecoder;
import java.util.TreeMap;
import mpi.jnatools.jnaChmodChgrp;
import nl.mpi.lamus.archive.permissions.PermissionAdjusterHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({jnaChmodChgrp.class})
public class LamusPermissionAdjusterHelperTest {
    
    private PermissionAdjusterHelper permissionAdjusterHelper;
    
    public LamusPermissionAdjusterHelperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        permissionAdjusterHelper = new LamusPermissionAdjusterHelper();
        
        ReflectionTestUtils.setField(permissionAdjusterHelper, "permissionConfigFile",
                new File(URLDecoder.decode(getClass().getClassLoader().getResource("mockPermissionConfigFile").getFile())));
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void loadConfiguredPermissions() throws FileNotFoundException, IOException {
        
        TreeMap<String, ApaPermission> expectedPermissions = getTestPermissions();
        
        permissionAdjusterHelper.loadConfiguredPermissions();
        
        TreeMap<String, ApaPermission> loadedConfiguredPermissions = (TreeMap<String, ApaPermission>) ReflectionTestUtils.getField(permissionAdjusterHelper, "configuredPermissions");
        
        //check if permissions are correct
        assertEquals("Loaded permissions different from expected", expectedPermissions, loadedConfiguredPermissions);
    }

    @Test
    public void getCurrentPermissionsForPath() {
        
        final int gid = 1;
        final int mode = Integer.parseInt("644", 8);
        final String path = "/some/path/file.txt";
        
        ApaPermission expectedPermissions = new ApaPermission(gid, mode);
        
        stub(method(jnaChmodChgrp.class, "getGid", String.class)).toReturn(gid);
        stub(method(jnaChmodChgrp.class, "getMode", String.class)).toReturn(mode);
        
        ApaPermission result = permissionAdjusterHelper.getCurrentPermissionsForPath(path);
        
        assertEquals("Permissions different from expected", expectedPermissions, result);
    }

    @Test
    public void getDesiredPermissionsForPath() {
        
        TreeMap<String, ApaPermission> testPermissions = getTestPermissions();
        ReflectionTestUtils.setField(permissionAdjusterHelper, "configuredPermissions", testPermissions);
        
        ApaPermission expectedPermissions = new ApaPermission("somegroup", Integer.parseInt("640", 8));
        
        ApaPermission result = permissionAdjusterHelper.getDesiredPermissionsForPath("/archive/path/test/photo_test/file.txt");
        
        assertEquals("Retrieved permissions different from expected", expectedPermissions, result);
    }
    
    @Test
    public void getDesiredPermissionsForPath_longerPath() {
        
        TreeMap<String, ApaPermission> testPermissions = getTestPermissions();
        ReflectionTestUtils.setField(permissionAdjusterHelper, "configuredPermissions", testPermissions);
        
        ApaPermission expectedPermissions = new ApaPermission("someothergroup", Integer.parseInt("644", 8));
        
        ApaPermission result = permissionAdjusterHelper.getDesiredPermissionsForPath("/archive/path/test/lamus_test/somefolder/file.txt");
        
        assertEquals("Retrieved permissions different from expected", expectedPermissions, result);
    }
    
    
    private TreeMap<String, ApaPermission> getTestPermissions() {
        TreeMap<String, ApaPermission> testPermissions = new TreeMap<>();
        testPermissions.put("/archive/path/test/.*\\..*", new ApaPermission("somegroup", Integer.parseInt("644", 8)));
        testPermissions.put("/archive/path/test/photo_test/.*\\..*", new ApaPermission("somegroup", Integer.parseInt("640", 8)));
        testPermissions.put("/archive/path/test/photo_test/.*\\.cmdi", new ApaPermission("somegroup", Integer.parseInt("644", 8)));
        testPermissions.put("/archive/path/test/lamus_test/.*\\..*", new ApaPermission("someothergroup", Integer.parseInt("644", 8)));
        return testPermissions;
    }
}