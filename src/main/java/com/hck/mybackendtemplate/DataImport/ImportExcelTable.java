package com.hck.mybackendtemplate.DataImport;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import java.util.Date;

@Data
public class ImportExcelTable {

    @ExcelProperty("id")
    private Long id;

    @ExcelProperty("userName")
    private String userName;

    @ExcelProperty("userAccount")
    private String userAccount;

    @ExcelProperty("userAvatar")
    private String userAvatar;

    @ExcelProperty("gender")
    private Integer gender;

    @ExcelProperty("userPassword")
    private String userPassword;

    @ExcelProperty("phone")
    private String phone;

    @ExcelProperty("email")
    private String email;

    @ExcelProperty("userStatus")
    private Integer userStatus;

    @ExcelProperty("createTime")
    private Date createTime;

    @ExcelProperty("updateTime")
    private Date updateTime;

    @ExcelProperty("isDelete")
    @TableLogic
    private Integer isDelete;

    @ExcelProperty("userRole")
    private Integer userRole;

}
