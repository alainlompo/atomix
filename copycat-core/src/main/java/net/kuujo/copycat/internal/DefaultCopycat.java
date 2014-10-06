/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.copycat.internal;

import net.kuujo.copycat.Copycat;
import net.kuujo.copycat.CopycatConfig;
import net.kuujo.copycat.CopycatContext;
import net.kuujo.copycat.StateMachine;
import net.kuujo.copycat.cluster.Cluster;
import net.kuujo.copycat.cluster.Member;
import net.kuujo.copycat.event.*;
import net.kuujo.copycat.log.Log;
import net.kuujo.copycat.protocol.SubmitHandler;
import net.kuujo.copycat.spi.service.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Primary copycat API.<p>
 *
 * The <code>CopyCat</code> class provides a fluent API for
 * combining the {@link DefaultCopycatContext} with an {@link net.kuujo.copycat.spi.service.Service}.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultCopycat implements Copycat {
  private final Service service;
  private final CopycatContext context;

  public <M extends Member> DefaultCopycat(Service service, StateMachine stateMachine, Log log, Cluster<M> cluster, CopycatConfig config) {
    this.context = new DefaultCopycatContext(stateMachine, log, cluster, config);
    this.service = service;
    this.service.submitHandler(new SubmitHandler() {
      @Override
      public <T> CompletableFuture<T> submit(String command, Object... args) {
        return context.submitCommand(command, args);
      }
    });
  }

  public DefaultCopycat(Service service, CopycatContext context) {
    this.service = service;
    this.context = context;
  }

  @Override
  public CopycatContext context() {
    return context;
  }

  @Override
  public Events on() {
    return context.on();
  }

  @Override
  public <T extends Event> EventContext<T> on(Class<T> event) {
    return context.on().event(event);
  }

  @Override
  public EventHandlers events() {
    return context.events();
  }

  @Override
  public <T extends Event> EventHandlerRegistry<T> event(Class<T> event) {
    return context.event(event);
  }

  @Override
  public CompletableFuture<Void> start() {
    return context.start().thenRun(()->{});
  }

  @Override
  public CompletableFuture<Void> stop() {
    return service.stop();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
