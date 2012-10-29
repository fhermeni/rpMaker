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

import entropy.configuration.*;
import entropy.plan.Plan;
import entropy.plan.PlanException;
import entropy.plan.TimedReconfigurationPlan;
import entropy.vjob.VJob;

import java.util.List;

/**
 * Tool to generate a configuration.
 *
 * @author Fabien Hermenier
 */
public final class Generator {

    private Generator() {
    }

    /**
     * Generate a configuration.
     * all the VMs in the vjobs will be running.
     * Placement constraints so as the VM CPU demand will be satisfied.
     *
     * @param rp       the plan module to use
     * @param onlines  all the online nodes that will compose the configuration
     * @param offlines all the offline nodes that will compose the configuration
     * @param jobs     the list of vjobs to consider
     * @return the generate configuration
     */
    public static Configuration generate(Plan rp,
                                         ManagedElementSet<Node> onlines,
                                         ManagedElementSet<Node> offlines,
                                         List<VJob> jobs) {
        Configuration cfg = new SimpleConfiguration();
        for (Node n : onlines) {
            cfg.addOnline(n);
        }
        for (Node n : offlines) {
            cfg.addOffline(n);
        }

        //Every running VM must be in a waiting state
        ManagedElementSet<VirtualMachine> runnings = new SimpleManagedElementSet<VirtualMachine>();
        for (VJob v : jobs) {
            for (VirtualMachine vm : v.getVirtualMachines()) {
                if (!cfg.isWaiting(vm)) {
                    cfg.addWaiting(vm);
                    runnings.add(vm);
                }
            }
        }

        ManagedElementSet<VirtualMachine> empty = new SimpleManagedElementSet<VirtualMachine>();
        try {
            TimedReconfigurationPlan p = rp.compute(cfg, runnings, empty, empty, empty, cfg.getOnlines(), cfg.getOfflines(), jobs);
            Configuration dst = p.getDestination();
            if (!Configurations.futureOverloadedNodes(dst).isEmpty()) {
                return null;
            }

            return p.getDestination();
        } catch (PlanException e) {
            return null;
        }
    }
}
