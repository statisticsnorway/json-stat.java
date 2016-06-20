package org.jsonstat;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by hadrien on 07/06/16.
 *
 * @see <a href="https://json-stat.org/format/#version">json-stat.org/format/#version</a>
 */
public class JsonStat {

    private final Version version;

    private final Class clazz;

    public JsonStat(Version version, Class clazz) {
        this.version = version;
        this.clazz = clazz;
    }

    public String getVersion() {
        return version.getTag();
    }

    @JsonProperty("class")
    public String getClazz() {
        return clazz.toString().toLowerCase();
    }

    public enum Version {

        ONE("1.0"), TWO("2.0");

        private final String tag;

        Version(final String tag) {
            this.tag = tag;
        }

        String getTag() {
            return this.tag;
        }
    }

    public enum Class {
        DATASET,
        DIMENSION,
        COLLECTION
    }

}
