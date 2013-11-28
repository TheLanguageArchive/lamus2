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
package nl.mpi.lamus.web.components;

import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author guisil
 */
public class CheckBoxPanel extends Panel {
    
    private Check checkbox;
    
    public CheckBoxPanel(String id, IModel<WorkspaceTreeNode> model) { //IModel<Boolean> model) {
        super(id);
        checkbox = new Check("checkbox", model) {

            @Override
            protected void onModelChanged() {
                super.onModelChanged(); //To change body of generated methods, choose Tools | Templates.
            }
            
        };
        add(checkbox);
    }
    
//    public CheckBoxPanel(String id) {
//        this(id, new Model<Boolean>());
//    }
    
    public Check getCheckbox() {
        return checkbox;
    }
}
