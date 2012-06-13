/*
 * TreePage.java
 *
 * Created on March 21, 2012, 1:47 PM
 */
package nl.mpi.archivetree.wicket;

import java.awt.Cursor;
import java.io.Serializable;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import mpi.bcapplet.DynamicExpandableTreeNode;
import mpi.bcapplet.btypes.NodeInfo;
import mpi.bcapplet.btypes.NodeType;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public abstract class TreePage extends WebPage implements TreeSelectionListener, TreeExpansionListener, Serializable {

    public TreePage() {
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        add(new ButtonPage("buttonpage"));
        add(new HeaderPanel("headerpanel", "Welcome To Wicket"));
    }

    /**
     * Returns the tree on this pages. This is used to collapse, expand the tree
     * and to switch the rootless mode.
     *
     * @return Tree instance on this page
     */
    protected abstract AbstractTree getTree();

    protected IModel<TreeModel> createRemoteTree() {

        final Model<DefaultTreeModel> defaultTreeModelModel = new Model(loadTree());

        IModel<TreeModel> remoteTreeModel = new IModel<TreeModel>() {

            @Override
            public TreeModel getObject() {
                return defaultTreeModelModel.getObject();
            }

            @Override
            public void setObject(TreeModel object) {
                defaultTreeModelModel.setObject((DefaultTreeModel) object);
            }

            @Override
            public void detach() {
                defaultTreeModelModel.detach();
            }
        };

        return remoteTreeModel;
    }

    public DefaultTreeModel loadTree() {
        rootNode = new DynamicExpandableTreeNode(getNodeInfo(rootNodeId));
        TreeModel model = new DefaultTreeModel(rootNode) {

            protected NodeInfo nodeInf(DefaultMutableTreeNode node) {
                final TreePage treeNode = (TreePage) node.getUserObject();
                return treeNode.getNodeInfo(rootNodeId);
            }
        };
        return (DefaultTreeModel) model;
    }

    /**
     * This Method gets a NodeInfo Object by querying the server for the given
     * nodeid needs to be overwritten by classes which inherit from this class
     *
     * @param nodeId the nodeid for which the method should look
     * @return NodeInfo the Object read from the server or null
     */
    public NodeInfo getNodeInfo(String nodeId) {
        NodeInfo ni = new NodeInfo(nodeId, NodeType.getCorpus(), "TopNode", "Demo");
        return ni;
    }
    //protected String rootNodeId = "MPI239098#";  // given Parameter "rootnodeid" overrules this setting
    protected String rootNodeId = "MPI0#";
    protected String viewcontroller_url; //moved to MyTreeViewer class
    protected String sessionid;
    protected DynamicExpandableTreeNode rootNode;
    protected DefaultTreeModel treeModel;
    protected JTree tree;
    private static final long serialVersionUID = 1L;
    private Cursor oldCursor;

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        System.out.println("TreeViewer:TreeSelectionEvent " + e.getPath());
    }

    public abstract void expandNode(DynamicExpandableTreeNode parent);

    //public abstract void collapseNode(DynamicExpandableTreeNode parent);
//    public DefaultMutableTreeNode addToTree(DefaultMutableTreeNode parent, NodeInfo child, boolean canHaveChildren, boolean shouldBeVisible) {
//        Enumeration allchildren = parent.children();
//        while (allchildren != null) {
//            Object childNode = allchildren.nextElement();
//        }
//
//        return null;
//    }

    /**
     * adds the node which holds the given object to the given parent when
     * number_of_childs is bigger then 0 a dummy_subnode is created for
     * visualize that there are subchilds if shouldBeVisible is true, the tree
     * is expanded (as long as it isn't already) after adding the node
     */
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, NodeInfo child, boolean canHaveChildren, boolean shouldBeVisible) {
        DynamicExpandableTreeNode childNode = new DynamicExpandableTreeNode(child);
        if (parent == null) {
            parent = rootNode;
        }

        treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
        if (!canHaveChildren) {
            childNode.setAlreadyExpanded(true);
        }
        if (shouldBeVisible) {
            tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }

//    public DefaultMutableTreeNode removeObject(DefaultMutableTreeNode parent, NodeInfo child, boolean collapse) {
//        DynamicExpandableTreeNode childNode = new DynamicExpandableTreeNode(child);
//        if (parent == null) {
//            parent = rootNode;
//        }
//        if (collapse) {
//            treeModel.removeNodeFromParent(childNode);
//        } else {
//            childNode.setAlreadyExpanded(false);
//        }
//        return childNode;
//    }

    /**
     * opens the tree at the given path and selects the node in the tree
     *
     * @param nodeIdPath path of nodeIds from the root node to the node which
     * should be selected
     * @return success or failure
     */
//    public boolean browseToNodeWithNodePath(String nodeIdPath) {
//        DynamicExpandableTreeNode actNode = (DynamicExpandableTreeNode) treeModel.getRoot();
//        String rootNodeId = ((NodeInfo) actNode.getUserObject()).getNodeId();
//        if (nodeIdPath.startsWith("/")) {
//            nodeIdPath = nodeIdPath.substring(1);
//        }
//        String[] nodeIds = nodeIdPath.split("\\/");
//        //the nodeIdPath is the path from the root of the corpus
//        //this does not need to coincide with the root of the tree(model)
//        // therefore we first search the root of the tree(model) in the nodeIdPath
//        //
//        int start = -1;
//        for (int j = 0; j < nodeIds.length; j++) { //
//            if (nodeIds[j].equals(rootNodeId)) {
//                start = j;
//                break;
//            }
//        }
//        if (start == -1) {
//            System.err.println("TreeViewer:browseToNodeWithNodePath: rootnode " + rootNodeId
//                    + " not found in openpath parameter " + nodeIdPath);
//            return false;
//        }
//        for (int i = start + 1; i < nodeIds.length; i++) {
//            //System.out.println("search in path: "+nodeIds[i]);
//            boolean found = false;
//            Enumeration e = actNode.children();
//            while ((e.hasMoreElements()) && (!found)) {
//                DynamicExpandableTreeNode node = (DynamicExpandableTreeNode) e.nextElement();
//                if (node != null) {
//                    Object uo = node.getUserObject();
//                    if (uo instanceof NodeInfo) {
//                        NodeInfo ni = (NodeInfo) uo;
//                        if (ni != null && ni.getNodeId().equals(nodeIds[i])) {
//                            //System.out.println("found in path: "+ni.getNodeId());
//                            found = true;
//                            actNode = node;
//                            if ((i + 1) < nodeIds.length) {
//                                expandNode(node);
//                            } else {
//                                TreePath tp = new TreePath(node.getPath());
//                                tree.setSelectionPath(tp);
//                                tree.scrollPathToVisible(tp);
//                            }
//                        }
//                    }
//                }
//            }
//            if (!found) {
//                System.err.println("did not find node with Id " + nodeIds[i]);
//                return false;
//            }
//        }//for
//        return true;
//    }
    public void setWaitCursor(boolean waiting) { //Cannot get it to work under Safari. 
        if (waiting) {
//            Cursor cursor = getCursor();
//            if (cursor.getType() != Cursor.WAIT_CURSOR) {
//                oldCursor = cursor;
//                setCursor(waitCursor);
//            }
//        } else {
//             setCursor(oldCursor);
        }
    }
}
