package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
            // throw new 用于抛出异常的关键字组合
            // throw 抛出异常关键字
            // new AccountNotFoundException()  创建了一个异常的实例
            // MessageConstant.ACCOUNT_NOT_FOUND 是异常的错误消息 一般是个字符串
        }

        //密码比对
        // 对前端传来的密码进行加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

//    @Override
    public void save(EmployeeDTO employeeDTO) {
//        System.out.println("当前线程的id：" + Thread.currentThread().getId());

        Employee employee = new Employee();
        // 对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置账号的状态 默认正常 1表示正常 0表示锁定
        employee.setStatus(StatusConstant.ENABLE);

        // 设置密码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 设置当前记录的创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());

        employee.setUpdateTime(LocalDateTime.now());

        // 设置当前记录创建人和修改人ID
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     * */
    public PageResult pageQuery (EmployeePageQueryDTO employeePageQueryDTO) {
        // 开始分页查询 select * from employee Limit 0,10
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        // 调用mybatis插件 分页查询 传入开始查询的页码和每页的数量  这个插件会自动的吧Limit这个数据配置进去 并且吧这个参数动态的去计算


        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        // 因为使用了插件 所以就要遵循插件的规则 必须使用Page 并且导包的时候不能导错 上方有包的路径

        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total, records);
    }

    /**
     * 禁用启用账号
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, long id) {

      /*  Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);*/

        // 上下这是两种写法 上面这种是比较传统的写法 下面这种是因在在Employee这个类中调用了@Builder这个注解 所以这样写也是完全没有任何问题的
        // 这是两种不同的编码风格 所以怎么写都行

        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);
    }


}
