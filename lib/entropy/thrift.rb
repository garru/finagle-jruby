class FinagleThriftClient
  attr_reader :finagle_client, :client_class, :server_list, :options, :client_methods

  def initialize(client_class, servers, options = {})
    @client_class = client_class
    @server_list = Array(servers).join(",")
    @options = options
    @finagle_client = Finagle::Thrift::Client.new(:host => @server_list, :client_id => @options[:client_id], :connection_limit => @options[:connection_limit])

    @client_methods = []
    @client_class.instance_methods.each do |method_name|
      if method_name != 'send_message' && method_name =~ /^send_(.*)$/
        instance_eval("def #{$1}(*args); async_proxy(:'#{$1}', *args); end", __FILE__, __LINE__)
        @client_methods << $1
      end
    end
  end

private
  def async_proxy(method_name, *args)
    input_buffer = Thrift::MemoryBufferTransport.new()
    output_buffer = Thrift::MemoryBufferTransport.new()
    client = @client_class.new(Thrift::BinaryProtocol.new(input_buffer),Thrift::BinaryProtocol.new(output_buffer))
    buffer = client.send("send_#{method_name}", *args)
    @finagle_client.apply(output_buffer.instance_variable_get(:@buf)).map do |response|
      input_buffer.write(response)
      client.send("recv_#{method_name}")
    end
  end
end