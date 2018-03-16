package com.morrisons.services.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.morrisons.services.demo.account.AccountService;
import com.morrisons.services.demo.authentication.DirectAuthenticationService;
import com.morrisons.services.demo.customer.CustomerService;
import com.morrisons.services.demo.model.Client;
import com.morrisons.services.demo.model.account.Account;
import com.morrisons.services.demo.model.customer.Customer;

@RestController
public class OBPController {

	private static final Logger logger = LoggerFactory.getLogger(OBPController.class);

	@Autowired
	AccountService accountService;

	@Autowired
	CustomerService customerService;

	@Autowired
	private DirectAuthenticationService directAuthenticationService;

	@Value("${obp.username}")
	private String username;

	@Value("${obp.password}")
	private String password;

	@RequestMapping(value = "/obp/validateClient", method = RequestMethod.POST)
	public ResponseEntity<Client> validateClient(@RequestBody Client client) {

		logger.info("Start of validateClient method");

		String authToken = directAuthenticationService.login(username, password);

		List<Account> accountList = accountService.fetchPrivateAccounts(authToken, false);
		checkInputAccountAvailableOrNot(accountList, client);

		List<Customer> customerList = customerService.getCustomers(authToken, client.getBankId());
		checkCustomerAvailableorNot(customerList, client);

		return new ResponseEntity<Client>(client, HttpStatus.OK);

	}

	private void checkInputAccountAvailableOrNot(List<Account> accountList, Client client) {

		for (Account account : accountList) {
			if (account.getId().equalsIgnoreCase(client.getAccountId())) {
				client.setAccountAvailable(true);
				break;
			}
		}

	}

	private void checkCustomerAvailableorNot(List<Customer> customerList, Client client) {

		for (Customer customer : customerList) {

			if (customer.getCustomerNumber().equalsIgnoreCase(client.getCustomerNumber())) {
				client.setCustomerAvailable(true);
				client.setKYCDone(customer.getKycStatus());
				break;
			}
		}

	}

}
