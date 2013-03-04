$:.unshift("../lib")
require 'finagle'
require 'rubygems'

class MyApp
  def self.call(env)
    @client ||= Finagle::Http::Client.new(:host => "google.com:80", :connection_limit => 100)
    @client.apply(Finagle::Http::Request.new("/", {}, "", "get")).map do |x|
      puts x
      [200, {}, x[2]]
    end
  end
end

Finagle::Rack::Handler.run(MyApp, :Port => 3001)