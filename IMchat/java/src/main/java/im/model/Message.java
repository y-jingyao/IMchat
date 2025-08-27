package im.model;

import java.sql.Timestamp;

public class Message {
    private Long mid;
    private Long senderId;
    private Long receiverId;
    private String content;
    private int status; // 0=未送达, 1=已送达
    private Timestamp createdAt;

    public Message(Long mid, Long senderId, Long receiverId, String content, int status, Timestamp createdAt) {
        this.mid = mid;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Message(Long mid, Long senderId, Long receiverId, String content, int status) {
        this(mid, senderId, receiverId, content, status, null);
    }

    public Long getMid() { return mid; }
    public Long getSenderId() { return senderId; }
    public Long getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public int getStatus() { return status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setStatus(int status) { this.status = status; }
}