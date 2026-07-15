package protobufhandler.model;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
    private boolean scopeRegex; // true: scope を正規表現として扱う
    private String protoDescPath;
    private Descriptor descriptor;
    private List<String> cachedMessageTypes;
    private List<String> toolScope;
    private boolean requestHandling; // true: Request, false: Response
    private String replaceResponseBody;
    private String comment;

    private transient Pattern scopePattern; // scope をコンパイルした正規表現のキャッシュ
    private transient boolean scopePatternComputed;

    public AppModel() {
        this.enabled = false;
        this.scope = "";
        this.scopeRegex = false;
        this.protoDescPath = "";
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

    public boolean isScopeRegex() {
        return scopeRegex;
    }

    // scope をコンパイルした正規表現を返す。不正な正規表現なら null（結果はキャッシュ）。
    public Pattern getScopePattern() {
        if (!scopePatternComputed) {
            scopePatternComputed = true;
            try {
                scopePattern = Pattern.compile(scope);
            } catch (PatternSyntaxException e) {
                scopePattern = null;
            }
        }
        return scopePattern;
    }

    public String getProtoDescPath() {
        return protoDescPath;
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
        this.scopePattern = null;          // scope 変更時は正規表現キャッシュを破棄
        this.scopePatternComputed = false;
    }

    public void setScopeRegex(boolean flag) {
        this.scopeRegex = flag;
    }

    public void setProtoDescPath(String path) {
        this.protoDescPath = path;
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
        this.cachedMessageTypes.clear();
    }

}
