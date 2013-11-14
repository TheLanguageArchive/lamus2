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

import nl.mpi.lamus.web.providers.LamusWicketPagesProvider;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Main page or index page Displays options for further navigation
 *
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
public final class IndexPage extends LamusPage {

    @SpringBean
    private LamusWicketPagesProvider pagesProvider;
    
    /**
     * Constructor.
     */
    public IndexPage() {
        super();
        
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
    }

    /**
     * Constructor.
     *
     * @param parameters Page parameters
     */
//    public IndexPage(PageParameters params) {
//        //TODO:  process page parameters
//    }
}
