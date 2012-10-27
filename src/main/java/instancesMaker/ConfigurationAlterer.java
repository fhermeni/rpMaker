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

package instancesMaker;

import entropy.configuration.*;
import entropy.vjob.Fence;
import entropy.vjob.Lonely;
import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJob;

import java.util.List;

/**
 * Tools to alter a configuration and get some statistics.
 *
 * @author Fabien Hermenier
 */
public final class ConfigurationAlterer {

    private ConfigurationAlterer() {
    }


    /**
     * Alter a configuration by putting a certain ratio of the online nodes offlines.
     * Nodes are selected randomly.
     *
     * @param cfg   the configuration to alter
     * @param ratio the ratio of nodes that have to be offline. Between 0.0 and 1.0
     * @return the set of virtual machines that was on the selected nodes (running or sleeping VMs)
     */
    public static ManagedElementSet<VirtualMachine> applyNodeFailureRatio(Configuration cfg, double ratio) {
        int toFail = (int) (cfg.getOnlines().size() * ratio);
        ManagedElementSet<VirtualMachine> vms = new SimpleManagedElementSet<VirtualMachine>();
        while (toFail > 0) {
            Node n = ManagedElementSets.randomNode(cfg.getOnlines());
            ManagedElementSet<VirtualMachine> toRemove = new SimpleManagedElementSet<VirtualMachine>();
            toRemove.addAll(cfg.getRunnings(n));
            toRemove.addAll(cfg.getSleepings(n));
            vms.addAll(toRemove);
            for (VirtualMachine vm : toRemove) {
                cfg.remove(vm);
            }
            boolean ret = cfg.addOffline(n);
            assert ret;
            toFail--;
        }
        return vms;
    }

    /**
     * Relocate a running VM to another node.
     * The new location of the VM will satisfy all the placement constraints so the resource consumption.
     *
     * @param cfg   the configuration to alter
     * @param vm    the virtual machine to relocate
     * @param vjobs the vjobs
     * @return {@code true} if the VM was relocated.
     */
    public static boolean relocate(Configuration cfg, VirtualMachine vm, List<VJob> vjobs) {
        return relocate(cfg, vm, vjobs, cfg.getOnlines().size());
    }

    /**
     * Relocate a running VM to another node.
     * The new location of the VM will satisfy all the placement constraints so the resource consumption.
     *
     * @param cfg   the configuration to alter
     * @param vm    the virtual machine to relocate
     * @param vjobs the vjobs
     * @param tries the maximum number of tryout to find a node
     * @return {@code true} if the VM was relocated.
     */
    public static boolean relocate(Configuration cfg, VirtualMachine vm, List<VJob> vjobs, int tries) {
        if (!cfg.isRunning(vm)) {
            return false;
        }
        Node current = cfg.getLocation(vm);
        ManagedElementSet<Node> candidates = cfg.getOnlines().clone();
        boolean relocated = false;
        while (!relocated && tries > 0) {
            boolean fine = true;
            if (candidates.isEmpty()) {
                return false;
            }
            Node n = ManagedElementSets.randomNode(candidates);
            if (n.equals(current)) {
                tries--;
                continue;
            }
            cfg.setRunOn(vm, n);
            //Check the constraints
            if (Configurations.currentlyOverloaded(cfg, n)) {
                fine = false;
            } else {
                for (VJob v : vjobs) {
                    for (PlacementConstraint c : v.getConstraints()) {
                        //TODO: use c.getAllVirtualMachines().contains(vm) is good in theory
                        //to avoid useless check. But some constraints like Lonely may not be compatible with this tweak.
                        //idem. with constraints focused on nodes
                        if (!(c instanceof Lonely) && c.getAllVirtualMachines().contains(vm) && !c.isSatisfied(cfg)) {
                            fine = false;
                            break;
                        } else if (c instanceof Lonely && !c.isSatisfied(cfg)) {
                            fine = false;
                            break;
                        }
                        if (c.getAllVirtualMachines().contains(vm) && c instanceof Fence) {
                            //There must be a better solution to handle partitioning constraints that are simple domain
                            // resriction
                            candidates.retainAll(c.getNodes());
                        }
                    }
                    if (!fine) {
                        break;
                    }
                }
            }
            if (!fine) {
                tries--;
                candidates.remove(n);
            } else {
                relocated = true;
            }
        }
        //Reset the position in case of failure
        if (!relocated) {
            cfg.setRunOn(vm, current);
        }
        return relocated;
    }

    public static void shuffle(Configuration cfg, List<VJob> vjobs, int nbMoves) {
        for (int j = 0; j < nbMoves; j++) {
            VirtualMachine vm = ManagedElementSets.randomVirtualMachine(cfg.getAllVirtualMachines());
            ConfigurationAlterer.relocate(cfg, vm, vjobs);
        }
    }

    public static double getCPUConsumptionLoad(Configuration cfg) {
        int[] capa = ManagedElementSets.sum(cfg.getOnlines(), ResourcePicker.NodeRc.cpuCapacity);
        int[] cons = ManagedElementSets.sum(cfg.getAllVirtualMachines(), ResourcePicker.VMRc.cpuConsumption);
        return 1.0d * cons[0] / capa[0];
    }

    public static double getCPUDemandLoad(Configuration cfg) {
        int[] capa = ManagedElementSets.sum(cfg.getOnlines(), ResourcePicker.NodeRc.cpuCapacity);
        int[] cons = ManagedElementSets.sum(cfg.getAllVirtualMachines(), ResourcePicker.VMRc.cpuDemand);
        return 1.0d * cons[0] / capa[0];
    }


    public static double getMemoryConsumptionLoad(Configuration cfg) {
        int[] capa = ManagedElementSets.sum(cfg.getOnlines(), ResourcePicker.NodeRc.memoryCapacity);
        int[] cons = ManagedElementSets.sum(cfg.getAllVirtualMachines(), ResourcePicker.VMRc.memoryConsumption);
        return 1.0d * cons[0] / capa[0];
    }

}
