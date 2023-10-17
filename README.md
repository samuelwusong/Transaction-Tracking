# Transaction Tracking
A transaction tracking application implemented with Spring Boot. It has the following dependencies:
* JDK 17 or 11
* Spring Boot 3.1.0
* Spring Data JPA 3.1.0
* H2 Database 
* Swagger 3
* Jacoco


# Run/Debug
* Start the main Java application `WexTagApplication` or run the MAVEN build `spring-boot:run`
* Open the Swagger UI in a browser with the following url: [http://localhost:8080/swagger-ui/index.html#/](http://localhost:8080/swagger-ui/index.html#/)
* `exchange-rate-controller` contains an endpoint which can retrieve the valid currency codes from **Treasury Reporting Rates of Exchange**. The currency code can be used to convert the transaction amount.
* `transaction-controller` contains three endpoints which can be used to create a transaction, retrieve existing transactions from database and collect a transaction by its Id and convert its amount to a foreign currency.

    Create a transaction with a date, a description and an amount with a payload similar to the following one and `POST` it to `/api/transactions`. The amount will be rounded the nearest cent.

``` json
{
 	"date": "2023-01-17",
  	"description": "trade amount of 100.126",
  	"amount": 100.126
}
```


    Run a `GET` request from `/api/transactions`. The created transactions will be returned. It has an optional parameter `date` which helps to collected the transactions on a specific date.
    Run a `GET` request from  `/api/transactions/{id}`, where `id` is the transaction Id. When the optional parameter `currency` is set to a valid currency code, the transaction data will be displayed in both original US dollar and the currency specified. Without the currency code, it displays the amount in US dollar only. The amount will be rounded the nearest cent.
    When an invalid currency code or transaction Id is entered, an error message will be displayed.
    When a negative amount is provided in creating a transaction or the description is over 50 characters, an error message will also be displayed.
    When the incorrect type of data are used to create a transaction, an error message is displayed.
    The length of the transactions list `GET` from `/api/transactions` is 50 by default. It can be changed in `application.properties` by setting `transaction.page.size`.
    The format of the date in the project is `yyyy-mm-dd`.
    

# Implementation Highlight
There are four major parts in this project: **Transaction Management**, **Exchange Rate Collecting**, **Swagger UI** and **Jacoco Code Coverage**.

Although this project focus on the backend and doesn't include UI, it's designed to connect with the UI. It provides a list of valid currency codes which can be used to create a selection list in UI or as a dictionary to the code completion. The transaction list can also help the user to find the transaction interested. Currently Swagger or Postman is needed to do the `POST` to create  to a transaction.

The current input validation and error message handling are all included in the backend. It will perform better if these functions can be moved to UI.

## Transaction Management
This part creates and display the transaction information. It contains `Transaction`, `TransactionController`,  `TransactionRepository` and `TransactionService`
### Transaction
This is the data model of the transaction. It is used to persist the transaction to the database.

### TransactionController
It contains a **POST** endpoint and two **GET** ones. It validates the user input and handles the error messages. The error messages are externalized in application.properties. 

### TransactionService
It takes requests from  `TransactionController`,  collects, processes and returns the data needed.

### TransactionRepository
It helps to access the transaction data persisted in the database. It currently uses a H2 database in memory.

## Exchange Rate Collecting
This part connects with the **Treasury Reporting Rates of Exchange** API and collects the exchange rate information needed. It contains `CurrencyData`, `ExchangeRateController` and `ExchangeRateService`.

### CurrencyData
This is the data model of the exchange rate data. It contains the information collected from **Treasury Reporting Rates of Exchange**.

### ExchangeRateController
It contains an endpoint which provides the valid currency code in the last year.

### ExchangeRateService
It connects with **Treasury Reporting Rates of Exchange** API and collects the exchange rate information needed.


## Swagger UI
The Swagger Ui is configured in `SwaggerConfig`

## Jacoco Code Coverage
Jacoco code coverage report is configured in `pom.xml` and can be accessed at `.../target/site/jacoco/index.html`

# Appendix
Sample URL to access **Treasury Reporting Rates of Exchange**

* [Collect the valid currency codes](https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange?fields=country_currency_desc&filter=record_date:gte:2023-01-01,record_date:lte:2023-10-14&page[size]=350)

* [Collect the exchange rate on a currency](https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange?fields=country_currency_desc,exchange_rate,record_date&filter=country_currency_desc:eq:Canada-Dollar,record_date:gte:2020-01-01,record_date:lte:2023-01-01&sort=-record_date&page[size]=50)
