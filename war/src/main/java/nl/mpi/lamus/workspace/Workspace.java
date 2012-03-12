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

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface Workspace {
    
    public int getWorkspaceID();
    
    public void setWorkspaceID(int workspaceID);
    
    public String getUserID();
    
    public void setUserID(String userID);
    
    //
    public int getTopNodeID();
    
    //
    public void setTopNodeID(int topNodeID);    
    
    public Date getStartDate();
    
    public void setStartDate(Date startDate);
    
    public Date getEndDate();
    
    public void setEndDate(Date endDate);
    
    public Date getSessionStartDate();
    
    public void setSessionStartDate(Date sessionStartDate);
    
    public Date getSessionEndDate();
    
    public void setSessionEndDate(Date sessionEndDate);
    
    public void updateStartDates();
    
    public void updateEndDates();
    
    public long getUsedStorageSpace();
    
    //
    public void setUsedStorageSpace(long usedStorageSpace);
    
    
    public long getMaxStorageSpace();
    
    public void setMaxStorageSpace(long maxStorageSpace);

    
    public WorkspaceStatus getStatus();
    
    public void setStatus(WorkspaceStatus status);
    
    public String getMessage();
    
    public void setMessage(String message);
    
    public String getArchiveInfo();
    
    public void setArchiveInfo(String archiveInfo);
}
