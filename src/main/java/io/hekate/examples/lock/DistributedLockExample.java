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

package io.hekate.examples.lock;

import io.hekate.core.Hekate;
import io.hekate.lock.DistributedLock;
import io.hekate.lock.LockRegion;
import io.hekate.lock.LockRegionConfig;
import io.hekate.spring.boot.EnableHekate;
import java.util.concurrent.CancellationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * An example of how to use distributed locks.
 */
@EnableHekate
@SpringBootApplication
public class DistributedLockExample {
    /**
     * Runs the application.
     *
     * @param args Command-line arguments.
     *
     * @throws Exception Signals application failure.
     */
    public static void main(String[] args) throws Exception {
        Hekate hekate = SpringApplication.run(DistributedLockExample.class, args).getBean(Hekate.class);

        // Get a pre-configured region (see exampleLockRegionConfig() method).
        LockRegion region = hekate.locks().region("example-region");

        try {
            while (region.cluster().forRemotes().awaitForNodes()) {
                // Get a lock instance for arbitrary key.
                DistributedLock lock = region.get("example-lock");

                say("Awaiting for the lock.");

                lock.lock();

                try {
                    say("Got the lock.");

                    // ...emulate some work while the lock is being held...

                    Thread.sleep(3000);
                } finally {
                    lock.unlock();
                }

                say("Released the lock.");
            }
        } catch (CancellationException e) {
            // Local node had been stopped manually (Ctrl+C) while we were waiting for the lock.
            say("Locking cancelled.");
        }
    }

    /**
     * Configuration of 'example-region'.
     *
     * @return Configuration.
     */
    @Bean
    public LockRegionConfig exampleLockRegionConfig() {
        return new LockRegionConfig("example-region");
    }

    private static void say(String msg) {
        System.out.println(">>>: " + msg);
    }
}
