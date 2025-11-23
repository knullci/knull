package org.knullci.knull.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class User {
    private Long id;

	private String username;

	private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
