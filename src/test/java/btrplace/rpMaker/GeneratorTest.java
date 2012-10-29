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
import entropy.plan.choco.ChocoCustomRP;
import entropy.plan.choco.constraint.pack.SatisfyDemandingSlicesHeightsFastBP;
import entropy.plan.durationEvaluator.MockDurationEvaluator;
import entropy.vjob.DefaultVJob;
import entropy.vjob.VJob;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class GeneratorTest {

    @Test
    public void testBasic() {
        ManagedElementSet<Node> onlines = new SimpleManagedElementSet<Node>();
        ManagedElementSet<Node> offlines = new SimpleManagedElementSet<Node>();
        for (int i = 0; i < 30; i++) {
            Node n = new SimpleNode("N" + i, 5, 5, 5);
            if (i < 20) {
                onlines.add(n);
            } else {
                offlines.add(n);
            }
        }
        ManagedElementSet<VirtualMachine> vms = new SimpleManagedElementSet<VirtualMachine>();
        for (int i = 0; i < 50; i++) {
            VirtualMachine vm = null;
            if (i % 2 == 0) {
                vm = new SimpleVirtualMachine("VM" + i, 1, 2, 1);
            } else {
                vm = new SimpleVirtualMachine("VM" + i, 1, 1, 2);
            }
            vms.add(vm);
        }
        VJob v = new DefaultVJob("foo");
        v.addVirtualMachines(vms);
        List<VJob> vjobs = new ArrayList<VJob>();
        vjobs.add(v);
        ChocoCustomRP rp = new ChocoCustomRP(new MockDurationEvaluator(1, 1, 1, 1, 1, 1, 1, 1, 1));
        rp.setPackingConstraintClass(new SatisfyDemandingSlicesHeightsFastBP());
        Configuration cfg = Generator.generate(rp, onlines, offlines, vjobs);
        Assert.assertEquals(cfg.getAllVirtualMachines().size(), 50);
        Assert.assertEquals(cfg.getRunnings().size(), 50);
        Assert.assertEquals(cfg.getOnlines().size(), 20);
        Assert.assertEquals(cfg.getOfflines().size(), 10);
    }
}
