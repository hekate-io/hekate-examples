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

package io.hekate.examples.election;

import io.hekate.election.Candidate;
import io.hekate.election.CandidateConfig;
import io.hekate.election.FollowerContext;
import io.hekate.election.LeaderContext;
import io.hekate.spring.boot.EnableHekate;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * An example of how to use Hekate election service to implement a cluster-wide singleton.
 */
@EnableHekate
@SpringBootApplication
public class ElectionExample {
    /**
     * Candidate for elections.
     */
    @Component
    public static class ExampleCandidate implements Candidate {
        @Override
        public void becomeLeader(LeaderContext leader) {
            say("I'm leader. Will do some work...");

            while (doSomeLeaderWork()) {
                say("Will do more work as a leader...");
            }

            say("Can't work anymore. Yielding leadership...");

            leader.yieldLeadership();
        }

        @Override
        public void becomeFollower(FollowerContext follower) {
            say("I'm follower of " + follower.leader().address().socket());

            follower.addListener(it ->
                say("Leader changed: " + it.leader().address().socket())
            );
        }

        @Override
        public void terminate() {
            say("Terminated.");
        }
    }

    /**
     * Runs the application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        // Start Spring Boot application.
        SpringApplication.run(ElectionExample.class, args);
    }

    /**
     * Register {@link ExampleCandidate} as {@link CandidateConfig}.
     *
     * @param candidate Candidate.
     *
     * @return Configuration.
     */
    @Bean
    public CandidateConfig electionCandidateConfig(ExampleCandidate candidate) {
        return new CandidateConfig()
            .withGroup("example-election-group")
            .withCandidate(candidate);
    }

    /**
     * Emulate some workload when local node becomes a leader.
     *
     * @return {@code true} if should continue to run.
     */
    private static boolean doSomeLeaderWork() {
        // Emulate some workload.
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // No-op.
        }

        say("Successfully did some work as a leader.");

        // Random outcome: true - success, false - failure.
        return ThreadLocalRandom.current().nextBoolean();
    }

    private static void say(String msg) {
        System.out.println(">>>: " + msg);
    }
}
