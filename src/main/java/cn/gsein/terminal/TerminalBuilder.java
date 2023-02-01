package cn.gsein.terminal;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

public class TerminalBuilder {

    private String host;
    private int port = 22;
    private String username = "root";
    private String password;
    private OutputStream out = System.out;
    private OutputStream err = System.err;

    private File workDir = new File(System.getProperty("user.dir"));

    private TerminalType type = TerminalType.LOCAL;

    public static TerminalBuilder builder() {
        return new TerminalBuilder();
    }

    public TerminalBuilder host(String host) {
        this.host = host;
        return this;
    }

    public TerminalBuilder port(int port) {
        this.port = port;
        return this;
    }

    public TerminalBuilder username(String username) {
        this.username = username;
        return this;
    }

    public TerminalBuilder password(String password) {
        this.password = password;
        return this;
    }

    public TerminalBuilder out(OutputStream out) {
        this.out = out;
        return this;
    }

    public TerminalBuilder err(OutputStream err) {
        this.err = err;
        return this;
    }

    public TerminalBuilder workDir(File workDir) {
        this.workDir = workDir;
        return this;
    }

    public TerminalBuilder remote() {
        this.type = TerminalType.REMOTE;
        return this;
    }

    public TerminalBuilder local() {
        this.type = TerminalType.LOCAL;
        return this;
    }

    private TerminalBuilder() {
    }

    public AbstractTerminal build() {
        AbstractTerminal terminal;
        try {
            terminal = type.getClazz().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException("创建终端失败", e);
        }
        terminal.out = out;
        terminal.err = err;
        if (terminal instanceof RemoteTerminal remoteTerminal) {

            if (host == null || host.isEmpty()) {
                throw new RuntimeException("远程终端主机不能为空");
            }

            if (port == 0) {
                throw new RuntimeException("远程终端端口不能为空");
            }

            if (username == null || username.isEmpty()) {
                throw new RuntimeException("远程终端用户名不能为空");
            }

            if (password == null || password.isEmpty()) {
                throw new RuntimeException("远程终端密码不能为空");
            }

            remoteTerminal.setHost(host);
            remoteTerminal.setPort(port);
            remoteTerminal.setUsername(username);
            remoteTerminal.setPassword(password);
        } else if (terminal instanceof LocalTerminal localTerminal) {
            if (workDir == null) {
                throw new RuntimeException("本地终端工作目录不能为空");
            }
            localTerminal.setWorkDir(workDir);
        }
        return terminal;
    }
}
