package main

import (
	"bytes"
	"fmt"
	"net/http"
	"net/url"

	"google.golang.org/protobuf/proto"

	pb "protobuf-example/hello"
)

func main() {
	PROXY_URL := "http://127.0.0.1:8090"
	REQUEST_METHOD := "POST"
	REQUEST_URL := "http://localhost:8000/user"

	body := &pb.PostUserRequest{
		User: &pb.User{
			Id: "1",
			Name: "Tanakaz",
			Email: "tanakaz@example.com",
		},
	}

	protobufBody, _ := proto.Marshal(body)
	request, _ := http.NewRequest(REQUEST_METHOD, REQUEST_URL, bytes.NewReader(protobufBody))
	request.Header.Set("Content-Type", "application/x-protobuf")

	proxy, _ := url.Parse(PROXY_URL)
	client := &http.Client{
		Transport: &http.Transport{
			Proxy: http.ProxyURL(proxy),
		},
	}
	response, err := client.Do(request)
	if err != nil {
		panic(err)
	}
	defer response.Body.Close()

	fmt.Println("Resp status", response.Status)
}
