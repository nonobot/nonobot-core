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

package io.nonobot.groovy.core.handler;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.nonobot.core.identity.Identity
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * A message sent to an handler.
*/
@CompileStatic
public class Message {
  private final def io.nonobot.core.handler.Message delegate;
  public Message(Object delegate) {
    this.delegate = (io.nonobot.core.handler.Message) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public Map<String, Object> room() {
    def ret = (Map<String, Object>)InternalHelper.wrapObject(this.delegate.room()?.toJson());
    return ret;
  }
  public Map<String, Object> user() {
    def ret = (Map<String, Object>)InternalHelper.wrapObject(this.delegate.user()?.toJson());
    return ret;
  }
  /**
   * @return the message body
   * @return 
   */
  public String body() {
    def ret = this.delegate.body();
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
   * Reply to the message with an acknowledgement handler given a <code>timeout</code>.
   * @param msg the reply
   * @param ackTimeout the acknowledgement timeout
   * @param ackHandler handler to be notified if the reply is consumed effectively
   */
  public void reply(String msg, long ackTimeout, Handler<AsyncResult<Void>> ackHandler) {
    this.delegate.reply(msg, ackTimeout, ackHandler);
  }
}