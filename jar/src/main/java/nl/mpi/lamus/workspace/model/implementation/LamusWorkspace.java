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

import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @see Workspace
 * @author guisil
 */
public class LamusWorkspace implements Workspace {
    
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
    private String crawlerID;
    
    
    public LamusWorkspace(String userID, long usedStorageSpace, long maxStorageSpace) {
        this.userID = userID;
        this.usedStorageSpace = usedStorageSpace;
        this.maxStorageSpace = maxStorageSpace;
        Date now = Calendar.getInstance().getTime();
        this.startDate = now;
        this.sessionStartDate = now;
        this.status = WorkspaceStatus.UNINITIALISED;
        this.message = "Workspace uninitialised";
        this.crawlerID = "";
    }
    
    public LamusWorkspace(int workspaceID, String userID, int topNodeID, URI topNodeArchiveURI, URL topNodeArchiveURL,
            Date startDate, Date endDate, Date sessionStartDate, Date sessionEndDate,
            long usedStorageSpace, long maxStorageSpace, WorkspaceStatus status, String message, String crawlerID) {
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
        this.crawlerID = crawlerID;
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
    public void setStatusMessageInitialising() {
        setStatus(WorkspaceStatus.INITIALISING);
        setMessage("Workspace initialising");
    }

    @Override
    public void setStatusMessageErrorDuringInitialisation() {
        setStatus(WorkspaceStatus.ERROR_INITIALISATION);
        setMessage("Error during initialisation");
    }
    
    @Override
    public void setStatusMessageInitialised() {
        setStatus(WorkspaceStatus.INITIALISED);
        setMessage("Workspace successfully initialised");
    }

    @Override
    public String getCrawlerID() {
        return this.crawlerID;
    }

    @Override
    public void setCrawlerID(String crawlerID) {
        this.crawlerID = crawlerID;
    }
    
    @Override
    public String getWorkspaceSelectionDisplayString() {
        
        String stringResult = "ID: " + this.workspaceID + 
                ", Top Node: " + this.topNodeArchiveURI + " (" + FilenameUtils.getName(this.topNodeArchiveURL.toString()) + ")" +
                ", Start Date: " + this.startDate;
        
        return stringResult;
    }
    
    
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .append(this.workspaceID)
                .append(this.userID)
                .append(this.topNodeID)
                .append(this.topNodeArchiveURI)
                .append(this.topNodeArchiveURL)
                .append(this.startDate)
                .append(this.endDate)
                .append(this.sessionStartDate)
                .append(this.sessionEndDate)
                .append(this.usedStorageSpace)
                .append(this.maxStorageSpace);
        
        if(this.status != null) {
            hashCodeB.append(this.status);
        }
        if(this.message != null) {
            hashCodeB.append(this.message);
        }
        if(this.crawlerID != null) {
            hashCodeB.append(this.crawlerID);
        }
        
        return hashCodeB.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof LamusWorkspace)) {
            return false;
        }
        LamusWorkspace other = (LamusWorkspace) obj;
        
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .append(this.workspaceID, other.getWorkspaceID())
                .append(this.userID, other.getUserID())
                .append(this.topNodeID, other.getTopNodeID())
                .append(this.topNodeArchiveURI, other.getTopNodeArchiveURI())
                .append(this.topNodeArchiveURL, other.getTopNodeArchiveURL())
                .append(this.startDate, other.getStartDate())
                .append(this.endDate, other.getEndDate())
                .append(this.sessionStartDate, other.getSessionStartDate())
                .append(this.sessionEndDate, other.getSessionEndDate())
                .append(this.usedStorageSpace, other.getUsedStorageSpace())
                .append(this.maxStorageSpace, other.getMaxStorageSpace())
                .append(this.status, other.getStatus())
                .append(this.message, other.getMessage())
                .append(this.crawlerID, other.getCrawlerID());
        
        return equalsB.isEquals();
    }
    
    @Override
    public String toString() {
        
        String stringResult = "Workspace ID: " + this.workspaceID + ", User ID: " + this.userID +
                ", Top Node ID: " + this.topNodeID + ", Top Node Archive URI: " + this.topNodeArchiveURI +
                ", Top Node Archive URL: " + this.topNodeArchiveURL +
                ", Start Date: " + this.startDate + ", End Date: " + this.endDate +
                ", Session Start Date: " + this.sessionStartDate + ", Session End Date: " + this.sessionEndDate +
                ", Used Storage Space: " + this.usedStorageSpace + ", Max Storage Space: " + this.maxStorageSpace +
                ", Status: " + this.status + ", Message: " + this.message + ", Crawler ID: " + this.crawlerID;
        
        return stringResult;
    }
}
