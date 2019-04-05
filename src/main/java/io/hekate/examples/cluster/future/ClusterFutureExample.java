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

package io.hekate.examples.cluster.future;

import io.hekate.cluster.ClusterTopology;
import io.hekate.core.Hekate;
import io.hekate.spring.boot.EnableHekate;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * An example which shows how to register a cluster topology condition and get a notification once it is met.
 */
@EnableHekate
@SpringBootApplication
public class ClusterFutureExample {
    /**
     * Runs the application.
     *
     * @param args Command-line arguments.
     *
     * @throws Exception Signals application failure.
     */
    public static void main(String[] args) throws Exception {
        Hekate hekate = SpringApplication.run(ClusterFutureExample.class, args).getBean(Hekate.class);

        // Get a future object that will be completed once the cluster topology meets the specified condition.
        CompletableFuture<ClusterTopology> future = hekate.cluster().futureOf(topology -> topology.size() >= 2);

        try {
            say("Waiting for nodes...");

            future.get();

            say("All nodes are ready: " + hekate.cluster().topology());
        } catch (CancellationException e) {
            say("Stopped waiting (local node terminated).");
        }
    }

    private static void say(String msg) {
        System.out.println(">>>: " + msg);
    }
}
