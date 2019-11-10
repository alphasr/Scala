package  main
import java.nio.file.{Files, Paths}
import java.net._
import java.io._

import scala.concurrent.TimeoutException
import scala.io._

object server extends App {
val http200 = "HTTP/1.1 200 OK\n"
val content = "Content-Type: text/html\n"+"Content-Length: "
val contentimage = "Content-Type: image/"

  //send.println("HTTP/1.1 200 OK\nContent-Type: text/html\nContent-Length: 50\n\n<html><body><h1> YARRAMI YE ! </h1></body></html>")

  class request(socket: ServerSocket) {
    def create():Unit= {
      while (true) {
        val Client = socket.accept()
        try {
          val thread = new Thread(new Runnable() {
            def run(): Unit = serve(Client)
          })
          thread.start()
        }

        catch {
          case e: TimeoutException => err_500() // error 500
        }

      }
    }
    //ERROR FUNCTIONS
    def error_405():String= {
      val input = Source.fromFile("./src/err/405.html")
      val ss = input.getLines().mkString
      var re =""
      re+="HTTP/1.1 405 Not Found\n"+content + ss.length + "\n\n" +ss
      re
    }

    //ERROR FUNCTIONS
    def error_404():String= {
      val input = Source.fromFile("./src/err/404.html")
      val ss = input.getLines().mkString
      var re =""
      re+="HTTP/1.1 404 Not Found\n"+content + ss.length + "\n\n" +ss
      re
    }

    def err_500():String={
      Runtime.getRuntime.addShutdownHook( new Thread( () => server.stop() ))
      "this"
    }

    //ERROR FUNCTIONS

    def sendhtml(filename:String):String={

      val location = "./src/static" + filename
     try {
       val input = Source.fromFile(location)
       val ss = input.getLines().mkString
       var re=""
       //send.println("HTTP/1.1 200 OK\nContent-Type: text/html\nContent-Length: 50\n\n<html><body><h1> YARRAMI YE ! </h1></body></html>")
       re += http200 + content +  ss.length + "\n\n" + ss
       re
     }

      catch {
        case e: FileNotFoundException => error_404()
        case e: IOException => err_500()
      }

    }


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


    def serve(socket: Socket): Unit = {
      val reader = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val getreq = reader.readLine()
      val s = getreq.split(" ")
      val send = new PrintWriter( new BufferedWriter( new OutputStreamWriter( socket.getOutputStream ) ) )
      val os =socket.getOutputStream

      if(s(0) == "GET"){
        val a = s(1).split('.')
          val suffix = a(1)
       // println(suffix)
        suffix match{
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
      else send.println(error_405())
      //send.println("HTTP/1.1 200 OK\nContent-Type: text/html\nContent-Length: 50\n\n<html><body><h1> YARRAMI YE ! </h1></body></html>")
      send.flush()
      os.flush()
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