package edu.coursera.distributed;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.*;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs     A proxy filesystem to serve files from. See the PCDPFilesystem
     *               class for more detailed documentation of its usage.
     * @param ncores The number of cores that are available to your
     *               multi-threaded file server. Using this argument is entirely
     *               optional. You are free to use this information to change
     *               how you create your threads, or ignore it.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket socket, final PCDPFilesystem fs,
                    final int ncores) throws IOException {
        /*
         * Enter a spin loop for handling client requests to the provided
         * ServerSocket object.
         */
        while (true) {

            // TODO 1) Use socket.accept to get a Socket object
            Socket clientSocket = socket.accept();

            Thread thread = new Thread(
                    () -> {
                        /*
                         * TODO 2) Using Socket.getInputStream(), parse the received HTTP
                         * packet. In particular, we are interested in confirming this
                         * message is a GET and parsing out the path to the file we are
                         * GETing. Recall that for GET HTTP packets, the first line of the
                         * received packet will look something like:
                         *
                         *     GET /path/to/file HTTP/1.1
                         */
                        try {
                            InputStream inStream = clientSocket.getInputStream();
                            InputStreamReader inStreamReader = new InputStreamReader(inStream);
                            BufferedReader bufferedInStreamReader = new BufferedReader(inStreamReader);


                            String firstLine = bufferedInStreamReader.readLine();
                            assert firstLine != null;
                            assert firstLine.startsWith("GET");
                            final PCDPPath path = new PCDPPath(firstLine.split(" ")[1]);

                            /*
                             * TODO 3) Using the parsed path to the target file, construct an
                             * HTTP reply and write it to Socket.getOutputStream(). If the file
                             * exists, the HTTP reply should be formatted as follows:
                             *
                             *   HTTP/1.0 200 OK\r\n
                             *   Server: FileServer\r\n
                             *   \r\n
                             *   FILE CONTENTS HERE\r\n
                             *
                             * If the specified file does not exist, you should return a reply
                             * with an error code 404 Not Found. This reply should be formatted
                             * as:
                             *
                             *   HTTP/1.0 404 Not Found\r\n
                             *   Server: FileServer\r\n
                             *   \r\n
                             *
                             * Don't forget to close the output stream.
                             */

                            String fileContents = fs.readFile(path);
                            StringBuilder replyBuilder = new StringBuilder();
                            if (fileContents != null) {
                                replyBuilder.append("HTTP/1.0 200 OK\r\nServer: FileServer\r\n\r\n");
                                replyBuilder.append(fileContents);
                                replyBuilder.append("\r\n");
                            } else {
                                replyBuilder.append("HTTP/1.0 404 Not Found\r\nServer: FileServer\r\n\r\n");
                            }

                            OutputStream outStream = clientSocket.getOutputStream();
                            OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream);
                            BufferedWriter bufferedOutStreamWriter = new BufferedWriter(outStreamWriter);
                            bufferedOutStreamWriter.write(replyBuilder.toString());

                            bufferedOutStreamWriter.close();
                            outStreamWriter.close();
                            outStream.close();
                            bufferedInStreamReader.close();
                            inStreamReader.close();
                            inStream.close();
                        } catch (java.io.IOException io) {
                            throw new RuntimeException(io);
                        }
            });

            thread.start();
        }
    }
}
