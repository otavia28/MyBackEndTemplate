package com.hck.mybackendtemplate.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 * @author otavia28
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 746632009188916678L;
    private String userAccount;
    private String userPassword;
}
