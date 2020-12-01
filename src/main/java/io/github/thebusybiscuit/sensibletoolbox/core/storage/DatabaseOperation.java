package io.github.thebusybiscuit.sensibletoolbox.core.storage;

enum DatabaseOperation {

    INSERT,
    UPDATE,
    DELETE,
    FINISH,
    COMMIT;

    public boolean hasData() {
        return this != FINISH && this != COMMIT;
    }
}