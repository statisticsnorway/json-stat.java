/*
 * The MIT License
 *
 * Copyright 2022 Statistisk sentralbyrå - Statistics Norway
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package no.ssb.jsonstat;

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
