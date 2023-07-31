/*
 *     Copyright (C) 2021 wysohn and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.config.source;

import java.io.File;

public interface IConfigSourceFactory {
    /**
     * Create a new config file or load existing one.
     * <p>
     * Postcondition: the file is guaranteed to exist and loaded if not newly created.
     *
     * @param type     type of the config source.
     * @param folder   the folder where config file will reside
     * @param fileName name of the file <b>without</b> any dots. The underlying
     *                 factory will append the extension as needed.
     * @return config source
     */
    IConfigSource create(String type, File folder, String fileName);
}
