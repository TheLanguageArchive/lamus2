package nl.mpi.archivetree.model.mock;

import java.net.URL;
import java.util.Date;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MockWorkspace implements Workspace {

    private int workspaceID;
    private String userID;
    private int topNodeID;
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

    public String getArchiveInfo() {
	return archiveInfo;
    }

    public void setArchiveInfo(String archiveInfo) {
	this.archiveInfo = archiveInfo;
    }

    public Date getEndDate() {
	return endDate;
    }

    public void setEndDate(Date endDate) {
	this.endDate = endDate;
    }

    public long getMaxStorageSpace() {
	return maxStorageSpace;
    }

    public void setMaxStorageSpace(long maxStorageSpace) {
	this.maxStorageSpace = maxStorageSpace;
    }

    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
    }

    public Date getSessionEndDate() {
	return sessionEndDate;
    }

    public void setSessionEndDate(Date sessionEndDate) {
	this.sessionEndDate = sessionEndDate;
    }

    public Date getSessionStartDate() {
	return sessionStartDate;
    }

    public void setSessionStartDate(Date sessionStartDate) {
	this.sessionStartDate = sessionStartDate;
    }

    public Date getStartDate() {
	return startDate;
    }

    public void setStartDate(Date startDate) {
	this.startDate = startDate;
    }

    public URL getTopNodeArchiveURL() {
	return topNodeArchiveURL;
    }

    public void setTopNodeArchiveURL(URL topNodeArchiveURL) {
	this.topNodeArchiveURL = topNodeArchiveURL;
    }

    public int getTopNodeID() {
	return topNodeID;
    }

    public void setTopNodeID(int topNodeID) {
	this.topNodeID = topNodeID;
    }

    public long getUsedStorageSpace() {
	return usedStorageSpace;
    }

    public void setUsedStorageSpace(long usedStorageSpace) {
	this.usedStorageSpace = usedStorageSpace;
    }

    public String getUserID() {
	return userID;
    }

    public void setUserID(String userID) {
	this.userID = userID;
    }

    public int getWorkspaceID() {
	return workspaceID;
    }

    public void setWorkspaceID(int workspaceID) {
	this.workspaceID = workspaceID;
    }

    public WorkspaceStatus getStatus() {
	return status;
    }

    public void setStatus(WorkspaceStatus status) {
	this.status = status;
    }

    public void setStatusMessageInitialising() {
        setStatus(WorkspaceStatus.INITIALISING);
        setMessage("Workspace initialising");
    }

    public void setStatusMessageErrorDuringInitialisation() {
        setStatus(WorkspaceStatus.ERROR_DURING_INITIALISATION);
        setMessage("Error during initialisation");
    }

    @Override
    public String toString() {
	return String.format("Workspace %1$d", getWorkspaceID());
    }
}
