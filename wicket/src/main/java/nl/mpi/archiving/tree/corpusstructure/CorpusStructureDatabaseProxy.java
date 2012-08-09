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
package nl.mpi.archiving.tree.corpusstructure;

import java.util.List;
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.CorpusNode;
import nl.mpi.corpusstructure.CorpusStructureDB;
import nl.mpi.corpusstructure.Node;
import nl.mpi.corpusstructure.UnknownNodeException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CorpusStructureDatabaseProxy implements CorpusStructureDB {

    private final CorpusStructureDBFactory csdbFactory;
    private transient CorpusStructureDB csdb;

    public CorpusStructureDatabaseProxy(CorpusStructureDBFactory csdbFactory) {
	this.csdbFactory = csdbFactory;
    }

    private synchronized CorpusStructureDB getCsdb() {
	if (csdb == null) {
	    csdb = csdbFactory.createCorpusStructureDB();
	}
	return csdb;
    }

    @Override
    public Node getNode(String nodeId) throws UnknownNodeException {
	return getCsdb().getNode(nodeId);
    }

    @Override
    public String[] getSubnodes(String nodeId) throws UnknownNodeException {
	return getCsdb().getSubnodes(nodeId);
    }

    @Override
    public ArchiveAccessContext getArchiveRoots() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CorpusNode getCorpusNode(String nodeId) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRootNodeId() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Node[] getChildrenNodes(String nodeId) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getParentNodes(String nodeId) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getDescendants(String nodeId, int nodeType, String format) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getDescendants(String nodeId, int nodeType, String[] formats) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getDescendants(String nodeId, int nodeType, String[] formats, String user, boolean onsite) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CorpusNode[] getDescendantResources(String nodeId, boolean onsiteOnly, String userToRead, String userToWrite) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getSessions(String nodeId) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getSessionsAndCatalogues(String nodeId) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getResourcesFromArchive(int nodeType, String[] formats, String user, boolean onlyAvailable, boolean onlyOnSite) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List getResourcesAccessInfo() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getCanonicalVPath(String nodeId) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getNamePath(String nodeId) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] translateToNamePath(String[] nodeids) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String resolveNameInAnnotationContext(String annotationNodeId, String name, String function) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String resolveNameInSessionContext(String sessionNodeId, String name, String function) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getCanonicalParent(String nodeId) throws UnknownNodeException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getAdminKey(String name) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getStatus() {
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
