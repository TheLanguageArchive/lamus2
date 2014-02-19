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
package nl.mpi.lamus.workspace.model.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceTest {
    
    private int workspaceID = 1;
    private String userID = "someUser";
    private int topNodeID = 1;
    private URI topNodeArchiveURI;
    private URL topNodeArchiveURL;
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
    public void setUp() throws URISyntaxException, MalformedURLException {
        this.topNodeArchiveURI = new URI(UUID.randomUUID().toString());
        this.topNodeArchiveURL = new URL("file:/some/archive/folder/node.cmdi");
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
        assertEquals("Value for 'status' is not the expected one.", WorkspaceStatus.UNINITIALISED, testWorkspace.getStatus());
        assertEquals("Value for 'message' is not the expected one.", "Workspace uninitialised", testWorkspace.getMessage());
        //TODO move message to properties file
    }

    /**
     * Test for one of the constructors
     */
    @Test
    public void constructorWithAllParametersProperlyCreatesWorkspace() {

        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        assertEquals("Value for 'workspaceID' is not the expected one.", this.workspaceID, testWorkspace.getWorkspaceID());
        assertEquals("Value for 'userID' is not the expected one.", this.userID, testWorkspace.getUserID());
        assertEquals("Value for 'topNodeID' is not the expected one.", this.topNodeID, testWorkspace.getTopNodeID());
        assertEquals("Value for 'topNodeArchiveURI' is not the expected one.", this.topNodeArchiveURI, testWorkspace.getTopNodeArchiveURI());
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
    public void constructorWithAllParametersReceivesNullDates() {
        
        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                null, null, null, null,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        assertNull("Value retrieved for 'startDate' should be null.", testWorkspace.getStartDate());
        assertNull("Value retrieved for 'sessionStartDate' should be null.", testWorkspace.getSessionStartDate());
        assertNull("Value retrieved for 'endDate' should be null.", testWorkspace.getEndDate());
        assertNull("Value retrieved for 'sessionEndDate' should be null.", testWorkspace.getSessionEndDate());
    }
    
    /**
     * 
     */
    @Test
    public void constructorWithAllParametersClonesDateObjects() {
        
        Calendar localCalendar = Calendar.getInstance();
        Date localTestDate = localCalendar.getTime();
        
        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                localTestDate, localTestDate, localTestDate, localTestDate,
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
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        Workspace testWorkspace2 = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        assertEquals("Workspace objects are not equal.", testWorkspace1, testWorkspace2);
    }
    
    @Test
    public void workspacesHaveSameHashCode() {

        Workspace testWorkspace1 = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        Workspace testWorkspace2 = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        assertEquals("Workspace objects don't have the same hashcode.", testWorkspace1.hashCode(), testWorkspace2.hashCode());
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
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        Workspace testWorkspace2 = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, differentDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        assertFalse("Workspace objects should not be equal.", testWorkspace1.equals(testWorkspace2));
    }
    
    @Test
    public void workspacesHaveDifferentHashCodes() {
        
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.add(Calendar.HOUR, -1);
        Date differentDate = newCalendar.getTime();

        Workspace testWorkspace1 = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        Workspace testWorkspace2 = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, differentDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        assertFalse("Workspace objects should not have the same hashcode.", testWorkspace1.hashCode() == testWorkspace2.hashCode());
    }
    
    @Test
    public void workspacesComparedWithObjectOfDifferentType() {

        Workspace testWorkspace1 = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        
        Object testWorkspace2 = new SomeOtherWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
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
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
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
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
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
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
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
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
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
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
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
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
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
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
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
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
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
    
    @Test
    public void testToString() {
        
        Workspace testWorkspace = new LamusWorkspace(
                this.workspaceID, this.userID, this.topNodeID, this.topNodeArchiveURI, this.topNodeArchiveURL,
                this.testDate, this.testDate, this.testDate, this.testDate,
                this.usedStorageSpace, this.maxStorageSpace,
                this.status, this.message, this.archiveInfo);
        String expectedString = "Workspace ID: " + testWorkspace.getWorkspaceID()
                + ", User ID: " + testWorkspace.getUserID()
                + ", Top Node ID: " + testWorkspace.getTopNodeID()
                + ", Top Node Archive URI: " + testWorkspace.getTopNodeArchiveURI()
                + ", Top Node Archive URL: " + testWorkspace.getTopNodeArchiveURL()
                + ", Start Date: " + testWorkspace.getStartDate()
                + ", End Date: " + testWorkspace.getEndDate()
                + ", Session Start Date: " + testWorkspace.getSessionStartDate()
                + ", Session End Date: " + testWorkspace.getSessionEndDate()
                + ", Used Storage Space: " + testWorkspace.getUsedStorageSpace()
                + ", Max Storage Space: " + testWorkspace.getMaxStorageSpace()
                + ", Status: " + testWorkspace.getStatus()
                + ", Message: " + testWorkspace.getMessage()
                + ", Archive Info: " + testWorkspace.getArchiveInfo();
        
        String actualString = testWorkspace.toString();
        
        assertEquals(expectedString, actualString);
    }
    
    @Test
    public void statusAndMessageSetAsInitialising() {
        Workspace testWorkspace = new LamusWorkspace(this.userID, this.usedStorageSpace, this.maxStorageSpace);
        testWorkspace.setStatusMessageInitialising();
        assertEquals("Value for 'status' is not the expected one.", WorkspaceStatus.INITIALISING, testWorkspace.getStatus());
        assertEquals("Value for 'message' is not the expected one.", "Workspace initialising", testWorkspace.getMessage());
        //TODO move message to properties file
    }
    
    @Test
    public void statusAndMessageSetAsErrorDuringInitialisation() {
        Workspace testWorkspace = new LamusWorkspace(this.userID, this.usedStorageSpace, this.maxStorageSpace);
        testWorkspace.setStatusMessageErrorDuringInitialisation();
        assertEquals("Value for 'status' is not the expected one.", WorkspaceStatus.ERROR_DURING_INITIALISATION, testWorkspace.getStatus());
        assertEquals("Value for 'message' is not the expected one.", "Error during initialisation", testWorkspace.getMessage());
        //TODO move message to properties file
    }
}

class SomeOtherWorkspace implements Workspace {

    private int workspaceID;
    private String userID;
    private int topNodeID;
    private URI topNodeArchiveURI;
    private URL topNodeArchiveURL;
    private Date startDate;
    private Date endDate;
    private Date sessionStartDate;
    private Date sessionEndDate;
    private long usedStorageSpace;
    private long maxStorageSpace;
    private WorkspaceStatus status;
    private String message;
    private String archiveInfo;
    
    public SomeOtherWorkspace(String userID, long usedStorageSpace, long maxStorageSpace) {
        this.userID = userID;
        this.usedStorageSpace = usedStorageSpace;
        this.maxStorageSpace = maxStorageSpace;
        Date now = Calendar.getInstance().getTime();
        this.startDate = now;
        this.sessionStartDate = now;
        this.status = WorkspaceStatus.INITIALISING;
        //TODO set message, etc
    }
    
    public SomeOtherWorkspace(int workspaceID, String userID, int topNodeID, URI topNodeArchiveURI, URL topNodeArchiveURL,
            Date startDate, Date endDate, Date sessionStartDate, Date sessionEndDate,
            long usedStorageSpace, long maxStorageSpace, WorkspaceStatus status, String message, String archiveInfo) {
        this.workspaceID = workspaceID;
        this.userID = userID;
        this.topNodeID = topNodeID;
        this.topNodeArchiveURI = topNodeArchiveURI;
        this.topNodeArchiveURL = topNodeArchiveURL;
        if(startDate != null) {
            this.startDate = (Date) startDate.clone();
        }
        if(endDate != null) {
            this.endDate = (Date) endDate.clone();
        }
        if(sessionStartDate != null) {
            this.sessionStartDate = (Date) sessionStartDate.clone();
        }
        if(sessionEndDate != null) {
            this.sessionEndDate = (Date) sessionEndDate.clone();
        }
        this.usedStorageSpace = usedStorageSpace;
        this.maxStorageSpace = maxStorageSpace;
        this.status = status;
        this.message = message;
        this.archiveInfo = archiveInfo;
    }
    
    @Override
    public int getWorkspaceID() {
        return this.workspaceID;
    }
    
    @Override
    public void setWorkspaceID(int workspaceID) {
        this.workspaceID = workspaceID;
    }

    @Override
    public String getUserID() {
        return this.userID;
    }
    
    @Override
    public void setUserID(String userID) {
        this.userID = userID;
    }
    
    @Override
    public int getTopNodeID() {
        return this.topNodeID;
    }
    
    @Override
    public void setTopNodeID(int topNodeID) {
        this.topNodeID = topNodeID;
    }
    
    @Override
    public URI getTopNodeArchiveURI() {
        return this.topNodeArchiveURI;
    }

    @Override
    public void setTopNodeArchiveURI(URI topNodeArchiveURI) {
        this.topNodeArchiveURI = topNodeArchiveURI;
    }
    
    @Override
    public URL getTopNodeArchiveURL() {
        return this.topNodeArchiveURL;
    }
    
    @Override
    public void setTopNodeArchiveURL(URL topNodeArchiveURL) {
        this.topNodeArchiveURL = topNodeArchiveURL;
    }

    @Override
    public Date getStartDate() {
        Date toReturn = null;
        if(this.startDate != null) {
            toReturn = (Date) this.startDate.clone();
        }
        return toReturn;
    }
    
    @Override
    public void setStartDate(Date startDate) {
        Date toSet = null;
        if(startDate != null) {
            toSet = (Date) startDate.clone();
        }
        this.startDate = toSet;
    }

    @Override
    public Date getEndDate() {
        Date toReturn = null;
        if(this.endDate != null) {
            toReturn = (Date) this.endDate.clone();
        }
        return toReturn;
    }
    
    @Override
    public void setEndDate(Date endDate) {
        Date toSet = null;
        if(endDate != null) {
            toSet = (Date) endDate.clone();
        }
        this.endDate = toSet;
    }

    @Override
    public Date getSessionStartDate() {
        Date toReturn = null;
        if(this.sessionStartDate != null) {
            toReturn = (Date) this.sessionStartDate.clone();
        }
        return toReturn;
    }
    
    @Override
    public void setSessionStartDate(Date sessionStartDate) {
        Date toSet = null;
        if(sessionStartDate != null) {
            toSet = (Date) sessionStartDate.clone();
        }
        this.sessionStartDate = toSet;
    }

    @Override
    public Date getSessionEndDate() {
        Date toReturn = null;
        if(this.sessionEndDate != null) {
            toReturn = (Date) this.sessionEndDate.clone();
        }
        return toReturn;
    }
    
    @Override
    public void setSessionEndDate(Date sessionEndDate) {
        Date toSet = null;
        if(sessionEndDate != null) {
            toSet = (Date) sessionEndDate.clone();
        }
        this.sessionEndDate = toSet;
    }
    
//    public void updateStartDates() {
//        throw new UnsupportedOperationException("not yet implemented");
//    }
//    
//    public void updateEndDates() {
//        throw new UnsupportedOperationException("not yet implemented");
//    }
    
    @Override
    public long getUsedStorageSpace() {
        return this.usedStorageSpace;
    }

    @Override
    public void setUsedStorageSpace(long usedStorageSpace) {
        this.usedStorageSpace = usedStorageSpace;
    }

    @Override
    public long getMaxStorageSpace() {
        return this.maxStorageSpace;
    }

    @Override
    public void setMaxStorageSpace(long maxStorageSpace) {
        this.maxStorageSpace = maxStorageSpace;
    }
        
    @Override
    public WorkspaceStatus getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(WorkspaceStatus status) {
        this.status = status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getArchiveInfo() {
        return this.archiveInfo;
    }

    @Override
    public void setArchiveInfo(String archiveInfo) {
        this.archiveInfo = archiveInfo;
    }

    @Override
    public void setStatusMessageInitialising() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setStatusMessageErrorDuringInitialisation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setStatusMessageInitialised() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateDatesForOpening() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getWorkspaceSelectionDisplayString() {
        return toString();
    }
}