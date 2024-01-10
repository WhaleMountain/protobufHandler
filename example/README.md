# Usage

* protoc コマンドの実行
```
cd protobufHandler/example/
protoc --proto_path=./proto --go_out=./server-go/hello --go_opt=paths=source_relative ./proto/*.proto
protoc --proto_path=./proto --go_out=./client-go/hello --go_opt=paths=source_relative ./proto/*.proto
```

* server-go の実行
```
cd protobufHandler/example/server-go/
go run main.go
```

* client-go の実行
```
cd protobufHandler/example/client-go/
go run main.go
```