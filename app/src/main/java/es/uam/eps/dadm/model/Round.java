package es.uam.eps.dadm.model;

import java.util.Date;
import java.util.UUID;
public class Round {
    private int size;
    private String id;
    private String title;
    private String date;
    private TableroDamas board;
    public Round(int size) {
        this.size = size;
        id = UUID.randomUUID().toString();
        title = "ROUND " + id.toString().substring(19, 23).toUpperCase();
        date = new Date().toString();
        board = new TableroDamas();
    }
    public int getSize() { return size;}
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public TableroDamas getBoard() {
        return board;
    }
    public void setBoard(TableroDamas board) {
        this.board = board;
    }
}