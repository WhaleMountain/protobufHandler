package main

import (
	"fmt"
	"io"
	"log"
	"net/http"
	"regexp"

	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"google.golang.org/protobuf/proto"

	pb "protobuf-example/hello"
)

type User struct {
	Id    string
	Name  string
	Email string
}

var IN_MEMORY_USER_DB = make(map[string]*User)

func main() {
	// Echo instance
	e := echo.New()

	// Middleware
	//e.Use(middleware.Logger())
	e.Use(middleware.Recover())

	e.Use(middleware.CORSWithConfig(middleware.CORSConfig{
		AllowOriginFunc: allowOrigin,
		AllowMethods:    []string{http.MethodGet, http.MethodPost},
		AllowHeaders:    []string{"content-type"},
	}))

	// Routes
	e.POST("/hello", sayHello)
	e.POST("/hello/user", sayHelloUser)
	e.POST("/user", postUser)

	// Start server
	e.Logger.Fatal(e.Start(":8000"))
}

func allowOrigin(origin string) (bool, error) {
	// In this example we use a regular expression but we can imagine various
	// kind of custom logic. For example, an external datasource could be used
	// to maintain the list of allowed origins.
	return regexp.MatchString(`^http:\/\/localhost:8001$`, origin)
}

// Handler
func sayHello(c echo.Context) error {
	body, _ := io.ReadAll(c.Request().Body)
	helloReq := &pb.HelloRequest{}
	proto.Unmarshal(body, helloReq)

	log.Printf("RECV: %v", helloReq)

	reply := &pb.HelloReply{
		Message: fmt.Sprintf("Hello, %s!", helloReq.Name),
	}

	log.Printf("SEND: %v", reply)

	replyProto, _ := proto.Marshal(reply)

	return c.Blob(http.StatusOK, "application/x-protobuf", replyProto)
}

// Handler
func sayHelloUser(c echo.Context) error {
	body, _ := io.ReadAll(c.Request().Body)
	userReq := &pb.HelloUserRequest{}
	proto.Unmarshal(body, userReq)

	log.Printf("RECV: %v", userReq)

	if _, ok := IN_MEMORY_USER_DB[userReq.Id]; !ok {
		user := &pb.User{}
		reply := &pb.HelloUserReply{
			Status:  http.StatusBadRequest,
			User:    user,
			Message: "The requested UserID is not registered.",
		}
		log.Printf("SEND: %v", reply)
		replyProto, _ := proto.Marshal(reply)
		return c.Blob(http.StatusBadRequest, "application/x-protobuf", replyProto)
	}

	dbuser := IN_MEMORY_USER_DB[userReq.Id]
	user := &pb.User{
		Id:    dbuser.Id,
		Name:  dbuser.Name,
		Email: dbuser.Email,
	}

	reply := &pb.HelloUserReply{
		Status:  http.StatusOK,
		User:    user,
		Message: "",
	}

	log.Printf("SEND: %v", reply)

	replyProto, _ := proto.Marshal(reply)

	return c.Blob(http.StatusOK, "application/x-protobuf", replyProto)
}

// Handler
func postUser(c echo.Context) error {
	body, _ := io.ReadAll(c.Request().Body)
	userReq := &pb.PostUserRequest{}
	proto.Unmarshal(body, userReq)

	log.Printf("RECV: %v", userReq)

	if _, ok := IN_MEMORY_USER_DB[userReq.User.Id]; ok {
		reply := &pb.PostUserReply{
			Status:  http.StatusBadRequest,
			Message: "The requested UserID is already registered.",
		}
		log.Printf("SEND: %v", reply)
		replyProto, _ := proto.Marshal(reply)
		return c.Blob(http.StatusBadRequest, "application/x-protobuf", replyProto)
	}

	IN_MEMORY_USER_DB[userReq.User.Id] = &User{
		Id:    userReq.User.Id,
		Name:  userReq.User.Name,
		Email: userReq.User.Email,
	}

	reply := &pb.PostUserReply{
		Status:  http.StatusCreated,
		Message: "Your registration is complete.",
	}

	log.Printf("SEND: %v", reply)

	replyProto, _ := proto.Marshal(reply)

	return c.Blob(http.StatusCreated, "application/x-protobuf", replyProto)
}
