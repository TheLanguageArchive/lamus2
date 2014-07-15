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
package nl.mpi.lamus.util;

import java.net.URI;
import javax.json.JsonObject;

/**
 * Interface providing methods to help interacting with RESTful services using jersey.
 * @author guisil
 */
public interface JerseyHelper {
    
    /**
     * Submits a POST request to the service in the given location and paths,
     * passing the given JSON object.
     * @param requestJsonObject JSON object to be passed to the request
     * @param location Location of the service
     * @param paths Paths (each path is like a directory name) to be appended
     *              to the service location in order to call the required method
     * @return JSON object containing the response
     */
    public JsonObject postRequestCreateVersions(JsonObject requestJsonObject, String location, String... paths);
    
    /**
     * Submits a POST request to the service in the given location and paths,
     * passing the given URI.
     * @param requestUri URI to be passed to the request
     * @param location Location of the service
     * @param paths Paths (each path is like a directory name) to be appended
     *              to the service location in order to call the required method
     * @return JSON object containing the response
     */
    public JsonObject postRequestCallCrawler(URI requestUri, String location, String... paths);
    
    /**
     * Submits a GET request to the service in the given location and paths,
     * passing the given String (ID of the crawler).
     * @param requestCrawlerID String, containing the crawler ID, to be passed to the request
     * @param location Location of the service
     * @param paths Paths (each path is like a directory name) to be appended
     *              to the service location in order to call the required method
     * @return JSON object containing the response
     */
    public JsonObject getRequestCrawlerDetails(String requestCrawlerID, String location, String... paths);
}
