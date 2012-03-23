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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspace implements Workspace {
    
    private int workspaceID;
    private String userID;
    private int topNodeID;
    private Date startDate;
    private Date endDate;
    private Date sessionStartDate;
    private Date sessionEndDate;
    private long usedStorageSpace;
    private long maxStorageSpace;
    private WorkspaceStatus status;
    private String message;
    private String archiveInfo;
    
    public LamusWorkspace(String userID, long usedStorageSpace, long maxStorageSpace) {
        this.userID = userID;
        this.usedStorageSpace = usedStorageSpace;
        this.maxStorageSpace = maxStorageSpace;
        Date now = Calendar.getInstance().getTime();
        this.startDate = now;
        this.sessionStartDate = now;
        this.status = WorkspaceStatus.INITIALISING;
        //TODO set message, etc
    }
    
    public LamusWorkspace(int workspaceID, String userID, int topNodeID,
            Date startDate, Date endDate, Date sessionStartDate, Date sessionEndDate,
            long usedStorageSpace, long maxStorageSpace, WorkspaceStatus status, String message, String archiveInfo) {
        this.workspaceID = workspaceID;
        this.userID = userID;
        this.topNodeID = topNodeID;
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
    
    public int getWorkspaceID() {
        return this.workspaceID;
    }
    
    public void setWorkspaceID(int workspaceID) {
        this.workspaceID = workspaceID;
    }

    public String getUserID() {
        return this.userID;
    }
    
    public void setUserID(String userID) {
        this.userID = userID;
    }
    
    public int getTopNodeID() {
        return this.topNodeID;
    }

    public void setTopNodeID(int topNodeID) {
        this.topNodeID = topNodeID;
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
    
//    public void updateStartDates() {
//        throw new UnsupportedOperationException("not yet implemented");
//    }
//    
//    public void updateEndDates() {
//        throw new UnsupportedOperationException("not yet implemented");
//    }
    
    public long getUsedStorageSpace() {
        return this.usedStorageSpace;
    }

    public void setUsedStorageSpace(long usedStorageSpace) {
        this.usedStorageSpace = usedStorageSpace;
    }

    public long getMaxStorageSpace() {
        return this.maxStorageSpace;
    }

    public void setMaxStorageSpace(long maxStorageSpace) {
        this.maxStorageSpace = maxStorageSpace;
    }
        
    public WorkspaceStatus getStatus() {
        return this.status;
    }

    public void setStatus(WorkspaceStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getArchiveInfo() {
        return this.archiveInfo;
    }

    public void setArchiveInfo(String archiveInfo) {
        this.archiveInfo = archiveInfo;
    }
    
    
    
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .append(this.workspaceID)
                .append(this.userID)
                .append(this.topNodeID)
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
        if(this.archiveInfo != null) {
            hashCodeB.append(this.archiveInfo);
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
                .append(this.startDate, other.getStartDate())
                .append(this.endDate, other.getEndDate())
                .append(this.sessionStartDate, other.getSessionStartDate())
                .append(this.sessionEndDate, other.getSessionEndDate())
                .append(this.usedStorageSpace, other.getUsedStorageSpace())
                .append(this.maxStorageSpace, other.getMaxStorageSpace())
                .append(this.status, other.getStatus())
                .append(this.message, other.getMessage())
                .append(this.archiveInfo, other.getArchiveInfo());
        
        return equalsB.isEquals();
    }
    
    @Override
    public String toString() {
        
        String stringResult = "Workspace ID: " + this.workspaceID + ", User ID: " + this.userID +
                ", Top Node ID: " + this.topNodeID + ", Start Date: " + this.startDate +
                ", End Date: " + this.endDate + ", Session Start Date: " + this.sessionStartDate +
                ", Session End Date: " + this.sessionEndDate + ", Used Storage Space: " + this.usedStorageSpace +
                ", Max Storage Space: " + this.maxStorageSpace + ", Status: " + this.status +
                ", Message: " + this.message + ", Archive Info: " + this.archiveInfo;
        
        return stringResult;
    }
}
