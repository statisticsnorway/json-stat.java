package org.jsonstat;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Created by hadrien on 08/06/16.
 */
public class JsonStatModule extends SimpleModule {

    private final String NAME = "JsonStatModule";

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);

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
