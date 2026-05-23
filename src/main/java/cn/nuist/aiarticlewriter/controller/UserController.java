package cn.nuist.aiarticlewriter.controller;

import cn.nuist.aiarticlewriter.annotation.AuthCheck;
import cn.nuist.aiarticlewriter.common.BaseResponse;
import cn.nuist.aiarticlewriter.common.DeleteRequest;
import cn.nuist.aiarticlewriter.common.ResultUtils;
import cn.nuist.aiarticlewriter.constant.UserConstant;
import cn.nuist.aiarticlewriter.exception.BusinessException;
import cn.nuist.aiarticlewriter.exception.ErrorCode;
import cn.nuist.aiarticlewriter.model.dto.user.UserLoginRequest;
import cn.nuist.aiarticlewriter.model.dto.user.UserQueryRequest;
import cn.nuist.aiarticlewriter.model.dto.user.UserRegisterRequest;
import cn.nuist.aiarticlewriter.model.entity.User;
import cn.nuist.aiarticlewriter.model.vo.LoginUserVO;
import cn.nuist.aiarticlewriter.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User controller.
 */
@RestController
@RequestMapping("/user")
@Tag(name = "User Controller")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * User register.
     *
     * @param userRegisterRequest user register request
     * @return new user id
     */
    @PostMapping("/register")
    @Operation(summary = "User register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * User login.
     *
     * @param userLoginRequest user login request
     * @param request          HTTP request
     * @return login user view object
     */
    @PostMapping("/login")
    @Operation(summary = "User login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * Get current login user.
     *
     * @param request HTTP request
     * @return login user view object
     */
    @GetMapping("/get/login")
    @Operation(summary = "Get current login user")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        LoginUserVO loginUserVO = userService.getLoginUserVO(loginUser);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * User logout.
     *
     * @param request HTTP request
     * @return logout result
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * Get user by id.
     *
     * @param id user id
     * @return user view object
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "Get user by id")
    public BaseResponse<LoginUserVO> getUserById(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO userVO = userService.getUserVOById(id);
        return ResultUtils.success(userVO);
    }

    /**
     * Page users.
     *
     * @param userQueryRequest user query request
     * @return user page
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "Page users")
    public BaseResponse<Page<LoginUserVO>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userQuery = new User();
        BeanUtils.copyProperties(userQueryRequest, userQuery);
        Page<LoginUserVO> userPage = userService.pageUserVO(
                userQueryRequest.getPageNum(),
                userQueryRequest.getPageSize(),
                userQuery,
                userQueryRequest.getSortField(),
                userQueryRequest.getSortOrder()
        );
        return ResultUtils.success(userPage);
    }

    /**
     * Delete user.
     *
     * @param deleteRequest delete request
     * @param request       HTTP request
     * @return delete result
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "Delete user")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (deleteRequest.getId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Cannot delete current login user");
        }
        boolean result = userService.deleteUser(deleteRequest.getId());
        return ResultUtils.success(result);
    }
}
