package cn.gsein.terminal;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalTerminal extends AbstractTerminal {

    private File workDir;

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    @Override
    public void run() {
        // 创建线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(5);

        Process p;
        try {
            p = Runtime.getRuntime().exec("/bin/zsh", null, workDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        threadPool.submit(() -> getAndWriteCommandRepeatedly(threadPool, p));

        threadPool.submit(() -> transferOutAndErrorStream(p));

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        threadPool.shutdown();
    }

    private void getAndWriteCommandRepeatedly(ExecutorService threadPool, Process p) {
        while (true) {
            String command = getCommandFromQueue();
            if (command == null) {
                continue;
            }
            if (EXIT.equals(command)) {
                threadPool.shutdown();
                p.destroy();
                System.exit(0);
                break;
            }
            writeCommentToProcess(p, command);
        }
    }

    private void writeCommentToProcess(Process p, String command) {
        try {
            PrintWriter pw = new PrintWriter(p.getOutputStream());
            pw.println(command);
            pw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void transferOutAndErrorStream(Process p) {
        try {
            p.getInputStream().transferTo(out);
            p.getErrorStream().transferTo(err);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}


