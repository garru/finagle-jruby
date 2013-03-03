$:.unshift("../lib")
require 'finagle-jruby'
require 'rubygems'
require 'sinatra/base'

# class MyApp < Sinatra::Base
#   # ... app code here ...

#   get '/twitter' do
#     @client ||= Finagle::Http::Client.new(:host => "api.twitter.com:80", :connection_limit => 100)
#     @client.apply(Finagle::Http::Request.new("1/statuses/show.json?id=213372504465616897&include_entities=true", {}, "", "get")).map do |x|
#       [200, {}, x[2].read]
#     end
#   end

#   get "/hi/:hello" do |x|
#     Future.value(x)
#   end
# end

class MyApp
  def self.call(env)
    puts env.inspect
    @client ||= Finagle::Http::Client.new(:host => "api.twitter.com:80", :connection_limit => 100)
    @client.apply(Finagle::Http::Request.new("/1/statuses/show.json?id=213372504465616897&include_entities=true", {}, "", "get")).map do |x|
      [200, {}, x[2].read]
    end
  end
end


# class MyApp2
#   def self.call(env)
#     @client ||= Finagle::Http::Client.new(:host => "api.twitter.com:80", :connection_limit => 100)
#     blah = @client.apply(Finagle::Http::Request.new("/1/statuses/show.json?id=213372504465616897&include_entities=true", {}, "", "get")).apply()
#     [200, {}, blah[2].read]
#   end
# end

Finagle::Rack::Handler.run(MyApp, :Port => 3001)