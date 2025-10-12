package com.example.demo.dto;

public class LogResponseDTO {
    private boolean ok;
    private String id;

    public LogResponseDTO(boolean ok, String id) {
        this.ok = ok;
        this.id = id;
    }

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
