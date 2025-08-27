package im.db;

import im.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FriendRepository {
    private static final Logger logger = LoggerFactory.getLogger(FriendRepository.class);

    // 添加好友请求
    public boolean addFriendRequest(long userId, long friendId) {
        String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, 0)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setLong(2, friendId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adding friend request from {} to {}", userId, friendId, e);
            return false;
        }
    }

    // 更新好友关系状态
    public boolean updateFriendStatus(long user1, long user2, byte status) {
        String sqlUpdateRequest = "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?";
        String sqlInsertFriend = "INSERT IGNORE INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateRequest);
                 PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertFriend)) {

                pstmtUpdate.setByte(1, status);
                pstmtUpdate.setLong(2, user2);
                pstmtUpdate.setLong(3, user1);
                int updateRows = pstmtUpdate.executeUpdate();
                if (updateRows == 0) {
                    conn.rollback();
                    logger.warn("未找到待更新的好友请求：发起方={}, 接收方={}", user2, user1);
                    return false;
                }

                pstmtInsert.setLong(1, user1);
                pstmtInsert.setLong(2, user2);
                pstmtInsert.setByte(3, status);
                pstmtInsert.executeUpdate();

                conn.commit();
                logger.info("好友状态更新成功：双向关系（{}<->{}），状态={}", user1, user2, status);
                return true;

            } catch (SQLException e) {
                conn.rollback();
                logger.error("更新好友状态时事务回滚：user1={}, user2={}", user1, user2, e);
                return false;
            }

        } catch (SQLException e) {
            logger.error("获取数据库连接失败（好友状态更新）", e);
            return false;
        }
    }
    // 删除好友关系（双向删除）
    public boolean deleteFriend(long user1, long user2) {
        String sql = "DELETE FROM friends WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user1);
            pstmt.setLong(2, user2);
            pstmt.setLong(3, user2);
            pstmt.setLong(4, user1);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting friend relationship between {} and {}", user1, user2, e);
            return false;
        }
    }

    // 获取好友列表
    public List<User> findFriendsByUserId(long userId) {
        List<User> friends = new ArrayList<>();
        String sql = "SELECT u.uid, u.username, u.nickname FROM users u " +
                "JOIN friends f ON u.uid = f.friend_id " +
                "WHERE f.user_id = ? AND f.status = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    friends.add(new User(rs.getLong("uid"), rs.getString("username"), null, rs.getString("nickname"), null,null));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding friends for user {}", userId, e);
        }
        return friends;
    }

    // 获取待处理的好友请求
    public List<User> findPendingFriendRequests(long userId) {
        List<User> requests = new ArrayList<>();
        String sql = "SELECT u.uid, u.username, u.nickname FROM users u " +
                "JOIN friends f ON u.uid = f.user_id " +
                "WHERE f.friend_id = ? AND f.status = 0";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(new User(rs.getLong("uid"), rs.getString("username"), null, rs.getString("nickname"), null,null));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding pending requests for user {}", userId, e);
        }
        return requests;
    }
}

