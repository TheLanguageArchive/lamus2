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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Expandable/Collapsible Panel to contain the feedback panel.
 * @author guisil
 */
public abstract class ExpandableFeedbackPanel extends Panel {
    
    private ResourceReference closed = new PackageResourceReference(ExpandableFeedbackPanel.class, "plus.png");
    private ResourceReference open = new PackageResourceReference(ExpandableFeedbackPanel.class, "minus.png");
    
    private boolean visible = false;
    protected FeedbackPanel innerPanel;
    
    public ExpandableFeedbackPanel(String id, IModel<String> titleModel, boolean visibleByDefault) {
        super(id);
        
        visible = visibleByDefault;
        
//        innerPanel = getInnerFeedbackPanel();
//        innerPanel.setVisible(visibleByDefault);
//        innerPanel.setOutputMarkupId(true);
//        innerPanel.setOutputMarkupPlaceholderTag(true);
//        add(innerPanel);
        
        final Image showHideIcon = new Image("showHideIcon") {

            @Override
            protected ResourceReference getImageResourceReference() {
                return visible ? open : closed;
            }
        };
        showHideIcon.setOutputMarkupId(true);
        IndicatingAjaxLink showHideLink = new IndicatingAjaxLink("showHideLink") {
            
            @Override
            public void onClick(AjaxRequestTarget target) {
                visible = !visible;
//                innerPanel.setVisible(visible);
//                getInnerFeedbackPanel().setVisible(visible);
//                target.add(innerPanel);
                target.add(getInnerFeedbackPanel());
                target.add(showHideIcon);
            }
        };
        showHideLink.add(showHideIcon);
        add(new Label("titlePanel", titleModel));
        add(showHideLink);
    }
    
    /**
     * To be overridden. Allows the parent component to create the feedback panel.
     * @return feedback panel
     */
    protected abstract FeedbackPanel getInnerFeedbackPanel();
}
