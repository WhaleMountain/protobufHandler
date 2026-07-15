package protobufhandler.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.Descriptors.Descriptor;

import burp.api.montoya.logging.Logging;
import burp.api.montoya.persistence.PersistedObject;
import protobufhandler.model.AppModel;

// AppModel（ルール）の一覧を JSON でシリアライズし、Burp プロジェクトファイルや
// 外部ファイルへ保存/読込するためのユーティリティ。
// descriptor は永続化されない（transient）ので、読込後に .desc を再読込して再解決する。
public class SettingsStore {
    public static final String KEY = "protobufhandler.rules";

    private static final Type RULE_LIST_TYPE = new TypeToken<List<AppModel>>() {}.getType();

    private final Gson gson = new Gson();
    private final Logging logging;

    public SettingsStore(Logging logging) {
        this.logging = logging;
    }

    public String toJson(List<AppModel> rules) {
        return gson.toJson(rules, RULE_LIST_TYPE);
    }

    // JSON からルール一覧へ復元し、各ルールの descriptor を再解決する。
    public List<AppModel> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        List<AppModel> rules = gson.fromJson(json, RULE_LIST_TYPE);
        if (rules == null) {
            return new ArrayList<>();
        }

        for (AppModel rule : rules) {
            resolveDescriptor(rule);
        }
        return rules;
    }

    // Burp プロジェクトファイルへ保存する。
    public void saveToProject(PersistedObject data, List<AppModel> rules) {
        data.setString(KEY, toJson(rules));
    }

    // Burp プロジェクトファイルから読み込む。未保存なら空リスト。
    public List<AppModel> loadFromProject(PersistedObject data) {
        return fromJson(data.getString(KEY));
    }

    // 外部ファイル（JSON）へエクスポートする。
    public void exportToFile(File file, List<AppModel> rules) throws IOException {
        Files.writeString(file.toPath(), toJson(rules), StandardCharsets.UTF_8);
    }

    // 外部ファイル（JSON）からインポートする。
    public List<AppModel> importFromFile(File file) throws IOException {
        return fromJson(Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }

    // .desc を再読込し、cachedMessageTypes を再構築、messageType 名に一致する Descriptor を再解決する。
    // ファイル不在/パース失敗時は log に記録し、descriptor=null のまま継続する。
    private void resolveDescriptor(AppModel rule) {
        String path = rule.getProtoDescPath();
        if (path == null || path.isEmpty()) {
            return;
        }

        try {
            List<Descriptor> descriptors = Protobuffer.getMessageTypesFromProtoFile(path);
            rule.clearCachedMessageType();
            for (Descriptor descriptor : descriptors) {
                rule.setCachedMessageType(descriptor.getName());
                if (descriptor.getName().equals(rule.getMessageType())) {
                    rule.setDescriptor(descriptor);
                }
            }

        } catch (Exception e) {
            logging.logToError(e);
            logging.logToOutput("設定の復元: Protobuf file の読み込みに失敗しました。");
            logging.logToOutput("File: %s\n".formatted(path));
        }
    }
}
