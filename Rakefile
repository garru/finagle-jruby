# require 'bundler'
# Bundler::GemHelper.install_tasks
require 'rake'
require 'rake/testtask'
require 'rspec/core/rake_task'

RSpec::Core::RakeTask.new(:spec)

task :default => ['java:build', :spec]
task :build => ['java:build', :spec]
task :clean => ['java:clean', 'java:clobber', 'doc:clean']

require 'fileutils'

namespace :doc do
  desc "clean up generated docs"
  task :clean do
    d = 'doc'
    y = '.yardoc'
    FileUtils.rm_rf(d) if File.exist?(d) and File.writable?(d) and File.directory?(d)
    FileUtils.rm(y) if File.exist?(y) and File.writable?(y)
  end
end

namespace :java do

  desc "clean up java tool output"
  task :clean do
    system "cd ext;mvn --offline clean;cd .."
  end

  desc "delete the generated jar"
  task :clobber do
    FileUtils.rm Dir.glob('lib/java/*.jar')
  end

  desc "build java code and copy jars to lib folder"
  task :build => :clean do
    system "cd ext;mvn assembly:assembly;cp -vX target/*-with-dependencies.jar ../lib/finagle/finagle.jar;cd .."
  end
end

desc "Run tests"
Rake::TestTask.new :test do |t|
  t.libs << "lib"
  t.test_files = FileList["test/**/*.rb"]
end