/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.archivetree.wicket;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

/**
 *
 * @author jeafer
 */
public class ButtonPage extends Panel {

    public ButtonPage(String componentName) {
        super(componentName);
        //add(new Label("form", "Hello, World!"));
        // Add a form with an onSumbit implementation that sets a message
//        Form<?> form = new Form("form")
//        {
//            @Override
//            protected void onSubmit()
//            {
//                info("Form.onSubmit executed");
//            }
//        };
//
//        Button button1 = new Button("button1")
//        {
//            @Override
//            public void onSubmit()
//            {
//                info("button1.onSubmit executed");
//            }
//        };
//        form.add(button1);
//
//        Button button2 = new Button("button2")
//        {
//            @Override
//            public void onSubmit()
//            {
//                info("button2.onSubmit executed");
//            }
//        };
//        button2.setDefaultFormProcessing(false);       
//        form.add(button2);
//        
//        Button button3 = new Button("button3")
//        {
//            @Override
//            public void onSubmit()
//            {
//                info("button1.onSubmit executed");
//            }
//        };
//        form.add(button3);
//        
//        Button button4 = new Button("button4")
//        {
//            @Override
//            public void onSubmit()
//            {
//                info("button1.onSubmit executed");
//            }
//        };
//        form.add(button4);
//        
//        Button button5 = new Button("button5")
//        {
//            @Override
//            public void onSubmit()
//            {
//                info("button1.onSubmit executed");
//            }
//        };
//        form.add(button5);
//        
//        Button button6 = new Button("button6")
//        {
//            @Override
//            public void onSubmit()
//            {
//                info("button1.onSubmit executed");
//            }
//        };
//        form.add(button6);
//
//        Button button7 = new Button("button7")
//        {
//            @Override
//            public void onSubmit()
//            {
//                info("button1.onSubmit executed");
//            }
//        };
//        form.add(button7);
//        
//        add(form);
        add(new BookmarkablePageLink("button1", ButtonPage.class));
        add(new BookmarkablePageLink("button2", ButtonPage.class));
        add(new BookmarkablePageLink("button3", ButtonPage.class));
        add(new BookmarkablePageLink("button4", ButtonPage.class));
        add(new BookmarkablePageLink("button5", ButtonPage.class));
        add(new BookmarkablePageLink("button6", ButtonPage.class));
        add(new BookmarkablePageLink("button7", ButtonPage.class));
        //add(CSSPackageResource.getHeaderContribution(ButtonPage.class,  "css/lams.css" ));
    }
}
