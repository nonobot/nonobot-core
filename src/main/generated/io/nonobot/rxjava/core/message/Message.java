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

package io.nonobot.rxjava.core.message;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.nonobot.core.identity.Identity;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * A message sent to an handler.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.nonobot.core.message.Message original} non RX-ified interface using Vert.x codegen.
 */

public class Message {

  final io.nonobot.core.message.Message delegate;

  public Message(io.nonobot.core.message.Message delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public Identity room() { 
    Identity ret = this.delegate.room();
    return ret;
  }

  public Identity user() { 
    Identity ret = this.delegate.user();
    return ret;
  }

  /**
   * @return the message body
   * @return 
   */
  public String body() { 
    String ret = this.delegate.body();
    return ret;
  }

  /**
   * Reply to the message.
   * @param msg the reply
   */
  public void reply(String msg) { 
    this.delegate.reply(msg);
  }

  /**
   * Reply to the message with an acknowledgement handler.
   * @param msg the reply
   * @param ackHandler handler to be notified if the reply is consumed effectively
   */
  public void reply(String msg, Handler<AsyncResult<Void>> ackHandler) { 
    this.delegate.reply(msg, ackHandler);
  }

  /**
   * Reply to the message with an acknowledgement handler.
   * @param msg the reply
   * @return 
   */
  public Observable<Void> replyObservable(String msg) { 
    io.vertx.rx.java.ObservableFuture<Void> ackHandler = io.vertx.rx.java.RxHelper.observableFuture();
    reply(msg, ackHandler.toHandler());
    return ackHandler;
  }

  /**
   * Reply to the message with an acknowledgement handler given a <code>timeout</code>.
   * @param msg the reply
   * @param ackTimeout the acknowledgement timeout
   * @param ackHandler handler to be notified if the reply is consumed effectively
   */
  public void reply(String msg, long ackTimeout, Handler<AsyncResult<Void>> ackHandler) { 
    this.delegate.reply(msg, ackTimeout, ackHandler);
  }

  /**
   * Reply to the message with an acknowledgement handler given a <code>timeout</code>.
   * @param msg the reply
   * @param ackTimeout the acknowledgement timeout
   * @return 
   */
  public Observable<Void> replyObservable(String msg, long ackTimeout) { 
    io.vertx.rx.java.ObservableFuture<Void> ackHandler = io.vertx.rx.java.RxHelper.observableFuture();
    reply(msg, ackTimeout, ackHandler.toHandler());
    return ackHandler;
  }


  public static Message newInstance(io.nonobot.core.message.Message arg) {
    return arg != null ? new Message(arg) : null;
  }
}
