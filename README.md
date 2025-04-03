# Protobuf Handler

Protobuf メッセージと Json を相互変換するための Burp 拡張機能です。

機能
- Handler
- Message Editor

## Installation

リリースから jar ファイルをダウンロードして、Burp > Extensions > Add から追加してください

## Handler

Burp の Proxy、Repeater、Intruder、Scanner、Extensions を通過するリクエスト、レスポンスに対して Json から Protobuf メッセージへの変換を行います。

* [使い方](doc/handler.md)

## Message Editor

Burp のメッセージエディターに Protobuf メッセージと Json を相互変換するタブを表示します。

* [使い方](doc/message-editor.md)

## ビルド方法

Gradleを使ってビルドしてください。

```shell
$ git clone https://github.com/WhaleMountain/protobufHandler.git
$ cd protobufHandler
$ ./gradlew build
```

`app/build/libs/protobufHandler.jar` が作成されます。これを Burp Suite で読み込みしてください。

## LICENSE

MIT License. See [LICENSE](LICENSE)