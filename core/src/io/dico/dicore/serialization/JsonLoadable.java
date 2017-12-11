package io.dico.dicore.serialization;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public interface JsonLoadable {
    
    void writeTo(JsonWriter writer) throws IOException;
    
    void loadFrom(JsonReader reader) throws IOException;
}
