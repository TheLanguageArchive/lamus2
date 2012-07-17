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
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.Model;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class LamusPage extends WebPage {

    public LamusPage() {
	this("Lamus2 Language Archive Management and Upload System");
    }

    public LamusPage(String pageTitle) {
	super();
	add(CSSPackageResource.getHeaderContribution(LamusPage.class, "lams.css"));
	add(new Image("image", new ResourceReference(LamusPage.class, "lana.gif")));

	add(new Label("pageTitle", pageTitle));
	add(new Label("username", new Model<String>(LamusSession.get().getUserId())));
    }
}
