package com.shannon.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ECKeys {

    private String pubKey;
    private String priKey;
}
