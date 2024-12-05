package protobufhandler.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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
