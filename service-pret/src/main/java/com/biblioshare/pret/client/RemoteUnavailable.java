package com.biblioshare.pret.client;


public class RemoteUnavailable extends RuntimeException {
    public RemoteUnavailable(Throwable cause) {
        super("Service distant indisponible", cause);
    }
}
