/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.web;

import nl.mpi.lamus.web.pages.CreateWorkspacePage;
import nl.mpi.lamus.web.pages.IndexPage;
import nl.mpi.lamus.web.pages.LamusPage;
import nl.mpi.lamus.web.pages.SelectWorkspacePage;
import nl.mpi.lamus.web.pages.management.ManageWorkspacesPage;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.web.session.LamusSessionFactory;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Implementation of WebApplication for the LAMUS Wicket based UI.
 * @see WebApplication
 * @author guisil
 */
public class LamusWicketApplication extends WebApplication implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    
    @SpringBean
    private final LamusSessionFactory lamusSessionFactory;

    public LamusWicketApplication(LamusSessionFactory sessionFactory) {
	this.lamusSessionFactory = sessionFactory;
    }

    /**
     * @see WebApplication#getHomePage()
     */
    @Override
    public Class getHomePage() {
        
        if(!LamusSession.get().isAuthenticated()) {
            return IndexPage.class;
        } else {
            return CreateWorkspacePage.class;
        }
    }

    /**
     * @see WebApplication#init()
     */
    @Override
    protected void init() {
	super.init();	
	getComponentInstantiationListeners().add(
                new SpringComponentInjector(this, applicationContext, true));
        getResourceSettings().setThrowExceptionOnMissingResource(false);

        getApplicationSettings().setUploadProgressUpdatesEnabled(true);
        
        mountPage("/IndexPage", IndexPage.class);
        mountPage("/CreateWorkspacePage", CreateWorkspacePage.class);
        mountPage("/SelectWorkspacePage", SelectWorkspacePage.class);
        mountPage("/ManageWorkspacesPage", ManageWorkspacesPage.class);

        PackageResourceReference tlaLogoImageReference = new PackageResourceReference(LamusPage.class, "tla_logo.png");
        getSharedResources().add("tlaLogoImage", tlaLogoImageReference.getResource());
        PackageResourceReference homeImageReference = new PackageResourceReference(LamusPage.class, "home.png");
        getSharedResources().add("homeImage", homeImageReference.getResource());
        PackageResourceReference clarinInvertedImageReference = new PackageResourceReference(LamusPage.class, "CLARIN-inverted.png");
        getSharedResources().add("clarinInvertedImage", clarinInvertedImageReference.getResource());
    }

    /**
     * @see WebApplication#newSession(org.apache.wicket.request.Request, org.apache.wicket.request.Response)
     */
    @Override
    public LamusSession newSession(Request request, Response response) {
	return lamusSessionFactory.createSession(this, request, response);
    }
    
    /**
     * @see ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }
}
