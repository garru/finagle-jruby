package finagle;

import java.io.IOException;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.runtime.load.BasicLibraryService;

public class FinagleService implements BasicLibraryService {
    public boolean basicLoad(final Ruby runtime) throws IOException {
      RubyModule finagle = runtime.getOrCreateModule("Finagle");
      new com.twitter.finagle.jruby.RubyFutureLibrary().load(runtime, false);
      new com.twitter.finagle.jruby.StatsReceiverLibrary().load(runtime, finagle, false);
      loadRackLibrary(runtime, finagle);
      loadHttpLibrary(runtime, finagle);
      loadThriftLibrary(runtime, finagle);
      return true;
    }

    public void loadRackLibrary(final Ruby runtime, RubyModule finagle) throws IOException {
      RubyModule rack = finagle.defineOrGetModuleUnder("Rack");
      new com.twitter.finagle.jruby.rack.IOLibrary().load(runtime, rack, false);
      new com.twitter.finagle.jruby.rack.HandlerLibrary().load(runtime, rack, false);
    }

    public void loadHttpLibrary(final Ruby runtime, RubyModule finagle) throws IOException {
      RubyModule http = finagle.defineOrGetModuleUnder("Http");
      new com.twitter.finagle.jruby.http.ClientLibrary().load(runtime, http, false);
    }

    public void loadThriftLibrary(final Ruby runtime, RubyModule finagle) throws IOException {
      RubyModule thrift = finagle.defineOrGetModuleUnder("Thrift");
      new com.twitter.finagle.jruby.thrift.ClientLibrary().load(runtime, thrift, false);
    }
}

