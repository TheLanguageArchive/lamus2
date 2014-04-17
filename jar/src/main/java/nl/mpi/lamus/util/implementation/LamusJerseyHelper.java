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
package nl.mpi.lamus.util.implementation;

import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import nl.mpi.lamus.util.JerseyHelper;
import org.springframework.stereotype.Component;

/**
 * @see JerseyHelper
 * @author guisil
 */
@Component
public class LamusJerseyHelper implements JerseyHelper {

    /**
     * @see JerseyHelper#postRequest(javax.json.JsonObject, java.lang.String, java.lang.String[])
     */
    @Override
    public JsonObject postRequest(JsonObject requestJsonObject, String location, String... paths) {
        Client client = ClientBuilder.newClient();
        
        WebTarget finalTarget = client.target(location);
        for(String path : paths) {
            finalTarget = finalTarget.path(path);
        }
        
        Invocation.Builder invocationBuilder = finalTarget.request(MediaType.APPLICATION_JSON);
        
        Entity<JsonObject> jsonObjectEntity = Entity.entity(requestJsonObject, MediaType.APPLICATION_JSON);
        
//        invocationBuilder.accept(MediaType.APPLICATION_JSON)
        
        JsonObject responseJsonObject = invocationBuilder.post(jsonObjectEntity, JsonObject.class);
        
        return responseJsonObject;
        
        
        //TODO how to unit test this? most of these objects from javax.ws.rs cannot be mocked
    }
}
