package com.example.orm.jpa;

public interface EntityId<T> {
    public T getId();

    public String asString();
}
