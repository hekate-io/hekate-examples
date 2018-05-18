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

package io.hekate.examples.rpc;

import io.hekate.core.Hekate;
import io.hekate.rpc.Rpc;
import io.hekate.rpc.RpcAggregate;
import io.hekate.rpc.RpcServerConfig;
import io.hekate.rpc.RpcSplit;
import io.hekate.spring.boot.EnableHekate;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static java.util.Collections.singletonList;

/**
 * An example of how to perform a Map/Reduce style of RPC communications.
 */
@EnableHekate
@SpringBootApplication
public class RpcSplitAggregateExample {
    /**
     * RPC interface.
     */
    @Rpc
    public interface ExampleRpc {
        /**
         * Counts length of each word from the specified list.
         *
         * @param words List of words.
         *
         * @return List of lengths.
         */
        @RpcAggregate
        List<Integer> countChars(@RpcSplit List<String> words);
    }

    /**
     * Implementation of RPC interface.
     */
    @Component
    public static class ExampleRpcImpl implements ExampleRpc {
        @Override
        public List<Integer> countChars(List<String> words) {
            int sum = words.stream().mapToInt(String::length).sum();

            return singletonList(sum);
        }
    }

    /**
     * Runs the application.
     *
     * @param args Command-line arguments.
     *
     * @throws Exception Signals application failure.
     */
    public static void main(String[] args) throws Exception {
        Hekate hekate = SpringApplication.run(RpcSplitAggregateExample.class, args).getBean(Hekate.class);

        // Build RPC client proxy.
        ExampleRpc rpc = hekate.rpc().clientFor(ExampleRpc.class)
            .forRemotes()
            .build();

        while (hekate.rpc().clusterOf(ExampleRpc.class).forRemotes().awaitForNodes()) {
            // Everyone loves this song:)
            List<String> words = Arrays.asList((""
                + "We don't need no education "
                + "We don't need no thought control "
                + "No dark sarcasm in the classroom "
                + "Teachers leave them kids alone "
                + "Hey! Teachers! Leave them kids alone "
                + "All in all it's just another brick in the wall "
                + "All in all you're just another brick in the wall"
            ).split(" "));

            // Call RPC method on all remote nodes.
            int totalChars = rpc.countChars(words).stream()
                .mapToInt(Integer::intValue)
                .sum();

            // Print aggregated results.
            say("Result: " + totalChars);

            // Sleep for a while.
            Thread.sleep(1000);
        }
    }

    /**
     * Configuration of {@link ExampleRpc} server.
     *
     * @param rpcImpl RPC implementation.
     *
     * @return RPC server configuration.
     */
    @Bean
    public RpcServerConfig exampleRpcServerConfig(ExampleRpcImpl rpcImpl) {
        return new RpcServerConfig().withHandler(rpcImpl);
    }

    private static void say(String msg) {
        System.out.println(Thread.currentThread().getName() + " >>>: " + msg);
    }
}
