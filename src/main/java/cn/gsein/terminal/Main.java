package cn.gsein.terminal;

import cn.gsein.terminal.local.Local;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        AbstractTerminal terminal = TerminalBuilder.builder()
                .remote()
                .host(Local.HOST)
                .password(Local.PASSWORD)
                .build();
//        AbstractTerminal terminal = TerminalBuilder.builder()
//                .local()
//                .build();


        Thread thread = new Thread(terminal);
        thread.start();
//        terminal.downloadFile("/root/ReadMe", Paths.get(""));
//        terminal.uploadFile(Paths.get("README.md"), "/root");


        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String command = scanner.nextLine();
            terminal.sendCommand(command);
        }
    }
}
