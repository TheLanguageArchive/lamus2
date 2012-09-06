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
package nl.mpi.lamus.web.session.mock;

import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.web.session.LamusSessionFactory;
import org.apache.wicket.Application;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;

/**
 * Implementation of LamusSessionFactory. Set information and create session containing information.
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MockLamusSessionFactory implements LamusSessionFactory {

    private String userId;
    private boolean authenticated;

    public void setUserId(String userId) {
	this.userId = userId;
    }

    public void setAuthenticated(boolean authenticated) {
	this.authenticated = authenticated;
    }

    @Override
    public LamusSession createSession(Application application, Request request, Response response) {
	return new LamusSession(request) {

	    @Override
	    public String getUserId() {
		return userId;
	    }

	    @Override
	    public boolean isAuthenticated() {
		return authenticated;
	    }
	};
    }
}
