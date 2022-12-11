package dao;

import org.mockito.stubbing.Answer1;

import java.util.HashMap;
import java.util.Map;

public class UserDaoMock extends UserDao {

    private Map<Integer, Boolean> answers = new HashMap<>();
    private Answer1<Integer, Boolean> answer1;

    @Override
    public boolean delete(Integer userId) {
        //return false; Закомментировали - будем использовать answers
        return answers.getOrDefault(userId,false);
    }
}
