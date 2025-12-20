package org.knullci.knull.persistence.mapper;

import org.knullci.knull.domain.model.User;

public class UserMapper {
    public static User fromEntity(org.knullci.knull.persistence.entity.User user) {
        if (user == null) return null;
        return new User(user.getId(), user.getUsername(), user.getPassword());
    }

    public static org.knullci.knull.persistence.entity.User toEntity(User user) {
        if (user == null) return null;
        var _user = new org.knullci.knull.persistence.entity.User();
        _user.setId(user.getId());
        _user.setUsername(user.getUsername());
        _user.setPassword(user.getPassword());

        return _user;
    }
}
