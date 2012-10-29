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
import entropy.vjob.ContinuousSpread;
import entropy.vjob.DefaultVJob;
import entropy.vjob.Spread;
import entropy.vjob.VJob;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link btrplace.rpMaker.ConfigurationAlterer}
 *
 * @author Fabien Hermenier
 */
@Test
public class ConfigurationAltererTest {


    public void testapplyNodeFailureRatio() {
        Configuration cfg = new SimpleConfiguration();
        for (int i = 0; i < 100; i++) {
            Node n = new SimpleNode("N" + i, 10, 10, 10);
            cfg.addOnline(n);
            VirtualMachine vm1 = new SimpleVirtualMachine("VM" + i + "-1", 1, 1, 1);
            VirtualMachine vm2 = new SimpleVirtualMachine("VM" + i + "-2", 1, 1, 1);
            cfg.setRunOn(vm1, n);
            cfg.setSleepOn(vm2, n);
        }
        ManagedElementSet<VirtualMachine> offs = ConfigurationAlterer.applyNodeFailureRatio(cfg, 0.1);
        Assert.assertEquals(offs.size(), 20);
        Assert.assertEquals(cfg.getOfflines().size(), 10);
        Assert.assertEquals(cfg.getOnlines().size(), 90);
        Assert.assertEquals(cfg.getWaitings().size(), 0);
        Assert.assertEquals(cfg.getRunnings().size(), 90);
        Assert.assertEquals(cfg.getSleepings().size(), 90);
    }

    public void testRelocate() {
        Configuration cfg = new SimpleConfiguration();
        for (int i = 0; i < 100; i++) {
            Node n = new SimpleNode("N" + i, 1, 3, 3);
            cfg.addOnline(n);
            if (i < 99) {
                VirtualMachine vm1 = new SimpleVirtualMachine("VM" + i + "-1", 1, 1, 1);
                VirtualMachine vm2 = new SimpleVirtualMachine("VM" + i + "-2", 1, 1, 1);
                cfg.setRunOn(vm1, n);
                cfg.setSleepOn(vm2, n);
            }
        }
        Spread s = new ContinuousSpread(cfg.getRunnings());
        VJob v = new DefaultVJob("v1");
        v.addConstraint(s);
        List<VJob> vjobs = new ArrayList<VJob>();
        vjobs.add(v);
        VirtualMachine vm = cfg.getRunnings().get("VM1-1");
        Assert.assertTrue(ConfigurationAlterer.relocate(cfg, vm, vjobs));
        cfg.setRunOn(vm, cfg.getOnlines().get("N1"));
        Assert.assertTrue(cfg.remove(cfg.getOnlines().get("N99")));
        Assert.assertFalse(ConfigurationAlterer.relocate(cfg, vm, vjobs, 1));

        Assert.assertFalse(ConfigurationAlterer.relocate(cfg, vm, vjobs));
    }
}
