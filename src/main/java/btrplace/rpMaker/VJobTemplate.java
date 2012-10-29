/*
 * Copyright (c) Fabien Hermenier
 *
 *         This file is part of Entropy.
 *
 *         Entropy is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU Lesser General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *
 *         Entropy is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *
 *         GNU Lesser General Public License for more details.
 *         You should have received a copy of the GNU Lesser General Public License
 *         along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.rpMaker;

import entropy.vjob.VJob;

/**
 * A VJob template is a VJob that can be instantiated several times.
 * Each instance of the template has its own VMs and placement constraints
 * but the vjob architecture so the involved VMs and nodes have the same properties
 *
 * @author Fabien Hermenier
 */
public interface VJobTemplate extends VJob {

    /**
     * Instantiate the template
     *
     * @param rootId the id of the vjob
     * @param prefix prefix to add to each VM involved in the template.
     * @return an instance of the template
     */
    VJob instantiate(String rootId, String prefix);
}
