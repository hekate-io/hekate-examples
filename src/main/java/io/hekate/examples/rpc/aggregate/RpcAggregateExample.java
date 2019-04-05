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

package io.hekate.examples.rpc.aggregate;

import io.hekate.core.Hekate;
import io.hekate.rpc.Rpc;
import io.hekate.rpc.RpcAggregate;
import io.hekate.rpc.RpcServerConfig;
import io.hekate.spring.boot.EnableHekate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static java.util.Collections.singletonList;

/**
 * An example of how to submit an RPC request to all cluster nodes and how to aggregate RPC results.
 */
@EnableHekate
@SpringBootApplication
public class RpcAggregateExample {
    /**
     * RPC interface.
     */
    @Rpc
    public interface ExampleRpc {
        /**
         * Synchronously says 'Hello' from this cluster node.
         *
         * @param who To whom.
         *
         * @return Result.
         */
        @RpcAggregate
        List<String> hello(String who);

        /**
         * Asynchronously says 'Hello' from this cluster node.
         *
         * @param who To whom.
         *
         * @return Asynchronous result.
         */
        @RpcAggregate
        CompletableFuture<List<String>> helloAsync(String who);
    }

    /**
     * Implementation of RPC interface.
     */
    @Component
    public static class ExampleRpcImpl implements ExampleRpc {
        /** Reference to the local Hekate node that runs this RPC instance. */
        private final ObjectFactory<Hekate> hekate;

        /**
         * Constructs a new instance.
         *
         * @param hekate Reference to a Hekate node that runs this RPC.
         */
        public ExampleRpcImpl(ObjectFactory<Hekate> hekate) {
            this.hekate = hekate;
        }

        @Override
        public List<String> hello(String who) {
            String hello = "Hello to " + who + " from " + hekate.getObject().localNode().address().socket();

            return singletonList(hello);
        }

        @Override
        public CompletableFuture<List<String>> helloAsync(String who) {
            String hello = "Hello to " + who + " from " + hekate.getObject().localNode().address().socket();

            // Complete immediately for simplicity.
            return CompletableFuture.completedFuture(singletonList(hello));
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
        Hekate hekate = SpringApplication.run(RpcAggregateExample.class, args).getBean(Hekate.class);

        // Build RPC client proxy.
        ExampleRpc rpc = hekate.rpc().clientFor(ExampleRpc.class)
            .forRemotes()
            .build();

        while (hekate.rpc().clusterOf(ExampleRpc.class).forRemotes().awaitForNodes()) {
            // Local node's address.
            String from = hekate.localNode().address().socket().toString();

            /////////////////////////////////////////////////////////////////////
            // 1. Synchronous call of RPC method on all remote nodes.
            /////////////////////////////////////////////////////////////////////
            List<String> results = rpc.hello(from);

            say("Synchronous results: ");

            results.stream().sorted().forEach(result ->
                say("  " + result)
            );

            /////////////////////////////////////////////////////////////////////
            // 2. Asynchronous call of RPC method on all remote nodes.
            /////////////////////////////////////////////////////////////////////
            CompletableFuture<List<String>> future = rpc.helloAsync(from);

            future.whenComplete((asyncResults, err) -> {
                if (err == null) {
                    say("Asynchronous results: ");

                    asyncResults.stream().sorted().forEach(result ->
                        say("  " + result)
                    );
                } else {
                    say("Synchronous failure: " + err);
                }
            });

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
        System.out.println(">>>: " + msg);
    }
}
