require 'vertx/util/utils.rb'
# Generated from io.nonobot.core.Config
module Nonobot
  #  Configuration object.
  class Config
    # @private
    # @param j_del [::Nonobot::Config] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::Nonobot::Config] the underlying java delegate
    def j_del
      @j_del
    end
    #  Returns a config property given a <code>name</code>.
    # @param [String] name the property name
    # @return [String] the property value
    def get_property(name=nil)
      if name.class == String && !block_given?
        return @j_del.java_method(:getProperty, [Java::java.lang.String.java_class]).call(name)
      end
      raise ArgumentError, "Invalid arguments when calling get_property(name)"
    end
  end
end
