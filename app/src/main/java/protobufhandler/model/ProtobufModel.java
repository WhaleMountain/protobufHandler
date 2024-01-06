package protobufhandler.model;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class ProtobufModel {
    private final String protoDescPath;
    private List<Descriptor> messageTypes;

    public ProtobufModel(String protoDescPath) {
        this.protoDescPath = protoDescPath;
        try {
            messageTypes = getMessageTypesFromProtoFile(protoDescPath);
        } catch(IOException | DescriptorValidationException e) { 
            //TODO
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

    private List<Descriptor> getMessageTypesFromProtoFile(String protoFilePath) throws IOException, Descriptors.DescriptorValidationException {
        // .protoファイルを読み込み、FileDescriptorSetに変換
        FileDescriptorSet set = FileDescriptorSet.parseFrom(new FileInputStream(protoFilePath));

        // FileDescriptorSetからFileDescriptorを取得
        FileDescriptor fileDescriptor = FileDescriptor.buildFrom(set.getFile(0), new FileDescriptor[] {});

        return fileDescriptor.getMessageTypes();
    }

    /*public Descriptor getDescriptorFromProtoFile(String protoFilePath, String messageTypeName) throws IOException, Descriptors.DescriptorValidationException {
        // .protoファイルを読み込み、FileDescriptorSetに変換
        FileDescriptorSet set = FileDescriptorSet.parseFrom(new FileInputStream(protoFilePath));

        // FileDescriptorSetからFileDescriptorを取得
        FileDescriptor fileDescriptor = FileDescriptor.buildFrom(set.getFile(0), new FileDescriptor[] {});

        // FileDescriptorから指定したメッセージ型のDescriptorを取得
        Descriptor descriptor = fileDescriptor.findMessageTypeByName(messageTypeName);

        return descriptor;
    }*/
}
