$:.unshift File.expand_path('../lib', __FILE__)

require 'krypt/core/version'

Gem::Specification.new do |s|

  s.name = 'krypt-core'
  s.version = Krypt::Core::VERSION
  
  s.author = 'Hiroshi Nakamura, Martin Bosslet'
  s.email = 'Martin.Bosslet@gmail.com'
  s.homepage = 'https://github.com/krypt/krypt-core-java'
  s.summary = 'krypt-core API for JRuby'
  s.description = 'Java implementation of the krypt-core API'
  s.required_ruby_version     = '>= 1.9.3'

  s.files = %w(Rakefile LICENSE README.rdoc Manifest.txt) + Dir.glob('lib/**/*')
  s.require_path = "lib"
  s.license = 'MIT'

  s.platform = 'universal-java'
  s.add_dependency 'krypt-provider-jdk', Krypt::Core::VERSION

end
