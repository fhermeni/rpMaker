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

import entropy.configuration.ManagedElementSet;
import entropy.configuration.Node;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.VirtualMachine;
import entropy.vjob.DefaultVJob;
import entropy.vjob.PlacementConstraint;
import entropy.vjob.VJob;

import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of {@link VJobTemplate}
 * TODO: handle placement constraints
 *
 * @author Fabien Hermenier
 */
public class DefaultVJobTemplate implements VJobTemplate {

    private ManagedElementSet<VirtualMachine> vms;

    private ManagedElementSet<Node> ns;

    private Set<PlacementConstraint> constraints;

    private String id;

    /**
     * Create a template from an existing VJob.
     *
     * @param id     the identifier of the template
     * @param source the vjob to use
     */
    public DefaultVJobTemplate(String id, VJob source) {
        this(id);
        this.vms.addAll(source.getVirtualMachines());
    }

    public DefaultVJobTemplate(String id) {
        this.id = id;
        vms = new SimpleManagedElementSet<VirtualMachine>();
        ns = new SimpleManagedElementSet<Node>();
        this.constraints = new HashSet<PlacementConstraint>();
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public ManagedElementSet<VirtualMachine> getVirtualMachines() {
        ManagedElementSet<VirtualMachine> allVMs = vms.clone();
        for (PlacementConstraint c : constraints) {
            allVMs.addAll(c.getAllVirtualMachines());
        }
        return allVMs;
    }

    @Override
    public ManagedElementSet<Node> getNodes() {
        ManagedElementSet<Node> allNodes = ns.clone();
        for (PlacementConstraint c : constraints) {
            allNodes.addAll(c.getNodes());
        }
        return allNodes;
    }

    @Override
    public boolean addConstraint(PlacementConstraint c) {
        return this.constraints.add(c);
    }

    @Override
    public boolean removeConstraint(PlacementConstraint c) {
        return this.constraints.remove(c);
    }

    @Override
    public Set<PlacementConstraint> getConstraints() {
        return this.constraints;
    }

    @Override
    public boolean addVirtualMachines(ManagedElementSet<VirtualMachine> e) {
        return this.vms.addAll(e);
    }

    @Override
    public boolean addVirtualMachine(VirtualMachine vm) {
        return this.vms.add(vm);
    }

    @Override
    public VJob instantiate(String id, String prefix) {
        VJob instance = new DefaultVJob(id);
        //Got to clone every VM and constraint

        //Clone every isolated VM
        for (VirtualMachine vm : vms) {
            VirtualMachine cpy = vm.clone();
            //Name of the clone is the name of the VM prefixed by the RootID
            cpy.rename(new StringBuilder(prefix).append(vm.getName()).toString());
            instance.addVirtualMachine(cpy);
        }

        return instance;
    }
}
