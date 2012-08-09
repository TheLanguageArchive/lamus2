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

import java.util.Arrays;
import java.util.List;
import nl.mpi.archiving.tree.GenericTreeNode;
import nl.mpi.corpusstructure.CorpusStructureDB;
import nl.mpi.corpusstructure.Node;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CorpusStructureArchiveNode implements GenericTreeNode {

    private final CorpusStructureDB csdb;
    private final Node node;
    private final CorpusStructureArchiveNode parentNode;
    private List<String> descendantIds;

    public CorpusStructureArchiveNode(CorpusStructureDB csdb, String nodeId, CorpusStructureArchiveNode parentNode) {
	this(csdb, csdb.getNode(nodeId), parentNode);
    }

    public CorpusStructureArchiveNode(CorpusStructureDB csdb, Node node, CorpusStructureArchiveNode parentNode) {
	this.csdb = csdb;
	this.node = node;
	this.parentNode = parentNode;
    }

    private synchronized List<String> getDescendantIds() {
	if (descendantIds == null) {
	    descendantIds = Arrays.asList(csdb.getSubnodes(node.getNodeId()));

	}
	return descendantIds;
    }

    @Override
    public CorpusStructureArchiveNode getChild(int index) {
	return new CorpusStructureArchiveNode(csdb, getDescendantIds().get(index), this);
    }

    @Override
    public int getChildCount() {
	return descendantIds.size();
    }

    @Override
    public int getIndexOfChild(GenericTreeNode child) {
	return getDescendantIds().indexOf(((CorpusStructureArchiveNode) child).getNodeId());
    }

    @Override
    public CorpusStructureArchiveNode getParent() {
	return parentNode;
    }

    public String getNodeId() {
	return node.getNodeId();
    }

    @Override
    public String toString() {
	return node.toString();
    }
}
