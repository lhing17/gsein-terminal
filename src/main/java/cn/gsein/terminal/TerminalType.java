package cn.gsein.terminal;

public enum TerminalType {
    REMOTE(RemoteTerminal.class),
    LOCAL(LocalTerminal.class);

    private final Class<? extends AbstractTerminal> clazz;

    TerminalType(Class<? extends AbstractTerminal> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends AbstractTerminal> getClazz() {
        return clazz;
    }


}
