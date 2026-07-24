package protobufhandler.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.google.protobuf.Descriptors.Descriptor;

import burp.api.montoya.core.ToolType;


import java.util.ArrayList;

public class AppModel implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final ToolType[] RULE_TARGE_TOOL_TYPE = {
        ToolType.PROXY,
        ToolType.REPEATER,
        ToolType.INTRUDER,
        ToolType.SCANNER,
        ToolType.EXTENSIONS
    };

    private boolean enabled;
    private String scope;
    private String protoDescPath;
    // Descriptor は protobuf の型でシリアライズ不可なので transient にし、
    // 選択中の message type 名を descriptorName に退避して保存する。
    private transient Descriptor descriptor;
    private String descriptorName;
    private List<String> cachedMessageTypes;
    private List<String> toolScope;
    private boolean requestHandling; // true: Request, false: Response
    private String replaceResponseBody;
    private String comment;

    public AppModel() {
        this.enabled = false;
        this.scope = "";
        this.protoDescPath = "";
        this.descriptor = null;
        this.descriptorName = "";
        this.cachedMessageTypes = new ArrayList<>();
        this.toolScope = new ArrayList<>();
        this.requestHandling = true;
        this.replaceResponseBody = "";
        this.comment = "";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getScope() {
        return scope;
    }

    public String getProtoDescPath() {
        return protoDescPath;
    }

    public Descriptor getDescriptor() {
        return descriptor;
    }

    // 保存時に退避された message type 名（読み込み後の descriptor 再構築に使う）
    public String getDescriptorName() {
        return descriptorName;
    }

    public List<String> getCachedMessageTypes() {
        return this.cachedMessageTypes;
    }

    public List<String> getToolScope() {
        return toolScope;
    }

    public boolean isRequestHandling() {
        return requestHandling;
    }

    public String getReplaceResponseBody() {
        return replaceResponseBody;
    }

    public String getComment() {
        return comment;
    }


    public void setEnabled(boolean flag) {
        this.enabled = flag;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setProtoDescPath(String path) {
        this.protoDescPath = path;
    }

    public void setDescriptor(Descriptor descriptor) {
        this.descriptor = descriptor;
        // 保存/ログ用に message type 名も同期しておく（descriptor は transient のため）
        if (descriptor != null) {
            this.descriptorName = descriptor.getName();
        }
    }

    public void setCachedMessageType(String messageType) {
        if (this.cachedMessageTypes.contains(messageType)) {
            return;
        }
        
        this.cachedMessageTypes.add(messageType);
    }

    public void setToolScope(String toolName) {
        if (this.toolScope.contains(toolName)) {
            return;
        }
        
        this.toolScope.add(toolName);
    }

    public void setRequestHandling(boolean flag) { // true: Request, false: Responce
        this.requestHandling = flag;
    }

    public void setReplaceResponseBody(String body) {
        this.replaceResponseBody = body;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void removeToolScope(String toolName) {
        if (!this.toolScope.contains(toolName)) {
            return;
        }
        
        this.toolScope.remove(toolName);
    }

    public void clearCachedMessageType() {
        this.cachedMessageTypes.clear();
    }

    public void clearToolScope() {
        this.cachedMessageTypes.clear();
    }

    // カスタムシリアライズ処理
    // Descriptor は transient のため、選択中の message type 名だけを退避して保存する。
    private void writeObject(ObjectOutputStream oos) throws IOException {
        if (Objects.nonNull(this.descriptor)) {
            this.descriptorName = this.descriptor.getName();
        }
        oos.defaultWriteObject();
    }

    // カスタムデシリアライズ処理
    // descriptor 本体は proto ファイルから再構築する必要があるため、ここでは復元しない
    // （読み込み側で protoDescPath と descriptorName を使って再構築する）。
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        this.descriptor = null;
    }

}
