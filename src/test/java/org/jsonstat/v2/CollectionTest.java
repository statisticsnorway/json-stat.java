package org.jsonstat.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.io.Resources;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by hadrien on 07/06/16.
 */
public class CollectionTest {

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeMethod
    public void setUp() throws Exception {
        // TODO
        mapper.registerModule(new SimpleModule());
    }

    @Test(dependsOnMethods = "testDeserialize")
    public void testSerialize(String testFile) throws Exception {

        Collection deserialize = deserialize(Resources.getResource(testFile));

        mapper.writeValue(System.out, deserialize);

        // TODO: Check equivalence?

    }

    @Test
    public void testDeserialize(String testFile) throws Exception {

        Collection collection = deserialize(Resources.getResource(testFile));

        assertThat(collection).isNotNull();

    }

    private Collection deserialize(URL resource) throws java.io.IOException {
        return mapper.readValue(resource, Collection.class);
    }
}