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

package io.hekate.examples.cluster.listener;

import io.hekate.cluster.event.ClusterChangeEvent;
import io.hekate.cluster.event.ClusterEvent;
import io.hekate.cluster.event.ClusterEventListener;
import io.hekate.cluster.event.ClusterJoinEvent;
import io.hekate.cluster.event.ClusterLeaveEvent;
import io.hekate.core.HekateException;
import io.hekate.spring.boot.EnableHekate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

/**
 * An example which shows how to implement a simple listener of cluster events.
 */
@EnableHekate
@SpringBootApplication
public class ClusterListenerExample {
    /**
     * Cluster listener that simply prints all the cluster events to the standard output.
     */
    @Component
    public static class ExampleListener implements ClusterEventListener {
        @Override
        public void onEvent(ClusterEvent event) throws HekateException {
            switch (event.type()) {
                case JOIN: {
                    // Local node joined the cluster.
                    ClusterJoinEvent join = event.asJoin();

                    say("Joined : " + join.topology());

                    break;
                }
                case CHANGE: {
                    // Cluster topology changed.
                    ClusterChangeEvent change = event.asChange();

                    say("Topology change: " + change.topology());
                    say("          Added: " + change.added());
                    say("        Removed: " + change.removed());

                    break;
                }
                case LEAVE: {
                    // Local node left the cluster.
                    ClusterLeaveEvent leave = event.asLeave();

                    say("Left : " + leave.topology());

                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unsupported event type: " + event);
                }
            }
        }
    }

    /**
     * Runs the application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(ClusterListenerExample.class, args);
    }

    private static void say(String msg) {
        System.out.println(">>>: " + msg);
    }
}
