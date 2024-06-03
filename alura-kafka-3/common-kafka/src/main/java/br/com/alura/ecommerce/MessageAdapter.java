package br.com.alura.ecommerce;

import com.google.gson.*;

import java.lang.reflect.Type;

public class MessageAdapter implements JsonSerializer<Message>, JsonDeserializer<Message> {

    @Override
    public JsonElement serialize(Message message, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", message.getPayload().getClass().getName());
        object.add("correlationId", context.serialize(message.getId()));
        object.add("payload", context.serialize(message.getPayload()));

        return object;
    }

    @Override
    public Message deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        var object = jsonElement.getAsJsonObject();
        var payloadType = object.get("type").getAsString();
        var correlationId = (CorrelationId) context.deserialize(object.get("correlationId"), CorrelationId.class);
        try {
            // maybe you want to use a "accept list"
            var payload = context.deserialize(object.get("payload"), Class.forName(payloadType));
            return new Message(correlationId, payload);
        } catch (ClassNotFoundException e) {
            // you might want to deal with this exception
            throw new JsonParseException(e);
        }
    }
}
