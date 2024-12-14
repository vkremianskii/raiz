package net.kremianskii.roots.jooq;

import net.kremianskii.roots.jooq.Json.ObjectBuilder;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.kremianskii.roots.jooq.Json.ArrayBuilder.jsonArray;
import static net.kremianskii.roots.jooq.Json.ObjectBuilder.jsonObject;
import static net.kremianskii.roots.jooq.Json.jsonStringFromValue;
import static net.kremianskii.roots.jooq.Json.treeFromJsonString;
import static net.kremianskii.roots.jooq.Json.valueFromJsonString;
import static org.assertj.core.api.Assertions.assertThat;

class JsonTests {
    private final TestClass testObject = new TestClass(
            true,
            Long.MAX_VALUE,
            Double.MAX_VALUE,
            "Hello, world!",
            null);
    private final ArraysTestClass testArraysObject = new ArraysTestClass(
            List.of(false, true),
            List.of(Long.MIN_VALUE, Long.MAX_VALUE),
            List.of(Double.MIN_VALUE, Double.MAX_VALUE),
            List.of("", "Hello, world!"),
            List.of(new TestClass(
                    false,
                    Long.MIN_VALUE,
                    Double.MIN_VALUE,
                    "",
                    testObject)));

    private final ObjectBuilder jsonObject = jsonObject()
            .put("booleanField", true)
            .put("longField", Long.MAX_VALUE)
            .put("doubleField", Double.MAX_VALUE)
            .put("stringField", "Hello, world!");
    private final ObjectBuilder arraysJsonObject = jsonObject()
            .put("booleans", jsonArray().add(false).add(true))
            .put("longs", jsonArray().add(Long.MIN_VALUE).add(Long.MAX_VALUE))
            .put("doubles", jsonArray().add(Double.MIN_VALUE).add(Double.MAX_VALUE))
            .put("strings", jsonArray().add("").add("Hello, world!"))
            .put("objects", jsonArray().add(jsonObject()
                    .put("booleanField", false)
                    .put("longField", Long.MIN_VALUE)
                    .put("doubleField", Double.MIN_VALUE)
                    .put("stringField", "")
                    .put("objectField", jsonObject)));

    @Test
    void convertsValueToComplexJsonObjectAndBack() {
        // when
        var serialized = jsonStringFromValue(testArraysObject);
        var deserialized = valueFromJsonString(serialized, ArraysTestClass.class);

        // then
        assertThat(deserialized).isEqualTo(testArraysObject);
    }

    @Test
    void convertsJsonObjectToTree() {
        // given
        var serialized = arraysJsonObject.build().toString();

        // when
        var tree = treeFromJsonString(serialized);

        // then
        assertThat(tree).isEqualTo(arraysJsonObject.build());

    }

    @Test
    void buildsComplexJsonObjectThatIsValid() {
        // when
        var serialized = arraysJsonObject.build().toString();

        // then
        var deserialized = valueFromJsonString(serialized, ArraysTestClass.class);
        assertThat(deserialized).isEqualTo(testArraysObject);
    }

    private record TestClass(boolean booleanField,
                             long longField,
                             double doubleField,
                             String stringField,
                             @Nullable TestClass objectField) {
    }

    private record ArraysTestClass(List<Boolean> booleans,
                                   List<Long> longs,
                                   List<Double> doubles,
                                   List<String> strings,
                                   List<TestClass> objects) {
    }
}
