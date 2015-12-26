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

/** @module nonobot-js/ping_handler */
var utils = require('vertx-js/util/utils');
var ChatRouter = require('nonobot-js/chat_router');
var Vertx = require('vertx-js/vertx');
var ChatHandler = require('nonobot-js/chat_handler');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JPingHandler = io.nonobot.core.handlers.PingHandler;

/**

 @class
*/
var PingHandler = function(j_val) {

  var j_pingHandler = j_val;
  var that = this;

  /**

   @public
   @param vertx {Vertx} 
   @param router {ChatRouter} 
   @return {ChatHandler}
   */
  this.toChatHandler = function(vertx, router) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'object' && __args[1]._jdel) {
      return utils.convReturnVertxGen(j_pingHandler["toChatHandler(io.vertx.core.Vertx,io.nonobot.core.chat.ChatRouter)"](vertx._jdel, router._jdel), ChatHandler);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_pingHandler;
};

/**

 @memberof module:nonobot-js/ping_handler

 @return {PingHandler}
 */
PingHandler.create = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return utils.convReturnVertxGen(JPingHandler["create()"](), PingHandler);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = PingHandler;