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

package io.nonobot.core.adapter;

import io.nonobot.core.client.BotClient;
import io.nonobot.core.client.ReceiveOptions;
import io.vertx.core.Context;
import io.vertx.core.Handler;

import java.io.Console;
import java.io.PrintWriter;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ConsoleBotAdapter implements Handler<ConnectionRequest> {

  private Thread consoleThread;
  private final Console console = System.console();
  private final PrintWriter writer = console.writer();

  public ConsoleBotAdapter() {
  }

  @Override
  public void handle(ConnectionRequest event) {
    BotClient client = event.client();
    Context context = client.vertx().getOrCreateContext();
    synchronized (ConsoleBotAdapter.this) {
      consoleThread = new Thread(() -> {
        context.runOnContext(v -> {
          event.complete();
        });
        try {
          run(client);
        } finally {
          client.close();
        }
      });
      consoleThread.start();
    }
  }

  void run(BotClient client) {
    Console console = System.console();
    PrintWriter writer = console.writer();
    client.messageHandler(msg -> {
      if (msg.chatId().equals("console")) {
        System.out.println(msg.body());
        System.out.print("\n" + client.name() + "> ");
      }
    });
    while (true) {
      writer.write("\n" + client.name() + "> ");
      writer.flush();
      String line = console.readLine();
      if (line == null) {
        return;
      }
      client.receiveMessage(new ReceiveOptions().setChatId("console"), line, ar -> {
        if (ar.succeeded()) {
          System.out.println(ar.result());
          System.out.print("\n" + client.name() + "> ");
        }
      });
    }
  }
}
