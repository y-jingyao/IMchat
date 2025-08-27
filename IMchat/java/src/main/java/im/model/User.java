package im.model;

import java.sql.Timestamp;

public class User {
    private Long uid;
    private String username;
    private String passwordHash;
    private String nickname;
    private Timestamp createdAt;
    private Integer status;


    public User(Long uid, String username, String passwordHash, String nickname, Timestamp createdAt,Integer status) {
        this.uid = uid;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.createdAt = createdAt;
        this.status = status;
    }

    public Long getUid() { return uid; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getNickname() { return nickname; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
