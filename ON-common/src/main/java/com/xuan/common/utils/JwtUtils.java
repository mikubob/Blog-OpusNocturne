package com.xuan.common.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT (JSON Web Token) 工具类
 * <p>
 * 本类负责 Token 的全生命周期管理，包括：
 * 1. 从配置文件加载签名密钥、过期时间、请求头名称及 Token 前缀。
 * 2. 使用秘钥生成包含用户身份信息（Subject）和自定义载荷（Claims）的加密 Token。
 * 3. 校验 Token 的合法性及是否过期。
 * 4. 从加密字符串中还原出用户信息。
 * <p>
 * 它是整个系统权限认证体系的基石。
 *
 * @author 玄〤
 * @since 2026-02-18
 */
@Slf4j
@Component
@Getter
public class JwtUtils {

    /** 签名加密使用的密钥 */
    private final String secretKey;
    /** Token 的有效时长（秒） */
    private final long expiration;
    /** 标准 HTTP 请求头名称（如 Authorization）
     * -- GETTER --
     *  获取对应的 HTTP Header 名称（通常是 "Authorization"）
     */
    private final String header;
    /** Token 值的固定前缀（如 Bearer ）
     * -- GETTER --
     *  获取外部配置的 Token 前缀（如 "Bearer "）
     */
    private final String tokenPrefix;

    /**
     * 构造函数：由 Spring 自动注入 application.yaml 中的配置项
     *
     * @param secret      原始字符秘钥
     * @param expiration  过期时长
     * @param header      请求头 Key
     * @param tokenPrefix Token 前缀
     */
    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.expiration}") long expiration,
                    @Value("${jwt.header}") String header,
                    @Value("${jwt.token-prefix}") String tokenPrefix) {
        this.secretKey = secret;
        this.expiration = expiration;
        this.header = header;
        this.tokenPrefix = tokenPrefix;
    }

    /**
     * 生成基础 Token（仅包含用户名）
     *
     * @param username 用户标识（在本系统中通常是用户 ID）
     * @return 压缩后的 JWT 字符串
     */
    public String generateToken(String username) {
        return generateToken(username, Map.of());
    }

    /**
     * 生成全量 Token（包含自定义载荷）
     *
     * 流程：
     * 1. 设置自定义业务字段（Claims）。
     * 2. 设置面向的主体（Subject）。
     * 3. 记录签发时间（IssuedAt）。
     * 4. 计算并设置过期时刻。
     * 5. 使用指定的算法和秘钥进行数字签名。
     *
     * @param username 用户标识
     * @param claims   需要随 Token 传递的业务数据（如角色、部门 ID）
     * @return 签名后的 JWT 字符串
     */
    public String generateToken(String username, Map<String, Object> claims) {
        try {
            // 创建JWT声明集
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();

            // 添加自定义声明
            if (claims != null && !claims.isEmpty()) {
                for (Map.Entry<String, Object> entry : claims.entrySet()) {
                    builder.claim(entry.getKey(), entry.getValue());
                }
            }

            // 设置主体（用户名）
            builder.subject(username);

            // 设置过期时间
            Date expirationTime = new Date(System.currentTimeMillis() + expiration * 1000);
            builder.expirationTime(expirationTime);
            builder.issueTime(new Date());

            JWTClaimsSet claimsSet = builder.build();

            // 创建JWS头
            JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

            // 创建JWS对象
            JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(claimsSet.toJSONObject()));

            // 签名
            jwsObject.sign(new MACSigner(secretKey.getBytes(StandardCharsets.UTF_8)));

            // 序列化
            return jwsObject.serialize();
        } catch (Exception e) {
            log.error("生成Token失败: {}", e.getMessage());
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    /**
     * 从 Token 字符串中提取用户名（Subject）
     *
     * @param token 未带前缀的真实 Token
     * @return 用户标识
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, JWTClaimsSet::getSubject);
    }

    /**
     * 从 Token 中获取其设定的过期时间点
     *
     * @param token Token
     * @return 过期时刻 Date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, JWTClaimsSet::getExpirationTime);
    }

    /**
     * 泛型方法：从 Token 中提取特定的声明信息
     *
     * @param <T>            返回类型
     * @param token          Token 字符串
     * @param claimsResolver 函数式接口，定义如何从 Claims 中取值
     * @return 提取出的具体数据
     */
    public <T> T getClaimFromToken(String token, Function<JWTClaimsSet, T> claimsResolver) {
        final JWTClaimsSet claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析 Token 载荷的核心方法
     * <p>
     * 如果秘钥不对、Token 格式错误或已被篡改，解析过程将抛出异常。
     * 成功则返回载荷对象（Claims）。
     *
     * @param token Token 字符串
     * @return 解密后的 Claims 载荷
     */
    public JWTClaimsSet getAllClaimsFromToken(String token) {
        try {
            // 解析令牌
            SignedJWT signedJWT = SignedJWT.parse(token);

            // 验证签名
            JWSVerifier verifier = new MACVerifier(secretKey.getBytes(StandardCharsets.UTF_8));
            if (!signedJWT.verify(verifier)) {
                throw new Exception("Invalid JWT signature");
            }

            // 获取载荷
            return signedJWT.getJWTClaimsSet();
        } catch (Exception e) {
            // 记录错误日志，便于线上排查伪造 Token 请求
            log.error("解析Token失败: {}", e.getMessage());
            throw new RuntimeException("Invalid Token");
        }
    }

    /**
     * 验证 Token 是否已经失效
     * <p>
     * 判断逻辑：获取 Token 的过期时间点，检查其是否早于当前系统时间。
     *
     * @param token Token
     * @return true-已过期，false-仍在有效期内
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 全面校验 Token 的合法性
     * <p>
     * 条件：
     * 1. 提取出的用户名必须与传入的用户名一致。
     * 2. Token 对象本身必须处于有效期内。
     *
     * @param token    Token
     * @param username 预期匹配的用户名/ID
     * @return 是否通过校验
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

}
