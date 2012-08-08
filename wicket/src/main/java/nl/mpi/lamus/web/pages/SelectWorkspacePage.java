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
package nl.mpi.lamus.web.pages;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import nl.mpi.archiving.tree.ArchiveNode;
import nl.mpi.archiving.tree.ArchiveNodeTreeModelProvider;
import nl.mpi.archiving.tree.CorpusArchiveNode;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.components.ArchiveTreePanel;
import nl.mpi.lamus.web.model.WorkspaceModel;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author jeafer
 */
public final class SelectWorkspacePage extends LamusPage {

    @SpringBean
    private WorkspaceService workspaceService;
    @SpringBean(name = "workspaceSelectTreeProvider")
    private ArchiveNodeTreeModelProvider openedArchiveTreeProvider;
    private final Form nodeIdForm;
    
            //single list choice
        private static final List<String> WORKSPACES = Arrays.asList(new String[] {
			"3", "2", "1" });
        private String selectedWorkspace = "2";

    public SelectWorkspacePage() {
        super();
        nodeIdForm = createNodeIdForm("workspaceForm");      
    }


    private Form createNodeIdForm(String id) { 
ListChoice<String> listWorkspaces = new ListChoice<String>("workspace",
				new PropertyModel<String>(this, "selectedWorkspace"), WORKSPACES);
        
        listWorkspaces.setMaxRows(5);
        final Form<Workspace> form = new Form<Workspace>(id);
//        form.add(new Label("name"));
//        form.add(new Label("nodeId"));
        //form.add(new Label("workspaceId"));
//        form.add(new Label("userId"));
//                form.add(new Label("topnodeId"));
//        form.add(new Label("topnodearchiveurl"));
//        final TextField wsid = new TextField("workspaceId");
//        final TextField userid = new TextField("userId");
//        final TextField topnodeid = new TextField("topnodeId");
        //final TextField topnodearchiveurl = new TextField("topnodearchiveurl", new Model<URL>(topNodeArchiveURL));
//        final TextField<URL> topnodearchiveurl = new TextField<URL>("topnodearchiveurl");
//        form.add(wsid);
//        form.add(userid);
//        form.add(topnodeid);
//        form.add(topnodearchiveurl);
        Button submitButton = new Button("OpenWorkspace") {

            @Override
            public void onSubmit() {
                // Request a workspace with workspace service
                final Workspace openSelectedWorkspace = workspaceService.getWorkspace(Integer.parseInt(selectedWorkspace));
                // Show page for newly created workspace
                final WorkspacePage resultPage = new WorkspacePage(new WorkspaceModel(openSelectedWorkspace));
                setResponsePage(resultPage);
            }
        };
        form.add(submitButton);
        form.add(listWorkspaces);

        // Put details/submit form in container for refresh through AJAX 
        final MarkupContainer formContainer = new WebMarkupContainer("formContainer");
        formContainer.add(form);
        // Add container to page
        add(formContainer);
        return form;

    }
}
