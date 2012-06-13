/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.archivetree.wicket;

import imdibrowser.treeviewer.MyPopupMenu;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import mpi.bcapplet.DynamicExpandableTreeNode;
import mpi.bcapplet.btypes.NodeInfo;
import mpi.bcapplet.btypes.NodeType;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.markup.html.tree.ITreeStateListener;

/**
 *
 * @author jeafer
 */
public class HomePage extends TreePage {

    private static final long serialVersionUID = 1L;
    private String openpath = null; //the tree path that should be opened on initialisation
    // of the applet
    private String welcome_url = null; // the page to show if the applet is loaded
    private String[] tokens = null; // possible tokens that will be highlighted , multiple values can be separated by two semicolons e.g. <value1>;;<value2>
    private MyPopupMenu menu = null;
    private String viewcontroller_url = "http://corpus1/ds/imdi_browser/viewcontroller";
    private String showNodeInfos = "true";
    private boolean lastActionIsDefault = false;
    private boolean isopenpathvalid = false;
    WebMarkupContainer tableContainer;
    private WebMarkupContainer tablePanel;
    private final Tree myTree;

    
    public HomePage() {
        init();
        
        tableContainer = new WebMarkupContainer("tableContainer");
        tableContainer.setOutputMarkupId(true);
        tableContainer.setMarkupId("tableContainer");
        add(tableContainer);

        // Empty placeholder for table panel until a node is selected
        tablePanel = new WebMarkupContainer("tablePanel");
        tableContainer.add(tablePanel);

        

        myTree = new Tree("remoteTree", createRemoteTree());
        add(myTree);
        myTree.getTreeState().addTreeStateListener(new ITreeStateListener() {

            @Override
            public void allNodesCollapsed() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void allNodesExpanded() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void nodeCollapsed(Object node) {
                DynamicExpandableTreeNode parent = (DynamicExpandableTreeNode) node;
                parent.removeAllChildren();
            }

            @Override
            public void nodeExpanded(Object node) {
                DynamicExpandableTreeNode parent = (DynamicExpandableTreeNode) node;
                expandNode(parent);
            }

            @Override
            public void nodeSelected(Object node) {
            }

            @Override
            public void nodeUnselected(Object node) {
            }
        });
        myTree.getTreeState().collapseAll();
    }

