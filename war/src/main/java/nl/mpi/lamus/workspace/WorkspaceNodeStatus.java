/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public enum WorkspaceNodeStatus {

    /**
     * ISCOPY -> (IMDI file-) node is unchanged copy from archive. Set by
     * DataMoverIn for all .imdi nodes unless they are marked as not onsite in
     * corpusstructure AODB and/or are not in AODB. IMDIWorkSpaceDB.newFreeNode
     * only calls super.newFreeNode for ISCOPY nodes. Nothing else (yet) ever
     * tests for ISCOPY state.
     */
    NODE_ISCOPY,
    /**
     * UPLOADED -> node was uploaded, has no file conterpart in Archive. Both
     * IMDI and non-IMDI (resource, info) files can have this state. Set for all
     * files which are added to the workspace via the Upload class. Nothing
     * (yet) tests for this state, apart from a conditional log message in
     * DataMoverOut which is suppressed for uploaded files.
     */
    NODE_UPLOADED, // needed for newFreeNode calls in Upload (controller)
    /**
     * MODIFIED -> (IMDI-) node was modified, IMDI file should be updated. Only
     * set and tested for in WorkSpaceDBImpl, no direct access from other
     * classes. Was meant to flag which IMDI files have to be copied back to the
     * archive, but files are often changed without setting the flag. As
     * DataMoverOut can rearrange most physical locations during ingest, the
     * current LAMUS updates and writes ALL IMDI files from the workspace. Note:
     * Only modify iscopy/uploaded/created, never deleted/virtual/external
     * nodes.
     *
     * @deprecated do not set nodes to this state as that overwrites other state
     * info
     */
    NODE_MODIFIED, // deprecated!
    /**
     * CREATED -> (IMDI file-!) node was created without physical file
     * counterpart in WS or Archive. Created via WorkSpaceChangeController
     * (create or clone imdi node). Never tested for (yet) outside
     * IMDIWorkSpaceDB.newFreeNode itself.
     */
    NODE_CREATED, // needed for newFreeNode calls in WorkSpaceChangeController
    /**
     * DELETED -> node is deleted from DB, should later be deleted from archive.
     * Set if a node is deleted (possibly recursively) via WorkSpaceDBImpl.
     * Tested for by DataMoverOut, RewriteForIngest, UnlinkedNodesUtil and of
     * course WorkSpaceDBImpl, usually to skip over deleted nodes. The only way
     * to find a list of deleted nodes is to scan the getAllWorkSpaceNodes
     * result.
     */
    NODE_DELETED,
    /**
     * VIRTUAL -> node is virtual, just a placeholder for a file in the archive.
     * IMDIWorkSpaceDB.newFreeNode only calls super.newFreeNode for VIRTUAL
     * nodes.
     */
    NODE_VIRTUAL,
    /**
     * EXTERNAL -> node is external, points to some URL in the outside world.
     * IMDIWorkSpaceDB.newFreeNode only calls super.newFreeNode for EXTERNAL
     * nodes.
     */
    NODE_EXTERNAL, // needed for newFreeNode calls in WorkSpaceViewController
    /**
     * REPLACED -> node got replaced by another node. Replaced nodes are similar
     * to deleted nodes but hold information used for version-linking to the new
     * (replacing) node from the replaced node. Only local archive resource
     * (non-IMDI) nodes will support REPLACED state in the beginning. In other
     * words: No versioning for UPLOADED or EXTERNAL or IMDI, only for VIRTUAL.
     * The node title (!) is used to store the WS nodeid of the replacing node
     * (resource nodes have no title otherwise, only a name).
     */
    NODE_REPLACED;
}
