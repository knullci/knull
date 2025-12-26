package org.knullci.knull.web.form;

import lombok.Getter;
import lombok.Setter;
import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;

import java.util.List;

/**
 * Form for user creation and editing.
 */
@Getter
@Setter
public class UserForm {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String confirmPassword;
    private String displayName;
    private Role role;
    private List<Permission> additionalPermissions;
    private boolean active = true;
    private boolean accountLocked = false;
}
