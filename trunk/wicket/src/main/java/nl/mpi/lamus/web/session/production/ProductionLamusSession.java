/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web.session.production;

import javax.servlet.http.HttpServletRequest;
import nl.mpi.lamus.web.session.LamusSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 *
 * @author guisil
 */
public class ProductionLamusSession extends LamusSession {
    
    public ProductionLamusSession(Request request) {
        super(request);
    }

    @Override
    public String getUserId() {
        
        HttpServletRequest request = (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();
        String userid = request.getRemoteUser();
        if (userid == null || userid.equals("")) {
            userid = "anonymous";
        }
        return userid;
    }

    @Override
    public boolean isAuthenticated() {
        
        return !"anonymous".equals(getUserId());
    }
    
}
