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
package nl.mpi.lamus.archive.implementation;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.TransformerException;
import net.handle.hdllib.HandleException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.handle.util.HandleManager;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.lamus.archive.ArchiveHandleHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.MetadataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see ArchiveHandleHelper
 * @author guisil
 */
@Component
public class LamusArchiveHandleHelper implements ArchiveHandleHelper {

    private final CorpusStructureProvider corpusStructureProvider;
    private final NodeResolver nodeResolver;
    private final HandleManager handleManager;
    private final WorkspaceDao workspaceDao;
    private final MetadataApiBridge metadataApiBridge;
    private final NodeUtil nodeUtil;
    private final HandleParser handleParser;
    
    @Autowired
    public LamusArchiveHandleHelper(
            CorpusStructureProvider provider, NodeResolver resolver,
            HandleManager hManager, WorkspaceDao wsDao,
            MetadataApiBridge mApiBridge, NodeUtil nUtil,
            HandleParser hdlParser) {
        corpusStructureProvider = provider;
        nodeResolver = resolver;
        handleManager = hManager;
        workspaceDao = wsDao;
        metadataApiBridge = mApiBridge;
        nodeUtil = nUtil;
        handleParser = hdlParser;
    }
    
    /**
     * @see ArchiveHandleHelper#getArchiveHandleForNode(java.net.URI)
     */
    @Override
    public URI getArchiveHandleForNode(URI nodeURI) throws NodeNotFoundException {
        
        CorpusNode node = corpusStructureProvider.getNode(nodeURI);
        if(node == null) {
            String message = "Node with URI '" + nodeURI + "' not found";
            throw new NodeNotFoundException(nodeURI, message);
        }
        
        return handleParser.prepareHandleWithHdlPrefix(nodeResolver.getPID(node));
    }

    /**
     * @see ArchiveHandleHelper#deleteArchiveHandle(nl.mpi.lamus.workspace.model.WorkspaceNode, java.net.URL)
     */
    @Override
    public void deleteArchiveHandle(WorkspaceNode node, URL currentLocation)
            throws HandleException, IOException, TransformerException, MetadataException {
        
        handleManager.deleteHandle(URI.create(node.getArchiveURI().getSchemeSpecificPart()));
        
        node.setArchiveURI(null);
        workspaceDao.updateNodeArchiveUri(node);
        
        if(nodeUtil.isNodeMetadata(node)) {
            metadataApiBridge.removeSelfHandleAndSaveDocument(currentLocation);
        }
    }
}
