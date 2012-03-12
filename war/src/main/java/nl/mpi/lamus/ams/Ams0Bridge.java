package nl.mpi.lamus.ams;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nl.mpi.util.OurURL;
import nl.mpi.latimpl.core.LatServiceImpl;

/**
 * $Id$
 *
 * basic implementation of AmsBridge with NO actual access-management system behind.
 * 
 * This is the class which implements the no-access-management configuration of lams
 *
 * @author	last modified by $Author$, created by mategg
 * @version	$Revision$
 */
public class Ams0Bridge extends LatServiceImpl implements AmsBridge {
	
	private final static Log LOG = LogFactory.getLog(Ams0Bridge.class);
	
	/**
	 * default constructor
	 */
	public Ams0Bridge() {
	}

	/**
	 * @see lams.ams.AmsBridge#callAccessRightsManagementSystem(java.lang.String)
	 */
	public void callAccessRightsManagementSystem(String recalcDomainMpiID) {
		LOG.debug("mimic ams recalculation triggered for " + recalcDomainMpiID);
	}

	/**
	 * @see lams.ams.AmsBridge#close()
	 */
	public void close() {
		// nothing to do, no db behind
	}

	/**
	 * @see lams.ams.AmsBridge#close(java.lang.String)
	 */
//	@SuppressWarnings("unused")
	public void close(String reason) {
		// nothing to do, no db behind
	}

	/**
	 * @see lams.ams.AmsBridge#getMailAddress(java.lang.String)
	 */
//	@SuppressWarnings("unused")
	public String getMailAddress(String uid) {
		return uid + "@unknown";		// just not to return null
	}

	/**
	 * @see lams.ams.AmsBridge#getMaxStorageSpace(java.lang.String, nl.mpi.util.OurURL)
	 */
//	@SuppressWarnings("unused")
	public long getMaxStorageSpace(String uid, int nodeID /*OurURL ourl*/) {
		return ERROR_MB.longValue();
	}

	/**
	 * @see lams.ams.AmsBridge#getRealName(java.lang.String)
	 */
//	@SuppressWarnings("unused")
	public String getRealName(String uid) {
		return uid;	// just not to return null
	}

	/**
	 * @see lams.ams.AmsBridge#getStatus()
	 */
	public boolean getStatus() {
		return true;		// always pretend ok, since there is no db behind
	}

	/**
	 * @see lams.ams.AmsBridge#getUsedStorageSpace(java.lang.String, nl.mpi.util.OurURL)
	 */
//	@SuppressWarnings("unused")
	public long getUsedStorageSpace(String uid, int nodeID /*OurURL ourl*/) {
		return 0;
	}

	/**
	 * @see lams.ams.AmsBridge#hasWriteAccess(java.lang.String, nl.mpi.util.OurURL)
	 */
//	@SuppressWarnings("unused")
	public boolean hasWriteAccess(String userId, int nodeId/*OurURL ourl*/) {
		return true;		// always allow full access, ams is disabled
	}

	/**
	 * @see lams.ams.AmsBridge#setUsedStorageSpace(java.lang.String, nl.mpi.util.OurURL, long)
	 */
	public void setUsedStorageSpace(String uid, int nodeID, /*OurURL ourl,*/ long val) {
		StringBuffer msg = new StringBuffer("miming setUsedStorageSpace for ");
		msg.append(uid);
		msg.append(" on ").append(nodeID/*ourl*/);
		msg.append(" to ").append(val);
		LOG.debug(msg.toString());
	}

	/**
	 * @see lams.ams.AmsBridge#replaceNodeAms(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean replaceNodeAms(String oldNodeId, String newNodeId,
			String userId) {
		LOG.debug("Ams0Bridge.replaceNodeAms: Node " + oldNodeId + " replaced by node " + newNodeId);
		return true;
	}
}
