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

import java.util.Calendar;
import java.util.Date;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceTest {
    
    private int workspaceID = 1;
    private String userID = "someUser";
    private int topNodeID = 10;
    private Date testDate = Calendar.getInstance().getTime();
    private long usedStorageSpace = 0L;
    private long maxStorageSpace = 10000000L;
    private WorkspaceStatus status = WorkspaceStatus.INITIALISED;
    private String message = "bla bla bla";
    private String archiveInfo = "bla/bla|bla";
    
    public LamusWorkspaceTest() {
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
     * Test for one of the constructors
     */
    @Test
    public void constructorProperlyInitialisesWorkspace() {
        
        Workspace testWorkspace = new LamusWorkspace(this.userID, this.usedStorageSpace, this.maxStorageSpace);
        
        assertEquals("Value for 'userID' is not the expected one.", this.userID, testWorkspace.getUserID());
        assertEquals("Value for 'usedStorageSpace' is not the expected one.", this.usedStorageSpace, testWorkspace.getUsedStorageSpace());
        assertEquals("Value for 'maxStorageSpace' is not the expected one.", this.maxStorageSpace, testWorkspace.getMaxStorageSpace());
        assertNotNull("Value for 'startDate' should not be null.", testWorkspace.getStartDate());
        assertEquals("Value for 'startDate' is not the expected one.", testWorkspace.getStartDate(), testWorkspace.getSessionStartDate());
        assertEquals("Value for 'status' is not the expected one.", WorkspaceStatus.INITIALISING, testWorkspace.getStatus());
        //TODO more assertions? message, etc
    }

    /**
     * Test for one of the constructors
     */
    @Test
    public void constructorWithAllParametersProperlyCreatesWorkspace() {

        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        assertEquals("Value for 'workspaceID' is not the expected one.", this.workspaceID, testWorkspace.getWorkspaceID());
        assertEquals("Value for 'userID' is not the expected one.", this.userID, testWorkspace.getUserID());
        assertEquals("Value for 'topNodeID' is not the expected one.", this.topNodeID, testWorkspace.getTopNodeID());
        assertEquals("Value for 'startDate' is not the expected one.", this.testDate, testWorkspace.getStartDate());
        assertEquals("Value for 'endDate' is not the expected one.", this.testDate, testWorkspace.getEndDate());
        assertEquals("Value for 'sessionStartDate' is not the expected one.", this.testDate, testWorkspace.getSessionStartDate());
        assertEquals("Value for 'sessionEndDate' is not the expected one.", this.testDate, testWorkspace.getSessionEndDate());
        assertEquals("Value for 'usedStorageSpace' is not the expected one.", this.usedStorageSpace, testWorkspace.getUsedStorageSpace());
        assertEquals("Value for 'maxStorageSpace' is not the expected one.", this.maxStorageSpace, testWorkspace.getMaxStorageSpace());
        assertEquals("Value for 'status' is not the expected one.", this.status, testWorkspace.getStatus());
        assertEquals("Value for 'message' is not the expected one.", this.message, testWorkspace.getMessage());
        assertEquals("Value for 'archiveInfo' is not the expected one.", this.archiveInfo, testWorkspace.getArchiveInfo());
    }
    
    /**
     * 
     */
    @Test
    public void constructorWithAllParametersClonesDateObjects() {
        
        Calendar localCalendar = Calendar.getInstance();
        Date localTestDate = localCalendar.getTime();
        
        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, localTestDate, localTestDate, localTestDate, localTestDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        localCalendar.add(Calendar.HOUR, 2);
        localTestDate.setTime(localCalendar.getTimeInMillis());
        
        assertFalse("Changing the 'startDate' object used to create the workspace should not change the value inside the workspace object.",
                localTestDate.equals(testWorkspace.getStartDate()));
        assertNotSame("Changing the 'endDate' object used to create the workspace should not change the value inside the workspace object.",
                localTestDate.equals(testWorkspace.getEndDate()));
        assertNotSame("Changing the 'sessionStartDate' object used to create the workspace should not change the value inside the workspace object.",
                localTestDate.equals(testWorkspace.getSessionStartDate()));
        assertNotSame("Changing the 'sessionEDate' object used to create the workspace should not change the value inside the workspace object.",
                localTestDate.equals(testWorkspace.getSessionEndDate()));
    }

    /**
     * Test of equals method, of class LamusWorkspace.
     */
    @Test
    public void workspacesAreEqual() {

        Workspace testWorkspace1 = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        Workspace testWorkspace2 = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        assertEquals("Workspace objects are not equal.", testWorkspace1, testWorkspace2);
    }
    
    /**
     * Test of equals method, of class LamusWorkspace.
     */
    @Test
    public void workspacesAreNotEqual() {
        
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.add(Calendar.HOUR, -1);
        Date differentDate = newCalendar.getTime();

        Workspace testWorkspace1 = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        Workspace testWorkspace2 = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.testDate, this.testDate, differentDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        assertFalse("Workspace objects should not be equal.", testWorkspace1.equals(testWorkspace2));
    }
    
    /**
     * 
     */
    @Test
    public void getStartDateReturnsEqualButNotSameObject() {
        
        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        assertEquals("Retrieved value for 'startDate' does not match the inserted one.", this.testDate, testWorkspace.getStartDate());
        assertNotSame("The 'startDate' object used to create the workspace and the one retrieved should be clones and not the exact same object.",
                this.testDate, testWorkspace.getStartDate());
    }
    
    /**
     * 
     */
    @Test
    public void setStartDateSetsProperlyEqualButNotSameObject() {
        
        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.add(Calendar.HOUR, -1);
        Date newDate = newCalendar.getTime();
        testWorkspace.setStartDate(newDate);
        
        assertFalse("Value for 'startDate' originally used should not match the current value.", this.testDate.equals(testWorkspace.getStartDate()));
        assertEquals("Retrieved value for 'startDate' does not match the one used in the 'set' method.", newDate, testWorkspace.getStartDate());
        assertNotSame("The 'startDate' object used in the 'set' method and the one retrieved should be clones and not the exact same object.",
                newDate, testWorkspace.getStartDate());
    }
    
    /**
     * 
     */
    @Test
    public void getEndDateReturnsEqualButNotSameObject() {
        
        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        assertEquals("Retrieved value for 'endDate' does not match the inserted one.", this.testDate, testWorkspace.getEndDate());
        assertNotSame("The 'endDate' object used to create the workspace and the one retrieved should be clones and not the exact same object.",
                this.testDate, testWorkspace.getEndDate());
    }
    
    /**
     * 
     */
    @Test
    public void setEndDateSetsProperlyEqualButNotSameObject() {
        
        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.add(Calendar.HOUR, -1);
        Date newDate = newCalendar.getTime();
        testWorkspace.setEndDate(newDate);
        
        assertFalse("Value for 'endDate' originally used should not match the current value.", this.testDate.equals(testWorkspace.getEndDate()));
        assertEquals("Retrieved value for 'endDate' does not match the one used in the 'set' method.", newDate, testWorkspace.getEndDate());
        assertNotSame("The 'endDate' object used in the 'set' method and the one retrieved should be clones and not the exact same object.",
                newDate, testWorkspace.getEndDate());
    }
    
    /**
     * 
     */
    @Test
    public void getSessionStartDateReturnsEqualButNotSameObject() {
        
        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        assertEquals("Retrieved value for 'sessionStartDate' does not match the inserted one.", this.testDate, testWorkspace.getSessionStartDate());
        assertNotSame("The 'sessionStartDate' object used to create the workspace and the one retrieved should be clones and not the exact same object.",
                this.testDate, testWorkspace.getSessionStartDate());
    }
    
    /**
     * 
     */
    @Test
    public void setSessionStartDateSetsProperlyEqualButNotSameObject() {
        
        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.add(Calendar.HOUR, -1);
        Date newDate = newCalendar.getTime();
        testWorkspace.setSessionStartDate(newDate);
        
        assertFalse("Value for 'sessionStartDate' originally used should not match the current value.", this.testDate.equals(testWorkspace.getSessionStartDate()));
        assertEquals("Retrieved value for 'sessionStartDate' does not match the one used in the 'set' method.", newDate, testWorkspace.getSessionStartDate());
        assertNotSame("The 'sessionStartDate' object used in the 'set' method and the one retrieved should be clones and not the exact same object.",
                newDate, testWorkspace.getSessionStartDate());
    }
    
    /**
     * 
     */
    @Test
    public void getSessionEndDateReturnsEqualButNotSameObject() {
        
        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        assertEquals("Retrieved value for 'sessionEndDate' does not match the inserted one.", this.testDate, testWorkspace.getSessionEndDate());
        assertNotSame("The 'sessionEndDate' object used to create the workspace and the one retrieved should be clones and not the exact same object.",
                this.testDate, testWorkspace.getSessionEndDate());
    }
    
    /**
     * 
     */
    @Test
    public void setSessionEndDateSetsProperlyEqualButNotSameObject() {
        
        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.add(Calendar.HOUR, -1);
        Date newDate = newCalendar.getTime();
        testWorkspace.setSessionEndDate(newDate);
        
        assertFalse("Value for 'sessionEndDate' originally used should not match the current value.", this.testDate.equals(testWorkspace.getSessionEndDate()));
        assertEquals("Retrieved value for 'sessionEndDate' does not match the one used in the 'set' method.", newDate, testWorkspace.getSessionEndDate());
        assertNotSame("The 'sessionEndDate' object used in the 'set' method and the one retrieved should be clones and not the exact same object.",
                newDate, testWorkspace.getSessionEndDate());
    }
}
