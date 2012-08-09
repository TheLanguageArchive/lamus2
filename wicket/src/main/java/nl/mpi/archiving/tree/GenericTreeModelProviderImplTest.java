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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class GenericTreeModelProviderImplTest {

    private Mockery context = new JUnit4Mockery();

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetRoot() {
	GenericTreeNode archiveNode = context.mock(GenericTreeNode.class);
	GenericTreeModelProvider instance = new GenericTreeModelProviderImpl(archiveNode);
	assertEquals(archiveNode, instance.getRoot());
    }

    @Test
    public void testGetChild() {
	final GenericTreeNode parent = context.mock(GenericTreeNode.class, "parent");
	final GenericTreeNode child0 = context.mock(GenericTreeNode.class, "child0");
	final GenericTreeNode child3 = context.mock(GenericTreeNode.class, "child3");
	context.checking(new Expectations() {

	    {
		oneOf(parent).getChild(0);
		will(returnValue(child0));

		oneOf(parent).getChild(3);
		will(returnValue(child3));
	    }
	});

	GenericTreeModelProvider instance = new GenericTreeModelProviderImpl(parent);
	assertSame(child0, instance.getChild(parent, 0));
	assertSame(child3, instance.getChild(parent, 3));
    }
}
