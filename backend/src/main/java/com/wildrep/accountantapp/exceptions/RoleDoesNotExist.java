package com.wildrep.accountantapp.exceptions;

public class RoleDoesNotExist extends RuntimeException {

    private final String roleName;

    public RoleDoesNotExist(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String getMessage() {
        return String.format("Role with name: %s does not exist!", this.roleName);
    }
}
