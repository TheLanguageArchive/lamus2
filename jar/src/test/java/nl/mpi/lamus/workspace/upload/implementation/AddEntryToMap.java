/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.upload.implementation;

import java.util.Map;
import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

/**
 *
 * @author guisil
 */
public class AddEntryToMap<T,U> implements Action {
    
    T oneElement;
    U anotherElement;

    public AddEntryToMap(T oneElement,U anotherElement) {
        this.oneElement = oneElement;
        this.anotherElement = anotherElement;
    }
    
    public static <T,U> Action putElements(T t, U u) {
        return new AddEntryToMap<>(t, u);
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("puts ")
                   .appendValueList("", ", ", "", oneElement, anotherElement)
                   .appendText(" into a map");
    }

    @Override
    public Object invoke(Invocation invctn) throws Throwable {
        ((Map<T,U>) invctn.getParameter(4)).put(oneElement, anotherElement);
        return null;
    }

}
