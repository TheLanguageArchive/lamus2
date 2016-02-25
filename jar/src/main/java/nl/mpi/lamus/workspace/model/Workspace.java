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
package nl.mpi.lamus.workspace.model;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.Date;

/**
 * Represents a workspace.
 * @author guisil
 */
public interface Workspace extends Serializable {
    
    public int getWorkspaceID();
    
    public void setWorkspaceID(int workspaceID);
    
    public String getUserID();
    
    public void setUserID(String userID);
    
    public int getTopNodeID();
    
    public void setTopNodeID(int topNodeID);
    
    public URI getTopNodeArchiveURI();
    
    public void setTopNodeArchiveURI(URI topNodeArchiveURI);
    
    public URL getTopNodeArchiveURL();
    
    public void setTopNodeArchiveURL(URL topNodeArchiveURL);
    
    public Date getStartDate();
    
    public String getStartDateStr();
    
    public void setStartDate(Date startDate);
    
    public Date getEndDate();
    
    public String getEndDateStr();
    
    public void setEndDate(Date endDate);
    
    public Date getSessionStartDate();
    
    public String getSessionStartDateStr();
    
    public void setSessionStartDate(Date sessionStartDate);
    
    public Date getSessionEndDate();
    
    public String getSessionEndDateStr();
    
    public void setSessionEndDate(Date sessionEndDate);
    
    public long getUsedStorageSpace();
    
    public void setUsedStorageSpace(long usedStorageSpace);
    
    public long getMaxStorageSpace();
    
    public void setMaxStorageSpace(long maxStorageSpace);
    
    public WorkspaceStatus getStatus();
    
    public void setStatus(WorkspaceStatus status);
    
    public String getMessage();
    
    public void setMessage(String message);
    
    public void setStatusMessageInitialising();
    
    public void setStatusMessageErrorDuringInitialisation();
    
    public void setStatusMessageInitialised();
    
    public String getCrawlerID();
    
    public void setCrawlerID(String crawlerID);
    
    public String getWorkspaceSelectionDisplayString();
}
