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

import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.pages.CreateWorkspacePage;
import nl.mpi.lamus.web.pages.IndexPage;
import nl.mpi.lamus.web.pages.LamusPage;
import nl.mpi.lamus.web.pages.LoginInfoPage;
import nl.mpi.lamus.web.pages.SelectWorkspacePage;
import nl.mpi.lamus.web.pages.management.ManageWorkspacesPage;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.web.session.LamusSessionFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class LamusWicketApplication extends WebApplication implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    
    @SpringBean
    private LamusSessionFactory lamusSessionFactory;
//    @SpringBean
//    private WorkspaceService workspaceService;

    public LamusWicketApplication(LamusSessionFactory sessionFactory) {
	this.lamusSessionFactory = sessionFactory;
    }

    @Override
    public Class getHomePage() {
        
        if(!LamusSession.get().isAuthenticated()) {
            return IndexPage.class;
        } else {
//            if(workspaceService.userHasWorkspaces(LamusSession.get().getUserId())) {
//                return SelectWorkspacePage.class;
//            } else {
                return CreateWorkspacePage.class;
//            }
        }
        // if user logged in
            // if has open workspaces, set "select workspace page" as homepage
            // else set "create workspace" as homepage
        // else set "about" as homepage
    }

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
//        mountPage("/ManageWorkspacePage", ManageWorkspacesPage.class);
//        mountPage("/LoginInfoPage", LoginInfoPage.class);
        
//        PackageResourceReference lamus2CssReference = new PackageResourceReference(LamusPage.class, "lamus2.css");
//        getSharedResources().add("lamus2Css", lamus2CssReference.getResource());
//        mountResource("/css/lamus2.css", lamus2CssReference);
        
        PackageResourceReference tlaLogoImageReference = new PackageResourceReference(LamusPage.class, "tla_logo.png");
        getSharedResources().add("tlaLogoImage", tlaLogoImageReference.getResource());
//        mountResource("/images/tla_logo.png", tlaLogoImageReference);
        PackageResourceReference homeImageReference = new PackageResourceReference(LamusPage.class, "home.png");
        getSharedResources().add("homeImage", homeImageReference.getResource());
//        mountResource("/images/home.png", homeImageReference);
        PackageResourceReference clarinInvertedImageReference = new PackageResourceReference(LamusPage.class, "CLARIN-inverted.png");
        getSharedResources().add("clarinInvertedImage", clarinInvertedImageReference.getResource());
//        mountResource("/images/CLARIN-inverted.png", clarinInvertedImageReference);
        
//        mountResource("/fonts/lamus_icon_font/lamus_icon_font.eot", new PackageResourceReference(LamusPage.class, "lamus_icon_font/lamus_icon_font.eot"));
//        mountResource("/fonts/lamus_icon_font/lamus_icon_font.svg", new PackageResourceReference(LamusPage.class, "lamus_icon_font/lamus_icon_font.svg"));
//        mountResource("/fonts/lamus_icon_font/lamus_icon_font.ttf", new PackageResourceReference(LamusPage.class, "lamus_icon_font/lamus_icon_font.ttf"));
//        mountResource("/fonts/lamus_icon_font/lamus_icon_font.woff", new PackageResourceReference(LamusPage.class, "lamus_icon_font/lamus_icon_font.woff"));
        
//        mountResource("/css/fonts/icomoon/icomoon.eot", new PackageResourceReference(LamusPage.class, "icomoon/icomoon.eot"));
//        mountResource("/css/fonts/icomoon/icomoon.svg", new PackageResourceReference(LamusPage.class, "icomoon/icomoon.svg"));
//        mountResource("/css/fonts/icomoon/icomoon.ttf", new PackageResourceReference(LamusPage.class, "icomoon/icomoon.ttf"));
//        mountResource("/css/fonts/icomoon/icomoon.woff", new PackageResourceReference(LamusPage.class, "icomoon/icomoon.woff"));
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
