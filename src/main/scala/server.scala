package  main
import java.net._
import java.io._
import scala.io._

object server extends App {
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
      return ss
    }

    def err_500(): String = {
      val input = Source.fromFile("src/err/500.html")
      val aa = input.getLines().mkString
      println(aa)
      aa
    }

    def err_403(): String = {
      val input = Source.fromFile("src/err/403.html")
      val bb = input.getLines().mkString
      println(bb)
      bb
    }

    def err_405(): String = {
      val input = Source.fromFile("src/err/405.html")
      val cc = input.getLines().mkString
      println(cc)
      cc
    }

    //ERROR FUNCTIONS

    def sendhtml(filename:String):String={

      try {
        val input = Source.fromFile(filename)
        val ss = input.getLines().mkString
        println(ss)
        ss
      }

      catch {
        case e: FileNotFoundException => error_404()
        case e: IOException => err_500()
      }

    }



    def serve(socket: Socket): Unit = {
      val reader = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val getreq = reader.readLine()
      println(getreq)
      val s = getreq.split(" ")
      val send = new PrintWriter( new BufferedWriter( new OutputStreamWriter( socket.getOutputStream ) ) )
      if(s(0) == "GET"){
        val a = s(1).split('.')
        val suffix = a(1)
        suffix match{
          case "html" => sendhtml(s(1))
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