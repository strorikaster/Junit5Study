package dao;

import java.util.HashMap;
import java.util.Map;

public class UserDaoSpy extends UserDao {


    private final UserDao userDao;
    private Map<Integer, Boolean> answers = new HashMap<>();

    public UserDaoSpy(UserDao userDao) {
        this.userDao = userDao;
    }
    //private Answer1<Integer, Boolean> answer1;

    @Override
    public boolean delete(Integer userId) {
        //invocation++; количество вызовов метода delete
        //return false; Закомментировали - будем использовать answers
        //return answers.getOrDefault(userId,false); Закомментировали - будем использовать spy
        return answers.getOrDefault(userId, userDao.delete(userId));
    }
}
