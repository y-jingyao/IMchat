package im.db;

import im.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageRepository {
    private static final Logger logger = LoggerFactory.getLogger(MessageRepository.class);

    public void saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, message.getSenderId());
            pstmt.setLong(2, message.getReceiverId());
            pstmt.setString(3, message.getContent());
            pstmt.setInt(4, message.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error saving message", e);
        }
    }

    public List<Message> getOfflineMessages(long receiverId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE receiver_id = ? AND status = 0 ORDER BY created_at ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, receiverId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new Message(
                            rs.getLong("mid"),
                            rs.getLong("sender_id"),
                            rs.getLong("receiver_id"),
                            rs.getString("content"),
                            rs.getInt("status"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching offline messages for user {}", receiverId, e);
        }
        return messages;
    }

    public void updateMessageStatus(long messageId, int status) {
        String sql = "UPDATE messages SET status = ? WHERE mid = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, status);
            pstmt.setLong(2, messageId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating status for message {}", messageId, e);
        }
    }
    public List<Message> getMessageHistory(long userId1, long userId2, long beforeMessageId, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages " +
                "WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) " +
                "AND mid < ? " +
                "ORDER BY mid DESC " + // 按ID倒序
                "LIMIT ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId1);
            pstmt.setLong(2, userId2);
            pstmt.setLong(3, userId2);
            pstmt.setLong(4, userId1);
            pstmt.setLong(5, beforeMessageId);
            pstmt.setInt(6, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new Message(
                            rs.getLong("mid"),
                            rs.getLong("sender_id"),
                            rs.getLong("receiver_id"),
                            rs.getString("content"),
                            rs.getInt("status"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
            // 反转列表
            Collections.reverse(messages);
        } catch (SQLException e) {
            logger.error("Error fetching message history between {} and {}", userId1, userId2, e);
        }
        return messages;
    }
}