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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * Reference page that contains common features
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class LamusPage extends WebPage {

//    public static final PackageResourceReference CSS_RESOURCE_REFERENCE = new PackageResourceReference(LamusPage.class, "lams.css");
    public static final PackageResourceReference LAMUS2_CSS_RESOURCE_REFERENCE = new PackageResourceReference(LamusPage.class, "lamus2.css");
//    public static final PackageResourceReference LANA_IMAGE_RESOURCE_REFERENCE = new PackageResourceReference(LamusPage.class, "lana.gif");
    public static final PackageResourceReference TLA_LOGO_RESOURCE_REFERENCE = new PackageResourceReference(LamusPage.class, "tla_logo.png");
    public static final PackageResourceReference CLARIN_LOGO_RESOURCE_REFERENCE = new PackageResourceReference(LamusPage.class, "CLARIN-inverted.png");
    public static final PackageResourceReference HOME_ICON_RESOURCE_REFERENCE = new PackageResourceReference(LamusPage.class, "home.png");
    
    public LamusPage() {
//        this("Lamus2 Language Archive Management and Upload System");
        this("The Language Archive");
    }

    /**
     * edit title of the page, logo and userName
     *
     * @param appName
     */
    public LamusPage(String appName) {
        super();
	add(new FeedbackPanel("feedbackPanel"));
        add(new Image("header_tla_logo", TLA_LOGO_RESOURCE_REFERENCE));
        add(new Label("header_appname", appName));
        add(new Image("header_clarin_logo", CLARIN_LOGO_RESOURCE_REFERENCE));
        
        Link homePageLink = new Link("home_page_link") {
            @Override
            public void onClick() {
                final IndexPage resultPage = new IndexPage();
                setResponsePage(resultPage);
            }
        };
        homePageLink.add(new Image("home_image", HOME_ICON_RESOURCE_REFERENCE));
        add(homePageLink);
        
        add(new Label("header_username", new Model<String>(LamusSession.get().getUserId())));
        
        if("anonymous".equals(LamusSession.get().getUserId())) {
            add(new ExternalLink("loginOrLogoutLink", "login", "login"));
        } else {
            add(new ExternalLink("loginOrLogoutLink", "logout", "logout"));
        }
        
        //TODO THE URLs IN THESE LINKS ARE NOT VALID AT THE MOMENT...
        
        
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
//        response.renderCSSReference(CSS_RESOURCE_REFERENCE);
//        response.render(CssHeaderItem.forReference(CSS_RESOURCE_REFERENCE));
        response.render(CssHeaderItem.forReference(LAMUS2_CSS_RESOURCE_REFERENCE));
    }
}
