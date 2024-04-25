package com.hck.mybackendtemplate.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 * @author otavia28
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 746632009188916678L;
    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
