package im.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import im.db.UserRepository;
import im.model.User;

public class SettingService {
    private static final UserRepository userRepository = new UserRepository();
    public boolean updateNickname(long userId, String newNickname, String password) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在？？？");
        }
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPasswordHash());
        if (!result.verified) {
            throw new IllegalArgumentException("密码验证失败。");
        }
        return userRepository.updateNickname(userId, newNickname);
    }

    public boolean updateUsername(long userID, String newUsername, String password){
        User user = userRepository.findById(userID);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在？？？");
        }
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPasswordHash());
        if (!result.verified) {
            throw new IllegalArgumentException("密码验证失败。");
        }
        return userRepository.updateUsername(userID, newUsername);
    }

    public boolean updatePassword(long userId, String newPassword, String Password) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在？？？");
        }
        BCrypt.Result result = BCrypt.verifyer().verify(Password.toCharArray(), user.getPasswordHash());
        String newPasswordHash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
        if(!result.verified) {
            throw new IllegalArgumentException("密码验证失败。");
        }
        return userRepository.updatePassword(userId, newPasswordHash);
    }
}

