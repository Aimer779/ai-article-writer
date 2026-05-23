package cn.nuist.aiarticlewriter.service;

import cn.nuist.aiarticlewriter.model.entity.User;
import cn.nuist.aiarticlewriter.model.vo.LoginUserVO;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;

/**
 * User service.
 */
public interface UserService {

    /**
     * Register user.
     *
     * @param userAccount   user login account
     * @param userPassword  user password
     * @param checkPassword confirmed user password
     * @return new user id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * Login user.
     *
     * @param userAccount  user login account
     * @param userPassword user password
     * @param request      HTTP request
     * @return login user view object
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * Get current login user.
     *
     * @param request HTTP request
     * @return current login user entity
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * Logout current user.
     *
     * @param request HTTP request
     * @return logout result
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * Convert user entity to login user view object.
     *
     * @param user user entity
     * @return login user view object
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * Get user view object by id.
     *
     * @param id user id
     * @return user view object
     */
    LoginUserVO getUserVOById(long id);

    /**
     * Page user view objects.
     *
     * @param pageNumber page number
     * @param pageSize   page size
     * @param user       query user condition
     * @param sortField  sort field
     * @param sortOrder  sort order
     * @return user view object page
     */
    Page<LoginUserVO> pageUserVO(long pageNumber, long pageSize, User user, String sortField, String sortOrder);

    /**
     * Delete user by id.
     *
     * @param id user id
     * @return delete result
     */
    boolean deleteUser(long id);
}
