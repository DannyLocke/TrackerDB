package com.ironyard;

/**
 * Created by dlocke on 12/11/16.
 */
public class Twitter {

    int id;
    String author;
    String text;

    public Twitter (int id, int replyId, String author, String text) {
        this.id = id;
        this.author = author;
        this.text = text;
    }

    public Twitter() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}//end class Twitter
