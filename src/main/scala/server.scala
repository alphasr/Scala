package  main
import java.net._
import java.io._
import java.nio.file.{Files, Paths}

import main.server.http200

import scala.io._

object server extends App {
  val http200 = "HTTP/1.1 200 OK\n"
  val content = "Content-Type: text/html\n"+"Content-Length: "
  val content2 =  "Content-Type: dir\n"+"Content-Length: "
  val contentimage =  "Content-Type: image/"

  //send.println("HTTP/1.1 200 OK\nContent-Type: text/html\nContent-Length: 50\n\n<html><body><h1> YARRAMI YE ! </h1></body></html>")

  class request(socket: ServerSocket) {
    def create():Unit={
      while(true){
        val Client = socket.accept()
        val thread = new Thread(new Runnable () {
          def run():Unit = serve(Client)
        })
        thread.start()
      }
    }

    //IMAGE PROCCES
    def sendImage(filename: String, extension:String,send:PrintWriter,os:OutputStream): Unit ={


      val location = "./src/static" + filename

      try {
        val byteArray = Files.readAllBytes(Paths.get(location))
        var re=""
        re+= http200 + contentimage+extension+ "\nContent-Length: "+byteArray.length + "\n\n"
        println(re)
        send.println(re)
        send.flush()
        os.write(byteArray,0,byteArray.length)
        os.flush()
      }
      catch {
        case e: FileNotFoundException => error_404()
        case e: IOException => err_500()
      }
    }
    //IMAGE PROCCESS

    //ERROR FUNCTIONS
    def error_404():String= {
      val input = Source.fromFile("./src/err/404.html")
      val ss = input.getLines().mkString
      var re =""
      re+="HTTP/1.1 404 Not Found\n"+content + ss.length + "\n\n" +ss
      re
    }

    def err_500(): String = {
      val input = Source.fromFile("src/err/500.html")
      val ss = input.getLines().mkString
      var re =""
      re+="HTTP/1.1 500 IO EXCEPTION\n"+content + ss.length + "\n\n" +ss
      re
    }


    def error_403():String= {
      val input = Source.fromFile("./src/err/403.html")
      val ss = input.getLines().mkString
      var re =""
      re+="HTTP/1.1 Permission Denied\n"+content + ss.length + "\n\n" +ss
      re
    }

    def error_405():String= {
      val input = Source.fromFile("./src/err/405.html")
      val ss = input.getLines().mkString
      var re =""
      re+="HTTP/1.1 M\n"+content + ss.length + "\n\n" +ss
      re
    }
    //ERROR FUNcTIONS

    def sendhtml(filename:String):String={
      var location = ""
      if(filename == "/src"){
        val ss = "Directories: \n\nhome\n\nstatic\n\nmain"
        var re = ""
        //send.println("HTTP/1.1 200 OK\nContent-Type: text/html\nContent-Length: 50\n\n<html><body><h1> YARRAMI YE ! </h1></body></html>")
        re += http200+ content2 + ss.length + "\n\n" + ss
        re
      }

      else {
        if(filename contains ".html"){
          location = "./src/" + filename
        }
        else {
          location = "./src/" + filename + "/index.html"
        }
        try {
          val input = Source.fromFile(location)
          val ss = input.getLines().mkString
          var re = ""
          //send.println("HTTP/1.1 200 OK\nContent-Type: text/html\nContent-Length: 50\n\n<html><body><h1> YARRAMI YE ! </h1></body></html>")
          re += http200 + content + ss.length + "\n\n" + ss
          re
        }

        catch {
          case e: FileNotFoundException => error_404()
          case e: IOException => err_500()
        }
      }
    }



    def serve(socket: Socket): Unit = {
      val reader = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val getreq = reader.readLine()
      println(getreq)
      val s = getreq.split(" ")
      val send = new PrintWriter( new BufferedWriter( new OutputStreamWriter( socket.getOutputStream ) ) )
      val os =new DataOutputStream(socket.getOutputStream)
      //println(s(1))
      val check = s(1).split('.')
      if(s(0) == "GET" ) {
        if (getreq == "GET / HTTP/1.1") {
          send.println(sendhtml("home/index.html") + getreq + 200 )
        }
        else if(s(1) == "/src"){
          send.println(sendhtml(s(1)))
        }
        else if(check.length == 1){
          //val a:String = check(0)

          val n = s(1).split('/')
          if((n(1) contains "main") || (n(1) contains "classified") ){
            send.println(error_403())
          }
          else {
            //println("n:" + n(1))
            send.println(sendhtml(n(1)))
          }
        }
        else {
          val a = s(1).split('.')
          val suffix = a(1)
          suffix match {
            case "html" => send.println(sendhtml(s(1)))
            case "css" => send.println(sendhtml(s(1)))
            case "css" => send.println(sendhtml(s(1)))
            case "js" => send.println(sendhtml(s(1)))

            case "png" => sendImage(s(1),"png",send,os)
            case "jpeg" =>sendImage(s(1),"jpeg",send,os)
            case "jpg" => sendImage(s(1),"jpg",send,os)
            case "gif" => sendImage(s(1),"gif",send,os)
            case "svg" => sendImage(s(1),"svg",send,os)
            case _ =>
          }
        }
      }

      else{
        send.println(sendhtml("err/405.html"))
      }
      //send.println("HTTP/1.1 200 OK\nContent-Type: text/html\nContent-Length: 50\n\n<html><body><h1> YARRAMI YE ! </h1></body></html>")
      send.flush()
    }
    def stop(): Unit = {
      println("Stopped")
      socket.close()
    }
  }
  // creates a socket for TCP/IP on port 8080
  val socket = new ServerSocket(8080 )
  val server = new request( socket )
  // CATCHES SIGNAL CTRL-C and stops server from running
  Runtime.getRuntime.addShutdownHook( new Thread( () => server.stop() ))
  server.create()
}