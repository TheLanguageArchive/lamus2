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

import nl.mpi.lamus.web.session.LamusSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Reference page that contains common features
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class LamusPage extends WebPage {

    @SpringBean(name = "registerUrl")
    private String registerUrl;
    
    private FeedbackPanel feedbackPanel;
    
    /**
     * edit title of the page, logo and userName
     */
    public LamusPage() {
        super();
        
        String appName = getLocalizer().getString("header_app_name", this);
        
        feedbackPanel = new FeedbackPanel("feedbackPanel");
        feedbackPanel.setOutputMarkupId(true);
	add(feedbackPanel);
        add(new Image("header_tla_logo", new SharedResourceReference("tlaLogoImage")));
        add(new Label("header_appname", appName));
        add(new Image("header_clarin_logo", new SharedResourceReference("clarinInvertedImage")));
        
        Link homePageLink = new Link("home_page_link") {
            @Override
            public void onClick() {
                final IndexPage resultPage = new IndexPage();
                setResponsePage(resultPage);
            }
        };
        homePageLink.add(new Image("home_image", new SharedResourceReference("homeImage")));
        add(homePageLink);
        
        add(new ExternalLink("register_link", Model.of(registerUrl)));
        
        add(new Label("header_username", new HeaderUsernameModel()));
        
        if("anonymous".equals(LamusSession.get().getUserId())) {
            add(new ExternalLink("loginOrLogoutLink", "login", getLocalizer().getString("header_login_label", this)));
        } else {
            add(new ExternalLink("loginOrLogoutLink", "logout", getLocalizer().getString("header_logout_label", this)));
        }
        
        //TODO THE URLs IN THESE LINKS ARE NOT VALID AT THE MOMENT...

    }
    
    protected FeedbackPanel getFeedbackPanel() {
        return feedbackPanel;
    }
    
    private static class HeaderUsernameModel extends AbstractReadOnlyModel<String> {

        public HeaderUsernameModel() {
        }

        @Override
        public String getObject() {
            return LamusSession.get().getUserId();
        }
    }
}
