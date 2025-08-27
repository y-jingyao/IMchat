CREATE DATABASE IF NOT EXISTS im_system CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE im_system;

CREATE TABLE IF NOT EXISTS users (

uid BIGINT PRIMARY KEY AUTO_INCREMENT,

username VARCHAR(50) NOT NULL UNIQUE,

password_hash VARCHAR(100) NOT NULL,

nickname VARCHAR(50) DEFAULT NULL,

created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

status TINYINT DEFAULT NULL,

) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS messages (

mid BIGINT PRIMARY KEY AUTO_INCREMENT,

sender_id BIGINT NOT NULL,

receiver_id BIGINT NOT NULL,

content TEXT NOT NULL,

status TINYINT NOT NULL DEFAULT 0, -- 0=未送达，1=已送达

created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

INDEX idx_receiver_status (receiver_id, status),

INDEX idx_sender_created (sender_id, created_at),

CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES users(uid),

CONSTRAINT fk_msg_receiver FOREIGN KEY (receiver_id) REFERENCES users(uid)

) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS friends (

fid BIGINT NOT NULL AUTO_INCREMENT ,

user_id BIGINT NOT NULL ,

friend_id BIGINT NOT NULL ,

status TINYINT NOT NULL DEFAULT 0 ,

created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

PRIMARY KEY (fid),

UNIQUE KEY uk_user_friend (user_id, friend_id),

KEY idx_friend_user (friend_id, user_id),

CONSTRAINT fk_friends_user FOREIGN KEY (user_id) REFERENCES users (uid) ON DELETE CASCADE,

CONSTRAINT fk_friends_friend FOREIGN KEY (friend_id) REFERENCES users (uid) ON DELETE CASCADE,

CHECK (user_id != friend_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;