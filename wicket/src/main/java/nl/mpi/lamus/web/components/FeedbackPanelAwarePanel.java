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

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

/**
 * Class to be extended by panels which need access to the feedback panel,
 * in order to display messages to the user.
 * 
 * @author guisil
 */
public class FeedbackPanelAwarePanel<T extends Object> extends GenericPanel<T> {
    
    private final FeedbackPanel feedbackPanel;
    
    public FeedbackPanelAwarePanel(String id, IModel<T> model, FeedbackPanel feedbackPanel) {
        super(id);
        this.feedbackPanel = feedbackPanel;
    }
    
    protected FeedbackPanel getFeedbackPanel() {
        return this.feedbackPanel;
    }
}
