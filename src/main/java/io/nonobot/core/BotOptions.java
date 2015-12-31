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

package io.nonobot.core;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject
public class BotOptions {

  public static final String DEFAULT_NAME = "nono";

  private String name;

  public BotOptions() {
    name = DEFAULT_NAME;
  }

  public BotOptions(JsonObject json) {
    throw new UnsupportedOperationException();
  }

  public BotOptions(BotOptions that) {
    name = that.name;
  }

  public String getName() {
    return name;
  }

  public BotOptions setName(String name) {
    this.name = name;
    return this;
  }
}
