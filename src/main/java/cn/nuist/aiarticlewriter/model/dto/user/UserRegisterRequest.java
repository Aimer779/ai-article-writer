package cn.nuist.aiarticlewriter.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * User register request.
 */
@Data
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User login account.
     */
    private String userAccount;

    /**
     * User password.
     */
    private String userPassword;

    /**
     * Confirmed user password.
     */
    private String checkPassword;
}
