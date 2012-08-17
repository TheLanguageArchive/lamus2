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
package nl.mpi.lamus.web.session;

import org.apache.wicket.Application;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface LamusSessionFactory {

    LamusSession createSession(Application application, Request request, Response response);
}