    private void init() {
        System.out.println("MyTreeViewer:init() version 2");
        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    String s = rootNode.toString();
                    try {
                        if (s != null && !s.equals("")) {
                            rootNodeId = s;
                            System.out.println("rootnode parameter = " + rootNodeId);
                        } else {
                            rootNodeId = "MPI0#";
                        }
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                        System.err.println("You must give an integer as parameter");
                    }
                    tokens[0] = "&token=";
                    String tokenValue = tokens[0];
                    if (tokenValue != null) {
                        //tokens = tokenValue.split(";;");
                        System.out.println("token parameter = " + tokens);
                    } else {
                        System.out.println("No token parameter specified");
                    }

                    s = "true";
                    if (s != null && s.equalsIgnoreCase("true")) {
                        lastActionIsDefault = true;
                        System.out.println("lastactiondefault parameter = " + s);
                    } else {
                        System.out.println("No lastactiondefault parameter specified");
                    }

                    s = "welcome";
                    if (s != null) {
                        String welcome = s;
                        System.out.println("welcome parameter = " + welcome);
                        String prefix;
                        int lastQindex = getPath().toString().lastIndexOf('?');
                        int lastSindex = getPath().toString().lastIndexOf('/');
                        if (lastQindex != -1 && lastQindex < lastSindex) {
                            prefix = getPath().toString().substring(0, lastQindex + 1);
                            prefix = prefix.substring(0, prefix.lastIndexOf('/') + 1);
                        } else {
                            prefix = getPath().toString().substring(0, lastSindex + 1);
                        }
                        welcome_url = prefix + welcome;
                    } else {
                        System.out.println("No welcome parameter specified");
                    }
                    expandNode(rootNode); //Always expand root otherwise openPath doesn't work and normally we show the first tree level expanded anyway
                    s = "true";
                    if (s != null) {
                        showNodeInfos = s;
                    } else {
                        showNodeInfos = "notFound!";
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("MyTreeViewer:init(): didn't successfully complete");
        }
        //now at the end of initialise we see what file we have to show as welcome file
        // 0) If openpath != null -> show the content of the selected node
        // 1) If there is an info file of the topnode we show this one 
        // 2) if there is no info file of the topnode and 
        //    if we specified a file as welcome parameter of the applet, we 
        //    show this one
        // 3) we show the content of the top node
        //
        try {
            boolean displayed = false;
            String cmd = "";
            if (!isopenpathvalid) {
                System.err.println("Invalid OpenPath");
                String invalidopenpathUrl = viewcontroller_url + "?request=invalidopenpath";
                displayed = true;
            } else if (openpath == null || openpath.equals("")) {
                displayed = showWelcomeInfoFile((DefaultMutableTreeNode) rootNode);
                if (!displayed) {
                    if (welcome_url != null) {
                        cmd = welcome_url;
                        displayed = true;
                    } else {
                        NodeInfo ni = (NodeInfo) rootNode.getUserObject();
                        displayed = showNodeContent(ni);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("problem showing description " + e);
        }
    }

    /**
     * This Method gets a NodeInfo Object by querying the server for the given
     * nodeid
     *
     * @param nodeId the nodeid for which the method should look
     * @return NodeInfo the Object read from the server or null
     */
    @Override
    public NodeInfo getNodeInfo(String nodeId) {
        NodeInfo ni = null;
        try {
            URL gniurl = new URL(viewcontroller_url
                    + "?request=nodeinfo&nodeid=" + URLEncoder.encode(nodeId, "UTF-8") + "&token=");
            URLConnection gniurlcon = gniurl.openConnection();
            gniurlcon.setUseCaches(false);
            ObjectInputStream inputFromServlet = new ObjectInputStream(gniurlcon.getInputStream());
            ni = (NodeInfo) inputFromServlet.readObject();
            inputFromServlet.close();
            if (ni == null) {
                System.err.println("got a null object for nodeid " + nodeId);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return ni;
    }

//    @Override
//    public void collapseNode(DynamicExpandableTreeNode parent) {
//               setWaitCursor(true);
//        try {
//            long start = System.currentTimeMillis();
//            long urlTime = 0;
//            long addObjectTime = 0;
//            if (!parent.isLeaf()) {
//                //if (!parent.isAlreadyExpanded()) {
//                parent.setAlreadyExpanded(true);
//                try {
//                    NodeInfo ni = (NodeInfo) parent.getUserObject();
//                    if (ni != null) {
//                        Map resourceCountMap = new HashMap();
//                        long start2 = System.currentTimeMillis();
//                        URL gcurl = new URL(viewcontroller_url + "?request=childs&nodeid=" + URLEncoder.encode(ni.getNodeId(), "UTF-8"));
//                        System.out.println("request=" + gcurl);
//
//                        URLConnection gcurlcon = gcurl.openConnection();
//                        gcurlcon.setUseCaches(false);
//                        ObjectInputStream inputFromServlet = new ObjectInputStream(gcurlcon.getInputStream());
//                        NodeInfo[] childs = (NodeInfo[]) inputFromServlet.readObject();
//                        inputFromServlet.close();
//                        urlTime = System.currentTimeMillis() - start2;
//
//                        System.out.println("Start add " + childs.length + " objects...");
//                        start2 = System.currentTimeMillis();
//                        boolean showResources = "true".equalsIgnoreCase(showNodeInfos);
//                        for (int i = 0; i < childs.length; i++) {
//                            if (childs[i] != null) {
//                                if ((childs[i].getNodeType() == NodeType.getCorpus()) || (childs[i].getNodeType() == NodeType.getSession())) {
//                                    //childs[i].setShowSessionsAndResources(showResources);
//                                    DefaultMutableTreeNode addedNode = removeObject(parent, childs[i], true);
//                                    if (showResources) {
//                                        resourceCountMap.put(childs[i].getNodeId(), addedNode);
//                                    }
//                                } else {
//                                    if (childs[i].getNodeType() == NodeType.getInfo()) {
//                                      //  hasInfoChild = true;
//                                    }
//                                    childs[i].setShowSessionsAndResources(false);//no children so don't show empty brackets.
//                                    removeObject(parent, childs[i], false); //Nodes have no children so do not try to expand
//                                }
//                            }
//                        }
//                        addObjectTime = System.currentTimeMillis() - start2;
//                        //addResources(resourceCountMap, ni, hasInfoChild, parent);
//                    } else {
//                        System.err.println("getchilds called for a nonexisting node - this should not happen");
//                    }
//                } catch (IOException ioe) {
//                    ioe.printStackTrace();
//                } catch (ClassNotFoundException cnfe) {
//                    cnfe.printStackTrace();
//                }
//            }
//            if (urlTime != 0 && addObjectTime != 0) {
//                System.out.println("End expand took:" + (System.currentTimeMillis() - start) + " ms (" + urlTime + " ms getting Data, " + addObjectTime + " ms adding children).");
//            }
//        } finally {
//            //setWaitCursor(false);
//        }
//    }

    /**
     * Returns the tree on this pages. This is used to collapse, expand the tree
     * and to switch the rootless mode.
     *
     * @return Tree instance on this page
     */
    @Override
    public void expandNode(DynamicExpandableTreeNode parent) {
        setWaitCursor(true);
        try {
            long start = System.currentTimeMillis();
            long urlTime = 0;
            long addObjectTime = 0;
            if (!parent.isLeaf()) {
                //if (!parent.isAlreadyExpanded()) {
                parent.setAlreadyExpanded(true);
                try {
                    NodeInfo ni = (NodeInfo) parent.getUserObject();
                    if (ni != null) {
                        Map resourceCountMap = new HashMap();
                        boolean hasInfoChild = false;
                        long start2 = System.currentTimeMillis();
                        URL gcurl = new URL(viewcontroller_url + "?request=childs&nodeid=" + URLEncoder.encode(ni.getNodeId(), "UTF-8"));
                        System.out.println("request=" + gcurl);

                        URLConnection gcurlcon = gcurl.openConnection();
                        gcurlcon.setUseCaches(false);
                        ObjectInputStream inputFromServlet = new ObjectInputStream(gcurlcon.getInputStream());
                        NodeInfo[] childs = (NodeInfo[]) inputFromServlet.readObject();
                        inputFromServlet.close();
                        urlTime = System.currentTimeMillis() - start2;

                        System.out.println("Start add " + childs.length + " objects...");
                        start2 = System.currentTimeMillis();
                        boolean showResources = "true".equalsIgnoreCase(showNodeInfos);
                        for (int i = 0; i < childs.length; i++) {
                            if (childs[i] != null) {
                                boolean scrollPathToVisible = i == 0; //scroll to first node only, do not set this to true for all nodes that is a major performance bottleneck
                                if ((childs[i].getNodeType() == NodeType.getCorpus()) || (childs[i].getNodeType() == NodeType.getSession())) {
                                    childs[i].setShowSessionsAndResources(showResources);
                                    DefaultMutableTreeNode addedNode = addObject(parent, childs[i], true, scrollPathToVisible);
                                    if (showResources) {
                                        resourceCountMap.put(childs[i].getNodeId(), addedNode);
                                    }
                                } else {
                                    if (childs[i].getNodeType() == NodeType.getInfo()) {
                                        hasInfoChild = true;
                                    }
                                    childs[i].setShowSessionsAndResources(false);//no children so don't show empty brackets.
                                    addObject(parent, childs[i], false, scrollPathToVisible); //Nodes have no children so do not try to expand
                                }
                            }
                        }
                        addObjectTime = System.currentTimeMillis() - start2;
                        addResources(resourceCountMap, ni, hasInfoChild, parent);
                    } else {
                        System.err.println("getchilds called for a nonexisting node - this should not happen");
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (ClassNotFoundException cnfe) {
                    cnfe.printStackTrace();
                }
            }
            if (urlTime != 0 && addObjectTime != 0) {
                System.out.println("End expand took:" + (System.currentTimeMillis() - start) + " ms (" + urlTime + " ms getting Data, " + addObjectTime + " ms adding children).");
            }
        } finally {
            //setWaitCursor(false);
        }
    }

    /**
     * Calculates resource count (the [4] after a node in the tree). Queries the
     * server in a separate thread so the tree stays responsive. Calculating the
     * counts is expensive.
     *
     * @param resourceCountMap, map of nodeId (String) -> node
     * (DefaultMutableTreeNode), the nodes are the children of the parent that
     * can have resources.
     * @param parent
     */
    private void addResources(final Map resourceCountMap, final NodeInfo parent, final boolean hasInfoChild, final DynamicExpandableTreeNode treeparent) {
        if (resourceCountMap.size() > 0) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    URL url = null;
                    try {
                        if (parent.getResources() == 0 || (resourceCountMap.size() == 1 && !hasInfoChild)) {
                            //Parent has 0 resources so just set all children to 0 as well.
                            //only one child so just copy the resourceCount of the parent (but just if there isn't as well an info child, in which case the resource count of the session/corpus child is different).
                            for (Iterator iter = resourceCountMap.values().iterator(); iter.hasNext();) {
                                DefaultMutableTreeNode node = (DefaultMutableTreeNode) iter.next();
                                ((NodeInfo) node.getUserObject()).setResources(parent.getResources());
                            }
                        } else { // Query the resource count from server
                            url = new URL(viewcontroller_url + "?request=resourceCount&nodeid="
                                    + URLEncoder.encode(parent.getNodeId(), "UTF-8"));
                            URLConnection conn = url.openConnection();
                            conn.setUseCaches(false);
                            ObjectInputStream inputFromServlet = new ObjectInputStream(conn.getInputStream());
                            NodeInfo[] children = (NodeInfo[]) inputFromServlet.readObject();
                            inputFromServlet.close();
                            for (int i = 0; i < children.length; i++) {
                                DefaultMutableTreeNode node = (DefaultMutableTreeNode) resourceCountMap.get(children[i].getNodeId());
                                if (node != null) { //not all nodes have to be in, for instance resource node never need to be updated
                                    ((NodeInfo) node.getUserObject()).setResources(children[i].getResources());
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("exception on request=" + url + ". Exception=" + e);
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    }


    /*
     * show a description in the body frame if there is a info file connected
     * this is taken as the description if no info file is available it takes
     * the content of the description field.
     */
    private boolean showWelcomeInfoFile(DefaultMutableTreeNode node) {
        String cmd = null;
        try {
            int nrc = rootNode.getChildCount();
            for (int i = 0; i < nrc; i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                NodeInfo ni = (NodeInfo) child.getUserObject();
                cmd = viewcontroller_url + "?request=view&nodeid=";
                if (ni.getNodeType() == NodeType.getInfo()
                        && ni.getName().toUpperCase().indexOf("WELCOME") != -1) {
                    cmd += URLEncoder.encode(ni.getNodeId(), "UTF-8");
                    System.out.println("MyTreeViewer: executing " + cmd);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("showNodeDescription: " + e);
        }
        return false;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
        NodeInfo ni = (NodeInfo) node.getUserObject();
        System.out.println("MyTreeViewer:TreeSelectionEvent " + e.getPath() + " " + ni.getNodeId());
        if (lastActionIsDefault && menu.getLastActionURL() != null) {
            menu.doLastAction(ni.getNodeId(), "body");
        } else {
            showNodeContent(ni);
        }
    }

    private boolean showNodeContent(NodeInfo ni) {
        if (ni.getNodeType() != NodeType.getMediaRes()
                && ni.getNodeType() != NodeType.getWrittenRes()
                && ni.getNodeType() != NodeType.getUnknown()) {
            menu.doViewNode(ni.getNodeId(), "body", tokens);
            return true;
        } else {
            menu.doViewResource(ni.getNodeId(), "body");
        }
        return false;
    }

    @Override
    protected AbstractTree getTree() {
        //return myTree;
        return null;
    }

    @Override
    public void treeExpanded(TreeExpansionEvent tee) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent tee) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
