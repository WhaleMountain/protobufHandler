# Protobuf Handler

Protobuf メッセージと Json を相互変換するための Burp 拡張機能です。　　

機能
- Handler
- Message Editor

## Installation

リリースから jar ファイルをダウンロードして、Burp > Extensions > Add から追加してください

## Handler

Burp の Repeater、Intruder、Scanner、Extensions を通過するリクエストに対して、Json から Protobuf メッセージへの変換を行います。

* [使い方](doc/handler.md)

## Message Editor

Burp のメッセージエディターに Protobuf メッセージを Json に変換するタブを表示します。

* [使い方](doc/message-editor.md)

## LICENSE

MIT License. See [LICENSE](LICENSE)