/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.archivetree.model.mock;

import java.net.URL;
import java.util.Date;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;

/**
 *
 * @author jeafer
 */
public class MockWorkspace implements Workspace {

    private int workspaceId;
    private String userId;
    private int topnodeId;
    private URL topnodearchiveUrl;
    private Date startDate;
    private Date endDate;
    private Date sessionStartDate;
    private Date sessionEndDate;
    private long usedStorageSpace;
    private long maxStorageSpace;
    private WorkspaceStatus status;
    private String message;
    private String archiveInfo;

    public MockWorkspace(int workspaceId) {
        this.workspaceId = workspaceId;
    }

    public int getWorkspaceID() {
        return workspaceId;
    }

    public void setWorkspaceID(int workspaceID) {
        this.workspaceId = workspaceID;
    }

    public String getUserID() {
        return userId;
    }

    public void setUserID(String userID) {
        this.userId = userID;
    }

    public int getTopNodeID() {
        return topnodeId;
    }

    public void setTopNodeID(int topNodeID) {
        this.topnodeId = topNodeID;
    }

    public URL getTopNodeArchiveURL() {
        return topnodearchiveUrl;
    }

    public void setTopNodeArchiveURL(URL topNodeArchiveURL) {
        this.topnodearchiveUrl = topNodeArchiveURL;
    }

    public Date getStartDate() {
                Date toReturn = null;
        if(this.startDate != null) {
            toReturn = (Date) this.startDate.clone();
        }
        return toReturn;
    }

    public void setStartDate(Date startDate) {
                Date toSet = null;
        if(startDate != null) {
            toSet = (Date) startDate.clone();
        }
        this.startDate = toSet;
    }

    public Date getEndDate() {
                Date toReturn = null;
        if(this.endDate != null) {
            toReturn = (Date) this.endDate.clone();
        }
        return toReturn;
    }

    public void setEndDate(Date endDate) {
                Date toSet = null;
        if(endDate != null) {
            toSet = (Date) endDate.clone();
        }
        this.endDate = toSet;
    }

    public Date getSessionStartDate() {
                Date toReturn = null;
        if(this.sessionStartDate != null) {
            toReturn = (Date) this.sessionStartDate.clone();
        }
        return toReturn;
    }

    public void setSessionStartDate(Date sessionStartDate) {
                Date toSet = null;
        if(sessionStartDate != null) {
            toSet = (Date) sessionStartDate.clone();
        }
        this.sessionStartDate = toSet;
    }

    public Date getSessionEndDate() {
                Date toReturn = null;
        if(this.sessionEndDate != null) {
            toReturn = (Date) this.sessionEndDate.clone();
        }
        return toReturn;
    }

    public void setSessionEndDate(Date sessionEndDate) {
        Date toSet = null;
        if(sessionEndDate != null) {
            toSet = (Date) sessionEndDate.clone();
        }
        this.sessionEndDate = toSet;
    }

    public long getUsedStorageSpace() {
        return usedStorageSpace;
    }

    public void setUsedStorageSpace(long usedStorageSpace) {
        this.usedStorageSpace = usedStorageSpace;
    }

    public long getMaxStorageSpace() {
        return maxStorageSpace;
    }

    public void setMaxStorageSpace(long maxStorageSpace) {
        this.maxStorageSpace = maxStorageSpace;
    }

    public WorkspaceStatus getStatus() {
        return status;
    }

    public void setStatus(WorkspaceStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatusMessageInitialising() {
        setStatus(WorkspaceStatus.INITIALISING);
        setMessage("Workspace initialising");
    }

    public void setStatusMessageErrorDuringInitialisation() {
        setStatus(WorkspaceStatus.ERROR_DURING_INITIALISATION);
        setMessage("Error during initialisation");
    }

    public String getArchiveInfo() {
        return archiveInfo;
    }

    public void setArchiveInfo(String archiveInfo) {
        this.archiveInfo = archiveInfo;
    }
}
