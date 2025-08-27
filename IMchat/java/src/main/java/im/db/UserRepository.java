package im.db;

import im.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong("uid"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("nickname"),
                            rs.getTimestamp("created_at"),
                            rs.getInt("status")
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
        }
        return null;
    }

    public boolean save(User user) {
        String sql = "INSERT INTO users (username, password_hash, nickname) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getNickname());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error saving user: {}", user.getUsername(), e);
            return false;
        }
    }
    public List<User> searchByUsername(String username, long currentUserId) {
        List<User> users = new ArrayList<>();
        //sql查询语句ChatGpt立大功
        String sql = "SELECT u.uid, u.username, u.nickname, u.created_at,\n" +
                "       (\n" +
                "           SELECT f.status\n" +
                "           FROM friends f\n" +
                "           WHERE (f.user_id = ? AND f.friend_id = u.uid)\n" +
                "              OR (f.friend_id = ? AND f.user_id = u.uid)\n" +
                "           LIMIT 1\n" +
                "       ) AS status\n" +
                "FROM users u\n" +
                "WHERE u.username LIKE ?\n" +
                "  AND u.uid != ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, currentUserId);
            pstmt.setLong(2, currentUserId);
            pstmt.setString(3, "%" + username + "%");
            pstmt.setLong(4, currentUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Integer status = rs.getObject("status") != null ? rs.getInt("status") : null;
                    users.add(new User(
                            rs.getLong("uid"),
                            rs.getString("username"),
                            null, // 不返回密码哈希
                            rs.getString("nickname"),
                            rs.getTimestamp("created_at"),
                            status
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching for users by username: {}", username, e);
        }
        return users;
    }

    public User findById(long userId) {
        String sql = "SELECT * FROM users WHERE uid = ?";
        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong("uid"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("nickname"),
                            rs.getTimestamp("created_at"),
                            rs.getInt("status")
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by id: {}", userId, e);
        }
        return null;
    }

    public boolean updateNickname(long userId, String newNickname) {
        String sql = "UPDATE users SET nickname = ? WHERE uid = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newNickname);
            pstmt.setLong(2, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating nickname for user {}", userId, e);
            return false;
        }
    }

    public boolean updateUsername(long userId, String newUsernaem) {
        String sql = "UPDATE users SET username = ? WHERE uid = ?";
        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1,newUsernaem);
            pstmt.setLong(2,userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }catch (SQLException e) {
            logger.error("Error updating username for user {}", userId, e);
            return false;
        }
    }

    public boolean updatePassword(long userId, String newPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE uid = ?";
        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, newPassword);
            pstmt.setLong(2,userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }catch (SQLException e) {
            logger.error("Error updating password for user {}", userId, e);
            return false;
        }
    }
}