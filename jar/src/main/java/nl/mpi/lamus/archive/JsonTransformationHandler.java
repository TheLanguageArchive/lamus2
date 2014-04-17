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
package nl.mpi.lamus.archive;

import java.net.URISyntaxException;
import java.util.Collection;
import javax.json.JsonObject;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;

/**
 * Interface providing methods to handle transformations related with JSON objects.
 * @author guisil
 */
public interface JsonTransformationHandler {
    
    /**
     * Creates a JSON object from the given collection of WorkspaceNodeReplacement objects.
     * @param nodeReplacementCollection collection to transform
     * @return JSON object
     */
    public JsonObject createJsonObjectFromNodeReplacementCollection(Collection<WorkspaceNodeReplacement> nodeReplacementCollection);
    
    /**
     * Creates a collection of WorkspaceNodeReplacement objects from the given JSON object.
     * @param jsonObject JSON object to transform
     * @return collection of WorkspaceNodeReplacement objects
     */
    public Collection<WorkspaceNodeReplacement> createNodeReplacementCollectionFromJsonObject(JsonObject jsonObject) throws URISyntaxException;
}
