package protobufhandler.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.FieldMask;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.util.FieldMaskUtil;
import com.google.protobuf.util.JsonFormat;

public class Protobuffer {
    // jsonをprotobufメッセージに変換する
    public static DynamicMessage jsonToProtobuf(String json, Descriptor descriptor) throws InvalidProtocolBufferException {
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
        JsonFormat.parser().merge(json, builder);
        overwriteFieldMaskPaths(builder, JsonParser.parseString(json).getAsJsonObject());
        return builder.build();
    }

    // FieldMask 型のフィールドを、元 JSON のパスで大小文字を保ったまま上書きする
    // (JsonFormat は camelCase→snake_case 変換で大文字を小文字化してしまうため)
    private static void overwriteFieldMaskPaths(Message.Builder builder, JsonObject jsonObj) {
        for (FieldDescriptor field : builder.getDescriptorForType().getFields()) {
            if (field.getType() == FieldDescriptor.Type.MESSAGE
                    && field.getMessageType().getFullName().equals("google.protobuf.FieldMask")
                    && jsonObj.has(field.getJsonName())) {                 // "updateMask"
                List<String> paths = new ArrayList<>();
                JsonElement el = jsonObj.get(field.getJsonName());
                if (el.isJsonPrimitive()) {                                // "Note,Rating"
                    for (String p : el.getAsString().split(",")) {
                        String t = p.trim();
                        if (!t.isEmpty()) paths.add(t);                    // 空パスを弾く
                    }
                } else if (el.isJsonObject() && el.getAsJsonObject().has("paths")) { // {"paths":[...]}
                    for (JsonElement p : el.getAsJsonObject().getAsJsonArray("paths")) {
                        String s = p.getAsString();
                        if (!s.isEmpty()) paths.add(s);                    // 空パスを弾く
                    }
                }
                Message.Builder fm = builder.newBuilderForField(field);
                FieldDescriptor pathsField = field.getMessageType().findFieldByName("paths");
                for (String p : paths) fm.addRepeatedField(pathsField, p);
                builder.setField(field, fm.build());
            }
        }
    }

    // descriptorがない場合にdecode_rawの形で返す
    public static String decodeRaw(byte[] message) throws InvalidProtocolBufferException {
        UnknownFieldSet.Builder builder = UnknownFieldSet.newBuilder();
        builder.mergeFrom(message);

        return builder.build().toString();
    }

    // protobufメッセージをjsonに変換する
    public static String protobufToJson(byte[] message, Descriptor descriptor) throws InvalidProtocolBufferException {
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
        builder.mergeFrom(message);
        DynamicMessage msg = builder.build();
        String json = JsonFormat.printer().print(msg);
        return restoreFieldMaskCase(json, msg);
    }

    // printer は FieldMask パスを snake_case→camelCase 変換して大文字を潰す ("Note" → "note")。
    // メッセージ上の生パスで JSON の値を上書きし、元の表記に戻す。
    private static String restoreFieldMaskCase(String json, DynamicMessage msg) throws InvalidProtocolBufferException {
        for (FieldDescriptor field : msg.getDescriptorForType().getFields()) {
            if (!isFieldMask(field) || !msg.hasField(field)) { continue; }
            FieldMask value = FieldMask.parseFrom(((Message) msg.getField(field)).toByteString());
            String printed = FieldMaskUtil.toJsonString(value);         // printer が出力した値 "note,rating"
            String verbatim = String.join(",", value.getPathsList());   // 生パス "Note,Rating"
            if (printed.equals(verbatim)) { continue; }
            json = json.replace(
                "\"" + field.getJsonName() + "\": \"" + printed + "\"",
                "\"" + field.getJsonName() + "\": \"" + verbatim + "\"");
        }
        return json;
    }

    private static boolean isFieldMask(FieldDescriptor field) {
        return field.getType() == FieldDescriptor.Type.MESSAGE
                && field.getMessageType().getFullName().equals("google.protobuf.FieldMask");
    }

    // descriptor_setからmessageTypeを取得する
    public static List<Descriptor> getMessageTypesFromProtoFile(String protoDescPath) throws IOException, Descriptors.DescriptorValidationException {
        FileInputStream protoFis = new FileInputStream(protoDescPath);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(protoFis);
        FileDescriptorSet set = FileDescriptorSet.parseFrom(bufferedInputStream);
        protoFis.close();
        bufferedInputStream.close();

        List<Descriptor> descriptors = new ArrayList<Descriptor>();
        List<FileDescriptor> dependenciesDescriptors = new ArrayList<FileDescriptor>();
        for (FileDescriptorProto descriptorProto : set.getFileList()) {
            FileDescriptor dependencieDescriptor = FileDescriptor.buildFrom(descriptorProto, dependenciesDescriptors.toArray(new FileDescriptor[dependenciesDescriptors.size()]));
            descriptors.addAll(dependencieDescriptor.getMessageTypes());
            dependenciesDescriptors.add(dependencieDescriptor);
        }

        return sortMessageType(descriptors);
    }

    private static List<Descriptor> sortMessageType(List<Descriptor> descriptors) {
        TreeMap<String, Descriptor> sortedTreeMap = new TreeMap<String, Descriptor>();
        for(Descriptor descriptor : descriptors) {
            sortedTreeMap.put(descriptor.getName(), descriptor);
        }
        List<Descriptor> sortedDescriptors = new ArrayList<Descriptor>(sortedTreeMap.values());

        return sortedDescriptors;
    }
}
