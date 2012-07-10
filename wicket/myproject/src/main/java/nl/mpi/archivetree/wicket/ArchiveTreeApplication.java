/*
 * ArchiveTreeApplication.java
 *
 * Created on March 21, 2012, 1:47 PM
 */
 
package nl.mpi.archivetree.wicket;           

import org.apache.wicket.protocol.http.WebApplication;
/** 
 *
 * @author jeafer
 * @version 
 */

public class ArchiveTreeApplication extends WebApplication {

    public ArchiveTreeApplication() {
    }

    public Class getHomePage() {
	return IndexPage.class;
    }

}
