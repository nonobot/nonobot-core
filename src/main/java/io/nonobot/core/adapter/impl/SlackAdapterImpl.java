package io.nonobot.core.adapter.impl;

import io.nonobot.core.NonoBot;
import io.nonobot.core.adapter.SlackAdapter;
import io.nonobot.core.adapter.SlackOptions;
import io.nonobot.core.client.BotClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SlackAdapterImpl implements SlackAdapter {

  private final NonoBot bot;
  private final SlackOptions options;
  private Handler<Void> closeHandler;
  private WebSocket websocket;
  private final Future<Void> completion = Future.future();
  private long serial;
  private Set<String> channels = new HashSet<>(); // All channels we belong to
  private String id; // Our own id

  public SlackAdapterImpl(NonoBot bot, SlackOptions options) {
    this.bot = bot;
    this.options = new SlackOptions(options);
  }

  @Override
  public void connect() {
    connect(null);
  }

  @Override
  public synchronized void connect(Handler<AsyncResult<Void>> completionHandler) {
    if (websocket != null) {
      throw new IllegalStateException("Already connected");
    }

    Vertx vertx = bot.vertx();
    completion.setHandler(completionHandler);

    HttpClient client = vertx.createHttpClient(options.getClientOptions());
    String token = this.options.getToken();

    HttpClientRequest req = client.get("/api/rtm.start?token=" + token, resp -> {
      if (resp.statusCode() == 200) {
        Buffer buffer = Buffer.buffer();
        resp.handler(buffer::appendBuffer);
        resp.exceptionHandler(err -> {
          if (!completion.isComplete()) {
            completion.fail(err);
          }
        });
        resp.endHandler(v1 -> {
          JsonObject respObj = buffer.toJsonObject();
          URL wsURL;
          try {
            wsURL = new URL(respObj.getString("url").replace("ws", "http"));
          } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
          }
          int port = wsURL.getPort();
          if (port == -1) {
            if (options.getClientOptions().isSsl()) {
              port = 443;
            } else {
              port = 80;
            }
          }

          // Collect all the channels we belong to
          JsonArray channelsObj = respObj.getJsonArray("channels");
          for (int i = 0;i < channelsObj.size();i++) {
            JsonObject channelObj = channelsObj.getJsonObject(i);
            if (channelObj.getBoolean("is_member")) {
              joinChannel(channelObj.getString("id"));
            }
          }

          id = respObj.getJsonObject("self").getString("id");
          client.websocket(port, wsURL.getHost(), wsURL.getPath(), ws -> wsOpen(ws),
              err -> {
                if (!completion.isComplete()) {
                  completion.fail(err);
                }
              });
        });
      } else {
        System.out.println("Got response " + resp.statusCode() + " / " + resp.statusMessage());
      }
    });
    req.exceptionHandler(err -> {
      if (completionHandler != null && !completion.isComplete()) {
        completion.fail(err);
        completionHandler.handle(completion);
      }
    });
    req.end();
  }

  private synchronized void schedulePing(long expectedSerial) {
    bot.vertx().setTimer(4000, timerID -> {
      synchronized (SlackAdapterImpl.this) {
        if (websocket != null) {
          if (expectedSerial == serial) {
            websocket.writeFinalTextFrame(new JsonObject().put("type", "ping").put("id", UUID.randomUUID().toString()).encode());
          }
          schedulePing(++serial);
        }
      }
    });
  }

  private void wsOpen(WebSocket ws) {
    synchronized (this) {
      websocket = ws;
    }
    LinkedList<WebSocketFrame> pendingFrames = new LinkedList<>();
    AtomicReference<Handler<WebSocketFrame>> handler = new AtomicReference<>();
    bot.client(ar -> {
      if (ar.succeeded()) {
        BotClient client = ar.result();
        schedulePing(serial);
        handler.set(frame -> {
          wsHandle(frame, client);
        });
        for (WebSocketFrame frame : pendingFrames) {
          wsHandle(frame, client);
        }
      } else {
        close();
      }
    });
    ws.frameHandler(frame -> {
      if (handler.get() != null) {
        handler.get().handle(frame);
      } else {
        pendingFrames.add(frame);
      }
    });
    ws.closeHandler(this::wsClose);
    if (!completion.isComplete()) {
      completion.complete();
    }
  }

  private synchronized void leaveChannel(String channel) {
    channels.remove(channel);
  }

  private synchronized void joinChannel(String channel) {
    channels.add(channel);
  }

  private synchronized void handleMessage(BotClient client, String text, String channel) {
    String msg;
    if (channels.contains(channel)) {
      if (text.startsWith("<@" + id + ">")) {
        msg = text.substring(id.length() + 3); // Replace mention to self
      } else {
        return;
      }
    } else {
      msg = text;
    }
    client.process(msg, reply -> {
      if (reply.succeeded()) {
        synchronized (SlackAdapterImpl.this) {
          serial++; // Need to sync on SlackAdapterImpl
          websocket.writeFinalTextFrame(new JsonObject().
              put("id", UUID.randomUUID().toString()).
              put("type", "message").
              put("channel", channel).
              put("text", reply.result()).encode());
        }
      } else {
        System.out.println("no reply for " + msg);
        reply.cause().printStackTrace();
      }
    });
  }

  void wsHandle(WebSocketFrame frame, BotClient client) {
    JsonObject json = new JsonObject(frame.textData());
    System.out.println(json);
    String type = json.getString("type", "");
    switch (type) {
      case "channel_left":
        leaveChannel(json.getString("channel"));
        break;
      case "channel_joined":
        joinChannel(json.getJsonObject("channel").getString("id"));
        break;
      case "message":
        String text = json.getString("text");
        String channel = json.getString("channel");
        if (text != null) {
          handleMessage(client, text, channel);
        } else {
          // What case ?
        }
        break;
    }
  }

  private void wsClose(Void v) {
    Handler<Void> handler;
    synchronized (this) {
      websocket = null;
      handler = closeHandler;
    }
    if (handler != null) {
      handler.handle(null);
    }
  }

  @Override
  public synchronized void closeHandler(Handler<Void> handler) {
    closeHandler = handler;
  }

  @Override
  public synchronized void close() {
    if (websocket != null) {
      websocket.close();
    }
  }
}
