/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.archivetree.wicket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import mpi.bcapplet.btypes.NodeInfo;
import mpi.bcapplet.btypes.NodeType;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.model.IModel;

/**
 *
 * @author jeafer
 */
class Lamus2WicketTree extends Tree{
    public Lamus2WicketTree(String id, IModel<TreeModel> model) {
	super(id, model);
    }
    private ITreeState state;


    @Override
    protected ITreeState newTreeState() {
	ITreeState treeState = super.newTreeState();
	//treeState.setAllowSelectMultiple(true);
	return treeState;
    }

    /**
     * Returns the TreeState of this tree.
     * 
     * @return Tree state instance
     */
    @Override
    public ITreeState getTreeState() {
	if (state == null) {
	    state = newTreeState();

	    // add this object as listener of the state
	    state.addTreeStateListener(this);
	    // FIXME: Where should we remove the listener?
//	    if (getModel().getObject() instanceof Lamus2WicketTreeModel) {
//		state.addTreeStateListener((Lamus2WicketTreeModel) this.getModel().getObject());
//	    }
	}
	return state;
    }
}
