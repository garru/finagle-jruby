class Finagle::Rack::Wrapper
  def initialize(app)
    @app = app
  end

  def call(env)
    Future.value(@app.call(env))
  end
end
