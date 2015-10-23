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

import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.model.IModel;

/**
 * Extension of IndicatingAjaxLink which
 * discards any call once there is an active request running.
 * This prevents multiple requests to be triggered by
 * accidentally clicking the link more than once.
 * @author guisil
 */
public abstract class AutoDisablingAjaxLink<T extends Object> extends IndicatingAjaxLink<T> {
    
    public AutoDisablingAjaxLink(String id) {
        super(id);
    }
    
    public AutoDisablingAjaxLink(String id, IModel<T> model) {
        super(id, model);
    }
    

    /**
     * @see IndicatingAjaxLink#updateAjaxAttributes(org.apache.wicket.ajax.attributes.AjaxRequestAttributes)
     */
    @Override
    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
        
        super.updateAjaxAttributes(attributes);
        
        attributes.setChannel(new AjaxChannel("autodisable", AjaxChannel.Type.ACTIVE));
    }
}