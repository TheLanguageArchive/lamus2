/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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

import nl.mpi.lamus.web.components.ButtonPanel.ConfirmationOptions;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 * Panel to be shown when the submission button is pressed.
 * The user will then be able to confirm the submission and
 * choose some other options.
 * 
 * @author guisil
 */
public class ConfirmPanel extends Panel {
    
    public ConfirmPanel(String id, final ModalWindow modalWindow, final ConfirmationOptions options) {
        super(id);
        
        Form confirmSubmitForm = new Form("confirmSubmitForm");
        
        MultiLineLabel infoText = new MultiLineLabel("confirmation_info_text", options.getConfirmationInfoText());
        
        confirmSubmitForm.add(infoText);
        
        confirmSubmitForm.add(new Label("confirmation_text", options.getConfirmationText()));
        
        CheckBox keepUnlinkedFilesCheckbox = new CheckBox("checkbox", Model.of(options.isKeepUnlinkedFiles())) {

            @Override
            protected void onSelectionChanged(Boolean newSelection) {
                options.setKeepUnlinkedFiles(newSelection);
            }

            @Override
            protected void onModelChanged() {
                options.setKeepUnlinkedFiles(getModelObject());
            }
        };
        
        keepUnlinkedFilesCheckbox.setLabel(Model.of("keep unlinked files"));
        confirmSubmitForm.add(keepUnlinkedFilesCheckbox);
        
        modalWindow.setTitle("Please confirm");
        
        if (options.getConfirmationInfoText() != null && !options.getConfirmationInfoText().isEmpty()) {
            modalWindow.setInitialHeight(300);
            modalWindow.setInitialWidth(580);        
        } else {
        	infoText.setVisible(Boolean.FALSE);
            modalWindow.setInitialHeight(200);
            modalWindow.setInitialWidth(350);
        }

        AjaxButton yesButton = new AjaxButton("yesButton", confirmSubmitForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                if (target != null) {
                    options.setConfirmed(true);
                    modalWindow.close(target);
                }
            }
        };

        AjaxButton noButton = new AjaxButton("noButton", confirmSubmitForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                if (target != null) {
                    options.setConfirmed(false);
                    modalWindow.close(target);
                }
            }
        };

        confirmSubmitForm.add(yesButton);
        confirmSubmitForm.add(noButton);

        add(confirmSubmitForm);
    }
}
