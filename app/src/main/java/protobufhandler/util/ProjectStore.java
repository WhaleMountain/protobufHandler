package protobufhandler.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Descriptors.Descriptor;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.persistence.PersistedObject;
import protobufhandler.model.AppModel;

// ルール一覧を Burp のユーザープロジェクト（プロジェクトファイル）へ永続化するユーティリティ。
// Montoya の PersistedObject (api.persistence().extensionData()) にルール一覧を Java 直列化した
// バイト列として保存し、拡張のリロード／Burp 再起動時に同じプロジェクトで自動復元する。
// descriptor は transient で保存されないため、復元時に .desc から再構築する。
public class ProjectStore {
    // PersistedObject 内でルール一覧を格納するキー
    public static final String RULES_KEY = "protobufhandler.rules";

    // 復元を許可するクラスを AppModel と標準コレクション/文字列等に限定し、
    // 不正なデータ読み込みによる任意コード実行を防ぐ。
    private static final String DESERIALIZE_FILTER =
        "protobufhandler.model.AppModel;java.util.*;java.lang.*;!*";

    private final PersistedObject data;
    private final Logging logging;

    public ProjectStore(PersistedObject data, Logging logging) {
        this.data = data;
        this.logging = logging;
    }

    // 現在のルール一覧をプロジェクトへ保存する。
    public void save(List<AppModel> rules) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new ArrayList<>(rules));
            oos.flush();
            data.setByteArray(RULES_KEY, ByteArray.byteArray(bos.toByteArray()));
        } catch (IOException e) {
            logging.logToError(e);
            logging.logToOutput("ルールのプロジェクトへの保存に失敗しました。");
        }
    }

    // プロジェクトからルール一覧を復元する。未保存なら空リストを返す。
    // 各ルールの descriptor は .desc から再構築する。
    public List<AppModel> load() {
        List<AppModel> rules = new ArrayList<>();

        ByteArray stored = data.getByteArray(RULES_KEY);
        if (stored == null || stored.length() == 0) {
            return rules;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(stored.getBytes()))) {
            ois.setObjectInputFilter(ObjectInputFilter.Config.createFilter(DESERIALIZE_FILTER));
            Object obj = ois.readObject();
            if (obj instanceof ArrayList<?> al) {
                for (Object element : al) {
                    if (element instanceof AppModel item) {
                        reconstructDescriptor(item);
                        rules.add(item);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logging.logToError(e);
            logging.logToOutput("プロジェクトからのルール読み込みに失敗しました。");
        }

        return rules;
    }

    // 読み込んだ AppModel の descriptor を proto ファイルから再構築する。
    // proto ファイルが無い / 該当 message type が見つからない場合は descriptor は null のまま。
    private void reconstructDescriptor(AppModel item) {
        String path = item.getProtoDescPath();
        String name = item.getDescriptorName();
        if (path == null || path.isBlank() || name == null || name.isBlank()) {
            return;
        }
        if (!new File(path).isFile()) {
            logging.logToOutput("Protobuf file (.desc) が見つからないため descriptor を復元できませんでした。");
            logging.logToOutput("File: %s\n".formatted(path));
            return;
        }
        try {
            List<Descriptor> descriptors = Protobuffer.getMessageTypesFromProtoFile(path);
            for (Descriptor descriptor : descriptors) {
                if (descriptor.getName().equals(name)) {
                    item.setDescriptor(descriptor);
                    break;
                }
            }
        } catch (Exception e) {
            logging.logToError(e);
            logging.logToOutput("Protobuf file の読み込みに失敗しました。");
            logging.logToOutput("File: %s\n".formatted(path));
        }
    }
}
