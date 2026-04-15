package protobufhandler.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.util.JsonFormat;

public class Protobuffer {
    // jsonをprotobufメッセージに変換する
    public static DynamicMessage jsonToProtobuf(String json, Descriptor descriptor) throws InvalidProtocolBufferException {
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
        JsonFormat.parser().merge(json, builder);
        return builder.build();
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
        return JsonFormat.printer().print(builder.build());
    }

    // descriptor_setからmessageTypeを取得する
    public static List<Descriptor> getMessageTypesFromProtoFile(String protoDescPath) throws IOException, Descriptors.DescriptorValidationException {
        FileDescriptorSet set;
        try (FileInputStream fis = new FileInputStream(protoDescPath);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            set = FileDescriptorSet.parseFrom(bis);
        }

        List<Descriptor> descriptors = new ArrayList<>();
        List<FileDescriptor> dependencyDescriptors = new ArrayList<>();
        for (FileDescriptorProto descriptorProto : set.getFileList()) {
            FileDescriptor fileDescriptor = FileDescriptor.buildFrom(
                    descriptorProto,
                    dependencyDescriptors.toArray(new FileDescriptor[0]));
            descriptors.addAll(fileDescriptor.getMessageTypes());
            dependencyDescriptors.add(fileDescriptor);
        }

        descriptors.sort(Comparator.comparing(Descriptor::getName));
        return descriptors;
    }
}
