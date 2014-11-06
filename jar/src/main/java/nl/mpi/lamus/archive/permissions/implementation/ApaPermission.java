/* File permission check / fix package
 */

package nl.mpi.lamus.archive.permissions.implementation;

import mpi.jnatools.jnaChmodChgrp;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Mostly copied from the old LAMUS.
 * Data object for group, group ID and mode (permissions) of a file
 * @author alekoe
 * @author guisil
 */
public class ApaPermission {

    /** Numerical groups ID */
    private int _gid;

    /** Mode, usually printed in octal notation, e.g. 0644 */
    private int _mode;

    /** Group name, or "unknown" */
    private String _groupName = "unknown";

    /** Constructor using group string, will be resolved to
     * numerical group ID using jnaChmodChgrp.getGroupId(...)
     * (group must be one of the groups of the current user)
     */
    public ApaPermission(String myGroup, int myMode) {
        this(jnaChmodChgrp.getGroupId(myGroup), myMode);
        _groupName = myGroup;
    }

    /** Constructor using numerical group ID, group name is 'unknown' */
    public ApaPermission(int myGid, int myMode) {
        _gid = myGid;
        _mode = myMode;
    }

    /**
     * Find differences between two apaPermission objects:
     * @param anotherPerm apaPermission that should be compared to this
     * @return 0 if both permissions are equal, 1 if group IDs differ,
     *   2 if modes differ, 3 if group IDs and modes differ. Group name
     *   strings are not compared!
     */
    public int compare(ApaPermission anotherPerm) {
        if (anotherPerm==null)
            return 0;
        return ((_gid != anotherPerm.getGID()) ? 1 : 0) |
            ((_mode != anotherPerm.getMode()) ? 2 : 0);
    }

    public String getGroupName() {
        return _groupName;
    }

    public int getGID() {
        return _gid;
    }

    public int getMode() {
        return _mode;
    }


    @Override
    public int hashCode() {
        
        HashCodeBuilder hashCodeB = new HashCodeBuilder()
                .append(this._gid)
                .append(this._mode)
                .append(this._groupName);
        
        return hashCodeB.toHashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof ApaPermission)) {
            return false;
        }
        ApaPermission other = (ApaPermission) obj;
        
        
        EqualsBuilder equalsB = new EqualsBuilder()
                .append(this._gid, other.getGID())
                .append(this._mode, other.getMode())
                .append(this._groupName, other.getGroupName());
        
        return equalsB.isEquals();
    }
}
