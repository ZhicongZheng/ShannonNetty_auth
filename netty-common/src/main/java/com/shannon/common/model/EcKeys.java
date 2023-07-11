package com.shannon.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ECC公钥私钥实体类
 * @author zzc
 */
@Data
@Accessors(chain = true)
public class EcKeys {

    private String pubKey;
    private String priKey;
}
