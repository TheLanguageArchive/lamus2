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
import nl.mpi.lamus.web.pages.UploadPage;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.web.session.LamusSessionFactory;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.file.Folder;

public class LamusWicketApplication extends WebApplication {

    @SpringBean
    private LamusSessionFactory sessionFactory;
    
    private Folder uploadFolder = null;

    public LamusWicketApplication(LamusSessionFactory sessionFactory) {
	this.sessionFactory = sessionFactory;
    }

    @Override
    public Class getHomePage() {
	return IndexPage.class;
    }

    @Override
    protected void init() {
	super.init();
	addComponentInstantiationListener(new SpringComponentInjector(this));
        getResourceSettings().setThrowExceptionOnMissingResource(false);

        uploadFolder = new Folder(System.getProperty("java.io.tmpdir"), "wicket-uploads");
        // Ensure folder exists
        uploadFolder.mkdirs();


        //getRootRequestMapperAsCompound().add(new MountedMapper("/single", UploadPage.class));

        //getApplicationSettings().setUploadProgressUpdatesEnabled(true);
    }

    @Override
    public LamusSession newSession(Request request, Response response) {
	return sessionFactory.createSession(this, request, response);
    }
    
    /**
     * @return the folder for uploads
     */
    public Folder getUploadFolder()
    {
        return uploadFolder;
    }
}
