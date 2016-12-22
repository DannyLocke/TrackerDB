package com.ironyard;

/**
 * Created by dlocke on 12/11/16.
 */
public class Twitter {

    int id;
    String author;
    String post;

    public Twitter (int id, int replyId, String author, String post) {
        this.id = id;
        this.author = author;
        this.post = post;
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
        return post;
    }

    public void setText(String post) {
        this.post = post;
    }
}//end class Twitter
