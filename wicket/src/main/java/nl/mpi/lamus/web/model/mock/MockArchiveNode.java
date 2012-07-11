package nl.mpi.lamus.web.model.mock;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import nl.mpi.archiving.tree.ArchiveNode;
import nl.mpi.archiving.tree.CorpusArchiveNode;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MockArchiveNode implements CorpusArchiveNode, Serializable {

    private ArchiveNode parent;
    private List<ArchiveNode> children = Collections.emptyList();
    private String name = "";
    private int nodeId;

    public void setChildren(List<ArchiveNode> children) {
	this.children = children;
    }

    @Override
    public ArchiveNode getChild(int index) {
	return children.get(index);
    }

    @Override
    public int getChildCount() {
	return children.size();
    }

    @Override
    public int getIndexOfChild(ArchiveNode child) {
	return children.indexOf(child);
    }

    public void setName(String name) {
	this.name = name;
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public ArchiveNode getParent() {
	return parent;
    }

    public void setParent(ArchiveNode parent) {
	this.parent = parent;
    }

    @Override
    public int getNodeId() {
	return nodeId;
    }

    public void setNodeId(int nodeId) {
	this.nodeId = nodeId;
    }

    @Override
    public String toString() {
	return name;
    }
}
