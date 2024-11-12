# Message Editor

Burp のメッセージエディターに Protobuf メッセージと Json を相互変換するタブを表示します。

## 手順

1. サポートしている `Content-Type` の時、「Protobuf to Json Decode」タブが表示されます
1. Protobuf to Json Decode タブを選択すると Raw フォーマットで表示されます
![](./image/editor-raw.png)
1. 「Choose」ボタンで descriptor_set_out で出力したファイルを選択します
1. 読み込みに成功すると Message Type が選択できるようになります
![](./image/editor-message.png)
1. デコードしたい Message Type を選択し、「Decode」ボタンをクリックしてください
![](./image/editor-decode.png)

### Repeater での相互変換

1. Repeater タブに対象のリクエストを送信する
1. Protobuf to Json Decode タブから Json に変換する
1. 出力された Json を編集し「Send」をクリックする
![](./image/editor-send.png)
1. Logger を表示すると、Protobuf に変換されていることを確認できます
![](./image/editor-log.png)

### サポートしているContent-Type

```
application/x-protobuf
application/protobuf
application/grpc-web+proto
```

> [!TIP]
> Content-Type を追加したい場合は `AppEditorProvider.java:18` の `ENABLE_EDITOR_CONTENT_TYPE` に追加してビルドし直してください。