create table  account
(
	id identity not null primary key,
	currency varchar2(3) not null,
	iban varchar2(50) not null unique,
	beneficiary varchar2(200) not null,
	balance number not null
);