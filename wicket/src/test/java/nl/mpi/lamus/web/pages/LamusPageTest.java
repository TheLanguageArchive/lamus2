/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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

import nl.mpi.lamus.web.AbstractLamusWicketTest;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author guisil
 */
public class LamusPageTest extends AbstractLamusWicketTest {
    
    private String mockRegisterUrl = "https://test.mpi.nl/registerUrl";
    
    @Override
    protected void setUpTest() {
        
        addMock(AbstractLamusWicketTest.BEAN_NAME_REGISTER_URL, mockRegisterUrl);
        
        LamusPage page = new LamusPage(AbstractLamusWicketTest.MOCK_LAMUS_PAGE_TITLE);
        getTester().startPage(page);
    }

    @Override
    protected void tearDownTest() {
        
    }
    
    
    @Test
    @DirtiesContext
    public void pageRendersSuccessfully() {
        
        getTester().assertRenderedPage(LamusPage.class);
    }
    
    @Test
    @DirtiesContext
    public void componentsRenderSuccessfully() {
        
        getTester().assertComponent("header_tla_logo", Image.class);
        getTester().assertEnabled("header_tla_logo");
        
        getTester().assertComponent("header_appname", Label.class);
        getTester().assertEnabled("header_appname");
        getTester().assertLabel("header_appname", AbstractLamusWicketTest.MOCK_LAMUS_PAGE_TITLE);
        
        getTester().assertComponent("header_clarin_logo", Image.class);
        getTester().assertEnabled("header_clarin_logo");
        
        getTester().assertComponent("header_username", Label.class);
        getTester().assertEnabled("header_username");
        getTester().assertLabel("header_username", AbstractLamusWicketTest.MOCK_USER_ID);
        
        getTester().assertComponent("feedbackPanel", FeedbackPanel.class);
        getTester().assertEnabled("feedbackPanel");
    }
    
    @Test
    @DirtiesContext
    public void headRendersSuccessfully() {
        
        //TODO test head content?
//        fail("not tested yet");
    }

}