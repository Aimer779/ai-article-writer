package cn.nuist.aiarticlewriter.service.impl;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.nuist.aiarticlewriter.constant.UserConstant;
import cn.nuist.aiarticlewriter.exception.BusinessException;
import cn.nuist.aiarticlewriter.exception.ErrorCode;
import cn.nuist.aiarticlewriter.exception.ThrowUtils;
import cn.nuist.aiarticlewriter.mapper.UserMapper;
import cn.nuist.aiarticlewriter.model.entity.User;
import cn.nuist.aiarticlewriter.model.vo.LoginUserVO;
import cn.nuist.aiarticlewriter.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * User service implementation.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final int MIN_ACCOUNT_LENGTH = 4;

    private static final int MIN_PASSWORD_LENGTH = 8;

    private static final String VALID_ACCOUNT_PATTERN = "^[a-zA-Z0-9_]+$";

    private static final long MAX_PAGE_SIZE = 50;

    private static final Set<String> USER_SORT_FIELD_SET = Set.of(
            "id",
            "userAccount",
            "userName",
            "userRole",
            "createTime",
            "editTime",
            "updateTime"
    );

    @Resource
    private UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Parameters cannot be blank");
        }
        ThrowUtils.throwIf(userAccount.length() < MIN_ACCOUNT_LENGTH, ErrorCode.PARAMS_ERROR, "User account is too short");
        ThrowUtils.throwIf(userPassword.length() < MIN_PASSWORD_LENGTH || checkPassword.length() < MIN_PASSWORD_LENGTH,
                ErrorCode.PARAMS_ERROR, "User password is too short");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "Passwords do not match");
        ThrowUtils.throwIf(!ReUtil.isMatch(VALID_ACCOUNT_PATTERN, userAccount), ErrorCode.PARAMS_ERROR, "User account contains invalid characters");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userAccount", userAccount);
        Long count = userMapper.selectCountByQuery(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "User account already exists");

        String encryptedPassword = getEncryptedPassword(userPassword);
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .userAccount(userAccount)
                .userPassword(encryptedPassword)
                .userName(userAccount)
                .userRole(UserConstant.DEFAULT_ROLE)
                .createTime(now)
                .editTime(now)
                .updateTime(now)
                .isDelete(0)
                .build();
        int result = userMapper.insert(user);
        ThrowUtils.throwIf(result <= 0 || user.getId() == null, ErrorCode.OPERATION_ERROR, "Register failed");
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Parameters cannot be blank");
        }
        ThrowUtils.throwIf(userAccount.length() < MIN_ACCOUNT_LENGTH, ErrorCode.PARAMS_ERROR, "User account is too short");
        ThrowUtils.throwIf(userPassword.length() < MIN_PASSWORD_LENGTH, ErrorCode.PARAMS_ERROR, "User password is too short");
        ThrowUtils.throwIf(!ReUtil.isMatch(VALID_ACCOUNT_PATTERN, userAccount), ErrorCode.PARAMS_ERROR, "User account contains invalid characters");

        String encryptedPassword = getEncryptedPassword(userPassword);
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userAccount", userAccount)
                .eq("userPassword", encryptedPassword);
        User user = userMapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "User account or password is incorrect");

        LoginUserVO loginUserVO = getLoginUserVO(user);
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, loginUserVO);
        return loginUserVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(!(userObj instanceof LoginUserVO), ErrorCode.NOT_LOGIN_ERROR);
        LoginUserVO currentUser = (LoginUserVO) userObj;

        User user = userMapper.selectOneById(currentUser.getId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        return user;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObj == null, ErrorCode.NOT_LOGIN_ERROR);
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO getUserVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userMapper.selectOneById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return getLoginUserVO(user);
    }

    @Override
    public Page<LoginUserVO> pageUserVO(long pageNumber, long pageSize, User user, String sortField, String sortOrder) {
        ThrowUtils.throwIf(pageNumber <= 0, ErrorCode.PARAMS_ERROR, "Page number is invalid");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > MAX_PAGE_SIZE, ErrorCode.PARAMS_ERROR, "Page size is invalid");

        QueryWrapper queryWrapper = QueryWrapper.create();
        if (user != null) {
            queryWrapper
                    .eq("id", user.getId(), user.getId() != null)
                    .like("userAccount", user.getUserAccount(), StrUtil.isNotBlank(user.getUserAccount()))
                    .like("userName", user.getUserName(), StrUtil.isNotBlank(user.getUserName()))
                    .like("userProfile", user.getUserProfile(), StrUtil.isNotBlank(user.getUserProfile()))
                    .eq("userRole", user.getUserRole(), StrUtil.isNotBlank(user.getUserRole()));
        }

        String finalSortField = StrUtil.blankToDefault(sortField, "createTime");
        ThrowUtils.throwIf(!USER_SORT_FIELD_SET.contains(finalSortField), ErrorCode.PARAMS_ERROR, "Sort field is invalid");
        boolean isAsc = "ascend".equals(sortOrder);
        queryWrapper.orderBy(finalSortField, isAsc);

        Page<User> userPage = userMapper.paginate(pageNumber, pageSize, queryWrapper);
        return userPage.map(this::getLoginUserVO);
    }

    @Override
    public boolean deleteUser(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userMapper.selectOneById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        int result = userMapper.deleteById(id);
        ThrowUtils.throwIf(result <= 0, ErrorCode.OPERATION_ERROR, "Delete failed");
        return true;
    }

    /**
     * Encrypt password.
     *
     * @param userPassword raw user password
     * @return encrypted password
     */
    private String getEncryptedPassword(String userPassword) {
        return SecureUtil.md5(UserConstant.SALT + userPassword);
    }
}
