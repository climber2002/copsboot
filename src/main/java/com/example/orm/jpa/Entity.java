package com.example.orm.jpa;

public interface Entity<T extends EntityId> {
    T getId();
}
