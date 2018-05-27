package com.github.flaxsearch.api;

public class UninvertData {

    public final long docid;

    public final String text;

    public UninvertData(long docid, String text) {
        this.docid = docid;
        this.text = text;
    }
}
