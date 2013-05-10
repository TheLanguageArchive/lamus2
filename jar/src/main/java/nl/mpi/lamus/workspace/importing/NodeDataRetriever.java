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
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface NodeDataRetriever {
    
    public MetadataDocument getArchiveNodeMetadataDocument(int nodeArchiveID)
            throws IOException, MetadataException, UnknownNodeException;

    public OurURL getResourceURL(Reference resourceReference) throws MalformedURLException, UnknownNodeException;
    
    public boolean shouldResourceBeTypechecked(Reference resourceReference, OurURL resourceURLWithContext, int nodeArchiveID);
    
    public TypecheckedResults getResourceFileChecked(int nodeArchiveID, Reference resourceReference,
            OurURL resourceURL, OurURL resourceURLWithContext) throws TypeCheckerException;
    
    public void verifyTypecheckedResults(OurURL resourceURL, Reference resourceReference, TypecheckedResults typecheckedResults);
}
