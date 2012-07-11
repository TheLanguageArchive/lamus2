/*
 * LamusWicketApplication.java
 *
 * Created on March 21, 2012, 1:47 PM
 */
package nl.mpi.lamus.web;

import nl.mpi.lamus.web.pages.IndexPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

/**
 *
 * @author jeafer
 * @version
 */
public class LamusWicketApplication extends WebApplication {

    public LamusWicketApplication() {
    }

    public Class getHomePage() {
	return IndexPage.class;
    }

    @Override
    protected void init() {
	super.init();
	addComponentInstantiationListener(new SpringComponentInjector(this));
    }
}
