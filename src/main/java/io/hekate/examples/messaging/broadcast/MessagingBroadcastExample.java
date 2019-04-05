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

package io.hekate.examples.messaging.broadcast;

import io.hekate.core.Hekate;
import io.hekate.messaging.MessagingChannel;
import io.hekate.messaging.MessagingChannelConfig;
import io.hekate.messaging.operation.BroadcastResult;
import io.hekate.spring.boot.EnableHekate;
import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static java.util.stream.Collectors.joining;

/**
 * A simple example of broadcast messaging.
 */
@EnableHekate
@SpringBootApplication
public class MessagingBroadcastExample {
    /**
     * Runs the application.
     *
     * @param args Command-line arguments.
     *
     * @throws Exception Signals application failure.
     */
    public static void main(String[] args) throws Exception {
        Hekate hekate = SpringApplication.run(MessagingBroadcastExample.class, args).getBean(Hekate.class);

        // Get channel by its name (see exampleChannelConfig() method).
        MessagingChannel<String> channel = hekate.messaging()
            .channel("broadcast-example", String.class)
            .forRemotes();

        while (channel.cluster().awaitForNodes()) {
            String message = "message from " + hekate.localNode();

            // Submit broadcast message.
            BroadcastResult<String> result = channel.broadcast(message);

            if (result.isSuccess()) {
                say("Submitted to: " + result.nodes().stream()
                    .map(node -> node.address().socket().toString())
                    .collect(joining(","))
                );
            } else {
                say("Failure: " + result.errors());
            }

            // Sleep for a while before submitting the next message.
            Thread.sleep(1000);
        }
    }

    /**
     * Configuration of 'broadcast-example' channel.
     *
     * @return Channel configuration.
     */
    @Bean
    public MessagingChannelConfig<String> exampleChannelConfig() {
        return MessagingChannelConfig.of(String.class)
            .withName("broadcast-example")
            .withRetryPolicy(retry -> retry
                .whileError(err -> err.isCausedBy(IOException.class))
                .maxAttempts(3)
            )
            .withReceiver(msg ->
                say("Received: " + msg.payload())
            );
    }

    private static void say(String msg) {
        System.out.println(">>>: " + msg);
    }
}
