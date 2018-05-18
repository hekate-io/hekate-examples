/*
 * Copyright 2018 The Hekate Project
 *
 * The Hekate Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.hekate.examples.cluster;

import io.hekate.cluster.ClusterView;
import io.hekate.cluster.event.ClusterEventType;
import io.hekate.core.Hekate;
import io.hekate.spring.boot.EnableHekate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * An example which shows how to build different cluster views based on dynamic filtering of the cluster topology.
 */
@EnableHekate
@SpringBootApplication
public class ClusterViewExample {
    /**
     * Runs the application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        Hekate hekate = SpringApplication.run(ClusterViewExample.class, args).getBean(Hekate.class);

        /////////////////////////////////////////////////////////////////////////////////////////
        // Nodes with 'example' role.
        /////////////////////////////////////////////////////////////////////////////////////////
        ClusterView withRole = hekate.cluster().forRole("example");

        say("Nodes with 'example' role: " + withRole.topology());

        withRole.addListener(event -> {
            say("All nodes with 'example' role: " + event.topology().nodes());
        }, ClusterEventType.CHANGE);

        /////////////////////////////////////////////////////////////////////////////////////////
        // Remote nodes with 'example' role.
        /////////////////////////////////////////////////////////////////////////////////////////
        ClusterView remotesWithRole = hekate.cluster().forRole("example").forRemotes();

        say("Remote nodes with 'example' role: " + remotesWithRole.topology());

        remotesWithRole.addListener(event -> {
            say("Remote nodes with 'example' role: " + event.topology().nodes());
        }, ClusterEventType.CHANGE);

        /////////////////////////////////////////////////////////////////////////////////////////
        // Oldest node with 'example' role.
        /////////////////////////////////////////////////////////////////////////////////////////
        ClusterView oldestWithRole = hekate.cluster().forRole("example").forOldest();

        say("Oldest node with 'example' role: " + oldestWithRole.topology());

        oldestWithRole.addListener(event -> {
            say("Oldest node with 'example' role: " + event.topology().nodes());
        }, ClusterEventType.CHANGE);

        /////////////////////////////////////////////////////////////////////////////////////////
        // Youngest node with 'example' role.
        /////////////////////////////////////////////////////////////////////////////////////////
        ClusterView youngestWithRole = hekate.cluster().forRole("example").forYoungest();

        say("Youngest node with 'example' role: " + youngestWithRole.topology());

        youngestWithRole.addListener(event -> {
            say("Youngest node with 'example' role: " + event.topology().nodes());
        }, ClusterEventType.CHANGE);

        /////////////////////////////////////////////////////////////////////////////////////////
        // Remote nodes with 'example' role and more than 1 CPU.
        /////////////////////////////////////////////////////////////////////////////////////////
        ClusterView customView = hekate.cluster().filter(node ->
            node.isRemote() && node.runtime().cpus() > 1 && node.hasRole("example")
        );

        say("Nodes with 'example' role and more than 1 CPU: " + customView.topology());

        customView.addListener(event -> {
            say("Nodes with 'example' role and more than 1 CPU: " + event.topology().nodes());
        }, ClusterEventType.CHANGE);
    }

    private static void say(String msg) {
        System.out.println(">>>: " + msg);
    }
}
