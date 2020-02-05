package  main
import java.net._
import java.io._
import java.nio.file.{Files, Paths}
import main.server

import scala.io._

object server extends App {
    val contentHtml = "Content-Type: text/html\n"+"Content-Length: "
    val contentDirectory = "Content-Type: dir\n"+"Content-Length: "
    val contentImage ="Content-Type: image/"
  val parseStatus = Map(200->"HTTP/1.1 200 OK\n",500->"HTTP/1.1 500 IO EXCEPTION\n",404 ->"HTTP/1.1 404 Not Found\n",
    403->"HTTP/1.1 Permission Denied\n",405->"HTTP/1.1")
  val parseContent = Map("html" -> contentHtml,"image" ->contentImage,
  "directory" ->contentDirectory)
  val parseErrorLocation = Map(403 -> "src/err/403.html",404 -> "src/err/404.html",405 ->"src/err/405.html"
    ,500->"src/err/500.html")

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


    def sendImage (fileName:String, statusCode:Int,socket: Socket): Unit ={
      val writer = new PrintWriter( new BufferedWriter( new OutputStreamWriter( socket.getOutputStream ) ) )
      val os =new DataOutputStream(socket.getOutputStream)
      val location = "./src/images" + fileName
      val extension = fileName.split('.')
      try {
        val byteArray = Files.readAllBytes(Paths.get(location))

        val response  = parseStatus(statusCode) + parseContent("image")+extension(1)+ "\nContent-Length: "+byteArray.length + "\n\n"
       writer.print(response)
       writer.flush()
       os.write(byteArray,0,byteArray.length)
       os.flush()
       writer.close()
       os.close()
      }
      catch {
        case e: FileNotFoundException => sendError(404,socket)//error_404()
        case e: IOException => sendError(500,socket)//err_500()
      }
    }


    def sendHtml(filename:String, statusCode:Int,socket: Socket):Unit={

      var location = ""
      val writer = new PrintWriter( new BufferedWriter( new OutputStreamWriter( socket.getOutputStream ) ) )

      if(filename == "/src" && statusCode == 200){
        val read = "Directories: \n\nhome\n\nstatic\n\nmain"
        val response:String = parseStatus(statusCode) + parseContent("directory") + read.length + "\n\n" + read
        writer.println(response)
        writer.flush()
        writer.close()
      }
      else {
        if (filename contains ".html")
          location = "./src/" + filename
        else
          location = "./src/" + filename + "/index.html"
        try {
          val file = Source.fromFile(location)
          val read = file.getLines().mkString
          val response: String = parseStatus(statusCode) + parseContent("html") + read.length + "\n\n" + read
          writer.println(response)
          writer.flush()
          writer.close()
        }
        catch {
          case e: FileNotFoundException => sendError(404,socket)
          case e: IOException => sendError(500,socket)
        }
      }
    }



    def sendError(statusCode: Int,socket: Socket):Unit={
      val writer = new PrintWriter( new BufferedWriter( new OutputStreamWriter( socket.getOutputStream ) ) )
      val file = Source.fromFile(parseErrorLocation(statusCode))
      val read =  file.getLines().mkString
      val response:String = parseStatus(statusCode) + parseContent("html") + read.length + "\n\n" + read
      writer.println(response)
      writer.flush()
      writer.close()
    }








    def serve(socket: Socket): Unit = {
      val reader = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val getRequest = reader.readLine()
      val str = getRequest.split(" ")
      val check = str(1).split('.')
      val parser = Map("html"-> sendHtml _,"css"-> sendHtml _,"js"->sendHtml _,
        "png"->sendImage _,"jpeg"->sendImage _,"gif"-> sendImage _,"svg"-> sendImage _)

      if(str(0) == "GET") {
        if( getRequest == "GET / HTTP/1.1"){
        sendHtml("home/index.html",200,socket)
        }
        else if ( str(1) =="/src") {
        sendHtml(str(0),200,socket)
        }


        else if(check.length == 1){
            val access = str(1).split('/')
            if((access(1) contains "main") || (access(1) contains "classified") ){
              sendError(403,socket)
            }
            else {
              sendHtml(access(1),200,socket)
            }
        }
        else {

          val extension = str(1).split('.')
          parser(extension(1))(str(1) ,200, socket)
        }
     }
    else {
        sendError(405,socket)
      }

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