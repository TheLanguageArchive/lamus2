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

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.exception.TypeCheckerException;
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
     * Retrieves the archive URL (location) for the given URI (probably an archive handle)
     * @param nodeArchiveURI URI of the node in the archive
     * @return URL of the node in the archive
     */
    public URL getNodeArchiveURL(URI nodeArchiveURI) throws NodeNotFoundException;
    
    /**
     * Decides if a resource should be typechecked (depending on its location and size).
     * @param resourceReference Reference to the resource, from the parent metadata file
     * @param resourceFile File object referring to the actual location of the resource
     * @return true if resource should be typechecked
     */
    public boolean shouldResourceBeTypechecked(Reference resourceReference, File resourceFile);
    
    /**
     * Invokes typechecking for the given resource.
     * @param resourceURL URL of the resource
     * @return results of the typechecker
     */
    public TypecheckedResults triggerResourceFileCheck(OurURL resourceURL) throws TypeCheckerException;
    
    /**
     * Invokes typechecking for the given resource.
     * @param resourceInputStream InputStream of the resource
     * @param resourceFilename Filename of the resource
     * @return results of the typechecker
     */
    public TypecheckedResults triggerResourceFileCheck(InputStream resourceInputStream, String resourceFilename) throws TypeCheckerException;
    
    /**
     * Verifies the results of the typechecker.
     * @param resourceFile File object referring to the resource
     * @param resourceReference Reference to the resource, from the parent metadata file
     * @param typecheckedResults Object containing the result of the typechecker
     */
    public void verifyTypecheckedResults(File resourceFile, Reference resourceReference, TypecheckedResults typecheckedResults);
    
    /**
     * @param urlToCheckInConfiguration URL of the archive branch for which the configuration has to be checked (top node of the workspace)
     * @param message Message from the typechecker
     * @return true if the previously checked resource is archivable, according to the configuration
     */
    public boolean isCheckedResourceArchivable(URL urlToCheckInConfiguration, StringBuilder message);
    
    /**
     * @param archiveNodeUri URI of the node in the archive
     * @return true if the given node should be protected from changes
     */
    public boolean isNodeToBeProtected(URI archiveNodeUri);
}
