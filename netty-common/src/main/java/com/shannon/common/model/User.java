package com.shannon.common.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author zzc
 */
@ApiModel
@Data
public class User {
    @ApiModelProperty(value = "用户名",example = "user1")
    private String name;
    @ApiModelProperty(value = "密码",example = "123456")
    private String pwd;
}
