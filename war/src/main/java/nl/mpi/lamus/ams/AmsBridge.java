package nl.mpi.lamus.ams;

import nl.mpi.util.OurURL;

/**
 * $Id$
 *
 * interface encapsulating ams (access management system) related functionalities,
 * enables lamus to switch between ams1, ams2 or no-ams (ams0) easily.
 * 
 * @see Configuration#getAmsBridge(String)
 *
 * @author	last modified by $Author$, created by mategg
 * @version	$Revision$
 */
public interface AmsBridge {
	
	/**
	 * contract from original ams1 impl: default value for storagespace getters
	 */
	public static final Integer	DEFAULT_MB	= Integer.valueOf(0);

	/**
	 * contract from original ams1 impl: error value for storagespace getters
	 */
	public static final Integer	ERROR_MB	= Integer.valueOf(-1);
	
	/**
	 * provides db-status information
	 * @return true if connected and available
	 */
	public boolean getStatus();
	
	
	/**
	 * closes the db connection and releases any resources
	 */
	public void close();
	
	/**
	 * closes the db connection and releases any resources
	 * @param reason
	 */
	public void close(String reason);
	
	/**
	 * authenticates given user(uid) by given pw
	 * @param username
	 * @param password
	 * @return
	 */
// removed, cause no longer needed, came from old/unused/removed lams.Authenticate
//	public boolean validateUser(String username, String password);
	
	/**
	 * determines whether given user (uid) has write access to given resource
	 * @param userId  destined user's uid
	 * @param ourl destined resource to check 
	 * @return true/false according to whether given user (uid) has write access to given resource
	 */
	public boolean hasWriteAccess(String userId, int nodeId/*OurURL ourl*/);
	
	/**
	 * sets the used-storage-space for the given user(uid) on the given resource(ourl)
	 * to given value
	 * @param uid target user's uid
	 * @param ourl target resource
	 * @param val value to be set
	 */
	public void setUsedStorageSpace(String uid, int nodeID, /*OurURL ourl,*/ long val);

	/**
	 * provides the used-storage-space of the given user(uid) on the given resource(ourl)
	 * @param uid destined user's uid
	 * @param ourl target resource
	 * @return  the used-storage-space of the given user(uid) on the given resource(ourl)
	 */
	public long getUsedStorageSpace(String uid, int nodeID /*OurURL ourl*/);

	/**
	 * provides the maximum-storage-space of the given user(uid) on the given resource(ourl)
	 * @param uid destined user's uid
	 * @param ourl target resource
	 * @return maximum-storage-space of the given user(uid) on the given resource(ourl)
	 */
	public long getMaxStorageSpace(String uid, int nodeID /*OurURL ourl*/);

	/**
	 * provides the email address of given user(uid)
	 * @param uid destined user's uid
	 * @return the email address of given user(uid)
	 */
	public String getMailAddress(String uid);

	/**
	 * provides the "real-name" of given user(uid): firstname surname
	 * @param uid destined user's uid 
	 * @return the "real-name" of given user(uid): firstname surname
	 */
	public String getRealName(String uid);
	
	
	/**
	 * Recalculate the resource access rights for the updated part of the archive,
	 * propagate them to the Apache htaccess file and signal the webserver
	 */
	public void callAccessRightsManagementSystem(String recalcDomainMpiID);

	/**
	 * Applies the necessary procedures to replace a node in AMS:
	 * - copy node-principal pairs and corresponding rules from the old to the new node and delete them from the old one
	 * - copy node-license pairs and corresponding licenses from the old to the new node and delete them from the old one
	 * - create a node-principal with the default rule for versioned nodes and apply it to the old node
	 * 
	 * @param oldNodeId	ID of the old node (replaced) in mpi-node-id string format
	 * @param newNodeId	ID of the new node (replacing) in mpi-node-id string format
	 * @param userId	ID of the current user
	 */
	public boolean replaceNodeAms(String oldNodeId, String newNodeId, String userId);

}