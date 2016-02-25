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

import nl.mpi.lamus.web.components.AboutPanel;
import nl.mpi.lamus.web.session.LamusSession;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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
 * @author guisil
 */
public class LamusPage extends WebPage implements IAjaxIndicatorAware{

    @SpringBean(name = "registerUrl")
    private String registerUrl;
    @SpringBean(name = "manualUrl")
    private String manualUrl;
    
    private final FeedbackPanel feedbackPanel;
    
    /**
     * edit title of the page, logo and userName
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public LamusPage() {
        super();
        
        String appName = getLocalizer().getString("header_app_name", this);
        
        feedbackPanel = new FeedbackPanel("feedbackPanel");
        feedbackPanel.setOutputMarkupId(true);
        feedbackPanel.setOutputMarkupPlaceholderTag(true);
        
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
        
        final ModalWindow modalAbout = createAboutModalWindow();
        add(modalAbout);
        add(new AjaxLink<Void>("showModalAbout") {
            @Override
            public void onClick(AjaxRequestTarget art) {
                modalAbout.show(art);
            }
        });
        
        
        add(new ExternalLink("manual_link", Model.of(manualUrl)));
        
        add(new ExternalLink("register_link", Model.of(registerUrl)));
        
        add(new Label("header_username", new HeaderUsernameModel()));
        
        if("anonymous".equals(LamusSession.get().getUserId())) {
            add(new ExternalLink("loginOrLogoutLink", "login", getLocalizer().getString("header_login_label", this)));
        } else {
            add(new ExternalLink("loginOrLogoutLink", "logout", getLocalizer().getString("header_logout_label", this)));
        }
    }
    
    protected FeedbackPanel getFeedbackPanel() {
        return feedbackPanel;
    }
    
    
    private ModalWindow createAboutModalWindow() {
        
        ModalWindow modalAbout = new ModalWindow("modalAbout");
        modalAbout.setContent(new AboutPanel(modalAbout.getContentId()));
        modalAbout.setTitle("About LAMUS 2");
        modalAbout.setCookieName("modal-about");
        modalAbout.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
            @Override
            public boolean onCloseButtonClicked(AjaxRequestTarget art) {
                return true;
            }
        });
        modalAbout.setWindowClosedCallback((new ModalWindow.WindowClosedCallback() {
            @Override
            public void onClose(AjaxRequestTarget art) {
            }
        }));
        
        return modalAbout;
    }

    /**
     * @see IAjaxIndicatorAware#getAjaxIndicatorMarkupId()
     */
    @Override
    public String getAjaxIndicatorMarkupId() {
        return "ajaxveil";
    }
    
    
    private static class HeaderUsernameModel extends AbstractReadOnlyModel<String> {

        HeaderUsernameModel() {
        }

        @Override
        public String getObject() {
            return LamusSession.get().getUserId();
        }
    }
}
