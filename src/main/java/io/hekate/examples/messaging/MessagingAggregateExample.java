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

package io.hekate.examples.messaging;

import io.hekate.core.Hekate;
import io.hekate.messaging.MessagingChannel;
import io.hekate.messaging.MessagingChannelConfig;
import io.hekate.messaging.operation.AggregateResult;
import io.hekate.spring.boot.EnableHekate;
import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static java.util.stream.Collectors.joining;

/**
 * An example of how to submit a message to all cluster nodes and how to aggregate their responses.
 */
@EnableHekate
@SpringBootApplication
public class MessagingAggregateExample {
    /**
     * Runs the application.
     *
     * @param args Command-line arguments.
     *
     * @throws Exception Signals application failure.
     */
    public static void main(String[] args) throws Exception {
        Hekate hekate = SpringApplication.run(MessagingAggregateExample.class, args).getBean(Hekate.class);

        // Get channel by its name (see exampleChannelConfig() method).
        MessagingChannel<String> channel = hekate.messaging()
            .channel("aggregate-example", String.class)
            .forRemotes();

        while (channel.cluster().awaitForNodes()) {
            // Submit aggregation request and get results.
            AggregateResult<String> result = channel.aggregate("get_some_value");

            if (result.isSuccess()) {
                say("Result: " + result.stream().sorted().collect(joining(",")));
            } else {
                say("Failure: " + result.errors());
            }

            // Sleep for a while before submitting the next message.
            Thread.sleep(1000);
        }
    }

    /**
     * Configuration of 'aggregate-example' channel.
     *
     * @return Channel configuration.
     */
    @Bean
    public MessagingChannelConfig<String> exampleChannelConfig() {
        return MessagingChannelConfig.of(String.class)
            .withName("aggregate-example")
            .withRetryPolicy(retry -> retry
                .whileError(err -> err.isCausedBy(IOException.class))
                .maxAttempts(3)
            )
            .withReceiver(request -> {
                if ("get_some_value".equals(request.payload())) {
                    String someValue = request.channel().cluster().topology().localNode().address().socket().toString();

                    // Reply asynchronously.
                    request.reply(someValue, err -> {
                        if (err != null) {
                            say("Aggregation response failure: " + err);
                        }
                    });
                } else {
                    throw new IllegalArgumentException("Unexpected request: " + request);
                }
            });
    }

    private static void say(String msg) {
        System.out.println(">>>: " + msg);
    }
}
