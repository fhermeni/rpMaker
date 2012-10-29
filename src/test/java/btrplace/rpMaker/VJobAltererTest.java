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

import entropy.configuration.ResourcePicker;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;
import entropy.vjob.DefaultVJob;
import entropy.vjob.VJob;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class VJobAltererTest {

    private static VJob makeVJob() {
        VJob v = new DefaultVJob("v");
        for (int i = 0; i < 10; i++) {
            VirtualMachine vm = new SimpleVirtualMachine("VM" + i, 1, 2, 3);
            vm.setCPUDemand(5);
            vm.setCPUMax(10);
            v.addVirtualMachine(vm);
        }

        return v;
    }

    @Test
    public void testGetResourceSum() throws Exception {
        VJob v = makeVJob();
        int[] sums = VJobAlterer.getResourceSum(v, ResourcePicker.VMRc.cpuConsumption, ResourcePicker.VMRc.cpuDemand, ResourcePicker.VMRc.memoryDemand);
        Assert.assertEquals(sums[0], 20);
        Assert.assertEquals(sums[1], 50);
        Assert.assertEquals(sums[2], 30);
    }

    @Test
    public void testSetCPUConsumptionRatio() throws Exception {
        VJob v = makeVJob();
        int total = VJobAlterer.setCPUConsumptionRatio(v, 0.6);
        Assert.assertEquals(total, 60);
        for (VirtualMachine vm : v.getVirtualMachines()) {
            Assert.assertEquals(vm.getCPUConsumption(), 6);
        }
    }

    @Test
    public void testSetCPUDemandRatio() throws Exception {
        VJob v = makeVJob();
        int total = VJobAlterer.setCPUDemandRatio(v, 0.7);
        Assert.assertEquals(total, 70);
        for (VirtualMachine vm : v.getVirtualMachines()) {
            Assert.assertEquals(vm.getCPUDemand(), 7);
        }
    }

    @Test
    public void testSetCPUConsumptionToDemand() throws Exception {
        VJob v = makeVJob();
        VJobAlterer.setCPUConsumptionToDemand(v);
        for (VirtualMachine vm : v.getVirtualMachines()) {
            Assert.assertEquals(vm.getCPUConsumption(), 5);
        }

    }
}
