package cn.nuist.aiarticlewriter.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Login user view object.
 */
@Data
public class LoginUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User id.
     */
    private Long id;

    /**
     * User login account.
     */
    private String userAccount;

    /**
     * User display name.
     */
    private String userName;

    /**
     * User avatar URL.
     */
    private String userAvatar;

    /**
     * User profile.
     */
    private String userProfile;

    /**
     * User role.
     */
    private String userRole;

    /**
     * VIP activation time.
     */
    private LocalDateTime vipTime;

    /**
     * Whether the user has VIP membership.
     */
    private Boolean vip;

    /**
     * Business edit time.
     */
    private LocalDateTime editTime;

    /**
     * Create time.
     */
    private LocalDateTime createTime;

    /**
     * Update time.
     */
    private LocalDateTime updateTime;
}
