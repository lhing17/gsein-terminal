简易终端，支持本地和远程连接

### 本地连接

```java
    AbstractTerminal terminal=TerminalBuilder.builder()
        .local()
        .build();


    Thread thread=new Thread(terminal);
    thread.start();
    thread.sendCommand("ls");
```

### 远程连接

```java
    AbstractTerminal terminal=TerminalBuilder.builder()
        .remote()
        .host(HOST)
        .password(PASSWORD)
        .build();
    Thread thread=new Thread(terminal);
    thread.start();
    thread.sendCommand("ls");
```
