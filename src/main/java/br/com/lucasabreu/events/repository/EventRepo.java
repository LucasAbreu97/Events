package br.com.lucasabreu.events.repository;

import org.springframework.data.repository.CrudRepository;

import br.com.lucasabreu.events.model.Event;

public interface EventRepo extends CrudRepository<Event, Integer>{
	
	public Event findByPrettyName(String prettyName);
}
