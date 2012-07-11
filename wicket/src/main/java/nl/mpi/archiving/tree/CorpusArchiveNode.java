package nl.mpi.archiving.tree;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface CorpusArchiveNode extends ArchiveNode {

    int getNodeId();
    
    String getName();
}
