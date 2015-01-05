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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;

/**
 * Extension of AutoDisablingAjaxButton which becomes hidden when there
 * is an active request.
 * @author guisil
 */
public class AutoHideDisablingAjaxButton extends AutoDisablingAjaxButton {
    
    public AutoHideDisablingAjaxButton(String id) {
        super(id);
    }

    /**
     * @see IndicatingAjaxButton#updateAjaxAttributes(org.apache.wicket.ajax.attributes.AjaxRequestAttributes)
     */
    @Override
    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
        
        super.updateAjaxAttributes(attributes);
        
        attributes.getAjaxCallListeners().add(new IAjaxCallListener() {

            @Override
            public CharSequence getBeforeHandler(Component cmpnt) {
                return "$(\"#" + cmpnt.getMarkupId() + "\").hide()";
            }

            @Override
            public CharSequence getPrecondition(Component cmpnt) {
                return "";
            }

            @Override
            public CharSequence getBeforeSendHandler(Component cmpnt) {
                return "";
            }

            @Override
            public CharSequence getAfterHandler(Component cmpnt) {
                return "";
            }

            @Override
            public CharSequence getSuccessHandler(Component cmpnt) {
                return "";
            }

            @Override
            public CharSequence getFailureHandler(Component cmpnt) {
                return "";
            }

            @Override
            public CharSequence getCompleteHandler(Component cmpnt) {
                return "";
            }
        });
    }
}
