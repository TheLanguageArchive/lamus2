/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.archivetree.wicket;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

/**
 *
 * @author jeafer
 */
public final class IndexPage extends WebPage {

    public IndexPage() {
        super();
        add(new BookmarkablePageLink("link", HomePage.class));
        add(new BookmarkablePageLink("link2", HomePage.class));
        add(new BookmarkablePageLink("link3", HomePage.class));
        add(new HeaderPanel("headerpanel", "Welcome To Wicket"));
    }
    
    public IndexPage(PageParameters params) {
        //TODO:  process page parameters
    }
}
