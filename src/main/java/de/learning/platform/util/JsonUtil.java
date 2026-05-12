package de.learning.platform.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public static <T> T readSingle(File file, Class<T> clazz) throws IOException {
        return mapper.readValue(file, clazz);
    }

    public static <T> List<T> readList(File file, Class<T> clazz) throws IOException {
        CollectionType listType = mapper.getTypeFactory()
                .constructCollectionType(List.class, clazz);
        return mapper.readValue(file, listType);
    }

    public static void write(Object obj, File file) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, obj);
    }
}
