/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace;

import java.util.Date;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class WorkspaceImplTest {
    
    public WorkspaceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getWorkspaceID method, of class WorkspaceImpl.
     */
    @Test
    public void testGetWorkspaceID() {
        System.out.println("getWorkspaceID");
        LamusWorkspace instance = null;
        int expResult = 0;
        int result = instance.getWorkspaceID();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWorkspaceID method, of class WorkspaceImpl.
     */
    @Test
    public void testSetWorkspaceID() {
        System.out.println("setWorkspaceID");
        int workspaceID = 0;
        LamusWorkspace instance = null;
        instance.setWorkspaceID(workspaceID);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUserID method, of class WorkspaceImpl.
     */
    @Test
    public void testGetUserID() {
        System.out.println("getUserID");
        LamusWorkspace instance = null;
        String expResult = "";
        String result = instance.getUserID();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTopNodeID method, of class WorkspaceImpl.
     */
    @Test
    public void testGetTopNodeID() {
        System.out.println("getTopNodeID");
        LamusWorkspace instance = null;
        int expResult = 0;
        int result = instance.getTopNodeID();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStartDate method, of class WorkspaceImpl.
     */
    @Test
    public void testGetStartDate() {
        System.out.println("getStartDate");
        LamusWorkspace instance = null;
        Date expResult = null;
        Date result = instance.getStartDate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEndDate method, of class WorkspaceImpl.
     */
    @Test
    public void testGetEndDate() {
        System.out.println("getEndDate");
        LamusWorkspace instance = null;
        Date expResult = null;
        Date result = instance.getEndDate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

//    /**
//     * Test of setEndDate method, of class WorkspaceImpl.
//     */
//    @Test
//    public void testSetEndDate() {
//        System.out.println("setEndDate");
//        Date endDate = null;
//        WorkspaceImpl instance = null;
//        instance.setEndDate(endDate);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getSessionStartDate method, of class WorkspaceImpl.
     */
    @Test
    public void testGetSessionStartDate() {
        System.out.println("getSessionStartDate");
        LamusWorkspace instance = null;
        Date expResult = null;
        Date result = instance.getSessionStartDate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

//    /**
//     * Test of setSessionStartDate method, of class WorkspaceImpl.
//     */
//    @Test
//    public void testSetSessionStartDate() {
//        System.out.println("setSessionStartDate");
//        Date sessionStartDate = null;
//        WorkspaceImpl instance = null;
//        instance.setSessionStartDate(sessionStartDate);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getSessionEndDate method, of class WorkspaceImpl.
     */
    @Test
    public void testGetSessionEndDate() {
        System.out.println("getSessionEndDate");
        LamusWorkspace instance = null;
        Date expResult = null;
        Date result = instance.getSessionEndDate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

//    /**
//     * Test of setSessionEndDate method, of class WorkspaceImpl.
//     */
//    @Test
//    public void testSetSessionEndDate() {
//        System.out.println("setSessionEndDate");
//        Date sessionEndDate = null;
//        WorkspaceImpl instance = null;
//        instance.setSessionEndDate(sessionEndDate);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getUsedStorageSpace method, of class WorkspaceImpl.
     */
    @Test
    public void testGetUsedStorageSpace() {
        System.out.println("getUsedStorageSpace");
        LamusWorkspace instance = null;
        long expResult = 0L;
        long result = instance.getUsedStorageSpace();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUsedStorageSpace method, of class WorkspaceImpl.
     */
    @Test
    public void testSetUsedStorageSpace() {
        System.out.println("setUsedStorageSpace");
        long usedStorageSpace = 0L;
        LamusWorkspace instance = null;
        instance.setUsedStorageSpace(usedStorageSpace);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaxStorageSpace method, of class WorkspaceImpl.
     */
    @Test
    public void testGetMaxStorageSpace() {
        System.out.println("getMaxStorageSpace");
        LamusWorkspace instance = null;
        long expResult = 0L;
        long result = instance.getMaxStorageSpace();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMaxStorageSpace method, of class WorkspaceImpl.
     */
    @Test
    public void testSetMaxStorageSpace() {
        System.out.println("setMaxStorageSpace");
        long maxStorageSpace = 0L;
        LamusWorkspace instance = null;
        instance.setMaxStorageSpace(maxStorageSpace);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStatus method, of class WorkspaceImpl.
     */
    @Test
    public void testGetStatus() {
        System.out.println("getStatus");
        LamusWorkspace instance = null;
        WorkspaceStatus expResult = null;
        WorkspaceStatus result = instance.getStatus();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setStatus method, of class WorkspaceImpl.
     */
    @Test
    public void testSetStatus() {
        System.out.println("setStatus");
        WorkspaceStatus status = null;
        LamusWorkspace instance = null;
        instance.setStatus(status);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMessage method, of class WorkspaceImpl.
     */
    @Test
    public void testGetMessage() {
        System.out.println("getMessage");
        LamusWorkspace instance = null;
        String expResult = "";
        String result = instance.getMessage();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMessage method, of class WorkspaceImpl.
     */
    @Test
    public void testSetMessage() {
        System.out.println("setMessage");
        String message = "";
        LamusWorkspace instance = null;
        instance.setMessage(message);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getArchiveInfo method, of class WorkspaceImpl.
     */
    @Test
    public void testGetArchiveInfo() {
        System.out.println("getArchiveInfo");
        LamusWorkspace instance = null;
        String expResult = "";
        String result = instance.getArchiveInfo();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    /**
     * Test of equals method, of class WorkspaceImpl.
     */
    @Test
    public void testEquals() {

        fail("The test case is a prototype.");
    }
    
        /**
     * Test of hashCode method, of class WorkspaceImpl.
     */
    @Test
    public void testHashCode() {

        fail("The test case is a prototype.");
    }
}
