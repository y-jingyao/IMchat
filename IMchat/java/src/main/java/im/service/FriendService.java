package im.service;

import im.db.FriendRepository;
import im.db.UserRepository;
import im.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendService {
    private final FriendRepository friendRepository = new FriendRepository();
    private final UserRepository userRepository = new UserRepository();

    public List<User> searchUsers(String username, long currentUserId) {
        return userRepository.searchByUsername(username, currentUserId);
    }

    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException("You cannot add yourself as a friend.");
        }
        // 可以在这里添加更多检查，例如是否已经是好友或已发送请求
        friendRepository.addFriendRequest(userId, friendId);
    }

    public boolean acceptFriendRequest(long userId, long friendId) {
        // 接受请求意味着双向关系状态都变为1
        byte s = 1;
        return friendRepository.updateFriendStatus(userId, friendId, s);
    }

    public boolean deleteFriend(long userId, long friendId) {
        // 删除关系是双向的
        return friendRepository.deleteFriend(userId, friendId);
    }

    public Map<String, List<?>> getFriendsAndRequests(long userId) {
        List<User> friends = friendRepository.findFriendsByUserId(userId);
        List<User> requests = friendRepository.findPendingFriendRequests(userId);
        Map<String, List<?>> result = new HashMap<>();
        result.put("friends", friends);
        result.put("requests", requests);
        return result;
    }
}