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

import nl.mpi.lamus.web.pages.providers.LamusWicketPagesProvider;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author guisil
 */
public class NavigationPanel extends Panel {
    
    @SpringBean
    private LamusWicketPagesProvider pagesProvider;
    
    /**
     * Constructor.
     */
    public NavigationPanel(String id) {
        super(id);
        
        add(new Link("createWorkspaceLink") {

            @Override
            public void onClick() {
                setResponsePage(pagesProvider.getCreateWorkspacePage());
            }
        });
        
        add(new Link("selectWorkspaceLink") {

            @Override
            public void onClick() {
                setResponsePage(pagesProvider.getSelectWorkspacePage());
            }
        });
        
        add(new Link("requestStorageSpaceLink") {

            @Override
            public void onClick() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        
        add(new Link("managementLink") {

            @Override
            public void onClick() {
                setResponsePage(pagesProvider.getManageWorkspacesPage());
            }
        });
    }
}
