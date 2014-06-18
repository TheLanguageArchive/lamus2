package nl.mpi.lamus.ams;

import java.io.Serializable;
import java.net.URI;

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
public interface AmsBridge extends Serializable {
	
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
	 * sets the used-storage-space for the given user(uid) on the given archive node
	 * to given value
	 * @param uid target user's uid
	 * @param archiveNodeURI URI of the archive node
	 * @param val value to be set
	 */
	public void setUsedStorageSpace(String uid, URI archiveNodeURI, long val);

	/**
	 * provides the used-storage-space of the given user(uid) on the given archive node
	 * @param uid destined user's uid
	 * @param archiveURI URI of the archive ndoe
	 * @return  the used-storage-space of the given user(uid) on the given resource(nodeIdStr)
	 */
	public long getUsedStorageSpace(String uid, URI archiveNodeURI);

	/**
	 * provides the maximum-storage-space of the given user(uid) on the given archive node
	 * @param uid destined user's uid
	 * @param archiveNodeURI URI of the archive node
	 * @return maximum-storage-space of the given user(uid) on the given resource(nodeIdStr)
	 */
	public long getMaxStorageSpace(String uid, URI archiveNodeURI);

	/**
	 * provides the email address of given user(uid)
	 * @param uid destined user's uid
	 * @return the email address of given user(uid)
	 */
//	public String getMailAddress(String uid);

	/**
	 * provides the "real-name" of given user(uid): firstname surname
	 * @param uid destined user's uid 
	 * @return the "real-name" of given user(uid): firstname surname
	 */
//	public String getRealName(String uid);
	
	
	/**
	 * Recalculate the resource access rights for the updated part of the archive,
	 * propagate them to the Apache htaccess file and signal the webserver
	 */
	public void callAccessRightsManagementSystem(URI recalcDomainArchiveURI);

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
//	public boolean replaceNodeAms(String oldNodeId, String newNodeId, String userId);

}