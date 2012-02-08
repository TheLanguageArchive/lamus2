package nl.mpi.lamus.workspace;

import java.util.Date;

public interface Workspace {

    public int getWorkspaceID();
    
    public String getUserID();
    
    public WorkspaceNode getTopNode();
    
    public void setTopNode(WorkspaceNode newTopNode);
    
    public Date getStartDate();
    
    public Date getEndDate();
    
    public void setEndDate(Date endDate);
    
    public Date getSessionStartDate();
    
    public void setSessionStartDate(Date sessionStartDate);
    
    public Date getSessionEndDate();
    
    public void setSessionEndDate(Date sessionEndDate);
    
    public long getUsedStorageSpace();
    
    public void updateUsedStorageSpace();
    
    public long getMaxStorageSpace();
    
    public WorkspaceStatus getStatus();
    
    public void setStatus(WorkspaceStatus status);
    
    public String getMessage();
    
    public void setMessage(String message);
    
    public String getArchiveInfo();
    
    public void setArchiveInfo(String archiveInfo);
    
}
