package protobufhandler.model;

import burp.api.montoya.logging.Logging;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class ProtobufModel {
    private final Logging logging;
    private final String protoDescPath;
    private List<Descriptor> messageTypes;

    public ProtobufModel(String protoDescPath, Logging logging) {
        this.protoDescPath = protoDescPath;
        this.logging = logging;
        try {
            messageTypes = getMessageTypesFromProtoFile(protoDescPath);
        } catch(IOException | DescriptorValidationException e) { 
            //TODO: ファイルがない時どうする〜
        }
    }

    // jsonをprotobufメッセージに変換する
    public DynamicMessage jsonToProtobuf(String json, String messageTypeName) throws InvalidProtocolBufferException {
        for (Descriptor descriptor : messageTypes) {
            if(descriptor.getName().equals(messageTypeName)) {
                DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
                JsonFormat.parser().merge(json, builder);
                return builder.build();
            }
        }

        throw new IndexOutOfBoundsException("no message type");
    }

    // protobufメッセージをjsonに変換する
    public String protobufToJson(DynamicMessage message) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(message);
    }

    private List<Descriptor> getMessageTypesFromProtoFile(String protoDescPath) throws IOException, Descriptors.DescriptorValidationException {
        FileInputStream protoFis = new FileInputStream(protoDescPath);
        FileDescriptorSet set = FileDescriptorSet.parseFrom(new BufferedInputStream(protoFis));
        protoFis.close();

        ArrayList<FileDescriptor> dependenciesDescriptors = new ArrayList<FileDescriptor>();
        for(int i = 0; i < set.getFileCount(); i++) {
            FileDescriptor dependenciesDescriptor = FileDescriptor.buildFrom(set.getFile(i), dependenciesDescriptors.toArray(new FileDescriptor[dependenciesDescriptors.size()]));
            dependenciesDescriptors.add(dependenciesDescriptor);
        }

        FileDescriptor fileDescriptor = FileDescriptor.buildFrom(set.getFile(set.getFileCount() - 1), dependenciesDescriptors.toArray(new FileDescriptor[dependenciesDescriptors.size()]));

        return fileDescriptor.getMessageTypes();
    }
}
