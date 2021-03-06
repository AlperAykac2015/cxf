/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package demo.jaxrs.tracing.server.camel;

import com.uber.jaeger.Tracer;
import com.uber.jaeger.metrics.Metrics;
import com.uber.jaeger.metrics.NullStatsReporter;
import com.uber.jaeger.metrics.StatsFactoryImpl;
import com.uber.jaeger.propagation.TextMapCodec;
import com.uber.jaeger.reporters.RemoteReporter;
import com.uber.jaeger.samplers.ConstSampler;
import com.uber.jaeger.senders.HttpSender;

import org.apache.camel.opentracing.starter.CamelOpenTracing;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import io.opentracing.propagation.Format.Builtin;

@EnableAutoConfiguration
@SpringBootApplication
@CamelOpenTracing
public class Server {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Server.class)
            .web(false)
            .build()
            .run(args);
    }
    
    @Bean
    Tracer tracer() {
        final Metrics metrics = new Metrics(new StatsFactoryImpl(new NullStatsReporter()));
        
        final Tracer.Builder builder = 
            new Tracer.Builder(
                "camel-server", 
                new RemoteReporter(new HttpSender("http://localhost:14268/api/traces"), 1000, 100, metrics),
                new ConstSampler(true)
            )
            .registerExtractor(Builtin.TEXT_MAP, new TextMapCodec(true));
        
        return builder.build();
    }
}

