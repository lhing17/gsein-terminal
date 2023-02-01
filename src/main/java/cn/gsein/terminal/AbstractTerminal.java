package cn.gsein.terminal;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTerminal implements Closeable, Runnable {

    public static final String EXIT = "exit";

    protected BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    protected OutputStream out;
    protected OutputStream err;

    public void sendCommand(String command) {
        try {
            //noinspection ResultOfMethodCallIgnored
            commandQueue.offer(command, 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("向队列中写入命令失败", e);
        }
    }

    protected String getCommandFromQueue() {
        String command;
        try {
            command = commandQueue.poll(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("从队列中获取命令失败", e);
        }
        return command;
    }

    @Override
    public void close() throws IOException {
        out.close();
        err.close();
    }
}
