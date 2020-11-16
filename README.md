Implementation of JSON Stat in Java - http://json-stat.org


Status
======

[![Build Status](https://travis-ci.org/statisticsnorway/json-stat.java.svg?branch=master)](https://travis-ci.org/statisticsnorway/json-stat.java)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/no.ssb.jsonstat/json-stat-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/no.ssb.jsonstat/json-stat-java)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/no.ssb.jsonstat/json-stat-java/badge.svg)](http://www.javadoc.io/doc/no.ssb.jsonstat/json-stat-java)
[![Gitter Chat](https://img.shields.io/gitter/room/nwjs/nw.js.svg)](https://gitter.im/json-stat-java/Lobby)

Usage
=====

Add json stat dependency into your project:

````xml
<dependency>
            <groupId>no.ssb.jsonstat</groupId>
            <artifactId>json-stat-java</artifactId>
            <version>0.2.5</version>
</dependency>
````

Register the module
````java
class Example {
    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JsonStatModule());
    }
}
````
Create a new json stat data set


````java
class Example {
    static {
        Dataset.Builder builder = Dataset.create().withLabel("My dataset");

        builder.withDimension(
            Dimension.create("year")
                .withRole(Dimension.Roles.TIME)
                .withIndexedLabels(ImmutableMap.of("2003", "2003", "2004", "2004", "2005", "2005")
            )
        );

        builder.withDimension(
            Dimension.create("month").withRole(Dimension.Roles.TIME)
                .withIndexedLabels(ImmutableMap.of("may", "may", "june", "june", "july", "july")
            )
        );

        builder.withDimension(
            Dimension.create("week").withTimeRole()
                .withIndexedLabels(ImmutableMap.of("30", "30", "31", "31", "32", "32")
            )
        );

        builder.withDimension(
            Dimension.create("population").withMetricRole()
                     .withIndexedLabels(ImmutableMap.of(
                                "A", "active population",
                                "E", "employment",
                                "U", "unemployment",
                                "I", "inactive population",
                                "T", "population 15 years old and over"))
        );

        builder.withDimension(
            Dimension.create("amount").withMetricRole()
                .withIndexedLabels(ImmutableMap.of("millions", "millions"))
        );

        builder.withDimension(
            Dimension.create("percent").withMetricRole()
                .withIndexedLabels(ImmutableMap.of("%", "percent"))
        );

        Dataset dataset = builder.withMapper(
                dimensions -> newArrayList(
                        dimensions.hashCode(),
                        dimensions.hashCode())
        );
    }
}

````

Deserialize a dataset 

````java
class Example {
    static {
        mapper = new ObjectMapper();
        
        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        
        mapper.registerModule(new JsonStatModule());

        Dataset.Builder builder = mapper.readValue("{ ... }", Dataset.Builder.class);
        // Or
        Dataset dataset = mapper.readValue("{ ... }", Dataset.class);
    }
}
````


