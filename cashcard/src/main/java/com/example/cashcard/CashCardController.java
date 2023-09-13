package com.example.cashcard;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.net.URI;
import java.security.Principal;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
	private CashCardRepository cashCardRepository;
	
	public CashCardController(CashCardRepository cashCardRepository) {
		this.cashCardRepository = cashCardRepository;
	}

    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
        Optional<CashCard> cashCardOptional = Optional.ofNullable(cashCardRepository.findByIdAndOwner(requestedId, principal.getName()));
        if (cashCardOptional.isPresent()) {
        	return ResponseEntity.ok(cashCardOptional.get());
        }else {
        	return ResponseEntity.notFound().build();        
        	  }
    }
    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal) {
    	CashCard cashCardWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
    	CashCard savedCashCard = cashCardRepository.save(cashCardWithOwner);
    	URI locationOfNewCashCard = ucb.path("cashcards/{id}").buildAndExpand(savedCashCard.id()).toUri();
    	return ResponseEntity.created(locationOfNewCashCard).build();
    }
    @GetMapping()
    public ResponseEntity<Iterable<CashCard>> findAll(Pageable pageable, Principal principal){
    	Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(),
    			PageRequest.of(
    					pageable.getPageNumber(), 
    					pageable.getPageSize(),
    					pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
    					));
    	return ResponseEntity.ok(page.getContent());
    }
    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal){
    	CashCard cashCard = cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    	if(cashCard != null) {
    		CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());
        	cashCardRepository.save(updatedCashCard);
        	return ResponseEntity.noContent().build();
    	}
    	return ResponseEntity.notFound().build();
    }
    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal){
    	if(!cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
    		return ResponseEntity.notFound().build();
    	}
    	cashCardRepository.deleteById(id);
    	return ResponseEntity.noContent().build();
    }
}
