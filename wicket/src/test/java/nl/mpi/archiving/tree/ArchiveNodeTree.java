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
package nl.mpi.archiving.tree;

import javax.swing.JFrame;
import javax.swing.JTree;
import nl.mpi.archiving.tree.swingtree.GenericTreeSwingTreeModel;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArchiveNodeTree extends JTree {

    public ArchiveNodeTree(GenericTreeNode rootNode) {
	super(new GenericTreeSwingTreeModel(new GenericTreeModelProviderImpl(rootNode)));
    }

    public static void main(String[] args) {
	Mockery context = new JUnit4Mockery();

	final GenericTreeNode parent = context.mock(GenericTreeNode.class, "parent");
	final GenericTreeNode child0 = context.mock(GenericTreeNode.class, "child0");
	final GenericTreeNode child00 = context.mock(GenericTreeNode.class, "child00");
	final GenericTreeNode child1 = context.mock(GenericTreeNode.class, "child1");
	context.checking(new Expectations() {

	    {
		// Root

		allowing(parent).getChildCount();
		will(returnValue(2));

		allowing(parent).getChild(0);
		will(returnValue(child0));

		allowing(parent).getChild(1);
		will(returnValue(child1));

		// -Child 0

		allowing(child0).getChildCount();
		will(returnValue(1));

		allowing(child0).getChild(0);
		will(returnValue(child00));

		// --Child 00

		allowing(child00).getChildCount();
		will(returnValue(0));

		// -Child 1

		allowing(child1).getChildCount();
		will(returnValue(0));

	    }
	});

	final ArchiveNodeTree tree = new ArchiveNodeTree(parent);

	javax.swing.SwingUtilities.invokeLater(new Runnable() {

	    public void run() {
		createAndShowGUI(tree);
	    }
	});
    }

    private static void createAndShowGUI(JTree tree) {
	//Create and set up the window.
	JFrame frame = new JFrame("ArchiveNode tree test");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	frame.getContentPane().add(tree);

	//Display the window.
	frame.pack();
	frame.setVisible(true);
    }
}
