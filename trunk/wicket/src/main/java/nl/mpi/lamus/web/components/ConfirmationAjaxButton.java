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

import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;

/**
 * Extension of AutoDisablingAjaxButton which includes a confirmation dialog.
 * @author guisil
 */
public class ConfirmationAjaxButton extends AutoDisablingAjaxButton {
    
    private final String text;
    
    public ConfirmationAjaxButton(String id, String text) {
        super(id);
        this.text = text;
    }

    /**
     * @see IndicatingAjaxButton#updateAjaxAttributes(org.apache.wicket.ajax.attributes.AjaxRequestAttributes)
     */
    @Override
    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
        
        super.updateAjaxAttributes(attributes);
        
        AjaxCallListener ajaxCallListener = new AjaxCallListener();
        ajaxCallListener.onPrecondition("return confirm('" + text + "');");
        attributes.getAjaxCallListeners().add(ajaxCallListener);
    }
}
