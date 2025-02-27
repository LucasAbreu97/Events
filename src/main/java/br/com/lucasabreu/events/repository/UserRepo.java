package br.com.lucasabreu.events.repository;

import org.springframework.data.repository.CrudRepository;

import br.com.lucasabreu.events.model.User;

public interface UserRepo extends CrudRepository<User, Integer> {

	public User findByEmail(String email);
	
}
