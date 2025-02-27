package br.com.lucasabreu.events.service;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.lucasabreu.events.dto.SubscriptionRankingByUser;
import br.com.lucasabreu.events.dto.SubscriptionRankingItem;
import br.com.lucasabreu.events.dto.SubscriptionResponse;
import br.com.lucasabreu.events.exception.EventNotFoundException;
import br.com.lucasabreu.events.exception.SubscriptionConflictException;
import br.com.lucasabreu.events.exception.UserIndicadorNotFoundException;
import br.com.lucasabreu.events.model.Event;
import br.com.lucasabreu.events.model.Subscription;
import br.com.lucasabreu.events.model.User;
import br.com.lucasabreu.events.repository.EventRepo;
import br.com.lucasabreu.events.repository.SubscriptionRepo;
import br.com.lucasabreu.events.repository.UserRepo;

@Service
public class SubscriptionService {

	@Autowired
	private EventRepo evtRepo;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private SubscriptionRepo subRepo;
	
	public SubscriptionResponse createNewSubscription(String eventName, User user, Integer userId) {

		//RECUPERAR O ACESSO PELO NOME
		Event evt = evtRepo.findByPrettyName(eventName);
		if(evt == null) {
			throw new EventNotFoundException("Evento " + eventName + " nao existe!");
		}
		User userRec = userRepo.findByEmail(user.getEmail());
		if(userRec == null) {
			userRec = userRepo.save(user);
		}
		
		User indicador = null;
		
		if(userId != null) {
			indicador = userRepo.findById(userId).orElse(null);
			if(indicador == null) {
				throw new UserIndicadorNotFoundException("Usuario "+userId+" indicador não existe!");
			}
		}
		
		Subscription subs = new Subscription();		
		subs.setEvent(evt);
		subs.setSubscriber(userRec);
		subs.setIndication(indicador);
		
		Subscription tmpSub = subRepo.findByEventAndSubscriber(evt, userRec);
		if(tmpSub != null) {
			throw new SubscriptionConflictException("Ja existe inscrição para o usuário " + userRec.getName() + " no evento "+ evt.getTitle());
		}
		
		Subscription res = subRepo.save(subs);
		
		return new SubscriptionResponse(res.getSubscriptionNumber(), "http://codecfraft.com/subscription/" + res.getEvent().getPrettyName()+ "/" + res.getSubscriber().getId());
	}
	
	public List<SubscriptionRankingItem> getCompleteRanking(String prettyName){
		Event evt = evtRepo.findByPrettyName(prettyName);
		if(evt == null) {
			throw new EventNotFoundException("Ranking do evento "+ prettyName+ "nao existe!");
		}
		return subRepo.generateRanking(evt.getEventId());
	}
	
	public SubscriptionRankingByUser getRankingByUser(String prettyName, Integer userId) {
		List<SubscriptionRankingItem> ranking = getCompleteRanking(prettyName);
		
		SubscriptionRankingItem item = ranking.stream().filter(i->i.userId().equals(userId)).findFirst().orElse(null);
		if(item == null) {
			throw new UserIndicadorNotFoundException("Nao há inscriçoes com indicacao do usuario "+ userId);
		}
		Integer posicao = IntStream.range(0, ranking.size()).filter(pos -> ranking.get(pos).userId().equals(userId)).findFirst().getAsInt();
		
		return new SubscriptionRankingByUser(item, posicao);
	}
}
