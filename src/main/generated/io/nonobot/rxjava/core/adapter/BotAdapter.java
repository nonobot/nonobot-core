/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.nonobot.rxjava.core.adapter;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.nonobot.core.client.ClientOptions;
import io.vertx.rxjava.core.Vertx;
import io.nonobot.rxjava.core.client.BotClient;
import io.vertx.core.Handler;
import io.vertx.rxjava.core.Future;

/**
 * Expose the bot to an external (usually remote) service.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.nonobot.core.adapter.BotAdapter original} non RX-ified interface using Vert.x codegen.
 */

public class BotAdapter {

  final io.nonobot.core.adapter.BotAdapter delegate;

  public BotAdapter(io.nonobot.core.adapter.BotAdapter delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Create new adapter.
   * @param vertx the vertx instance to use
   * @return the bot adapter
   */
  public static BotAdapter create(Vertx vertx) { 
    BotAdapter ret= BotAdapter.newInstance(io.nonobot.core.adapter.BotAdapter.create((io.vertx.core.Vertx) vertx.getDelegate()));
    return ret;
  }

  /**
   * Run the bot adapter, until it is closed.
   * @param options the client options to use
   */
  public void run(ClientOptions options) { 
    this.delegate.run(options);
  }

  public boolean isRunning() { 
    boolean ret = this.delegate.isRunning();
    return ret;
  }

  public boolean isConnected() { 
    boolean ret = this.delegate.isConnected();
    return ret;
  }

  /**
   * Set the connection request handler.
   * @param handler the connection request handler
   * @return this object so it can be used fluently
   */
  public BotAdapter requestHandler(Handler<ConnectionRequest> handler) { 
    this.delegate.requestHandler(new Handler<io.nonobot.core.adapter.ConnectionRequest>() {
      public void handle(io.nonobot.core.adapter.ConnectionRequest event) {
        handler.handle(new ConnectionRequest(event));
      }
    });
    return this;
  }

  /**
   * Like {@link io.nonobot.rxjava.core.adapter.BotAdapter#connect}.
   * @param client 
   * @return 
   */
  public BotAdapter connect(BotClient client) { 
    this.delegate.connect((io.nonobot.core.client.BotClient) client.getDelegate());
    return this;
  }

  /**
   * Connect to the adapted service.
   * @param client the client to use
   * @param completionFuture the future to complete or fail when connection is either a success or a failure
   * @return this object so it can be used fluently
   */
  public BotAdapter connect(BotClient client, Future<Void> completionFuture) { 
    this.delegate.connect((io.nonobot.core.client.BotClient) client.getDelegate(), (io.vertx.core.Future<java.lang.Void>) completionFuture.getDelegate());
    return this;
  }

  /**
   * Close the adapter.
   */
  public void close() { 
    this.delegate.close();
  }


  public static BotAdapter newInstance(io.nonobot.core.adapter.BotAdapter arg) {
    return arg != null ? new BotAdapter(arg) : null;
  }
}
