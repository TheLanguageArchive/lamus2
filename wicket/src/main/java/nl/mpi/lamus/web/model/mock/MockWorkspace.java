/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web.model.mock;

import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;

/**
 * A Lamus workspace. mock of the actual class which extends {@link Serializable}, <em>all implementations should be serializable!</em>
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MockWorkspace implements Workspace {

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

    @Override
    public String getCrawlerID() {
	return archiveInfo;
    }

    @Override
    public void setCrawlerID(String archiveInfo) {
	this.archiveInfo = archiveInfo;
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
    public String getEndDateStr() {
        Date date = getEndDate();
        if(date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
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
    public long getMaxStorageSpace() {
	return maxStorageSpace;
    }

    @Override
    public void setMaxStorageSpace(long maxStorageSpace) {
	this.maxStorageSpace = maxStorageSpace;
    }

    @Override
    public String getMessage() {
	return message;
    }

    @Override
    public void setMessage(String message) {
	this.message = message;
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
    public String getSessionEndDateStr() {
        Date date = getSessionEndDate();
        if(date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
    
    @Override
    public void setSessionEndDate(Date sessionEndDate) {
	Date toSet = null;
        if(sessionEndDate != null) {
            toSet = (Date) sessionEndDate.clone();
        }
        this.sessionEndDate = toSet;
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
    public String getSessionStartDateStr() {
        Date date = getSessionStartDate();
        if(date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
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
    public Date getStartDate() {
	Date toReturn = null;
        if(this.startDate != null) {
            toReturn = (Date) this.startDate.clone();
        }
        return toReturn;
    }
    
    @Override
    public String getStartDateStr() {
        Date date = getStartDate();
        if(date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
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
    public URI getTopNodeArchiveURI() {
	return topNodeArchiveURI;
    }

    @Override
    public void setTopNodeArchiveURI(URI topNodeArchiveURI) {
	this.topNodeArchiveURI = topNodeArchiveURI;
    }
    
    @Override
    public URL getTopNodeArchiveURL() {
        return topNodeArchiveURL;
    }
    
    @Override
    public void setTopNodeArchiveURL(URL topNodeArchiveURL) {
        this.topNodeArchiveURL = topNodeArchiveURL;
    }
    
    @Override
    public int getTopNodeID() {
        return topNodeID;
    }
    
    @Override
    public void setTopNodeID(int topNodeID) {
        this.topNodeID = topNodeID;
    }

    @Override
    public long getUsedStorageSpace() {
	return usedStorageSpace;
    }

    @Override
    public void setUsedStorageSpace(long usedStorageSpace) {
	this.usedStorageSpace = usedStorageSpace;
    }

    @Override
    public String getUserID() {
	return userID;
    }

    @Override
    public void setUserID(String userID) {
	this.userID = userID;
    }

    @Override
    public int getWorkspaceID() {
	return workspaceID;
    }

    @Override
    public void setWorkspaceID(int workspaceID) {
	this.workspaceID = workspaceID;
    }

    @Override
    public WorkspaceStatus getStatus() {
	return status;
    }

    @Override
    public void setStatus(WorkspaceStatus status) {
	this.status = status;
    }

    @Override
    public void setStatusMessageInitialising() {
	setMessage("Workspace initialising");
    }

    @Override
    public void setStatusMessageInitialised() {
	setMessage("Workspace initialised");
    }

    @Override
    public void setStatusMessageErrorDuringInitialisation() {
	setMessage("Error during initialisation");
    }
    
    @Override
    public String getWorkspaceSelectionDisplayString() {
        return toString();
    }

    @Override
    public String toString() {
	return String.format("Workspace %1$d", getWorkspaceID());
    }
}
