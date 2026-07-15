package protobufhandler.model;

import java.util.List;

import com.google.protobuf.Descriptors.Descriptor;

import burp.api.montoya.core.ToolType;


import java.util.ArrayList;

public class AppModel {
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
    private String messageType; // 選択されたメッセージ型名 (descriptor の永続化用表現)
    private transient Descriptor descriptor; // .desc から実行時に再構築するため永続化しない
    private List<String> cachedMessageTypes;
    private List<String> toolScope;
    private boolean requestHandling; // true: Request, false: Response
    private String replaceResponseBody;
    private String comment;

    public AppModel() {
        this.enabled = false;
        this.scope = "";
        this.protoDescPath = "";
        this.messageType = "";
        this.descriptor = null;
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

    public String getMessageType() {
        return messageType;
    }

    public Descriptor getDescriptor() {
        return descriptor;
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

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setDescriptor(Descriptor descriptor) {
        this.descriptor = descriptor;
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
        this.toolScope.clear();
    }

}
