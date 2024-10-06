package springbatch_example;

import org.springframework.batch.item.ItemProcessor;

import springbatch.model.User;

public class UserItemProcessor implements ItemProcessor<User, User>{

	@Override
	public User process(User user) throws Exception {

		return user;
	}

}
