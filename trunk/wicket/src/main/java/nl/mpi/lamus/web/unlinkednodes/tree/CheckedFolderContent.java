/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.web.unlinkednodes.tree;

import java.util.Iterator;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.CheckedFolder;
import org.apache.wicket.extensions.markup.html.repeater.util.ProviderSubset;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

/**
 *
 * based on the example class from Wicket
 * @author Sven Meier
 * 
 * @author guisil
 */
public class CheckedFolderContent extends Content {
   
    
    private ProviderSubset<WorkspaceTreeNode> checked;

    public CheckedFolderContent(ITreeProvider<WorkspaceTreeNode> provider)
    {
        checked = new ProviderSubset<WorkspaceTreeNode>(provider, false);
    }

    @Override
    public void detach()
    {
        checked.detach();
    }

    protected Iterator<WorkspaceTreeNode> getChecked() {
        return checked.iterator();
    }
    
    protected boolean isChecked(WorkspaceTreeNode wsTreeNode)
    {
        return checked.contains(wsTreeNode);
    }

    protected void check(WorkspaceTreeNode wsTreeNode, boolean check)
    {
        if (check)
        {
            checked.add(wsTreeNode);
        }
        else
        {
            checked.remove(wsTreeNode);
        }
    }

    
    @Override
    public Component newContentComponent(String id, final AbstractTree<WorkspaceTreeNode> tree, IModel<WorkspaceTreeNode> model)
    {
        return new CheckedFolder<WorkspaceTreeNode>(id, tree, model)
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<Boolean> newCheckBoxModel(final IModel<WorkspaceTreeNode> model)
            {
                return new IModel<Boolean>()
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Boolean getObject()
                    {
                        return isChecked(model.getObject());
                    }

                    @Override
                    public void setObject(Boolean object)
                    {
                        check(model.getObject(), object);
                    }

                    @Override
                    public void detach()
                    {
                    }
                };
            }
        };
    }
}
