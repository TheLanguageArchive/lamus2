/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.importing;

import java.io.IOException;
import java.net.MalformedURLException;
import nl.mpi.corpusstructure.UnknownNodeException;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.workspace.exception.TypeCheckerException;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.util.OurURL;

/**
 * Helper interface that provides methods for retrieving information
 * regarding a given node.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface NodeDataRetriever {
    
    /**
     * Retrieves the MetadataDocument object for the given archive node.
     * @param nodeArchiveID ID of the node in the archive
     * @return MetadataDocument object corresponding to the node
     * @throws IOException if there is some I/O issue retrieving the document
     * @throws MetadataException if there is some metadata issue retrieving the document
     * @throws UnknownNodeException if there is some issue retrieving the node from the database
     */
    public MetadataDocument getArchiveNodeMetadataDocument(int nodeArchiveID)
            throws IOException, MetadataException, UnknownNodeException;

    /**
     * Retrieves the URL of a resource, given its reference from the parent file
     * @param resourceReference Reference to the resource, from the parent metadata file
     * @return URL for the resource
     * @throws MalformedURLException if the Reference doesn't contain a handle or a valid URL
     * @throws UnknownNodeException if the Reference contains a handle, but the node can't be found in the database
     */
    public OurURL getResourceURL(Reference resourceReference) throws MalformedURLException, UnknownNodeException;
    
    /**
     * Decides if a resource should be typechecked (depending on its location and size).
     * @param resourceReference Reference to the resource, from the parent metadata file
     * @param resourceURLWithContext URL with the actual location of the resource
     * @param nodeArchiveID ID of the resource in the archive
     * @return true if resource should be typechecked
     */
    public boolean shouldResourceBeTypechecked(Reference resourceReference, OurURL resourceURLWithContext, int nodeArchiveID);
    
    /**
     * Invokes typechecking for the given resource.
     * @param nodeArchiveID ID of the resource in the archive
     * @param resourceReference Reference to the resource, from the parent metadata file
     * @param resourceURL URL of the resource
     * @param resourceURLWithContext URL with the actual location of the resource
     * @return results of the typechecker
     * @throws TypeCheckerException if the typechecker runs into some problem
     */
    public TypecheckedResults getResourceFileChecked(int nodeArchiveID, Reference resourceReference,
            OurURL resourceURL, OurURL resourceURLWithContext) throws TypeCheckerException;
    
    /**
     * Verifies the results of the typechecker.
     * @param resourceURL URL of the resource
     * @param resourceReference Reference to the resource, from the parent metadata file
     * @param typecheckedResults Object containing the result of the typechecker
     */
    public void verifyTypecheckedResults(OurURL resourceURL, Reference resourceReference, TypecheckedResults typecheckedResults);
}