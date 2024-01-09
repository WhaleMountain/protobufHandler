package protobufhandler.util;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class Protobuffer {
    // jsonをprotobufメッセージに変換する
    public static DynamicMessage jsonToProtobuf(String json, Descriptor descriptor) throws InvalidProtocolBufferException {
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
        JsonFormat.parser().merge(json, builder);
        return builder.build();
    }

    // protobufメッセージをjsonに変換する
    public static String protobufToJson(DynamicMessage message) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(message);
    }

    public static List<Descriptor> getMessageTypesFromProtoFile(String protoDescPath) throws IOException, Descriptors.DescriptorValidationException {
        FileInputStream protoFis = new FileInputStream(protoDescPath);
        FileDescriptorSet set = FileDescriptorSet.parseFrom(new BufferedInputStream(protoFis));
        protoFis.close();

        List<Descriptor> descriptors = new ArrayList<Descriptor>();
        List<FileDescriptor> dependenciesDescriptors = new ArrayList<FileDescriptor>();
        for(int i = 0; i < set.getFileCount(); i++) {
            FileDescriptor dependenciesDescriptor = FileDescriptor.buildFrom(set.getFile(i), dependenciesDescriptors.toArray(new FileDescriptor[dependenciesDescriptors.size()]));
            descriptors.addAll(dependenciesDescriptor.getMessageTypes());
            dependenciesDescriptors.add(dependenciesDescriptor);
        }

        //FileDescriptor fileDescriptor = FileDescriptor.buildFrom(set.getFile(set.getFileCount() - 1), dependenciesDescriptors.toArray(new FileDescriptor[dependenciesDescriptors.size()]));
        //descriptors.addAll(fileDescriptor.getMessageTypes());
        //return fileDescriptor.getMessageTypes();
        
        return descriptors;
    }
}
