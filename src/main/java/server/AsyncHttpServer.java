package server;

import utils.DefaultHttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncHttpServer implements HttpServer {

    private final AsynchronousChannelGroup channelGroup;
    private final AsynchronousServerSocketChannel channel;
    private final HttpHandler httpHandler;

    public AsyncHttpServer(int port, HttpHandler httpHandler) throws IOException, InterruptedException {
        this.httpHandler = Objects.requireNonNullElseGet(httpHandler, DefaultHttpHandler::new);
        this.channelGroup = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 1);
        this.channel = AsynchronousServerSocketChannel.open(channelGroup);
        initServer(port);
    }

    private void initServer(int port) throws IOException, InterruptedException {
        bind(new InetSocketAddress(port));
        accept();
        awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return channelGroup.awaitTermination(timeout, unit);
    }

    @Override
    public void bind(SocketAddress socketAddress) throws IOException {
        channel.bind(socketAddress);
    }

    @Override
    public HttpHandler handler() {
        return this.httpHandler;
    }

    @Override
    public void accept() {
        channel.accept(this.channel, httpHandler);
    }

    @Override
    public void close() throws IOException {
        channel.close();
        channelGroup.shutdownNow();
    }
}
