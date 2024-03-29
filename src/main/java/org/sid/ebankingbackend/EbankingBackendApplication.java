package org.sid.ebankingbackend;

import org.sid.ebankingbackend.dtos.BankAccountDTO;
import org.sid.ebankingbackend.dtos.CurrentBankAccountDTO;
import org.sid.ebankingbackend.dtos.CustomerDTO;
import org.sid.ebankingbackend.dtos.SavingBankAccountDTO;
import org.sid.ebankingbackend.entities.*;
import org.sid.ebankingbackend.enums.AccountStatus;
import org.sid.ebankingbackend.enums.OperationType;
import org.sid.ebankingbackend.exceptions.BalanceNotSufficientException;
import org.sid.ebankingbackend.exceptions.BankAccountNotFoundException;
import org.sid.ebankingbackend.exceptions.CustomerNotFoundException;
import org.sid.ebankingbackend.repositories.AccountOperationRepository;
import org.sid.ebankingbackend.repositories.BankAccountRepository;
import org.sid.ebankingbackend.repositories.CustomerRepository;
import org.sid.ebankingbackend.services.BankAccountService;
import org.sid.ebankingbackend.services.BankService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EbankingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbankingBackendApplication.class, args);
	}


	@Bean
	CommandLineRunner commandLineRunner(BankAccountService bankAccountService){
		return args ->{
			Stream.of("Hassan", "imane", "Mohamed").forEach(name->{
				CustomerDTO customer = new CustomerDTO();
				customer.setName(name);
				customer.setEmail(name + "@gmail.com");
				bankAccountService.saveCustomer(customer);
			});
			bankAccountService.listCustomer().forEach(customer -> {
                try {
                    bankAccountService.saveCurrentBankAccount(Math.random()*90000,9000,customer.getId());
                	bankAccountService.saveSavingBankAccount(Math.random()*120000, 5.5, customer.getId());

				} catch (CustomerNotFoundException e) {
                    e.printStackTrace();
                }

            });
			List<BankAccountDTO> bankAccounts =  bankAccountService.bankAccountList();
			for (BankAccountDTO bankAccount : bankAccounts){
				for (int i = 0; i < 10; i++) {
					String accountId;
					if(bankAccount instanceof SavingBankAccountDTO){
						accountId =  ((SavingBankAccountDTO) bankAccount).getId();
					} else {
						accountId =((CurrentBankAccountDTO) bankAccount).getId();
					}
					bankAccountService.credit(accountId, 1000 + Math.random()*120000, "Credit");
					bankAccountService.debit(accountId, 1000 + Math.random()*9000, "Debit");
				}
			}
		};
	}
	//@Bean
	CommandLineRunner start(CustomerRepository customerRepository,
							BankAccountRepository bankAccountRepository,
							AccountOperationRepository accountOperationRepository){
		return args -> {
			Stream.of("Hassan", "Yassine", "Aicha").forEach(name -> {
				Customer customer = new Customer();
				customer.setName(name);
				customer.setEmail(name+"@gmail.com");
				customerRepository
						.save(customer);

			});

			customerRepository.findAll().forEach(customer -> {
				CurrentAccount currentAccount =  new CurrentAccount();
				currentAccount.setId(UUID.randomUUID().toString());
				currentAccount.setBalance(Math.random()*90000);
				currentAccount.setStatus(AccountStatus.CREATED);
				currentAccount.setCreatedAt(new Date());
				currentAccount.setCustomer(customer);
				currentAccount.setOverDraft(900);
				bankAccountRepository.save(currentAccount);


				SavingAccount savingAccount  =  new SavingAccount();
				savingAccount.setId(UUID.randomUUID().toString());
				savingAccount.setBalance(Math.random()*90000);
				savingAccount.setStatus(AccountStatus.CREATED);
				savingAccount.setCreatedAt(new Date());
				savingAccount.setCustomer(customer);
				savingAccount.setInterestRate(5.5);
				bankAccountRepository.save(savingAccount);
			});

			bankAccountRepository.findAll().forEach(bankAccount -> {
				for (int i = 0; i <10 ; i++) {
					AccountOperation accountOperation = new AccountOperation();
					accountOperation.setOperationDate(new Date());
					accountOperation.setAmount(Math.random()*120000);
					accountOperation.setType(Math.random() > 0.5 ? OperationType.DEBIT : OperationType.CREDIT);
					accountOperation.setBankAccount(bankAccount);
					accountOperationRepository.save(accountOperation);
				}
			});


		};
	}
}
