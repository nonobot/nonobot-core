/*
 * Copyright 2015 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nonobot.core.chat.impl;

import io.nonobot.core.chat.Message;
import io.nonobot.core.chat.ChatHandler;
import io.nonobot.core.chat.ChatRouter;
import io.nonobot.core.chat.SendOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ChatRouterImpl implements ChatRouter {

  static final class Key {

    final Vertx vertx;
    final String name;

    public Key(Vertx vertx, String name) {
      this.vertx = vertx;
      this.name = name;
    }

    public boolean equals(Object that) {
      return ((Key) that).vertx == vertx && ((Key) that).name.equals(name);
    }

    @Override
    public int hashCode() {
      return vertx.hashCode() ^ name.hashCode();
    }
  }

  static final ConcurrentMap<Key, ChatRouterImpl> routers = new ConcurrentHashMap<>();

  public static ChatRouter getShared(Vertx vertx, String name, Handler<AsyncResult<Void>> initHandler) {
    ChatRouterImpl router = routers.computeIfAbsent(new Key(vertx, name), key -> new ChatRouterImpl(key.vertx, key.name));
    if (initHandler != null) {
      Context context = vertx.getOrCreateContext();
      router.registerForInit(ar -> context.runOnContext(v -> initHandler.handle(ar)));
    }
    return router;
  }

  final Vertx vertx;
  final MessageConsumer<JsonObject> consumer;
  final List<MessageHandlerImpl> messageHandlers = new ArrayList<>();
  final List<Handler<AsyncResult<Void>>> initHandlers = new CopyOnWriteArrayList<>();
  final Future<Void> initFuture = Future.future();
  final String outboundAddress;

  public ChatRouterImpl(Vertx vertx, String name) {
    this.consumer = vertx.eventBus().consumer("bots." + name + ".inbound", this::handle);
    this.vertx = vertx;
    this.outboundAddress = "bots." + name + ".outbound";

    consumer.completionHandler(ar -> {
      if (ar.succeeded()) {
        initFuture.succeeded();
      } else {
        initFuture.fail(ar.cause());
      }
    });

    initFuture.setHandler(ar -> {
      synchronized (ChatRouterImpl.this) {
        for (Handler<AsyncResult<Void>> completionHandler : initHandlers) {
          completionHandler.handle(ar);
        }
      }
    });
  }

  private synchronized ChatRouterImpl registerForInit(Handler<AsyncResult<Void>> initHandler) {
    if (initFuture.isComplete()) {
      initHandler.handle(initFuture);
    } else {
      initHandlers.add(initHandler);
    }
    return this;
  }

  private void handle(io.vertx.core.eventbus.Message<JsonObject> message) {
    JsonObject body = message.body();
    boolean respond = body.getBoolean("respond");
    String content = body.getString("content");
    String replyAddress = body.getString("replyAddress");
    String chatId = body.getString("chatId");
    for (MessageHandlerImpl handler : messageHandlers) {
      if (handler.respond == respond) {
        Matcher matcher = handler.pattern.matcher(content);
        if (matcher.matches()) {
          handler.handler.handle(new Message() {
            boolean replied;
            @Override
            public String chatId() {
              return chatId;
            }
            @Override
            public String body() {
              return content;
            }
            @Override
            public String matchedGroup(int index) {
              if (index > 0 && index <= matcher.groupCount()) {
                return matcher.group(index);
              } else {
                return null;
              }
            }
            @Override
            public void reply(String msg) {
              reply(msg, null);
            }
            @Override
            public void reply(String msg, Handler<AsyncResult<Void>> ackHandler) {
              reply(msg, DeliveryOptions.DEFAULT_TIMEOUT, ackHandler);
            }
            @Override
            public void reply(String msg, long ackTimeout, Handler<AsyncResult<Void>> ackHandler) {
              if (!replied) {
                replied = true;
                if (ackHandler != null) {
                  vertx.eventBus().send(replyAddress, msg, new DeliveryOptions().setSendTimeout(ackTimeout), ack -> {
                    if (ack.succeeded()) {
                      ackHandler.handle(Future.succeededFuture());
                    } else {
                      ackHandler.handle(Future.failedFuture(ack.cause()));
                    }
                  });
                } else {
                  vertx.eventBus().send(replyAddress, msg);
                }
              } else if (ackHandler != null) {
                ackHandler.handle(Future.failedFuture("Already replied"));
              }
            }
          });
          return;
        }
      }
    }
    message.reply(null);
  }

  @Override
  public ChatHandler when(String pattern, Handler<Message> handler) {
    MessageHandlerImpl messageHandler = new MessageHandlerImpl(false, Pattern.compile(pattern), handler);
    messageHandlers.add(messageHandler);
    return messageHandler;
  }

  @Override
  public ChatHandler respond(String pattern, Handler<Message> handler) {
    MessageHandlerImpl messageHandler = new MessageHandlerImpl(true, Pattern.compile(pattern), handler);
    messageHandlers.add(messageHandler);
    return messageHandler;
  }

  @Override
  public ChatRouter sendMessage(SendOptions options, String body) {
    vertx.eventBus().publish(outboundAddress, new JsonObject().put("chatId", options.getChatId()).put("body", body));
    return this;
  }

  @Override
  public void close() {
    consumer.unregister();
  }

  class MessageHandlerImpl implements ChatHandler {
    final boolean respond;
    final Pattern pattern;
    final Handler<Message> handler;
    public MessageHandlerImpl(boolean respond, Pattern pattern, Handler<Message> handler) {
      this.respond = respond;
      this.pattern = pattern;
      this.handler = handler;
    }
    @Override
    public void close() {
      throw new UnsupportedOperationException("todo");
    }
  }
}
