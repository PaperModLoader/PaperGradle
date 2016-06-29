package xyz.papermodloader.launcher;

public enum Side {
    CLIENT,
    SERVER;

    public Side invert() {
        return this == CLIENT ? SERVER : CLIENT;
    }
}
