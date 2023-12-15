package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
//import jdk.jpackage.internal.Log;
// TODO 不知道为什么在这里调用log这个包会报错找 java: 程序包jdk.jpackage.internal不存在
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        // 调用EmployeeService的login方法进行员工登录
        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();  // 创建一个Map用于存放JWT的声明（claims）
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),  // 使用的密钥，从jwtProperties中获取
                jwtProperties.getAdminTtl(),      // JWT的过期时间，从jwtProperties中获取
                claims);      // 存放声明的Map
        // 生成token的代码就是这些 照抄


        // 使用建造者模式创建EmployeeLoginVO对象
        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())        // 设置员工ID
                .userName(employee.getUsername()) // 设置员工用户名
                .name(employee.getName())   // 设置员工姓名
                .token(token)               // 设置JWT令牌
                .build();                   // 调用build方法构建对象

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出")
    public Result<String> logout() {
        return Result.success();
    }

    @PostMapping
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }


    /**
     * 员工分页查询
     *
    * */

    @GetMapping("/page")
    @ApiOperation("员工分页查询")
                               // 因为后端传入的数据并不是json格式的 所以不需要加入@RequestBody这个注解
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用员工账号")
    public Result startOrStop(@PathVariable("status") Integer status, long id) {
                               // 这里这个注解的意思是路径栏接收数据  status是通过地址栏接收的 所以要加上 id是通过地址栏接收数据
                               // @PathVariable("status")中的("status") 如果 /status/{status}和Integer status里面的值是一样的话 那么这里可以省略不写 但是初学阶段在这里写上了 以免忘了
        log.info("启用禁用员工账号：{},{}", status, id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据管理员id查询管理员信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据用户Id查询用户信息")
    public Result<Employee> getById(@PathVariable("id") Long id) {
        log.info("根据用户id查询用户信息id为：{}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 修改管理员信息
     */
    @PutMapping
    @ApiOperation("编辑员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑员工信息：{}", employeeDTO);
        employeeService.upDate(employeeDTO);
        return Result.success();
    }
}
