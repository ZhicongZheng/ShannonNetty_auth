package com.shannon.server.config;

import com.shannon.common.model.EcKeys;
import com.shannon.common.util.EncryptOrDecryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 定义ECC公钥私钥
 * @author zzc
 */
@Slf4j
@Configuration
public class EcKeysConfig {

    @Bean("EcKeys")
    public EcKeys ecKeysInit(){
        EcKeys ecKeys = EncryptOrDecryptUtil.getEcKeys();
        log.info("服务端初始化公钥serPubKey【{}】",ecKeys.getPubKey());
        log.info("服务端初始化私钥serPriKey【{}】",ecKeys.getPriKey());
        return ecKeys;
    }
}
