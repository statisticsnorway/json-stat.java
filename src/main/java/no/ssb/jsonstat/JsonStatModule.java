package no.ssb.jsonstat;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonStatModule extends SimpleModule {

    private final String NAME = "JsonStatModule";

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);

        context.addDeserializers(new JsonStatDeserializer());
        // TODO: Ensure Optional can be handled.
        // TODO: Ensure Instant can be handled.

    }

    @Override
    public String getModuleName() {
        return NAME;
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }
}
