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

import entropy.configuration.ManagedElementSet;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;
import entropy.vjob.DefaultVJob;
import entropy.vjob.VJob;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
@Test
public class DefaultVJobTemplateTest {

    private static VJob createVJob() {
        VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 3, 5, 7);
        vm1.setCPUDemand(9);
        vm1.setMemoryDemand(10);
        vm1.setCPUMax(35);
        vm1.addOption("fooO1");
        vm1.addOption("fooO2", "v2");
        vm1.setTemplate("bar");

        VJob v = new DefaultVJob("t1");
        Assert.assertTrue(v.addVirtualMachine(vm1));

        VirtualMachine vm2 = new SimpleVirtualMachine("VM2", 3, 3, 3);
        Assert.assertTrue(v.addVirtualMachine(vm2));

        VirtualMachine vm3 = new SimpleVirtualMachine("VM3", 1, 1, 1);
        VirtualMachine vm4 = new SimpleVirtualMachine("VM4", 1, 1, 1);
        ManagedElementSet<VirtualMachine> vms = new SimpleManagedElementSet<VirtualMachine>();
        vms.add(vm3);
        vms.add(vm4);
        Assert.assertTrue(v.addVirtualMachines(vms));
        return v;
    }

    public void testCreation() {
        VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 3, 5, 7);
        vm1.setCPUDemand(9);
        vm1.setMemoryDemand(10);
        vm1.setCPUMax(35);
        vm1.addOption("fooO1");
        vm1.addOption("fooO2", "v2");
        vm1.setTemplate("bar");

        VJobTemplate vt = new DefaultVJobTemplate("t1");
        Assert.assertTrue(vt.addVirtualMachine(vm1));

        VirtualMachine vm2 = new SimpleVirtualMachine("VM2", 3, 3, 3);
        Assert.assertTrue(vt.addVirtualMachine(vm2));

        VirtualMachine vm3 = new SimpleVirtualMachine("VM3", 1, 1, 1);
        VirtualMachine vm4 = new SimpleVirtualMachine("VM4", 1, 1, 1);
        ManagedElementSet<VirtualMachine> vms = new SimpleManagedElementSet<VirtualMachine>();
        vms.add(vm3);
        vms.add(vm4);
        Assert.assertTrue(vt.addVirtualMachines(vms));

        ManagedElementSet<VirtualMachine> all = vms.clone();
        all.add(vm1);
        all.add(vm2);
        Assert.assertTrue(vt.getVirtualMachines().equals(all));
        Assert.assertEquals(vt.id(), "t1");
    }

    public void testCreationFromVJob() {
        VJob v = createVJob();
        VJobTemplate t = new DefaultVJobTemplate("t1", v);
        Assert.assertTrue(t.getVirtualMachines().equals(v.getVirtualMachines()));
    }

    public void testInstantiation() {
        VJob v = createVJob();
        VJobTemplate t = new DefaultVJobTemplate("t1", v);
        VJob i = t.instantiate("v", "root.");
        Assert.assertEquals(i.id(), "v");
        Assert.assertEquals(v.getVirtualMachines().size(), t.getVirtualMachines().size());
        for (VirtualMachine vm : t.getVirtualMachines()) {
            VirtualMachine x = i.getVirtualMachines().get("root." + vm.getName());
            Assert.assertNotNull(x);
            Assert.assertEquals(vm.getCPUConsumption(), x.getCPUConsumption());
            Assert.assertEquals(vm.getCPUDemand(), x.getCPUDemand());
            Assert.assertEquals(vm.getCPUMax(), x.getCPUMax());
            Assert.assertEquals(vm.getMemoryConsumption(), x.getMemoryConsumption());
            Assert.assertEquals(vm.getMemoryDemand(), x.getMemoryDemand());
            Assert.assertEquals(vm.getNbOfCPUs(), x.getNbOfCPUs());
            Assert.assertEquals(vm.getTemplate(), x.getTemplate());
            Assert.assertEquals(vm.getOptions(), x.getOptions());

        }
    }
}
