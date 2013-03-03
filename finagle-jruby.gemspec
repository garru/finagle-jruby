# -*- encoding: utf-8 -*-
$:.push File.expand_path("../lib", __FILE__)

Gem::Specification.new do |s|
  s.name        = "finagle-jruby"
  s.version     = "0.0.1"
  s.platform    = Gem::Platform::CURRENT
  s.authors     = ["Gary Tsang"]
  s.email       = ["gary@twitter.com"]
  s.homepage    = "http://www.github.com/garru/finagle-jruby"
  s.summary     = %q{A Finagle Bridge for JRuby}
  s.description = %q{Finagle on JRuby see: http://twitter.github.com/finagle}

  s.files         = `git ls-files`.split("\n")
  s.test_files    = `git ls-files -- {spec,examples}/*`.split("\n")
  s.executables   = `git ls-files -- bin/*`.split("\n").map{ |f| File.basename(f) }
  s.require_paths = ["lib"]

  s.add_runtime_dependency "rack"
end
