package scoket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

public class IOServer {

    public static void main(String[] args) throws IOException {
        serve(9999);
    }
    public static void serve(int port) throws IOException {
        final ServerSocket socket = new ServerSocket(port);
        try {
            //没有停止条件的for循环，不断的监听端口
            for (;;) {
                //scoket.accept方法会阻塞到收到请求为止
                final Socket clientSocket = socket.accept();
                System.out.println(
                        "Accepted connection from " + clientSocket);
                //创建一个线程来处理请求
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OutputStream out;
                        InputStream inputStream;
                        try {
                            //输出接受到的词语
                            inputStream=clientSocket.getInputStream();
                            byte b[]=new byte[1024];
                            inputStream.read(b);
                            System.out.println(new String(b));
                            out = clientSocket.getOutputStream();
                            System.out.println();
                            //向客户端输出hi
                            out.write("Hi!\r\n".getBytes(
                                    Charset.forName("UTF-8")));
                            out.flush();
                            //关闭连接
                            clientSocket.close();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        finally {
                            try {
                                clientSocket.close();
                            }
                            catch (IOException ex) {
// ignore on close
                            }
                        }
                    }
                }).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
