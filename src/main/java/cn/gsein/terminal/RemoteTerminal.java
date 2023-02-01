package cn.gsein.terminal;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemoteTerminal extends AbstractTerminal {


    private String host;
    private int port;
    private String username;
    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void run() {
        // 创建线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(5);

        // 使用sshd连接远程服务器
        // 1. 创建SshClient对象
        try (SshClient sshClient = SshClient.setUpDefaultClient();) {
            // 2. 启动SshClient
            sshClient.start();

            // 3. 连接远程服务器
            ConnectFuture future = sshClient.connect(username, host, port);
            future.await(5000);
            ClientSession session = future.getSession();
            session.addPasswordIdentity(password);

            if (!session.auth().verify(5000).isSuccess()) {
                throw new IOException("验证失败");
            }

            // 6. 执行命令
            try (ClientChannel channel = session.createShellChannel()) {

                // 开启通道
                channel.open().verify(10000);

                // 写入命令的线程
                threadPool.submit(() -> getAndWriteCommandRepeatedly(threadPool, channel));

                // 读取命令结果的线程
                threadPool.submit(() -> transferOutAndErrorStream(channel));

                // 等待关闭信号，保证通道不会关闭
                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0L);
            }

            // 7. 关闭连接
            sshClient.stop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getAndWriteCommandRepeatedly(ExecutorService threadPool, ClientChannel channel) {
        while (true) {
            String command = getCommandFromQueue();
            if (command == null) {
                continue;
            }
            if (EXIT.equals(command)) {
                threadPool.shutdown();
                channel.close(false);
                System.exit(0);
                break;
            }
            writeCommandToChannel(channel, command);
        }
    }

    private void transferOutAndErrorStream(ClientChannel channel) {
        try {
            channel.getInvertedOut().transferTo(out);
            channel.getInvertedErr().transferTo(err);
        } catch (IOException e) {
            throw new RuntimeException("写入命令执行结果失败", e);
        }
    }

    private void writeCommandToChannel(ClientChannel channel, String command) {
        try {
            OutputStream pipedIn = channel.getInvertedIn();
            pipedIn.write((command + "\n").getBytes());
            pipedIn.flush();
        } catch (IOException e) {
            throw new RuntimeException("命令写入失败", e);
        }
    }


}
