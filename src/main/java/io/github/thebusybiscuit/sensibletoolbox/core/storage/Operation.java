package io.github.thebusybiscuit.sensibletoolbox.core.storage;

enum Operation {

    INSERT,
    UPDATE,
    DELETE,
    FINISH,
    COMMIT;

    public boolean hasData() {
        return this != FINISH && this != COMMIT;
    }
}