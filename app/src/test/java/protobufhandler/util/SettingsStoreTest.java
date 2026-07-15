package protobufhandler.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.jupiter.api.Test;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.logging.Logging;
import protobufhandler.model.AppModel;

class SettingsStoreTest {

    // Logging を呼ばれても何もしないダミー実装（descriptor 解決失敗時のログ用）
    private static Logging noopLogging() {
        return (Logging) Proxy.newProxyInstance(
                Logging.class.getClassLoader(),
                new Class[] { Logging.class },
                (proxy, method, args) -> null);
    }

    @Test
    void jsonRoundTripPreservesFields() {
        SettingsStore store = new SettingsStore(noopLogging());

        AppModel rule = new AppModel();
        rule.setEnabled(true);
        rule.setScope("/api/v1/user");
        rule.setProtoDescPath("/nonexistent/path/hello.desc"); // 解決失敗しても他フィールドは保持される
        rule.setMessageType("HelloRequest");
        rule.setCachedMessageType("HelloRequest");
        rule.setCachedMessageType("HelloReply");
        rule.setToolScope(ToolType.PROXY.toolName());
        rule.setToolScope(ToolType.REPEATER.toolName());
        rule.setRequestHandling(false);
        rule.setReplaceResponseBody("{\"message\":\"x\"}");
        rule.setComment("my rule");

        String json = store.toJson(List.of(rule));
        List<AppModel> restored = store.fromJson(json);

        assertEquals(1, restored.size());
        AppModel r = restored.get(0);
        assertTrue(r.isEnabled());
        assertEquals("/api/v1/user", r.getScope());
        assertEquals("/nonexistent/path/hello.desc", r.getProtoDescPath());
        assertEquals("HelloRequest", r.getMessageType());
        assertEquals(List.of("HelloRequest", "HelloReply"), r.getCachedMessageTypes());
        assertEquals(List.of(ToolType.PROXY.toolName(), ToolType.REPEATER.toolName()), r.getToolScope());
        assertFalse(r.isRequestHandling());
        assertEquals("{\"message\":\"x\"}", r.getReplaceResponseBody());
        assertEquals("my rule", r.getComment());
        assertNull(r.getDescriptor()); // 存在しない .desc なので未解決のまま
    }

    @Test
    void emptyOrNullJsonReturnsEmptyList() {
        SettingsStore store = new SettingsStore(noopLogging());
        assertTrue(store.fromJson(null).isEmpty());
        assertTrue(store.fromJson("").isEmpty());
        assertTrue(store.fromJson("   ").isEmpty());
    }
}
