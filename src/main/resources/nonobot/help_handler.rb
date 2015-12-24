require 'vertx/vertx'
require 'nonobot/chat_handler'
require 'vertx/util/utils.rb'
# Generated from io.nonobot.core.handlers.HelpHandler
module Nonobot
  #  @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
  class HelpHandler
    # @private
    # @param j_del [::Nonobot::HelpHandler] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::Nonobot::HelpHandler] the underlying java delegate
    def j_del
      @j_del
    end
    # @return [::Nonobot::HelpHandler]
    def self.create
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoNonobotCoreHandlers::HelpHandler.java_method(:create, []).call(),::Nonobot::HelpHandler)
      end
      raise ArgumentError, "Invalid arguments when calling create()"
    end
    # @param [::Vertx::Vertx] vertx 
    # @return [::Nonobot::ChatHandler]
    def to_chat_handler(vertx=nil)
      if vertx.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:toChatHandler, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::Nonobot::ChatHandler)
      end
      raise ArgumentError, "Invalid arguments when calling to_chat_handler(vertx)"
    end
  end
end