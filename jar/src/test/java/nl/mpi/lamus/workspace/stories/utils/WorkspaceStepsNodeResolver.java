/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.stories.utils;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import nl.mpi.archiving.corpusstructure.adapter.AdapterUtils;
import nl.mpi.archiving.corpusstructure.core.service.BaseNodeResolver;
import nl.mpi.archiving.corpusstructure.adapter.CorpusStructureAPIAdapterNodeResolver;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.util.OurURL;

/**
 * NodeResolver for the local acceptance tests (stories),
 * based on {@link CorpusStructureAPIAdapterNodeResolver}
 * @author guisil
 */
public class WorkspaceStepsNodeResolver extends BaseNodeResolver implements Serializable  {
    
    private final ArchiveObjectsDB ao;

    public WorkspaceStepsNodeResolver(ArchiveObjectsDB ao) {
	this.ao = ao;
    }

    /**
     * Resolves a CorpusNode, supporting 'node:' URIs as returned by the corpus structure adapter
     *
     * @param node
     * @return if the provided node has a NodeURI with schema 'node', the url provided by {@link ArchiveObjectsDB#getObjectURL(java.lang.String, int)
     * } for the encapsulated node ID. This method will specifically return the 'file:/' URL of the node, since it's meant to be used for the local archive created for the stories
     */
    @Override
    public URL getUrl(CorpusNode node) {
	final URI nodeURI = node.getNodeURI();
	if (nodeURI.getScheme().equalsIgnoreCase("node")) {
	    final OurURL objectURL = ao.getObjectURL(AdapterUtils.toNodeIdString(nodeURI), ArchiveAccessContext.FILE_UX_URL);
	    if (objectURL == null) {
		return null;
	    } else {
		return objectURL.toURL();
	    }
	} else {
	    try {
		return nodeURI.toURL();
	    } catch (MalformedURLException ex) {
		//log error
		return null;
	    }
	}
    }
}
