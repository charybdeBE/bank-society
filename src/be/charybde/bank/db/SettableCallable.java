package be.charybde.bank.db;

import java.util.concurrent.Callable;

public abstract  class SettableCallable<T> implements Callable<Void> {
    public T result;
    public void setResult(T result){
        this.result = result;
    }

    public abstract Void call();

}
