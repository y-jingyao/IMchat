package im.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import im.db.UserRepository;
import im.model.User;

public class UserService {
    private final UserRepository userRepository = new UserRepository();

    public boolean register(String username, String password,  String nickname) {
        if (userRepository.findByUsername(username) != null) {
            return false; // 用户已存在
        }
        String passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        User newUser = new User(null, username, passwordHash, nickname, null,null);
        return userRepository.save(newUser);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPasswordHash());
            if (result.verified) {
                return user;
            }
        }
        return null;
    }
}