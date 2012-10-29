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

import entropy.configuration.ManagedElementSets;
import entropy.configuration.ResourcePicker;
import entropy.configuration.VirtualMachine;
import entropy.vjob.VJob;

import java.util.List;
import java.util.Random;

/**
 * Tools to alter the resource consumption and demand of the VMs in a VJob.
 *
 * @author Fabien Hermenier
 */
public final class VJobAlterer {

    private VJobAlterer() {
    }

    /**
     * Compute the total resource consumption of a vjob for of some given resource name.
     *
     * @param v        the vjob
     * @param criteria the resource to consider
     * @return an array containing all the sums. Order by #criteria
     */
    public static int[] getResourceSum(VJob v, ResourcePicker.VMRc... criteria) {
        return ManagedElementSets.sum(v.getVirtualMachines(), criteria);
    }

    /**
     * Compute the total resource consumption of a vjob for of some given resource name.
     *
     * @param vjobs    the vjobs to sum up
     * @param criteria the resource to consider
     * @return an array containing all the sums. Order by #criteria
     */
    public static int[] getResourceSum(List<VJob> vjobs, ResourcePicker.VMRc... criteria) {
        int[] sums = new int[criteria.length];
        for (VJob v : vjobs) {
            int[] s = getResourceSum(v, criteria);
            for (int i = 0; i < sums.length; i++) {
                sums[i] += s[i];
            }
        }
        return sums;
    }

    /**
     * Set the global CPU consumption of a vjob.
     * the consumption of each VM is computed using a ratio and its maximum alloted CPU usage.
     *
     * @param v     the vjob to alter
     * @param ratio btw 0.0 and 1.0
     * @return the new global CPU consumption.
     */
    public static int setCPUConsumptionRatio(VJob v, double ratio) {
        int sum = 0;
        for (VirtualMachine vm : v.getVirtualMachines()) {
            double c = vm.getCPUMax() * ratio;
            sum += (int) c;
            vm.setCPUConsumption((int) c);
        }
        return sum;
    }

    /**
     * Set the global CPU demand of a vjob.
     * the demand of each VM is computed using a ratio and its maximum alloted CPU usage.
     *
     * @param v     the vjob to alter
     * @param ratio btw 0.0 and 1.0
     * @return the new global CPU demand.
     */
    public static int setCPUDemandRatio(VJob v, double ratio) {
        int sum = 0;
        for (VirtualMachine vm : v.getVirtualMachines()) {
            double c = vm.getCPUMax() * ratio;
            sum += (int) c;
            vm.setCPUDemand((int) c);
        }
        return sum;
    }

    public static double getCPUDemandRatio(VJob v) {
        double sumRatios = 0d;
        for (VirtualMachine vm : v.getVirtualMachines()) {
            double c = 1d * vm.getCPUDemand() / vm.getCPUMax();
            sumRatios += c;
        }
        return sumRatios / v.getVirtualMachines().size();
    }

    /**
     * Set all the VMs in the vjob having their CPU consumption equals to their CPU demand.
     *
     * @param vjobs the vjobs to alter
     */
    public static void setCPUConsumptionToDemand(VJob... vjobs) {
        for (VJob v : vjobs) {
            for (VirtualMachine vm : v.getVirtualMachines()) {
                vm.setCPUConsumption(vm.getCPUDemand());
            }
        }
    }

    public static void setCPUConsumptionToDemand(List<VJob> vjobs) {
        setCPUConsumptionToDemand(vjobs.toArray(new VJob[vjobs.size()]));
    }


    public static void setCPUDemandToConsumption(List<VJob> vjobs) {
        setCPUDemandToConsumption(vjobs.toArray(new VJob[vjobs.size()]));
    }

    private static void setCPUDemandToConsumption(VJob... vjobs) {
        for (VJob v : vjobs) {
            for (VirtualMachine vm : v.getVirtualMachines()) {
                vm.setCPUDemand(vm.getCPUConsumption());
            }
        }
    }

    private static Random random = new Random();

    public static void setRandomCPUConsumption(List<VJob> vjobs) {
        setRandomCPUConsumption(vjobs.toArray(new VJob[vjobs.size()]));
    }

    public static void setRandomCPUConsumption(VJob... vjobs) {
        for (VJob v : vjobs) {
            VJobAlterer.setCPUConsumptionRatio(v, random.nextDouble());
        }
    }

    public static void setRandomCPUDemand(List<VJob> vjobs) {
        setRandomCPUDemand(0, 1, vjobs.toArray(new VJob[vjobs.size()]));
    }

    public static void setRandomCPUDemand(double lb, double ub, List<VJob> vjobs) {
        setRandomCPUDemand(lb, ub, vjobs.toArray(new VJob[vjobs.size()]));
    }


    public static void setRandomCPUDemand(VJob... vjobs) {
        setRandomCPUDemand(0, 1, vjobs);
    }

    public static void setRandomCPUDemand(double lb, double ub, VJob... vjobs) {
        double diff = ub - lb;
        for (VJob v : vjobs) {
            VJobAlterer.setCPUDemandRatio(v, random.nextDouble() * diff + lb);
        }
    }
}
