/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.web.session.LamusSession;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.UrlValidator;

/**
 * 
 * @author guisil
 */
public class ExternalNodesPanel extends FeedbackPanelAwarePanel<Workspace> {
    
    @SpringBean
    private WorkspaceService workspaceService;
    
    @SpringBean
    private WorkspaceNodeFactory workspaceNodeFactory;
    
    private IModel<Workspace> model;
    private boolean linkVisible = true;
    
    
    public ExternalNodesPanel(String id, IModel<Workspace> model, FeedbackPanel feedbackPanel) {
        super(id, model, feedbackPanel);
        
        this.model = model;
            
        setOutputMarkupId(true);
            
        add(new AjaxFallbackLink("link") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                onShowForm(target);
            }

            @Override
            public boolean isVisible() {
                return linkVisible;
            }

        });

        add(new AddExternalNodeForm("form"));
    }
        
        
    void onShowForm(AjaxRequestTarget target) {
        linkVisible = false;
        target.add(this);
    }

    void onAddItem(AjaxRequestTarget target) {
        linkVisible = true;
        addComponentToTarget(target);
        target.add(this);
    }

    void onCancelItem(AjaxRequestTarget target) {
        linkVisible = true;
        target.add(this);
    }

    // to be overridden by parent
    public void addComponentToTarget(AjaxRequestTarget target) {
        
    }
    

    private final class AddExternalNodeForm extends Form<Void> {

        TextField<String> externalUrlField;
        DropDownChoice<WorkspaceNodeType> externalType;

        public AddExternalNodeForm(String id) {
            super(id);

            setOutputMarkupId(true);

            externalUrlField = new TextField<>("externalUrl", Model.of(""));
            externalUrlField.add(new UrlValidator());
            add(externalUrlField);
            
            List<WorkspaceNodeType> possibleTypes = new ArrayList<>();
            possibleTypes.add(WorkspaceNodeType.METADATA);
            possibleTypes.add(WorkspaceNodeType.RESOURCE_INFO);
            possibleTypes.add(WorkspaceNodeType.RESOURCE_AUDIO);
            possibleTypes.add(WorkspaceNodeType.RESOURCE_IMAGE);
            possibleTypes.add(WorkspaceNodeType.RESOURCE_VIDEO);
            possibleTypes.add(WorkspaceNodeType.RESOURCE_WRITTEN);
            
            externalType = new DropDownChoice<WorkspaceNodeType>("externalType", new Model<WorkspaceNodeType>(), possibleTypes) {

                @Override
                protected void onSelectionChanged(WorkspaceNodeType newSelection) {
                    super.onSelectionChanged(newSelection); //To change body of generated methods, choose Tools | Templates.
                    
                    setModel(new Model<>(newSelection));
                }
            };
            add(externalType);

            add(new AjaxButton("add", this) {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                    target.add(getFeedbackPanel());

                    String enteredString = AddExternalNodeForm.this.externalUrlField.getModelObject();
                    URI enteredUri;
                    try {
                        enteredUri = new URI(enteredString);
                    } catch (URISyntaxException ex) {
                        error(getLocalizer().getString("external_nodes_panel_invalid_uri_message", this));
                        return;
                    }
                    WorkspaceNode externalNode =
                        workspaceNodeFactory.getNewExternalNode(
                            model.getObject().getWorkspaceID(), enteredUri);
                    
                    externalNode.setType(externalType.getModelObject());
                    
                    try {

                        workspaceService.addNode(LamusSession.get().getUserId(), externalNode);

                    } catch (WorkspaceNotFoundException | WorkspaceAccessException ex) {
                        error(ex.getMessage());
                    }

                    onAddItem(target);

                    externalUrlField.setModel(Model.of(""));
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.add(getFeedbackPanel());
                }

            });

            add(new AjaxButton("cancel", this) {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    onCancelItem(target);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {

                }

            });
        }

        @Override
        public boolean isVisible() {
            return !linkVisible;
        }
    }
    
}
