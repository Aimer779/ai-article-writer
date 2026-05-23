package cn.nuist.aiarticlewriter.model.dto.user;

import cn.nuist.aiarticlewriter.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * User query request.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

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
     * User profile.
     */
    private String userProfile;

    /**
     * User role.
     */
    private String userRole;
}
