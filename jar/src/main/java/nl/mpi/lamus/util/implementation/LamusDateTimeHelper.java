/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.util.implementation;

import java.util.Calendar;
import java.util.Date;
import nl.mpi.lamus.util.DateTimeHelper;
import org.springframework.stereotype.Component;

/**
 * @see DateTimeHelper
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Component
public class LamusDateTimeHelper implements DateTimeHelper {

    /**
     * @see DateTimeHelper#getCurrentDateTime()
     */
    @Override
    public Date getCurrentDateTime() {
        return Calendar.getInstance().getTime();
    }
    
}
