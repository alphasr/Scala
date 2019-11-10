package  main
import java.net._
import java.io._
import scala.io._

object server extends App {
val http = "HTTP/1.1 200 OK\n"
val content = "Content-Type: text/html\n"+"Content-Length: "

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

    //ERROR FUNCTIONS
    def error_404():String= {
      val input = Source.fromFile("src/err/404.html")
      val ss = input.getLines().mkString
      println(ss)
       ss
    }

    def err_500() = ??? /// TODO AZIMJON

    //ERROR FUNCTIONS

    def sendhtml(filename:String):String={

      val location = "./src/static" + filename
      println(location)
     try {
       val input = Source.fromFile(location)
       val ss = input.getLines().mkString
       var re=""
       //send.println("HTTP/1.1 200 OK\nContent-Type: text/html\nContent-Length: 50\n\n<html><body><h1> YARRAMI YE ! </h1></body></html>")
       re += http + content +  ss.length + "\n\n" + ss
       re
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
      if(s(0) == "GET"){
        val a = s(1).split('.')
          val suffix = a(1)
        suffix match{
          case "html" => send.println(sendhtml(s(1)))
        }
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