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

import nl.mpi.lamus.web.pages.IndexPage;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.web.session.LamusSessionFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class LamusWicketApplication extends WebApplication implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    
    @SpringBean
    private LamusSessionFactory lamusSessionFactory;

    public LamusWicketApplication(LamusSessionFactory sessionFactory) {
	this.lamusSessionFactory = sessionFactory;
    }

    @Override
    public Class getHomePage() {
	return IndexPage.class;
    }

    @Override
    protected void init() {
	super.init();	
	getComponentInstantiationListeners().add(
                new SpringComponentInjector(this, applicationContext, true));
        getResourceSettings().setThrowExceptionOnMissingResource(false);

        getApplicationSettings().setUploadProgressUpdatesEnabled(true);
        
        mountPage("/IndexPage", IndexPage.class);
    }

    @Override
    public LamusSession newSession(Request request, Response response) {
	return lamusSessionFactory.createSession(this, request, response);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.applicationContext = ac;
    }
}
