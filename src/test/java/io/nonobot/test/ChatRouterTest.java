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

package io.nonobot.test;

import io.nonobot.core.Bot;
import io.nonobot.core.client.BotClient;
import io.nonobot.core.client.ReceiveOptions;
import io.nonobot.core.chat.ChatRouter;
import io.nonobot.core.chat.SendOptions;
import io.nonobot.core.chat.impl.ChatRouterImpl;
import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ChatRouterTest extends BaseTest {

  ChatRouter router;

  @Override
  public void before() {
    super.before();
    router = Bot.create(vertx).chatRouter();
  }

  @Test
  public void testRespond1(TestContext context) {
    testRespond(context, "nono echo hello world");
  }

  @Test
  public void testRespond2(TestContext context) {
    testRespond(context, "nono:echo hello world");
  }

  @Test
  public void testRespond3(TestContext context) {
    testRespond(context, "@nono echo hello world");
  }

  @Test
  public void testRespond4(TestContext context) {
    testRespond(context, "@nono:echo hello world");
  }

  private void testRespond(TestContext context, String message) {
    Async handleLatch = context.async();
    router.respond("^echo\\s+(.+)", msg -> {
          context.assertEquals("echo hello world", msg.body());
          handleLatch.complete();
        });
    BotClient.client(vertx, context.asyncAssertSuccess(client -> {
      client.receiveMessage(new ReceiveOptions(), message, ar -> {});
    }));
  }

  @Test
  public void testRespondGroup(TestContext context) {
    Async handleLatch = context.async();
    router.respond("^echo ([a-z]+) ([0-9]+)", msg -> {
      context.assertEquals("echo hello 12345", msg.body());
      context.assertNull(msg.matchedGroup(0));
      context.assertEquals("hello", msg.matchedGroup(1));
      context.assertEquals("12345", msg.matchedGroup(2));
      context.assertNull(msg.matchedGroup(3));
      handleLatch.complete();
    });
    BotClient.client(vertx, context.asyncAssertSuccess(client -> {
      client.receiveMessage(new ReceiveOptions(), "nono echo hello 12345", ar -> {});
    }));
  }

  @Test
  public void testMatch(TestContext context) {
    Async handleLatch = context.async();
    router.when("^echo\\s+(.+)", msg -> {
          context.assertEquals("echo hello world", msg.body());
          handleLatch.complete();
        });
    BotClient.client(vertx, context.asyncAssertSuccess(client -> {
      client.receiveMessage(new ReceiveOptions(), "echo hello world", ar -> {});
    }));
  }

  @Test
  public void testMatchGroup(TestContext context) {
    Async handleLatch = context.async();
    router.when("^([a-z]+) ([0-9]+)", msg -> {
      context.assertEquals("hello 12345", msg.body());
      context.assertNull(msg.matchedGroup(0));
      context.assertEquals("hello", msg.matchedGroup(1));
      context.assertEquals("12345", msg.matchedGroup(2));
      context.assertNull(msg.matchedGroup(3));
      handleLatch.complete();
    });
    BotClient.client(vertx, context.asyncAssertSuccess(client -> {
      client.receiveMessage(new ReceiveOptions(), "hello 12345", ar -> {});
    }));
  }

  @Test
  public void testTimeout(TestContext context) {
    Async failureLatch = context.async();
    BotClient.client(vertx, context.asyncAssertSuccess(client -> {
      client.receiveMessage(new ReceiveOptions().setTimeout(300), "echo hello world", ar -> {
        context.assertTrue(ar.failed());
        failureLatch.complete();
      });
    }));
  }

  @Test
  public void testHandlerOrder(TestContext context) {
    Async doneLatch = context.async();
    Bot bot = Bot.create(vertx);
    router.when("foobar", msg -> {
      msg.reply("1");
      doneLatch.countDown();
    });
    router.when("foobar", msg -> {
      context.fail();
    });
    BotClient.client(vertx, context.asyncAssertSuccess(client -> {
      client.receiveMessage(new ReceiveOptions(), "foobar", ar -> {
      });
    }));
  }

  @Test
  public void testConcurrentReplies(TestContext context) {
    Async doneLatch = context.async(2);
    Future<Void> replied = Future.future();
    router.when("foobar", msg -> {
      msg.reply("1", context.asyncAssertSuccess());
    });
    new ChatRouterImpl(vertx, "nono").when("foobar", msg -> {
      replied.setHandler(v1 -> {
        msg.reply("2", 200, context.asyncAssertFailure(v2 -> {
          doneLatch.countDown();
        }));
      });
    });
    BotClient.client(vertx, context.asyncAssertSuccess(client -> {
      client.receiveMessage(new ReceiveOptions(), "foobar", ar -> {
        context.assertTrue(ar.succeeded());
        context.assertEquals("1", ar.result());
        replied.complete();
        doneLatch.countDown();
      });
    }));
  }

  @Test
  public void testOverrideBotName(TestContext context) {
    Async handleLatch = context.async(2);
    router.respond("^echo\\s+(.+)", msg -> {
      context.assertEquals("echo hello world", msg.body());
      msg.reply("the_reply");
    });
    BotClient.client(vertx, context.asyncAssertSuccess(client -> {
      client.alias(Arrays.asList("bb8", "r2d2"));
      client.receiveMessage(new ReceiveOptions(), "bb8 echo hello world", ar -> {
        handleLatch.countDown();
      });
      client.receiveMessage(new ReceiveOptions(), "r2d2 echo hello world", ar -> {
        handleLatch.countDown();
      });
    }));
  }

  @Test
  public void testIdentity(TestContext context) {
    Async doneLatch = context.async();
    router.when("foobar", msg -> {
      context.assertEquals("the_chat_id", msg.chatId());
      doneLatch.countDown();
    });
    BotClient.client(vertx, context.asyncAssertSuccess(client -> {
      client.receiveMessage(new ReceiveOptions().
          setChatId("the_chat_id"), "foobar", ar -> {
      });
    }));
  }

  @Test
  public void testSendMessage(TestContext context) {
    Async doneLatch = context.async();
    BotClient.client(vertx, context.asyncAssertSuccess(client -> {
      client.messageHandler(msg -> {
        context.assertEquals("the_chat_id", msg.chatId());
        context.assertEquals("the_message", msg.body());
        doneLatch.complete();
      });
    }));
    router.sendMessage(new SendOptions().setChatId("the_chat_id"), "the_message");
  }
}
