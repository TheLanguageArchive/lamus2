/*
 * HeaderPanel.java
 *
 * Created on March 12, 2012, 2:13 PM
 */
 
package nl.mpi.archivetree.wicket;           

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;

/** 
 *
 * @author jeafer
 * @version 
 */

public class HeaderPanel extends Panel {

    /**
     * Construct.
     * @param componentName name of the component
     * @param exampleTitle title of the example
     */

    public HeaderPanel(String componentName, String exampleTitle)
    {
        super(componentName);
        add(new Label("exampleTitle", "Lamus2 Language Archive Management and Upload System"));
        add(CSSPackageResource.getHeaderContribution(IndexPage.class,  "css/lams.css" ));
        add(new Image("image", new ResourceReference(HeaderPanel.class, "lana.gif")));
    }

}
