module Finagle
  module Http
    class AbstractClient
      # options = {}
      # :host => String
      # :connection_limit => Numeric
      def initialize(options = {})
      end

      # request - Finagle::Http::Request
      def apply(request)
      end
    end
  end
end